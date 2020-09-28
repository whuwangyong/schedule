package cn.whu.wy.schedule.entity;

import cn.whu.wy.schedule.consts.JobStateConst;
import lombok.*;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * 对于pipeline和benchmark模式，某一时刻，最多只有一个job在执行，一个job只能有一个exe实例
 * 对于step模式，可以有多个job同时运行，但是，一个job只有一个exe实例
 * 所以，这张表里，
 * select count(*) from job_execution where job_id='xxx' and state='running' <= 1
 *
 * @Author WangYong
 * @Date 2020/04/07
 * @Time 16:10
 */
@Data
@Table(name = "job_execution")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class JobExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer exeId;
    private String jobId;
    private String state;
    //    private java.sql.Timestamp startTime;
//    private java.sql.Timestamp endTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration; // 秒
    private String resultMsg;
    private String runMode;
//    private Integer batchId; // 当runMode为pipeline和benchmark时，这些jobExecution属于同一个批次，具有同一个batchId

    public boolean isRunning() {
        return state != null && state.equals(JobStateConst.RUNNING);
    }

    public boolean isCompleted() {
        return state != null && state.equals(JobStateConst.COMPLETED);
    }


}
