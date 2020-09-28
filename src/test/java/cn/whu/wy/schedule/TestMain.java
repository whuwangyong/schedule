package cn.whu.wy.schedule;

import java.time.LocalDateTime;

/**
 * @Author WangYong
 * @Date 2020/04/13
 * @Time 14:22
 */
public class TestMain {
    static int count = 1;

    public static void main(String[] args) {
//        LocalDateTime start = LocalDateTime.now();
//        LocalDateTime end = LocalDateTime.of(2020, 4, 13, 13, 0, 0);
//        Duration between = Duration.between(end, start);
//        System.out.println(between.toString());
//        System.out.println(between.getSeconds());
//        System.out.println(between.toMinutes());


        /*
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (count == 3) invoke(null);
                else {
                    invoke(count + "");
                }
                count++;
            }
        }, 0, 2, TimeUnit.SECONDS);
        */
        System.out.println(LocalDateTime.now());
    }

    public static void invoke(String str) {
        try {
            System.out.println(Integer.valueOf(str));
        } catch (Exception e) {
            System.out.println("ex:" + e.getLocalizedMessage());
        }
    }
}
