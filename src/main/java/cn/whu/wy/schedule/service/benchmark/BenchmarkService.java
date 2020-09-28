package cn.whu.wy.schedule.service.benchmark;

import cn.whu.wy.schedule.beans.StartJobResult;
import cn.whu.wy.schedule.consts.BenchmarkStateConst;
import cn.whu.wy.schedule.entity.JobBenchmarkExe;
import cn.whu.wy.schedule.entity.JobInfo;
import cn.whu.wy.schedule.executor.ExecuteResult;
import cn.whu.wy.schedule.mapper.IJobBenchmarkExeMapper;
import cn.whu.wy.schedule.service.JobInfoManageService;
import cn.whu.wy.schedule.service.RunModeService;
import cn.whu.wy.schedule.service.ScheduleJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @Author WangYong
 * @Date 2020/04/10
 * @Time 09:38
 */
@Service
@Slf4j
public class BenchmarkService {
    @Autowired
    private IJobBenchmarkExeMapper benchmarkExeMapper;
    @Autowired
    private JobInfoManageService jobInfoManageService;
    @Autowired
    private RunModeService runModeService;
    @Autowired
    private ScheduleJobService scheduleJobService;


    public List<JobBenchmarkExe> showContent() {
        List<JobBenchmarkExe> jobBenchmarkExes = benchmarkExeMapper.selectAll();
        Collections.sort(jobBenchmarkExes);
        return jobBenchmarkExes;
    }

    public void add(String jobId, Integer runTimes) {
        JobBenchmarkExe benchmarkExe = JobBenchmarkExe.builder()
                .jobId(jobId)
                .jobName(jobInfoManageService.getByPrimaryKey(jobId).getJobName())
                .runTimes(runTimes)
                .doneTimes(0)
                .build();
        benchmarkExeMapper.insert(benchmarkExe);
    }


    /**
     * 第一次job exe启动后，后续的运行次数由checkJobService调起
     * 调用下层方法启动job，并在benchmark_exe表更新一条记录
     *
     * @param benchId
     * @return
     */
    public ExecuteResult start(Integer benchId) {
        log.info("start benchmark: benchId={}", benchId);
        ExecuteResult executeResult = new ExecuteResult(-1, "");
        try {
            if (checkBeforeStart(benchId)) {
                JobBenchmarkExe benchExe = benchmarkExeMapper.selectByPrimaryKey(benchId);// 该条记录之前由前端insert
                StartJobResult startJobResult =
                        scheduleJobService.startJob(jobInfoManageService.getByPrimaryKey(benchExe.getJobId()));

                benchExe.setStartTime(LocalDateTime.now());
                benchExe.addExeId(startJobResult.getJobExecutionId());

                executeResult = startJobResult.getExecuteResult();
                if (executeResult.getCode() == 0) { // 如果job 启动成功
                    benchExe.setState(BenchmarkStateConst.RUNNING);
                } else { // 如果job 启动失败
                    benchExe.setState(BenchmarkStateConst.TERMINATED);
                    benchExe.setEndTime(LocalDateTime.now());
                    benchExe.setDuration(Duration.between(benchExe.getStartTime(), benchExe.getEndTime()).getSeconds());
                }

                benchmarkExeMapper.updateByPrimaryKey(benchExe);
            }
        } catch (Exception e) {
            executeResult.setMsg(e.getLocalizedMessage());
        }
        return executeResult;
    }


    private boolean checkBeforeStart(Integer benchId) throws Exception {
        if (!runModeService.isBenchmark()) {
            throw new Exception("current runMode is not benchmark");
        }

        List<JobBenchmarkExe> jobBenchmarkExes = benchmarkExeMapper.selectByState(BenchmarkStateConst.RUNNING);
        if (jobBenchmarkExes.size() > 0) {
            throw new Exception("there is another job running in benchmark mode! job_id=" +
                    jobBenchmarkExes.get(0).getJobId());
        }

        JobBenchmarkExe benchmarkExe = benchmarkExeMapper.selectByPrimaryKey(benchId);
        assert benchmarkExe != null;
        if (benchmarkExe.isRunning()) {
            throw new Exception("benchmark is already running");
        }
        if (benchmarkExe.isCompleted()) {
            throw new Exception("benchmark is completed, can not run again");
        }
        if (benchmarkExe.isTerminated()) {
            throw new Exception("benchmark is terminated, can not run again");
        }

        return true;
    }

