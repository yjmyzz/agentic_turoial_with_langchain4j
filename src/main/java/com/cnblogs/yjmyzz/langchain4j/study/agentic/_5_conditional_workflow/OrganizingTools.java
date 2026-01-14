package com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrganizingTools {

    @Tool
    public Date getCurrentDate(){
        return new Date();
    }

    @Tool("根据给定的职位描述ID，查找需要参加现场面试的人员的电子邮件地址和姓名")
    public List<String> getInvolvedEmployeesForInterview(@P("职位描述ID") String jobDescriptionId){
        // 演示用虚拟实现
        return new ArrayList<>(List.of(
                "Anna Bolena: hiring.manager@company.com",
                "Chris Durue: near.colleague@company.com",
                "Esther Finnigan: vp@company.com"));
    }

    @Tool("根据电子邮件地址为员工创建日程条目")
    public void createCalendarEntry(@P("员工电子邮件地址列表") List<String> emailAddress, @P("会议主题") String topic, @P("开始日期和时间，格式为yyyy-mm-dd hh:mm") String start, @P("结束日期和时间，格式为yyyy-mm-dd hh:mm") String end){
        // 演示用虚拟实现
        System.out.println("*** 已创建日程条目 ***");
        System.out.println("主题：" + topic);
        System.out.println("开始时间：" + start);
        System.out.println("结束时间：" + end);
    }

    @Tool
    public int sendEmail(@P("收件人电子邮件地址列表") List<String> to, @P("抄送电子邮件地址列表") List<String> cc, @P("邮件主题") String subject, @P("正文") String body){
        // 演示用虚拟实现
        System.out.println("*** 已发送邮件 ***");
        System.out.println("收件人：" + to);
        System.out.println("抄送：" + cc);
        System.out.println("主题：" + subject);
        System.out.println("正文：" + body);
        return 1234; // 虚拟邮件ID
    }

    @Tool
    public void updateApplicationStatus(@P("职位描述ID") String jobDescriptionId, @P("候选人（名，姓）") String candidateName, @P("新的申请状态") String newStatus){
        // 演示用虚拟实现
        System.out.println("*** 已更新申请状态 ***");
        System.out.println("职位描述ID：" + jobDescriptionId);
        System.out.println("候选人姓名：" + candidateName);
        System.out.println("新状态：" + newStatus);
    }
}
