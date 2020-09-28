package cn.whu.wy.schedule.service;

import cn.whu.wy.schedule.consts.JobStateConst;
import cn.whu.wy.schedule.consts.RunModeConst;
import cn.whu.wy.schedule.entity.JobExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author WangYong
 * @Date 2020/04/07
 * @Time 10:51
 */
@Service
@Slf4j
public class RunModeService implements InitializingBean {

    @Autowired
    private JobExecutionService jobExecutionService;
    @Value("${schedule.runMode}")
    private String runMode;

    public String getRunMode() {
        return runMode;
    }

    /**
     * 切换模式时，如果当前模式有正在运行的job，则禁止切换
     *
     * @param newMode
     */
    public String changeRunMode(String newMode) {
        if (newMode == null || newMode.isEmpty()) {
            return "error! newMode should not be null!";
        }
        if (!RunModeConst.getAllMode().contains(newMode)) {
            return "error! newMode is illegal!";
        }
        if (getUsedMode() != null) {
            return "error! current mode[" + getRunMode() + "] has running jobs";
        }
        this.runMode = newMode;
        log.info("changeRunMode: newMode={}", newMode);
        return "success";
    }


    /**
     * 获取正在运行着的job使用的 mode
     *
     * @return
     */
    public String getUsedMode() {
        List<JobExecution> runningExecutions = jobExecutionService.getByState(JobStateConst.RUNNING);
        for (JobExecution execution : runningExecutions) {
            log.info("getUsedMode: jobId={}, jobState={}, runMode={}",
                    execution.getJobId(), execution.getState(), execution.getRunMode());
            return execution.getRunMode();
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String usedMode = getUsedMode();
        // 若不为空，表示有尚未跑完的job正在使用该runMode，而此时配置文件的runMode被修改了，不予启动。
        if (usedMode != null && !usedMode.equals(runMode)) {
            log.warn("有尚未跑完的job正在使用 {} 模式，而此时配置文件被修改为 {} 模式，二者不一致。" +
                    "已忽略配置文件，使用 {} 模式启动", usedMode, runMode, usedMode);
            this.runMode = usedMode;
        }

    }

    public boolean isPipeline() {
        return this.getRunMode().equals(RunModeConst.PIPELINE);
    }

    public boolean isStep() {
        return this.getRunMode().equals(RunModeConst.STEP);
    }

    public boolean isBenchmark() {
        return this.getRunMode().equals(RunModeConst.BENCHMARK);
    }


}
