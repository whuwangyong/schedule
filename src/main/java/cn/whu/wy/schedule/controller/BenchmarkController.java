package cn.whu.wy.schedule.controller;

import cn.whu.wy.schedule.dto.Result;
import cn.whu.wy.schedule.entity.JobBenchmarkExe;
import cn.whu.wy.schedule.executor.ExecuteResult;
import cn.whu.wy.schedule.service.JobInfoManageService;
import cn.whu.wy.schedule.service.RunModeService;
import cn.whu.wy.schedule.service.benchmark.BenchmarkService;
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
 * @Date 2020/04/10
 * @Time 09:37
 */
@Controller
@RequestMapping("/api/benchmark")
@Slf4j
public class BenchmarkController {

    @Autowired
    private BenchmarkService benchmarkService;
    @Autowired
    private RunModeService runModeService;
    @Autowired
    private JobInfoManageService jobInfoManageService;

    @GetMapping("/")
    public String show(Model model) {
        List<JobBenchmarkExe> benchmarkExes = benchmarkService.showContent();
        model.addAttribute("benchmarkExes", benchmarkExes);
        model.addAttribute("jobs", jobInfoManageService.getAllJobsSimple());
        model.addAttribute("runMode", runModeService.getRunMode());
        return "job-benchmark";
    }

    @PostMapping("/add")
    public void add(String jobId, Integer runTimes, HttpServletResponse response) throws IOException {
        log.info("add benchmark: jobId={}, runTimes={}", jobId, runTimes);
        benchmarkService.add(jobId, runTimes);
        response.sendRedirect("/api/benchmark/");
    }

    @PostMapping("/start")
    @ResponseBody
    public Object start(String benchId) {
        log.info("start benchmark: benchId={}", benchId);
        Result result = Result.failure();
        try {
            ExecuteResult executeResult = benchmarkService.start(Integer.valueOf(benchId));
            result.setMsg(executeResult.getMsg());
            if (executeResult.getCode() == 0)
                result = Result.success();
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            log.error("start bench ex:", e);
        }
        return result;
    }

    @PostMapping("/stop")
    @ResponseBody
    public Object stop(String benchId) {
        log.info("stop benchmark: benchId={}", benchId);
        Result result = Result.failure();
        try {
            ExecuteResult executeResult = benchmarkService.stop(Integer.valueOf(benchId));
            result.setMsg(executeResult.getMsg());
            if (executeResult.getCode() == 0)
                result = Result.success();
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            log.error("start bench ex:", e);
        }
        return result;
    }

    @PostMapping("/delete")
    @ResponseBody
    public Object delete(String benchId) {
        log.info("delete benchmark: benchId={}", benchId);
        Result result = Result.failure();
        if (benchId == null || benchId.isEmpty()) {
            result.setMsg("benchId is illegal!");
        } else {
            try {
                benchmarkService.delete(Integer.valueOf(benchId));
                result = Result.success();
            } catch (Exception e) {
                result.setMsg(e.getMessage());
                log.error("delete benchmark ex:", e);
            }
        }
        return result;
    }
}
