package cn.whu.wy.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * job-step-history.html
 * <p>
 * 显示每次stepJob执行的历史
 *
 * @Author WangYong
 * @Date 2020/04/09
 * @Time 15:52
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobStepHistoryDto implements Serializable, Comparable<JobStepHistoryDto> {
    private Integer exeId;
    private String jobId;
    private String jobName;
    private String state;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration; // 秒
    private String resultMsg;

    /**
     * 降序排列，新的id在上方显示
     *
     * @param dto
     * @return
     */
    @Override
    public int compareTo(JobStepHistoryDto dto) {
        return dto.getExeId().compareTo(this.exeId);
    }
}
