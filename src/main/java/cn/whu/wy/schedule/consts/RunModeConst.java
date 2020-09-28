package cn.whu.wy.schedule.consts;

import java.util.Arrays;
import java.util.List;

/**
 * @Author WangYong
 * @Date 2020/04/07
 * @Time 10:48
 */
public class RunModeConst {
    public static final String PIPELINE = "pipeline";
    public static final String STEP = "step";
    public static final String BENCHMARK = "benchmark";

    public static List<String> getAllMode() {
        return Arrays.asList(PIPELINE, STEP, BENCHMARK);
    }
}
