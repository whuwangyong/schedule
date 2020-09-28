package cn.whu.wy.schedule.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @Author WangYong
 * @Date 2020/06/02
 * @Time 14:04
 */

public class JobPipelineExeTest {

    @Test
    public void popExeId() {
        JobPipelineExe pipelineExe = new JobPipelineExe();
        pipelineExe.setExeIds("1,2,3,4,5");
        assertEquals("5", pipelineExe.popExeId());
        pipelineExe.addExeId(6);
        assertEquals(pipelineExe.getExeIds(), "1,2,3,4,6");

        pipelineExe.setExeIds("");
        assertEquals(pipelineExe.popExeId(), "");

        pipelineExe.setExeIds("1");
        assertEquals(pipelineExe.popExeId(), "1");
    }
}