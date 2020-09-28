package cn.whu.wy.schedule.service;

import cn.whu.wy.schedule.dto.JobInfoDto;
import cn.whu.wy.schedule.entity.JobInfo;
import cn.whu.wy.schedule.mapper.IJobInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * job的增删改查
 *
 * @Author WangYong
 * @Date 2020/04/07
 * @Time 12:20
 */
@Service
@Slf4j
public class JobInfoManageService {

    @Autowired
    private IJobInfoMapper jobInfoMapper;

    public Map<String, List<JobInfo>> showContent() {
        Map<String, List<JobInfo>> map = new HashMap<>();

        List<JobInfo> linkedJobs = getJobsSortByLink();
        List<JobInfo> unlinkedJobs = new ArrayList<>();

        List<JobInfo> jobInfos = jobInfoMapper.selectAll();
        if (linkedJobs.size() < jobInfos.size()) {
            jobInfos.forEach(o -> {
                if (!linkedJobs.contains(o)) {
                    unlinkedJobs.add(o);
                }
            });
        }


        map.put("linked", linkedJobs); //在依赖关系内的job
        map.put("unlinked", unlinkedJobs);//不在依赖关系内的job
        return map;
    }


    public Object addJob(JobInfo jobInfo) {
        return jobInfoMapper.insert(jobInfo);
    }

    public Object deleteJob(String jobId) {
        return jobInfoMapper.deleteByPrimaryKey(jobId);

    }

    public Object updateJob(JobInfo jobInfo) {
        return jobInfoMapper.updateByPrimaryKey(jobInfo);
    }


    /**
     * 以 nextJobId 构造链表。不在依赖关系内的job，不放入该链表
     *
     * @return
     */
    public LinkedList<JobInfo> getJobsSortByLink() {
        List<JobInfo> jobs = jobInfoMapper.selectAll();
        Map<String, JobInfo> map = jobs.stream().collect(Collectors.toMap(JobInfo::getJobId, Function.identity()));

        LinkedList<JobInfo> llj = new LinkedList<>();
        jobs.forEach(job -> {
            if (job.getPreJobId() == null || job.getPreJobId().trim().isEmpty()) {
                llj.addFirst(job);
            }
        });

        if (!llj.isEmpty()) {
            String nextId;
            while ((nextId = llj.getLast().getNextJobId()) != null && !nextId.trim().isEmpty()) {
                llj.addLast(map.get(nextId));
            }
        }
        return llj;
    }

    public JobInfo getFirstJob() {
        return getJobsSortByLink().getFirst();
    }

    public List<JobInfoDto> getAllJobsSimple() {
        List<JobInfoDto> dtos = new ArrayList<>();
        List<JobInfo> jobInfos = jobInfoMapper.selectAll();
        jobInfos.forEach(jobInfo -> dtos.add(new JobInfoDto(jobInfo.getJobId(), jobInfo.getJobName())));
        Collections.sort(dtos);
        return dtos;
    }

    public List<JobInfo> getAll() {
        return jobInfoMapper.selectAll();
    }

    public JobInfo getByPrimaryKey(String pk) {
        return jobInfoMapper.selectByPrimaryKey(pk);
    }
}
