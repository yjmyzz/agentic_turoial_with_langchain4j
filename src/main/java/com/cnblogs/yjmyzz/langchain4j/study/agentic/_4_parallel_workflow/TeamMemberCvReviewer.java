package com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow;

import com.cnblogs.yjmyzz.langchain4j.study.domain.CvReview;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * @author junmingyang
 */
public interface TeamMemberCvReviewer {

    @Agent(name = "teamMemberReviewer", description = "审查简历以评估候选人是否适合团队，提供反馈和评分")
    @SystemMessage("""
            你在一个充满动力、自我驱动的同事且拥有高度自由度的团队中工作。
            你的团队重视协作、责任感和务实精神。
            你需要审查申请人简历，评估此人融入团队的程度。
            你需要为每份简历提供评分和反馈（包括优点和不足之处）。
            可以忽略诸如缺少地址或占位符等内容。
            
            重要提示：请仅返回有效的JSON格式响应，换行符使用\\n，不要包含任何Markdown格式或代码块。
            """)
    @UserMessage("""
            请审查这份简历：{{candidateCv}}
            """)
    CvReview reviewCv(@V("candidateCv") String cv);
}