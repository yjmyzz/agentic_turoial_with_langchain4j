package com.cnblogs.yjmyzz.langchain4j.study.agentic._3_loop_workflow;

import com.cnblogs.yjmyzz.langchain4j.study.domain.CvReview;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CvReviewer {

    @Agent("根据特定指示审阅简历，提供反馈和分数。请考虑简历与职位要求的匹配程度")
    @SystemMessage("""
            您是此职位的招聘经理：
            {{jobDescription}}
            您需要审阅申请人的简历，并决定在众多申请人中邀请谁进行现场面试。
            您需要为每份简历提供分数和中文反馈（包括优点和不足之处）。
            您可以忽略地址缺失和占位符等信息。
            """)
    @UserMessage("""
            审阅此简历：{{cv}}
            """)
    CvReview reviewCv(@V("cv") String cv, @V("jobDescription") String jobDescription);
}
