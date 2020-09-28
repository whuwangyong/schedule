package cn.whu.wy.schedule.service.pipeline;

import cn.whu.wy.schedule.beans.StartJobResult;
import cn.whu.wy.schedule.consts.JobStateConst;
import cn.whu.wy.schedule.consts.PipelineStateConst;
import cn.whu.wy.schedule.consts.RunModeConst;
import cn.whu.wy.schedule.dto.JobPipelineDto;
import cn.whu.wy.schedule.entity.JobExecution;
import cn.whu.wy.schedule.entity.JobInfo;
import cn.whu.wy.schedule.entity.JobPipelineBench;
import cn.whu.wy.schedule.entity.JobPipelineExe;
import cn.whu.wy.schedule.executor.ExecuteResult;
import cn.whu.wy.schedule.mapper.IJobPipelineBenchMapper;
import cn.whu.wy.schedule.mapper.IJobPipelineExeMapper;
import cn.whu.wy.schedule.service.JobExecutionService;
import cn.whu.wy.schedule.service.JobInfoManageService;
import cn.whu.wy.schedule.service.RunModeService;
import cn.whu.wy.schedule.service.ScheduleJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 负责pipeline的启停
 *
 * @Author WangYong
 * @Date 2020/04/07
 * @Time 15:51
 */
@Service
@Slf4j
public class PipelineService implements InitializingBean {

    @Autowired
    private IJobPipelineExeMapper pipelineExeMapper;
    @Autowired
    private ScheduleJobService scheduleJobService;
    @Autowired
    private RunModeService runModeService;
    @Autowired
    private IJobPipelineBenchMapper pipelineBenchMapper;
    @Autowired
    private JobInfoManageService jobInfoManageService;
    @Autowired
    private JobExecutionService jobExecutionService;

    private int roundsTotal; // pipeline 总共跑几轮
    private int roundsDone; // 已经跑了几轮

    /**
     * 让调度程序无状态
     * 当程序重启或者多实例时，能保持状态一致
     */
    @Override
    public void afterPropertiesSet() {
        List<JobPipelineBench> all = pipelineBenchMapper.selectAll();
        if (all.size() == 0) {
            this.roundsTotal = 0;
            this.roundsDone = 0;
        } else {
            assert all.size() == 1;
            JobPipelineBench bench = all.get(0);
            this.roundsTotal = bench.getRoundsTotal();
            this.roundsDone = bench.getRoundsDone();
        }
    }


    /**
     * 断点续作。步骤：
     * 1. 找到 lastPipelineExe，且其状态必须是 terminated
     * 2.
     */
    public ExecuteResult resume() {
        if (!runModeService.isPipeline())
            return new ExecuteResult(-1, "error! current runMode is not pipeline");

        JobPipelineExe lastPipelineExe = getLastPipelineExe();
        if (lastPipelineExe == null)
            return new ExecuteResult(-1, "lastPipelineExe is null");

        if (!lastPipelineExe.getState().equals(PipelineStateConst.TERMINATED))
            return new ExecuteResult(-1, "lastPipelineExe.state is not terminated");


        if (lastPipelineExe.getExeIds().isEmpty()) { // 本次 pipeline 第一个job 都还没运行就终止了
            return start();// 直接另起一个 pipeline，重新开始
        } else {
            return resume0(lastPipelineExe);
        }
    }

    private ExecuteResult resume0(JobPipelineExe lastPipelineExe) {
        ExecuteResult executeResult;

        // 先从exeIds里移除终止job的exeId
        String lastJobExeId = lastPipelineExe.popExeId();

        // 然后再次执行终止的job
        JobInfo lastJob = jobInfoManageService.getByPrimaryKey(jobExecutionService.getByPrimaryKey(lastJobExeId).getJobId());
        log.info("pipeline resume from jobId={}", lastJob.getJobId());
        StartJobResult startJobResult = scheduleJobService.startJob(lastJob);

        // 将exeId加入pipelineExe，并修改状态
        lastPipelineExe.addExeId(startJobResult.getJobExecutionId());
        executeResult = startJobResult.getExecuteResult();
        if (executeResult.getCode() == 0) {
            lastPipelineExe.setState(PipelineStateConst.RUNNING);
        } else {
            lastPipelineExe.setState(PipelineStateConst.TERMINATED);
            lastPipelineExe.setEndTime(LocalDateTime.now());
            lastPipelineExe.setDuration(Duration.between(lastPipelineExe.getStartTime(), lastPipelineExe.getEndTime()).getSeconds());
        }
        pipelineExeMapper.updateByPrimaryKey(lastPipelineExe);

        return executeResult;
    }


