package cn.whu.wy.schedule.service.pipeline;

import cn.whu.wy.schedule.dto.JobPipelineHistoryDto;
import cn.whu.wy.schedule.entity.JobPipelineExe;
import cn.whu.wy.schedule.mapper.IJobPipelineExeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
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
 * @Time 10:33
 */
@Service
public class PipelineHistoryService {

    @Autowired
    private IJobPipelineExeMapper pipelineExeMapper;
    @Value("${schedule.pageHelper.navigatePages:5}")
    private int navigatePages;

    public Map<String, Object> showContent(int pageNum, int pageSize) {
        Map<String, Object> map = new HashMap<>();

        PageHelper.startPage(pageNum, pageSize);
        PageHelper.orderBy("pipeline_id DESC");
        List<JobPipelineExe> pipelineExes = pipelineExeMapper.selectAll();
        PageInfo<JobPipelineExe> pageInfo = new PageInfo<>(pipelineExes, navigatePages);
        map.put("pageInfo", pageInfo);
        map.put("pageNum", pageInfo.getPageNum());
        map.put("pageSize", pageInfo.getPageSize());
        map.put("isFirstPage", pageInfo.isIsFirstPage());
        map.put("isLastPage", pageInfo.isIsLastPage());
        map.put("totalPages", pageInfo.getPages());
        map.put("navigatePages", pageInfo.getNavigatePages());

        List<JobPipelineHistoryDto> dtos = new ArrayList<>();
        pipelineExes.forEach(exe -> {
            JobPipelineHistoryDto dto = new JobPipelineHistoryDto();
            BeanUtils.copyProperties(exe, dto);
            dtos.add(dto);
        });
//        Collections.sort(dtos);
        map.put("historyDtos", dtos);
        return map;
    }

}
