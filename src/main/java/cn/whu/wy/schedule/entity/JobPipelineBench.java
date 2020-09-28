package cn.whu.wy.schedule.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Author WangYong
 * @Date 2020/04/21
 * @Time 11:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "job_pipeline_bench")
public class JobPipelineBench {

    @Id
    private Integer id;
    private Integer roundsTotal;
    private Integer roundsDone;
}
