package cn.whu.wy.schedule.service;

import cn.whu.wy.schedule.consts.JobStateConst;
import cn.whu.wy.schedule.entity.JobExecution;
import cn.whu.wy.schedule.entity.JobInfo;
import cn.whu.wy.schedule.mapper.IJobExecutionMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author WangYong
 * @Date 2020/04/08
 * @Time 17:18
 */
@Service
@Slf4j
public class JobExecutionService {

    @Autowired
    private IJobExecutionMapper jobExecutionMapper;
    @Autowired
    private RunModeService runModeService;
    @Autowired
    private JobInfoManageService jobInfoManageService;

    @Value("${schedule.startJob.sh.resultMsg.maxLength}")
    private int maxLength;
    @Value("${schedule.pageHelper.navigatePages:5}")
    private int navigatePages;

    private Lock lock = new ReentrantLock();


    public List<JobExecution> query(List<String> exeIds) {
        return jobExecutionMapper.selectExesIn(exeIds);
    }

    public Map<String, Object> showAll(String jobId, String state, String runMode, int pageNum, int pageSize) {
        //以下三个字段是为了实现筛选，赋值为 null 是为了配合通用 mapper 的 Example
        if ("".equals(jobId)) jobId = null;
        if ("".equals(state)) state = null;
        if ("".equals(runMode)) runMode = null;

        Map<String, Object> map = new HashMap<>();

        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("exe_id DESC");
        List<JobExecution> jobExecutions = jobExecutionMapper.selectBy(jobId, state, runMode);
        map.put("executions", jobExecutions); // 最原始的数据展示

        //以下是为了实现翻页
        PageInfo<JobExecution> pageInfo = new PageInfo<>(jobExecutions, navigatePages);
        map.put("pageInfo", pageInfo);
        map.put("pageNum", pageInfo.getPageNum());
        map.put("pageSize", pageInfo.getPageSize());
        map.put("isFirstPage", pageInfo.isIsFirstPage());
        map.put("isLastPage", pageInfo.isIsLastPage());
        map.put("totalPages", pageInfo.getPages());
        map.put("navigatePages", pageInfo.getNavigatePages());

        //以下是为了在筛选modal-dialog显示
        map.put("jobs", jobInfoManageService.getAllJobsSimple());

        //以下是为了筛选后的翻页能符合预期
        map.put("jobId", jobId);
        map.put("state", state);
        map.put("runMode", runMode);
        return map;
    }


    public synchronized void getLock() {
        lock.lock();
    }

    public void releaseLock() {
        lock.unlock();
    }


    /**
     * pipeline：最多只有一个job在运行，且只有一个execution
     * step：可以有多个job在运行，每个job只有一个execution
     * benchmark：最多只有一个job在运行，且只有一个execution
     *
     * @return
     */
    List<JobExecution> getRunningJobExecutions() {
        List<JobExecution> runningExecutions = jobExecutionMapper.selectByState(JobStateConst.RUNNING);

        // 顺便做个检查
        if (runningExecutions.size() > 1 && !runModeService.isStep()) {
            log.error("非Step模式下居然有多个jobExecution实例，出bug了！");
            log.error("runningExecutions={},runMode={}", runningExecutions.toString(), runModeService.getRunMode());
        }

//        List<JobInfo> jobList = new ArrayList<>();
//        runningExecutions.forEach(exe -> jobList.add(jobMapper.selectByPrimaryKey(exe.getJobId())));

        return runningExecutions;
    }


    /**
     * job complete 或者 terminate时，调用该方法更新execution
     *
     * @param jobExecution
     * @param newState
     */
    public void updateState2End(JobExecution jobExecution, String newState, String resultMsg) {
        log.info("updateState2End: jobID={}, exeId={}, newState={}",
                jobExecution.getJobId(), jobExecution.getExeId(), newState);
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(jobExecution.getStartTime(), endTime);
        jobExecution.setState(newState);
        jobExecution.setEndTime(endTime);
        jobExecution.setDuration(duration.getSeconds());
        jobExecution.setResultMsg(resultMsg.length() > maxLength ? resultMsg.substring(0, maxLength) : resultMsg);
        jobExecutionMapper.updateByPrimaryKey(jobExecution);
    }

    /**
     * 方法1：jdbc 执行 select LAST_INSERT_ID()
     * 方法2：select all，然后取 MAX(id)。
     * <p>
     * 采用2。
     *
     * @return
     */
    public OptionalInt getLastInsertId() {
        return jobExecutionMapper.selectAll().stream().mapToInt(JobExecution::getExeId).max();
    }

    /**
     * @param jobInfo
     * @return job执行后的exe_id，由job_execution表exe_id字段自增而来
     */
    public Integer addExecution(JobInfo jobInfo, String state, String resultMsg) {
        JobExecution execution = JobExecution.builder()
                .jobId(jobInfo.getJobId())
                .state(state)
                .startTime(LocalDateTime.now())
                .runMode(runModeService.getRunMode())
                .resultMsg(resultMsg.length() > maxLength ? resultMsg.substring(0, maxLength) : resultMsg)
                .build();
        if (state.equals(JobStateConst.TERMINATED)) {
            execution.setEndTime(execution.getStartTime());
            execution.setDuration((long) 0);
        }
        jobExecutionMapper.insert(execution);
        return execution.getExeId();
    }

    public List<JobExecution> getRunningJobBy(String jobId) {
        return jobExecutionMapper.selectRunningJob(jobId);
    }

    public JobExecution getByPrimaryKey(Object key) {
        return jobExecutionMapper.selectByPrimaryKey(key);
    }

    public List<JobExecution> getBy(String runMode, String state) {
        return jobExecutionMapper.selectByModeAndState(runMode, state);
    }

    public List<JobExecution> getByMode(String runMode) {
        return jobExecutionMapper.selectByMode(runMode);
    }

    public List<JobExecution> getByState(String state) {
        return jobExecutionMapper.selectByState(state);
    }

}
