package cn.whu.wy.schedule.controller;

import cn.whu.wy.schedule.service.step.StepHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Author WangYong
 * @Date 2020/04/14
 * @Time 10:01
 */
@Controller
@RequestMapping("/api/step/history")
public class StepHistoryController {

    @Autowired
    private StepHistoryService stepHistoryService;

    @GetMapping("/")
    public String show(Model model,
                       @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                       @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {
        Map<String, Object> stringObjectMap = stepHistoryService.showContent(pageNum, pageSize);
        model.addAllAttributes(stringObjectMap);
        return "job-step-history";
    }

}
