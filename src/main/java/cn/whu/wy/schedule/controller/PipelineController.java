package cn.whu.wy.schedule.controller;

import cn.whu.wy.schedule.dto.JobPipelineDto;
import cn.whu.wy.schedule.dto.Result;
import cn.whu.wy.schedule.executor.ExecuteResult;
import cn.whu.wy.schedule.service.JobInfoManageService;
import cn.whu.wy.schedule.service.RunModeService;
import cn.whu.wy.schedule.service.pipeline.PipelineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @Author WangYong
 * @Date 2020/04/07
 * @Time 14:18
 */

@Controller
@RequestMapping("/api/pipeline")
@Slf4j
public class PipelineController {
    @Autowired
    private PipelineService pipelineService;
    @Autowired
    private RunModeService runModeService;
    @Autowired
    private JobInfoManageService jobInfoManageService;

    @GetMapping("/")
    public Object show(Model model) {
        List<JobPipelineDto> pipelineDtos = pipelineService.showContent();
        model.addAttribute("pipelineDtos", pipelineDtos);
        model.addAttribute("roundStatus", pipelineService.showRoundStatus());
        model.addAttribute("runMode", runModeService.getRunMode());
        model.addAttribute("jobs", jobInfoManageService.getAllJobsSimple());
        return "job-pipeline";
    }


    /**
     * 允许在运行过程中修改要跑的轮数
     *
     * @param roundsTotal
     * @param response
     * @throws IOException
     */
    @PostMapping(value = "/setRounds")
    public void setRounds(String roundsTotal, HttpServletResponse response) throws IOException {
        log.info("set: roundsTotal = {}", roundsTotal);
        if (isNumber(roundsTotal) && Integer.valueOf(roundsTotal) > 0) {
            pipelineService.setRoundsTotal(Integer.valueOf(roundsTotal));
        } else {
            log.error("roundsTotal illegal");
        }
        response.sendRedirect("/api/pipeline/");
    }


    @GetMapping(value = "/start")
    @ResponseBody // 因为使用的不是@RestController，所以这个注解必须。否则报错找不到模板。
    public Object start() {
        log.info("start pipeline");
        Result result = Result.failure();
        try {
            ExecuteResult executeResult = pipelineService.start();
            result.setMsg(executeResult.getMsg());

            if (executeResult.getCode() == 0)
                result = Result.success();
            else log.error(executeResult.getMsg());
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            log.error("start pipeline ex:", e);
        }
        return result;
    }

    @GetMapping(value = "/stop")
    @ResponseBody
    public Object stop() {
        log.info("stop pipeline");
        Result result = Result.failure();
        try {
            ExecuteResult executeResult = pipelineService.stop();
            result.setMsg(executeResult.getMsg());

            if (executeResult.getCode() == 0)
                result = Result.success();
            else log.error(executeResult.getMsg());
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            log.error("stop pipeline ex:", e);
        }
        return result;
    }


    @GetMapping(value = "/resume")
    @ResponseBody
    public Object resume() {
        log.info("resume pipeline");
        Result result = Result.failure();
        try {
            ExecuteResult executeResult = pipelineService.resume();
            result.setMsg(executeResult.getMsg());

            if (executeResult.getCode() == 0)
                result = Result.success();
            else log.error(executeResult.getMsg());
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            log.error("resume pipeline ex:", e);
        }
        return result;
    }

    // 0~9的数字，不含负数、小数
    private boolean isNumber(String str) {
        if (str == null || str.trim().isEmpty()) return false;
        String s = str.trim();
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
