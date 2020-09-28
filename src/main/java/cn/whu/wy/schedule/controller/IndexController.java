package cn.whu.wy.schedule.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

/**
 * @Author WangYong
 * @Date 2020/04/10
 * @Time 20:20
 */
@Controller
@Slf4j
public class IndexController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/about")
    public String about() {
        return "index";
    }

    @RequestMapping("/now")
    public Object now() {
        return LocalDateTime.now();
    }
}
