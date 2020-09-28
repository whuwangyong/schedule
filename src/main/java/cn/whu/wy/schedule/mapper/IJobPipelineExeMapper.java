package cn.whu.wy.schedule.mapper;

import cn.whu.wy.schedule.consts.PipelineStateConst;
import cn.whu.wy.schedule.entity.JobExecution;
import cn.whu.wy.schedule.entity.JobPipelineExe;
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
public interface IJobPipelineExeMapper extends
        BaseMapper<JobPipelineExe>, ExampleMapper<JobPipelineExe> {

    // 返回的 list.size() <=1
    default List<JobPipelineExe> selectRunningPipeline() {
        Example example = new Example(JobExecution.class);
        example.createCriteria().andEqualTo("state", PipelineStateConst.RUNNING);
        return selectByExample(example);
    }
}