    /**
     * （1）启动 pipeline，按照依赖顺序自动执行所有job。
     * 实际上，这里只需要启动第一个job，并保证 runMode 为pipeline，CheckJobService 会自动执行下一个job。
     * （2）新增一条记录至 job_pipeline_exe 表
     */
    public ExecuteResult start() {
        log.info("pipeline start");
        ExecuteResult executeResult = new ExecuteResult(-1, "");
        try {
            if (checkBeforeStart()) {
                JobInfo firstJobInfo = jobInfoManageService.getFirstJob();
                StartJobResult startJobResult = scheduleJobService.startJob(firstJobInfo);
                executeResult = startJobResult.getExecuteResult();

                JobPipelineExe pipelineExe = new JobPipelineExe();
                pipelineExe.setStartTime(LocalDateTime.now());
                pipelineExe.addExeId(startJobResult.getJobExecutionId());

                if (executeResult.getCode() == 0) {
                    pipelineExe.setState(PipelineStateConst.RUNNING);
                } else {
                    pipelineExe.setState(PipelineStateConst.TERMINATED);
                    pipelineExe.setEndTime(LocalDateTime.now());
                    pipelineExe.setDuration(Duration.between(pipelineExe.getStartTime(),
                            pipelineExe.getEndTime()).getSeconds());
                    pipelineExe.setDurationPure(0L);
                }

                pipelineExeMapper.insert(pipelineExe);

                if (roundsDone == roundsTotal) {//说明是N轮跑完后，重新开始
                    this.roundsDone = 0;
                    updateOrInsertRounds(roundsTotal, roundsDone);
                }
            }
        } catch (Exception e) {
            executeResult.setMsg(e.getLocalizedMessage());
        }
        return executeResult;
    }


    /**
     * 停止 pipeline
     * 获取当前正在执行的job，调用scheduleJobService.stop(job)即可
     *
     * @return
     */
    public ExecuteResult stop() {
        ExecuteResult executeResult = new ExecuteResult(-1, "");
        if (!runModeService.isPipeline()) {
            executeResult.setMsg("error! current runMode is not pipeline");
            return executeResult;
        }
        List<JobPipelineExe> rPipelines = pipelineExeMapper.selectRunningPipeline();
        if (rPipelines.size() == 0) {
            executeResult.setMsg("error! pipeline not running");
            return executeResult;
        }

        assert rPipelines.size() == 1;
        JobPipelineExe rPipeline = rPipelines.get(0);

        try {
            List<JobExecution> runningExes = jobExecutionService.getBy(RunModeConst.PIPELINE, JobStateConst.RUNNING);
            // 如果pipeline是running状态，则job_execution里有且仅有一条running记录
            assert runningExes.size() == 1;
            JobInfo jobInfo = jobInfoManageService.getByPrimaryKey(runningExes.get(0).getJobId());
            executeResult = scheduleJobService.stopJob(jobInfo);
            if (executeResult.getCode() == 0) {
                log.info("executeResult:{}", executeResult.toString());
                rPipeline.setState(PipelineStateConst.TERMINATED);
                rPipeline.setEndTime(LocalDateTime.now());
                rPipeline.setDuration(Duration.between(rPipeline.getStartTime(),
                        rPipeline.getEndTime()).getSeconds());
                pipelineExeMapper.updateByPrimaryKey(rPipeline);
            }

        } catch (Exception e) {
            executeResult.setMsg(e.getLocalizedMessage());
            log.error(e.getLocalizedMessage());
        }
        return executeResult;
    }


