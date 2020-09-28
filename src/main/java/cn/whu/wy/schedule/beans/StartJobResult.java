package cn.whu.wy.schedule.beans;

import cn.whu.wy.schedule.executor.ExecuteResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author WangYong
 * @Date 2020/04/13
 * @Time 21:38
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartJobResult {
    ExecuteResult executeResult;
    Integer jobExecutionId;
}
