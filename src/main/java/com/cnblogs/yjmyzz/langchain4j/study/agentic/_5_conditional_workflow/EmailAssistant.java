package com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface EmailAssistant {

    @Agent("向未通过筛选的候选人发送拒绝邮件，返回已发送邮件的ID，若无法发送则返回0")
    @SystemMessage("""
            您需要向未通过第一轮评审的求职候选人发送一封友好的邮件。
            同时，您需要将申请状态更新为“已拒绝”。
            您应返回已发送邮件的ID。
            """)
    @UserMessage("""
            被拒候选人：{{candidateContact}}
            
            申请职位：{{jobDescription}}
            """)
    int send(@V("candidateContact") String candidateContact, @V("jobDescription") String jobDescription);
}