    /**
     * 获取最近的 pipeline 执行，用于前端页面展示
     *
     * @return
     */
    private JobPipelineExe getLastPipelineExe() {
        List<JobPipelineExe> pipelineExes = pipelineExeMapper.selectAll();
        if (pipelineExes.size() == 0) {
            log.info("getLastPipelineExe(): job_pipeline_exe 表为空");
            return null;
        }
        Optional<JobPipelineExe> lastExe = pipelineExes.stream().max(Comparator.comparingInt(JobPipelineExe::getPipelineId));
        return lastExe.get();
    }

    private JobPipelineExe getCurrentRunningPipelineExe() {
        List<JobPipelineExe> rPipelines = pipelineExeMapper.selectRunningPipeline();
        assert rPipelines.size() == 1;
        return rPipelines.get(0);
    }


    /**
     * 启动前检查：
     * （1）模式为pipeline
     * （2）最近一次执行的pipeline已经结束
     *
     * @return
     * @throws Exception
     */
    private boolean checkBeforeStart() throws Exception {
        if (!runModeService.isPipeline()) {
            throw new Exception("error! current runMode is not pipeline");
        }
        List<JobPipelineExe> rPipelines = pipelineExeMapper.selectRunningPipeline();
        if (rPipelines.size() == 1) {
            throw new Exception("error! pipeline is already running");
        }
        return true;
    }


    /**
     * 启动pipeline依赖链中的下一个job，并更新 job_pipeline_exe 的exe_ids
     *
     * @param currentJobInfo
     */
    public ExecuteResult startNextJob(JobInfo currentJobInfo) {
        JobPipelineExe rPipeline = getCurrentRunningPipelineExe();

        String nextJobId = currentJobInfo.getNextJobId();
        if (nextJobId != null && !nextJobId.trim().isEmpty()) { //如果后面还有job，继续run起来，新增一个exe_id
            JobInfo nextJobInfo = jobInfoManageService.getByPrimaryKey(nextJobId);
            StartJobResult startJobResult = scheduleJobService.startJob(nextJobInfo);

            rPipeline.addExeId(startJobResult.getJobExecutionId());
            pipelineExeMapper.updateByPrimaryKey(rPipeline);

            ExecuteResult executeResult = startJobResult.getExecuteResult();
            if (executeResult.getCode() != 0) { // 如果下一个job没启起来
                setTerminated();
            }

            return executeResult;
        } else {
            // 如果没有了，更新一下状态，再跑下一轮
            rPipeline.setState(PipelineStateConst.COMPLETED);
            rPipeline.setEndTime(LocalDateTime.now());
            rPipeline.setDuration(Duration.between(rPipeline.getStartTime(), rPipeline.getEndTime()).getSeconds());
            rPipeline.setDurationPure(computeDurationPure(rPipeline));
            pipelineExeMapper.updateByPrimaryKey(rPipeline);

            String msg = "pipeline is completed! the last job in pipeline has been executed.";
            log.info(msg);

            roundsDone++;
            updateOrInsertRounds(roundsTotal, roundsDone);
            nextRound();

            return new ExecuteResult(0, msg);
        }
    }

    /**
     * 计算一个pipeline各job执行的耗时之和
     *
     * @param pipelineExe
     * @return
     */
    private Long computeDurationPure(JobPipelineExe pipelineExe) {
        long sum = 0;
        String[] exeIds = pipelineExe.getExeIds().split(",");
        for (String exeId : exeIds) {
            JobExecution execution = jobExecutionService.getByPrimaryKey(exeId);
            if (execution != null)
                sum += execution.getDuration();
        }
        return sum;
    }


