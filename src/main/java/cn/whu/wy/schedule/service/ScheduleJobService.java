package cn.whu.wy.schedule.service;

import cn.whu.wy.schedule.beans.StartJobResult;
import cn.whu.wy.schedule.consts.JobStateConst;
import cn.whu.wy.schedule.entity.JobExecution;
import cn.whu.wy.schedule.entity.JobInfo;
import cn.whu.wy.schedule.executor.ExecuteResult;
import cn.whu.wy.schedule.executor.ShellExecutor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 负责所有模式下 job 的启停
 *
 * @Author WangYong
 * @Date 2020/03/24
 * @Time 16:31
 */
@Service
@Slf4j
public class ScheduleJobService {

    @Autowired
    private ShellExecutor shellExecutor;
    @Autowired
    private RunModeService runModeService;
    @Autowired
    private JobExecutionService jobExecutionService;


    @Value("${schedule.startJob.sh.timeout.seconds}")
    private int timeoutStart;
    @Value("${schedule.stopJob.sh.timeout.seconds}")
    private int timeoutStop;


    /**
     * 启动job，不论成败，都在job_execution中插入一条记录
     *
     * @param jobInfo
     * @return
     */
    public StartJobResult startJob(@NonNull JobInfo jobInfo) {
        log.info("startJob: jobInfo={}, runMode={}", jobInfo, runModeService.getRunMode());
        ExecuteResult executeResult = new ExecuteResult(-1, "");
        StartJobResult startJobResult = new StartJobResult(executeResult, -1);
        if (runModeService.getRunMode() == null) {
            executeResult.setMsg("未指定runMode");
        } else {
            try {
                if (checkBeforeStart(jobInfo)) {
                    executeResult = shellExecutor.executeCommand("sh " + jobInfo.getStartCmd(), timeoutStart * 1000);
                }
            } catch (Exception e) {
                executeResult.setMsg(e.getMessage());
                log.error("shell startJob error! {}", e.getMessage());
            }

            if (executeResult.getCode() == 0) {
                log.info("shell execute success! executeResult={}", executeResult.toString().trim());
                Integer executionId = jobExecutionService.addExecution(jobInfo, JobStateConst.RUNNING, executeResult.getMsg());
                startJobResult.setJobExecutionId(executionId);
            } else {
                log.error("shell execute fail! executeResult={}", executeResult.toString());
                Integer executionId = jobExecutionService.addExecution(jobInfo, JobStateConst.TERMINATED, executeResult.getMsg());
                startJobResult.setJobExecutionId(executionId);
            }
        }

        startJobResult.setExecuteResult(executeResult);
        return startJobResult;
    }


    /**
     * 停止job，并更新该execution的状态
     * <p>
     * 停止job，要关注runMode吗？
     * 比如在step模式run了一个job，还没结束，此时改为pipeline模式。这时点启动会提示已经有job在运行，无法启动。
     * 那么此时能stop该job？
     * 两种方案：
     * （1）切换模式时，如果有在运行的job，禁止切换，必须跑完或者手动停止后才能切换
     * （2）切换时不做限制，切换后能stop其他模式的job
     * <p>
     * 采用方案（1）。所以，stop时，只需检测当前模式的job状态
     *
     * @param jobInfo
     * @return
     */
    public ExecuteResult stopJob(@NonNull JobInfo jobInfo) {
        log.info("stopJob: jobInfo={}, runMode={}", jobInfo, runModeService.getRunMode());
        ExecuteResult executeResult = new ExecuteResult(-1, "");
        try {
            jobExecutionService.getLock();
            List<JobExecution> runningExecutions = jobExecutionService.getRunningJobBy(jobInfo.getJobId());
            assert runningExecutions.size() <= 1;
            if (runningExecutions.size() == 0) {
                executeResult.setMsg(jobInfo.getJobId() + " not running!");
            } else {
                JobExecution runningExecution = runningExecutions.get(0);
                executeResult = shellExecutor.executeCommand("sh " + jobInfo.getStopCmd(), timeoutStop * 1000);
                if (executeResult.getCode() == 0) {
                    log.info("stopJob: executeResult={}", executeResult.toString().trim());
                    jobExecutionService.updateState2End(runningExecution, JobStateConst.TERMINATED, executeResult.getMsg());
                } else
                    log.error("stopJob: executeResult={}", executeResult.toString());
            }
        } catch (Exception e) {
            executeResult.setMsg(e.getMessage());
            log.error("shell stopJob error! {}", e.getMessage());
        } finally {
            jobExecutionService.releaseLock();
        }
        return executeResult;
    }


    // 不管什么模式，一个job不能同时运行多个execution实例
    private boolean checkBeforeStart(JobInfo jobInfo) throws Exception {
        List<JobExecution> jobExecutions = jobExecutionService.getRunningJobBy(jobInfo.getJobId());
        for (JobExecution jobExecution : jobExecutions) {
            if (jobExecution.isRunning()) {
                throw new Exception("job already running, jobExecution= " + jobExecution);
            }
        }

        return true;
    }

}