    public ExecuteResult stop(Integer benchId) {
        log.info("stop benchmark: benchId={}", benchId);
        ExecuteResult executeResult = new ExecuteResult(-1, "");
        try {
            if (checkBeforeStop(benchId)) {
                JobBenchmarkExe exe = benchmarkExeMapper.selectByPrimaryKey(benchId);
                executeResult = scheduleJobService.stopJob(
                        jobInfoManageService.getByPrimaryKey(exe.getJobId()));
                if (executeResult.getCode() == 0) {
                    log.info("executeResult:{}", executeResult.toString());
                    exe.setState(BenchmarkStateConst.TERMINATED);
                    exe.setEndTime(LocalDateTime.now());
                    exe.setDuration(Duration.between(exe.getStartTime(), exe.getEndTime()).getSeconds());
                    benchmarkExeMapper.updateByPrimaryKey(exe);
                }
            }
        } catch (Exception e) {
            executeResult.setMsg(e.getLocalizedMessage());
        }
        return executeResult;

    }

    private boolean checkBeforeStop(Integer benchId) throws Exception {
        if (!runModeService.isBenchmark()) {
            throw new Exception("current runMode is not benchmark");
        }

        JobBenchmarkExe benchmarkExe = benchmarkExeMapper.selectByPrimaryKey(benchId);
        assert benchmarkExe != null;
        if (!benchmarkExe.isRunning()) {
            throw new Exception("benchmark not running");
        }
        return true;
    }


    public void doNextExecution(JobInfo jobInfo) {
        JobBenchmarkExe crExe = getCurrentRunningBenchExe();

        // 先更新一把完成次数。进入这里，就说明已经跑完了1次
        // crExe.getDoneTimes() 初始为0
        crExe.setDoneTimes(crExe.getDoneTimes() + 1);
        log.info("benchId={}, jobId={}, doneTimes={}", crExe.getBenchId(), crExe.getJobId(), crExe.getDoneTimes());

        if (crExe.getDoneTimes().intValue() == crExe.getRunTimes().intValue()) {
            // 如果次数跑满了
            log.info("benchmark completed! doneTimes={}", crExe.getDoneTimes());
            crExe.setState(BenchmarkStateConst.COMPLETED);
            crExe.setEndTime(LocalDateTime.now());
            crExe.setDuration(Duration.between(crExe.getStartTime(), crExe.getEndTime()).getSeconds());
        } else {
            // 如果没满，再跑下一次
            StartJobResult startJobResult = scheduleJobService.startJob(jobInfo);
            crExe.addExeId(startJobResult.getJobExecutionId());
            ExecuteResult executeResult = startJobResult.getExecuteResult();
            if (executeResult.getCode() != 0) { // 如果下一次执行失败了，本次benchmark终止
                crExe.setState(BenchmarkStateConst.TERMINATED);
                crExe.setEndTime(LocalDateTime.now());
                crExe.setDuration(Duration.between(crExe.getStartTime(), crExe.getEndTime()).getSeconds());
            }
        }

        benchmarkExeMapper.updateByPrimaryKey(crExe);
    }


    private JobBenchmarkExe getCurrentRunningBenchExe() {
        List<JobBenchmarkExe> runningBenchExes = benchmarkExeMapper.selectByState(BenchmarkStateConst.RUNNING);
        assert runningBenchExes.size() == 1;
        return runningBenchExes.get(0);
    }

    /**
     * checkJobService 检测到job terminated时，更新本次 benchmark 的状态为
     */
    public void setTerminated() {
        JobBenchmarkExe crExe = getCurrentRunningBenchExe();
        crExe.setState(BenchmarkStateConst.TERMINATED);
        crExe.setEndTime(LocalDateTime.now());
        crExe.setDuration(Duration.between(crExe.getStartTime(), crExe.getEndTime()).getSeconds());
        benchmarkExeMapper.updateByPrimaryKey(crExe);
    }

    public Object delete(Integer benchId) {
        return benchmarkExeMapper.deleteByPrimaryKey(benchId);
    }
}
