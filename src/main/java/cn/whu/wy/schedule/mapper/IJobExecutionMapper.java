package cn.whu.wy.schedule.mapper;

import cn.whu.wy.schedule.consts.JobStateConst;
import cn.whu.wy.schedule.entity.JobExecution;
import org.apache.ibatis.annotations.Mapper;
import tk.mybatis.mapper.common.BaseMapper;
import tk.mybatis.mapper.common.ExampleMapper;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author WangYong
 * @Date 2020/04/07
 * @Time 10:50
 */
@Mapper
public interface IJobExecutionMapper extends BaseMapper<JobExecution>, ExampleMapper<JobExecution> {


    default List<JobExecution> selectExesIn(List<String> exeIds) {
        Example example = new Example(JobExecution.class);
        example.createCriteria().andIn("exeId", exeIds);
        return selectByExample(example);
    }

    /**
     * 当三个条件都为 null 时，等效于 selectAll()
     *
     * @param jobId
     * @param state
     * @param runMode
     * @return
     */
    default List<JobExecution> selectBy(String jobId, String state, String runMode) {
        Example example = new Example(JobExecution.class, true, false);
        example.createCriteria().andEqualTo("jobId", jobId)
                .andEqualTo("state", state)
                .andEqualTo("runMode", runMode);
        return selectByExample(example);
    }

    default List<JobExecution> selectByMode(String runMode) {
        Example example = new Example(JobExecution.class);
        example.createCriteria().andEqualTo("runMode", runMode);
        return selectByExample(example);
    }

    // 返回的list，size<=1。原因见 JobExecution 描述
    default List<JobExecution> selectRunningJob(String jobId) {
        Example example = new Example(JobExecution.class);
        example.createCriteria().andEqualTo("jobId", jobId)
                .andEqualTo("state", JobStateConst.RUNNING);
        return selectByExample(example);
    }

    default List<JobExecution> selectByJobId(String jobId) {
        Example example = new Example(JobExecution.class);
        example.createCriteria().andEqualTo("jobId", jobId);
        return selectByExample(example);
    }

    default List<JobExecution> selectByState(String state) {
        Example example = new Example(JobExecution.class);
        example.createCriteria().andEqualTo("state", state);
        return selectByExample(example);
    }

    default List<JobExecution> selectByModeAndState(String runMode, String state) {
        Example example = new Example(JobExecution.class);
        example.createCriteria().andEqualTo("runMode", runMode)
                .andEqualTo("state", state);
        return selectByExample(example);
    }

}
