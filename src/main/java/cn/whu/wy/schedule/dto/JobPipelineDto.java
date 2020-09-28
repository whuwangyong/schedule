package cn.whu.wy.schedule.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * job-pipeline.html
 *
 * @Author WangYong
 * @Date 2020/04/09
 * @Time 15:09
 */
@Data
@Builder
public class JobPipelineDto implements Serializable, Comparable<JobPipelineDto> {

    private String jobId;
    private String jobName;
    private String preJobId;
    private String nextJobId;
    private String state;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration; // 秒

    @Override
    public int compareTo(JobPipelineDto dto) {
        return this.getJobId().compareTo(dto.getJobId());
    }
}
