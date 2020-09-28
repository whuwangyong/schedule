package cn.whu.wy.schedule.mapper;

import cn.whu.wy.schedule.entity.JobInfo;
import org.apache.ibatis.annotations.Mapper;
import tk.mybatis.mapper.common.BaseMapper;
import tk.mybatis.mapper.common.ExampleMapper;

/**
 * @Author WangYong
 * @Date 2020/03/24
 * @Time 17:11
 */
@Mapper
public interface IJobInfoMapper extends BaseMapper<JobInfo>, ExampleMapper<JobInfo> {
}
