package cn.whu.wy.schedule.service.step;

import cn.whu.wy.schedule.consts.RunModeConst;
import cn.whu.wy.schedule.dto.JobStepHistoryDto;
import cn.whu.wy.schedule.entity.JobExecution;
import cn.whu.wy.schedule.entity.JobInfo;
import cn.whu.wy.schedule.service.JobExecutionService;
import cn.whu.wy.schedule.service.JobInfoManageService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author WangYong
 * @Date 2020/04/14
 * @Time 10:02
 */
@Service
public class StepHistoryService {

    @Autowired
    private JobInfoManageService jobInfoManageService;
    @Autowired
    private JobExecutionService jobExecutionService;
    @Value("${schedule.pageHelper.navigatePages:5}")
    private int navigatePages;

    public Map<String, Object> showContent(int pageNum, int pageSize) {
        Map<String, Object> map = new HashMap<>();

        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("exe_id DESC");
        List<JobExecution> jobExecutions = jobExecutionService.getByMode(RunModeConst.STEP);
        PageInfo<JobExecution> pageInfo = new PageInfo<>(jobExecutions, navigatePages);
        map.put("pageInfo", pageInfo);
        map.put("pageNum", pageInfo.getPageNum());
        map.put("pageSize", pageInfo.getPageSize());
        map.put("isFirstPage", pageInfo.isIsFirstPage());
        map.put("isLastPage", pageInfo.isIsLastPage());
        map.put("totalPages", pageInfo.getPages());
        map.put("navigatePages", pageInfo.getNavigatePages());

        List<JobStepHistoryDto> dtos = new ArrayList<>();
        jobExecutions.forEach(exe -> {
            JobInfo jobInfo = jobInfoManageService.getByPrimaryKey(exe.getJobId());
            JobStepHistoryDto dto = JobStepHistoryDto.builder()
                    .exeId(exe.getExeId())
                    .jobId(exe.getJobId())
                    //如果前端在jobInfo中删除某job后，execution取得的jobId在map中不存在，会导致NPE
                    .jobName(jobInfo == null ? "已删除" : jobInfo.getJobName())
                    .state(exe.getState())
                    .startTime(exe.getStartTime())
                    .endTime(exe.getEndTime())
                    .duration(exe.getDuration())
                    .resultMsg(exe.getResultMsg())
                    .build();
            dtos.add(dto);
        });
//        Collections.sort(dtos);

        map.put("historyDtos", dtos);
        return map;
    }


}
