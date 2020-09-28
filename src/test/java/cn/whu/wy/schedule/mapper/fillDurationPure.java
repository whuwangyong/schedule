package cn.whu.wy.schedule.mapper;

import cn.whu.wy.schedule.entity.JobExecution;
import cn.whu.wy.schedule.entity.JobPipelineExe;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * 填充 job_pipeline_exe 新增的 duration_pure 字段
 *
 * @Author WangYong
 * @Date 2020/06/01
 * @Time 13:58
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class fillDurationPure {

    @Autowired
    private IJobExecutionMapper jobExecutionMapper;
    @Autowired
    private IJobPipelineExeMapper jobPipelineExeMapper;

    @Test
    public void test() {
        List<JobPipelineExe> pipelineExes = jobPipelineExeMapper.selectAll();
        pipelineExes.forEach(exe -> {
            String[] exeIds = exe.getExeIds().split(",");
            long sum = 0;
            for (String exeId : exeIds) {
                JobExecution execution = jobExecutionMapper.selectByPrimaryKey(exeId);
                sum += execution.getDuration();
            }
            exe.setDurationPure(sum);
            jobPipelineExeMapper.updateByPrimaryKey(exe);
        });
    }

}