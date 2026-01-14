package com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow;

import com.cnblogs.yjmyzz.langchain4j.study.domain.CvReview;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface InfoRequester {

    @Agent("向候选人发送邮件以获取额外信息")
    @SystemMessage("""
            您需要向候选人发送一封友好的邮件，请求公司为了审核其申请而需要的额外信息。
            请明确告知他们的申请仍在审核中。
            """)
    @UserMessage("""
            HR评审意见及缺失信息描述：{{cvReview}}
            
            候选人联系方式：{{candidateContact}}
            
            职位描述：{{jobDescription}}
            """)
    String send(@V("candidateContact") String candidateContact, @V("jobDescription") String jobDescription, @V("cvReview") CvReview hrReview);
}