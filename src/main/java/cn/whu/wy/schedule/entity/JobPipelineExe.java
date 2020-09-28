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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 每次pipeline执行新增一行，将本行 lastFlag 置为 Y，将之前置 Y 的那一行置为空
 *
 * @Author WangYong
 * @Date 2020/04/08
 * @Time 08:48
 */
@Data
@Table(name = "job_pipeline_exe")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobPipelineExe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pipelineId;
    private String state;
    private String exeIds;// 逗号隔开
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;// 秒 该耗时表示pipeline的起止时间之差
    private Long durationPure;// 秒 该耗时是每个job耗时的和。与duration相比，刨除了job之间的耗时（如脚本启动时间、中途暂停的时间）


    public boolean isRunning() {
        return state != null && state.equals(PipelineStateConst.RUNNING);
    }

    public boolean isCompleted() {
        return state != null && state.equals(PipelineStateConst.COMPLETED);
    }

    public List<String> getExeIdAsList() {
        if (exeIds == null || exeIds.trim().isEmpty()) {
            return new ArrayList<>();
        } else return Arrays.asList(exeIds.split(","));
    }

    public void addExeId(Integer exeId) {
        if (exeId == -1) return;
        if (exeIds == null || exeIds.isEmpty())
            exeIds = String.valueOf(exeId);
        else exeIds += "," + exeId;
    }

    // 弹出末尾的 exeId
    public String popExeId() {
        LinkedList<String> exeIdsList = new LinkedList<>(getExeIdAsList());
        if (exeIdsList.size() == 0) return "";

        String removed = exeIdsList.removeLast();

        exeIds = "";
        exeIdsList.forEach(id -> exeIds += id + ",");
        if (exeIds.length() > 1) {
            exeIds = exeIds.substring(0, exeIds.length() - 1); // 移除末尾逗号
        }

        return removed;
    }


}
