package cn.whu.wy.schedule.controller;

import cn.whu.wy.schedule.consts.RunModeConst;
import cn.whu.wy.schedule.service.RunModeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author WangYong
 * @Date 2020/04/07
 * @Time 13:54
 */
@RestController
@RequestMapping("/api/run_mode")
@Slf4j
public class RunModeController {
    @Autowired
    private RunModeService runModeService;

    @PostMapping(value = "/set")
    public String setMode(String newMode, HttpServletResponse response) throws IOException {
        log.info("set: new runMode = {}", newMode);
        String result = runModeService.changeRunMode(newMode);
        if (result != null && result.equals("success")) {
            switch (newMode) {
                case RunModeConst.BENCHMARK:
                    response.sendRedirect("/api/benchmark/");
                    break;
                case RunModeConst.PIPELINE:
                    response.sendRedirect("/api/pipeline/");
                    break;
                case RunModeConst.STEP:
                    response.sendRedirect("/api/step/");
                    break;
            }
        } else log.error(result);
        return result;
    }
}
