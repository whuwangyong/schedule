package cn.whu.wy.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author WangYong
 * @Date 2020/04/16
 * @Time 12:28
 */
@Data
@AllArgsConstructor
public class JobInfoDto implements Serializable, Comparable<JobInfoDto> {
    private String jobId; // J01
    private String jobName;

    @Override
    public int compareTo(JobInfoDto o) {
        return this.jobId.compareTo(o.getJobId());
    }
}