    /**
     * 这是一个参数性质的页面，显示pipeline包含了几个job，以及他们的顺序。
     * （1）当pipeline一次都还没执行的时候，所有job的状态为ready/null
     * （2）当pipeline正在执行，显示该pipeline_id 下job的状态
     * （3）当pipeline跑完了，显示该pipeline_id 下job的状态
     *
     * @return
     */
    public List<JobPipelineDto> showContent() {
        List<JobPipelineDto> jobPipelineDtoList = new ArrayList<>();
        List<JobInfo> jobInfos = jobInfoManageService.getAll();

        // 先填充job基本信息
        jobInfos.forEach(jobInfo -> {
            JobPipelineDto dto = JobPipelineDto.builder()
                    .jobId(jobInfo.getJobId())
                    .jobName(jobInfo.getJobName())
                    .preJobId(jobInfo.getPreJobId())
                    .nextJobId(jobInfo.getNextJobId())
//                    .state(JobStateConst.READY) // 默认显示ready
                    .build();
            jobPipelineDtoList.add(dto);
        });


        // 再更新job的状态
        Map<String, JobPipelineDto> map = jobPipelineDtoList.stream()
                .collect(Collectors.toMap(JobPipelineDto::getJobId, Function.identity()));
        Objects.requireNonNull(getLastPipelineExe()).getExeIdAsList().forEach(exeId -> {
            JobExecution execution = jobExecutionService.getByPrimaryKey(exeId);
            JobPipelineDto dto = map.get(execution.getJobId());
            if (dto != null) { // 如果前端在jobInfo中删除某job后，execution取得的jobId在map中不存在，会导致NPE
                dto.setState(execution.getState());
                dto.setStartTime(execution.getStartTime());
                dto.setEndTime(execution.getEndTime());
                dto.setDuration(execution.getDuration());
            }
        });

//        Collections.sort(jobPipelineDtoList);
//        return jobPipelineDtoList;
        return sortByLink(jobPipelineDtoList);
    }

    /**
     * checkJobService 检测到job terminated时，更新本次pipeline的状态为
     */
    public void setTerminated() {
        JobPipelineExe rPipeline = getCurrentRunningPipelineExe();
        rPipeline.setState(PipelineStateConst.TERMINATED);
        rPipeline.setEndTime(LocalDateTime.now());
        rPipeline.setDuration(Duration.between(rPipeline.getStartTime(), rPipeline.getEndTime()).getSeconds());
        pipelineExeMapper.updateByPrimaryKey(rPipeline);
    }


    public void setRoundsTotal(int roundsTotal) {
        if (roundsTotal <= roundsDone) {
            log.error("roundsTotal <= roundsDone");
        } else {
            this.roundsTotal = roundsTotal;
            updateOrInsertRounds(roundsTotal, roundsDone);
        }
    }

    private void nextRound() {
        log.info("已跑完 {} 轮 pipeline，还剩 {} 轮", roundsDone, roundsTotal - roundsDone);

        if (roundsDone < roundsTotal) {
            start();
        }
    }

    public String showRoundStatus() {
        return String.format("[%s] 已跑完 %d 轮 pipeline，还剩 %d 轮.",
                LocalDateTime.now().toString().replace('T', ' '), roundsDone, roundsTotal - roundsDone);
    }


    private void updateOrInsertRounds(int total, int done) {
        List<JobPipelineBench> all = pipelineBenchMapper.selectAll();
        if (all.size() == 0) {
            pipelineBenchMapper.insert(new JobPipelineBench(1, total, done));
        } else {
            assert all.size() == 1;
            JobPipelineBench bench = all.get(0);
            bench.setRoundsTotal(total);
            bench.setRoundsDone(done);
            pipelineBenchMapper.updateByPrimaryKey(bench);
        }


    }

    // 以 nextJobId 构造链表
    private LinkedList<JobPipelineDto> sortByLink(List<JobPipelineDto> jobs) {
        Map<String, JobPipelineDto> map = jobs.stream().collect(Collectors.toMap(JobPipelineDto::getJobId, Function.identity()));

        LinkedList<JobPipelineDto> llj = new LinkedList<>();
        jobs.forEach(job -> {
            if (job.getPreJobId() == null || job.getPreJobId().trim().isEmpty()) {
                llj.addFirst(job);
            }
        });

        if (!llj.isEmpty()) {
            String nextId;
            while ((nextId = llj.getLast().getNextJobId()) != null && !nextId.trim().isEmpty()) {
                llj.addLast(map.get(nextId));
            }
        }

        // 不在依赖关系内的job，显示在最后
        // 不予显示
        /*
        if (llj.size() < map.size()) {
            map.forEach((k, v) -> {
                if (!llj.contains(v)) {
                    llj.addLast(v);
                }
            });
        }
        */

        return llj;
    }
}
