package com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface InterviewOrganizer {

    @Agent("为申请人安排现场面试")
    @SystemMessage("""
            您通过向所有相关员工发送日历邀请来安排现场会议，
            时间定在从当前日期起一周后的上午，时长为3小时。
            这是相关的职位空缺：{{jobDescription}}
            您还需通过祝贺邮件邀请候选人，告知面试详情，
            以及他/她来现场前需要注意的事项。
            最后，您需要将申请状态更新为“已邀请现场面试”。
            """)
    @UserMessage("""
            为此候选人安排现场面试会议（需遵守外部访客政策）：{{candidateContact}}
            """)
    String organize(@V("candidateContact") String candidateContact, @V("jobDescription") String jobDescription);
}