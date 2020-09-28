package cn.whu.wy.schedule.entity;

import cn.whu.wy.schedule.consts.PipelineStateConst;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @Author WangYong
 * @Date 2020/03/24
 * @Time 16:37
 */
@Data
@Table(name = "job_benchmark_exe")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobBenchmarkExe implements Serializable, Comparable<JobBenchmarkExe> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer benchId;
    String jobId;
    String jobName;
    String state; // 对于前端页面，如果状态是completed terminated，启动按钮disabled
    Integer runTimes;
    Integer doneTimes;
    String exeIds;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Long duration; // 秒

    public List<String> getExeIdAsList() {
        if (this.exeIds != null) {
            return Arrays.asList(exeIds.split(","));
        } else return null;
    }

    public void addExeId(Integer exeId) {
        if (exeId == -1) return;
        if (exeIds == null || exeIds.isEmpty())
            exeIds = String.valueOf(exeId);
        else exeIds += "," + exeId;
    }


    /**
     * 降序排列，前台页面显示时，最新的benchmark_exe显示在上面
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(JobBenchmarkExe o) {
        return o.getBenchId().compareTo(this.getBenchId());
    }

    public boolean isRunning() {
        return state != null && state.equals(PipelineStateConst.RUNNING);
    }

    public boolean isCompleted() {
        return state != null && state.equals(PipelineStateConst.COMPLETED);
    }

    public boolean isTerminated() {
        return state != null && state.equals(PipelineStateConst.TERMINATED);
    }
}
