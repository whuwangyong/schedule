package cn.whu.wy.schedule.controller;

import cn.whu.wy.schedule.dto.Result;
import cn.whu.wy.schedule.entity.JobInfo;
import cn.whu.wy.schedule.service.JobInfoManageService;
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
import java.util.Map;

/**
 * job 信息管理
 *
 * @Author WangYong
 * @Date 2020/04/07
 * @Time 12:22
 */
@Controller
@RequestMapping("/api/job-info-manage")
@Slf4j
public class JobInfoManageController {

    @Autowired
    private JobInfoManageService jobInfoManageService;


    @GetMapping("/")
    public String show(Model model) {
        Map<String, List<JobInfo>> content = jobInfoManageService.showContent();
        model.addAttribute("linkedJobs", content.get("linked"));
        model.addAttribute("unlinkedJobs", content.get("unlinked"));
        return "job-info-manage";
    }


    @PostMapping(value = "/add")
    @ResponseBody
    public Object addJob(JobInfo jobInfo, HttpServletResponse response) throws IOException {
        log.info("add jobInfo: jobInfo = {}", jobInfo);
        Result result = Result.failure();
        try {
            jobInfoManageService.addJob(jobInfo);
            result = Result.success();
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            log.error("add jobInfo ex:", e);
        }
//        response.sendRedirect("/api/job-info-manage/");
        return result;
    }

    @PostMapping("/update")
    @ResponseBody
    public Object updateJob(JobInfo jobInfo, HttpServletResponse response) throws IOException {
        log.info("update jobInfo: jobInfo = {}", jobInfo);
        Result result = Result.failure();
        try {
            jobInfoManageService.updateJob(jobInfo);
            result = Result.success();
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            log.error("update jobInfo ex:", e);
        }
//        response.sendRedirect("/api/job-info-manage/");
        return result;
    }

    @PostMapping("/delete")
    @ResponseBody
    public Object deleteJob(String jobId) {
        log.info("delete job: jobId = {}", jobId);
        Result result = Result.failure();
        try {
            jobInfoManageService.deleteJob(jobId);
            result = Result.success();
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            log.error("delete job ex:", e);
        }
        return result;
    }

}
