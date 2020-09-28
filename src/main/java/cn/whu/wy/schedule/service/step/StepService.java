package cn.whu.wy.schedule.service.step;

import cn.whu.wy.schedule.beans.StartJobResult;
import cn.whu.wy.schedule.dto.JobStepDto;
import cn.whu.wy.schedule.entity.JobExecution;
import cn.whu.wy.schedule.entity.JobInfo;
import cn.whu.wy.schedule.entity.JobStepExe;
import cn.whu.wy.schedule.executor.ExecuteResult;
import cn.whu.wy.schedule.mapper.IJobStepExeMapper;
import cn.whu.wy.schedule.service.JobExecutionService;
import cn.whu.wy.schedule.service.JobInfoManageService;
import cn.whu.wy.schedule.service.RunModeService;
import cn.whu.wy.schedule.service.ScheduleJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author WangYong
 * @Date 2020/04/10
 * @Time 09:56
 */
@Service
@Slf4j
public class StepService {
    @Autowired
    private JobExecutionService jobExecutionService;
    @Autowired
    private ScheduleJobService scheduleJobService;
    @Autowired
    private JobInfoManageService jobInfoManageService;
    @Autowired
    private IJobStepExeMapper jobStepExeMapper;
    @Autowired
    private RunModeService runModeService;

    public List<JobStepDto> showContent() {
        List<JobStepDto> jobStepDtoList = new ArrayList<>();
        List<JobInfo> jobInfos = jobInfoManageService.getAll();

        jobInfos.forEach(jobInfo -> {
            // 先填充job基本信息
            JobStepDto dto = JobStepDto.builder()
                    .jobId(jobInfo.getJobId())
                    .jobName(jobInfo.getJobName())
                    .preJobId(jobInfo.getPreJobId())
                    .nextJobId(jobInfo.getNextJobId())
//                    .state(JobStateConst.READY) // 默认显示ready
                    .build();

            // 如果有，再更新job的状态
            JobStepExe jobStepExe = jobStepExeMapper.selectByPrimaryKey(jobInfo.getJobId());
            if (jobStepExe != null) {
                JobExecution execution = jobExecutionService.getByPrimaryKey(jobStepExe.getLastExeId());
                dto.setStartTime(execution.getStartTime());
                dto.setEndTime(execution.getEndTime());
                dto.setState(execution.getState());
                dto.setDuration(execution.getDuration());
            }

            jobStepDtoList.add(dto);
        });

//        Collections.sort(jobStepDtoList);
//        return jobStepDtoList;
        return sortByLink(jobStepDtoList);
    }



    /**
     * 先检查该运行模式、该job的状态；
     * 然后调用下层方法启动job；
     * 最后更新 job_step_exe 表的 last_exe_id
     *
     * @param jobId
     * @return
     */
    public ExecuteResult startJob(String jobId) {
        log.info("startJob, jobId={}", jobId);
        ExecuteResult executeResult = new ExecuteResult(-1, "");
        JobInfo jobInfo = jobInfoManageService.getByPrimaryKey(jobId);
        if (!runModeService.isStep()) {
            executeResult.setMsg("error! current runMode is not step");
            return executeResult;
        }

        JobStepExe stepExe = jobStepExeMapper.selectByPrimaryKey(jobId);
        if (stepExe == null) { // 这个job第一次跑step模式
            stepExe = new JobStepExe(jobInfo.getJobId(), null);
            jobStepExeMapper.insert(stepExe);
        } else {
            Integer lastExeId = stepExe.getLastExeId();
            assert lastExeId != null;
            JobExecution jobExe = jobExecutionService.getByPrimaryKey(lastExeId);
            assert jobExe != null;
            if (jobExe.isRunning()) {
                executeResult.setMsg("error! job is already running");
                return executeResult;
            }
        }

        try {
            StartJobResult startJobResult = scheduleJobService.startJob(jobInfo);
            executeResult = startJobResult.getExecuteResult();
            Integer exeId = startJobResult.getJobExecutionId();
            if (exeId != -1) {
                stepExe.setLastExeId(exeId);
            }
            jobStepExeMapper.updateByPrimaryKey(stepExe);
        } catch (Exception e) {
            executeResult.setMsg(e.getLocalizedMessage());
        }

        return executeResult;
    }

    public ExecuteResult stopJob(String jobId) {
        log.info("stopJob,jobId={}", jobId);
        ExecuteResult executeResult = new ExecuteResult(-1, "");
        JobInfo jobInfo = jobInfoManageService.getByPrimaryKey(jobId);
        if (!runModeService.isStep()) {
            executeResult.setMsg("error! current runMode is not step");
            return executeResult;
        }

        List<JobExecution> runningExes = jobExecutionService.getRunningJobBy(jobId);
        assert runningExes.size() <= 1;
        if (runningExes.size() == 0) {
            executeResult.setMsg("error! job not running");
            return executeResult;
        } else {
            try {
                executeResult = scheduleJobService.stopJob(jobInfo);
                if (executeResult.getCode() == 0) {
                    log.info("executeResult:{}", executeResult.toString());
                    //没有其他表需要更新
                }
            } catch (Exception e) {
                executeResult.setMsg(e.getLocalizedMessage());
            }
        }
        return executeResult;
    }


    public void receiveExeId(JobInfo jobInfo, Integer exeId) {
        JobStepExe stepExe = jobStepExeMapper.selectByPrimaryKey(jobInfo.getJobId());
        if (stepExe != null) {
            stepExe.setLastExeId(exeId);
            jobStepExeMapper.updateByPrimaryKey(stepExe);
        } else {
            stepExe = new JobStepExe(jobInfo.getJobId(), exeId);
            jobStepExeMapper.insert(stepExe);
        }
    }

    // 以 nextJobId 构造链表
    private LinkedList<JobStepDto> sortByLink(List<JobStepDto> jobs) {
        Map<String, JobStepDto> map = jobs.stream().collect(Collectors.toMap(JobStepDto::getJobId, Function.identity()));

        LinkedList<JobStepDto> llj = new LinkedList<>();
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

        //不在依赖关系内的job，显示在最后
        if (llj.size() < map.size()) {
            map.forEach((k, v) -> {
                if (!llj.contains(v)) {
                    llj.addLast(v);
                }
            });
        }

        return llj;
    }

}
