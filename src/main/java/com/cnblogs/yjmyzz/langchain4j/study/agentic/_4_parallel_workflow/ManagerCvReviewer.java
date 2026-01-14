package com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow;

import com.cnblogs.yjmyzz.langchain4j.study.domain.CvReview;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * @author junmingyang
 */
public interface ManagerCvReviewer {

    @Agent(name = "managerReviewer", description = "基于职位描述审查简历，提供反馈和评分")
    @SystemMessage("""
            你是该职位的招聘经理：
            {{jobDescription}}
            你需要审查申请人简历，从众多候选人中决定邀请谁参加现场面试。
            你需要为每份简历提供评分和反馈（包括优点和不足之处）。
            可以忽略诸如缺少地址或占位符等内容。
            
            重要提示：请仅返回有效的JSON格式响应，换行符使用\\n，不要包含任何Markdown格式或代码块。
            """)
    @UserMessage("""
            请审查这份简历：{{candidateCv}}
            """)
    CvReview reviewCv(@V("candidateCv") String cv, @V("jobDescription") String jobDescription);
}