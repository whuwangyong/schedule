package cn.whu.wy.schedule.service;

import cn.whu.wy.schedule.consts.JobStateConst;
import cn.whu.wy.schedule.entity.JobExecution;
import cn.whu.wy.schedule.entity.JobInfo;
import cn.whu.wy.schedule.executor.ExecuteResult;
import cn.whu.wy.schedule.executor.ShellExecutor;
import cn.whu.wy.schedule.service.benchmark.BenchmarkService;
import cn.whu.wy.schedule.service.pipeline.PipelineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 检测job的执行状态
 * <p>
 * 该服务不仅属于pipeline模式，任何模式下，只要job运行，都需要该服务。
 * 用于更新job_execution表中的状态、完成时间等
 *
 * @Author WangYong
 * @Date 2020/03/24
 * @Time 22:03
 */
@Service
@Slf4j
public class CheckJobExeService implements InitializingBean {

    @Autowired
    private JobInfoManageService jobInfoManageService;
    @Autowired
    private ShellExecutor shellExecutor;
    @Autowired
    private RunModeService runModeService;
    @Autowired
    private JobExecutionService jobExecutionService;
    @Autowired
    private PipelineService pipelineService;
    @Autowired
    private BenchmarkService benchmarkService;

    @Value("${schedule.checkJob.logSwitch:false}")
    private boolean logSwitch;//由于该服务是定时任务，为了防止日志信息太多，通过该开关控制
    @Value("${schedule.checkJob.scheduleWithFixedDelay.seconds:5}")
    private int delay;
    @Value("${schedule.checkJob.sh.timeout.seconds:20}")
    private int timeout;

    @Override
    public void afterPropertiesSet() throws Exception {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(6);
        executor.scheduleWithFixedDelay(
                this::check,
                5,
                delay,
                TimeUnit.SECONDS);
    }


    /**
     * 一个bug：
     * check 时 runningJobExecutions 不为null，但是在 doCheck 时，runningJobExecution 已经被前端 stop，
     * 此时check 脚本返回的是completed（由于目前脚本比较粗糙，通过检测进程是否存在来判断目标程序是否结束。这无法区分目标程序是
     * completed 还是terminated。），所以会再调用一次 updateState2End()，以及startNextJob()/doNextExecution()。
     * 型号我在 getCurrentRunningPipelineExe() 里 assert了一下，stop 时已经将pipeline的状态改为 terminated了，不然就startNextJob了。
     * <p>
     * 怎么解决?
     * (1) 期望check脚本能返回正确的状态，区分 completed 和 terminated
     * (2) 在 JobExecution 中引入锁，前端改状态的时候，check 进程不能查数据; check 进程查到的数据还没释放，前端也不能改状态
     * 采用（2）
     */
    private void check() {
        try {
            jobExecutionService.getLock();
            List<JobExecution> runningJobExecutions = jobExecutionService.getRunningJobExecutions();
            for (JobExecution runningExecution : runningJobExecutions) {
                doCheck(runningExecution);
            }
        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
        } finally {
            jobExecutionService.releaseLock();
        }
    }

    private void doCheck(JobExecution runningExecution) {
        JobInfo runningJobInfo = jobInfoManageService.getByPrimaryKey(runningExecution.getJobId());
        // check脚本约定：
        // 如果正在运行返回running，正常结束返回completed，异常结束返回terminated
        ExecuteResult executeResult = shellExecutor.executeCommand("sh -x " + runningJobInfo.getCheckCmd(),
                timeout * 1000);
        if (logSwitch) {
            log.info("check job: jobName={}, result={}", runningJobInfo.getJobName(), executeResult.getMsg().trim());
        }
        String[] msgs = executeResult.getMsg().split("\n");
        String state = msgs[msgs.length - 1].trim();
        switch (state) {
            case JobStateConst.RUNNING:
                break;
            case JobStateConst.COMPLETED:
                jobExecutionService.updateState2End(runningExecution, JobStateConst.COMPLETED, executeResult.getMsg());
                if (runModeService.isPipeline())
                    pipelineService.startNextJob(runningJobInfo);
                if (runModeService.isBenchmark()) {
                    benchmarkService.doNextExecution(runningJobInfo);
                }
                break;
            case JobStateConst.TERMINATED:
                jobExecutionService.updateState2End(runningExecution, JobStateConst.TERMINATED, executeResult.getMsg());
                // 不用更新pipeline_exe 和 benchmark_exe 的exe_ids了，因为job启动时就添加了exe_id。
                // 但是要更新pipeline_exe 和 benchmark_exe 的状态
                if (runModeService.isPipeline())
                    pipelineService.setTerminated();
                if (runModeService.isBenchmark()) {
                    benchmarkService.setTerminated();
                }
                break;
            default:
                log.error("job state illegal! msg={}", executeResult.getMsg());
        }

    }

}

