package cn.whu.wy.schedule.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * job-pipeline-history.html
 * <p>
 * 显示每次pipeline执行的历史
 *
 * @Author WangYong
 * @Date 2020/04/09
 * @Time 15:52
 */
@Data
public class JobPipelineHistoryDto implements Serializable, Comparable<JobPipelineHistoryDto> {
    private Integer pipelineId;
    private String state;
    private String exeIds;// 逗号隔开
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;// 秒 该耗时表示pipeline的起止时间之差
    private Long durationPure;// 秒 该耗时是每个job耗时的和。与duration相比，刨除了job之间的耗时（如脚本启动时间、中途暂停的时间）

    /**
     * 降序排列，新的id在上方显示
     *
     * @param dto
     * @return
     */
    @Override
    public int compareTo(JobPipelineHistoryDto dto) {
        return dto.getPipelineId().compareTo(this.pipelineId);
    }
}
