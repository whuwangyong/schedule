package cn.whu.wy.schedule.mapper;

import cn.whu.wy.schedule.entity.JobBenchmarkExe;
import org.apache.ibatis.annotations.Mapper;
import tk.mybatis.mapper.common.BaseMapper;
import tk.mybatis.mapper.common.ExampleMapper;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author WangYong
 * @Date 2020/04/12
 * @Time 16:41
 */
@Mapper
public interface IJobBenchmarkExeMapper extends BaseMapper<JobBenchmarkExe>, ExampleMapper<JobBenchmarkExe> {
    default List<JobBenchmarkExe> selectByState(String state) {
        Example example = new Example(JobBenchmarkExe.class);
        example.createCriteria().andEqualTo("state", state);
        return selectByExample(example);

    }
}
