package cn.whu.wy.schedule.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Author WangYong
 * @Date 2020/03/24
 * @Time 16:33
 */
@Data
@Table(name = "job_info")
@ToString
public class JobInfo implements Comparable<JobInfo> {
    @Id
    private String jobId; // J01
    private String jobName;
    private String startCmd;
    private String checkCmd;
    private String stopCmd;
    private String initCmd;
    private String preJobId;
    private String nextJobId;


    @Override
    public int compareTo(JobInfo jobInfo) {
        return this.jobId.compareTo(jobInfo.getJobId());
    }

    public static void main(String[] args) {

        /*
        JobInfo jobInfo1 = new JobInfo();
        JobInfo jobInfo2 = new JobInfo();
        JobInfo jobInfo3 = new JobInfo();
        JobInfo jobInfo4 = new JobInfo();

        jobInfo1.setJobId("J01");
        jobInfo2.setJobId("J02");
        jobInfo3.setJobId("J03");
        jobInfo4.setJobId("J04");

        List<JobInfo> jobInfos = new ArrayList<>();
        jobInfos.add(jobInfo2);
        jobInfos.add(jobInfo4);
        jobInfos.add(jobInfo1);
        jobInfos.add(jobInfo3);
        */

        /*
        // 测试 Comparable
        jobInfos.forEach(job -> System.out.println(job.jobId));
        Collections.sort(jobInfos);
        jobInfos.forEach(job -> System.out.println(job.jobId));
        */

        /*
        // 测试 java 的引用传递。jobInfos 这个 list 经过 stream/collect 等操作，
        // 最后得到一个 map，map 中的对象与 list 中的对象是同一个
        Map<String, JobInfo> map = jobInfos.stream().collect(Collectors.toMap(JobInfo::getJobId, Function.identity()));
        System.out.println(map.get("J01") == jobInfo1);// true
        */
    }
}
