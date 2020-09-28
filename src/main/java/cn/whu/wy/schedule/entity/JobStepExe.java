package cn.whu.wy.schedule.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 有几个job，这个表最多就有几行
 * 每当job以step模式执行，就更新其lastExeId为最新
 */

@Data
@Table(name = "job_step_exe")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobStepExe {

    @Id
    private String jobId;
    private Integer lastExeId;

}
