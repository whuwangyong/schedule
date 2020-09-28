package cn.whu.wy.schedule.controller;

import cn.whu.wy.schedule.dto.JobStepDto;
import cn.whu.wy.schedule.dto.Result;
import cn.whu.wy.schedule.executor.ExecuteResult;
import cn.whu.wy.schedule.service.RunModeService;
import cn.whu.wy.schedule.service.step.StepService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 对于Step模式，允许同时运行多个job，但是一个job只有一个execution实例
 * 所以没有必要修改状态了，依赖关系不需要了
 *
 * @Author WangYong
 * @Date 2020/04/10
 * @Time 10:00
 */

@Controller
@RequestMapping("/api/step")
@Slf4j
public class StepController {
    @Autowired
    private StepService stepService;

    @Autowired
    private RunModeService runModeService;


    @GetMapping("/")
    public String show(Model model) {
        List<JobStepDto> stepDtos = stepService.showContent();
        model.addAttribute("stepDtos", stepDtos);
        model.addAttribute("runMode", runModeService.getRunMode());
        return "job-step";
    }

    @PostMapping(value = "/startJob")
    @ResponseBody
    public Object startJob(String jobId) {
        log.info("start job {}", jobId);
        Result result = Result.failure();
        try {
            ExecuteResult executeResult = stepService.startJob(jobId);
            result.setMsg(executeResult.getMsg());

            if (executeResult.getCode() == 0)
                result = Result.success();
            else log.error(executeResult.getMsg());
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            log.error("startJob ex:", e);
        }
        return result;
    }

    @PostMapping(value = "/stopJob")
    @ResponseBody
    public Object stopJob(String jobId) {
        log.info("stop job {}", jobId);
        Result result = Result.failure();
        try {
            ExecuteResult executeResult = stepService.stopJob(jobId);
            result.setMsg(executeResult.getMsg());

            if (executeResult.getCode() == 0)
                result = Result.success();
            else log.error(executeResult.getMsg());
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            log.error("stopJob ex:", e);
        }
        return result;
    }


}
