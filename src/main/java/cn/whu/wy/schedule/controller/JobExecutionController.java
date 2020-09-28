package cn.whu.wy.schedule.controller;

import cn.whu.wy.schedule.entity.JobExecution;
import cn.whu.wy.schedule.service.JobExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author WangYong
 * @Date 2020/04/14
 * @Time 14:14
 */
@Controller
@RequestMapping("/api/execution")
@Slf4j
public class JobExecutionController {

    @Autowired
    private JobExecutionService executionService;

    @GetMapping("/show")
    public String show(Model model, @RequestParam String exeIdStr) {
        if (exeIdStr == null || exeIdStr.trim().isEmpty()) {
            log.error("exeIds is illegal");
        } else {
            List<JobExecution> executions = executionService.query(Arrays.asList(exeIdStr.split(",")));
            model.addAttribute("executions", executions);
        }
        return "job-execution";
    }

    @GetMapping("/showAll")
    public String showAll(Model model,
                          @RequestParam(value = "jobId", required = false) String jobId,
                          @RequestParam(value = "state", required = false) String state,
                          @RequestParam(value = "runMode", required = false) String runMode,
                          @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                          @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {
        Map<String, Object> stringObjectMap = executionService.showAll(jobId, state, runMode, pageNum, pageSize);
        model.addAllAttributes(stringObjectMap);
        return "job-execution-all";
    }
}
