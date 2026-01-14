package com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow;

import com.cnblogs.yjmyzz.langchain4j.study.domain.CvReview;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * @author junmingyang
 */
public interface HrCvReviewer {

    @Agent(name = "hrReviewer", description = "审查简历以评估候选人是否符合HR要求，提供反馈和评分")
    @SystemMessage("""
            你作为HR专员，根据以下职位要求审查简历：
            {{hrRequirements}}
            你需要为每份简历提供评分和反馈（包括优点和不足之处）。
            可以忽略诸如缺少地址或占位符等内容。
            
            重要提示：请仅返回有效的JSON格式响应，换行符使用\\n，不要包含任何Markdown格式或代码块。
            """)
    @UserMessage("""
            请审查这份简历：{{candidateCv}}，以及附带的电话面试记录：{{phoneInterviewNotes}}
            """)
    CvReview reviewCv(@V("candidateCv") String cv, @V("phoneInterviewNotes") String phoneInterviewNotes, @V("hrRequirements") String hrRequirements);
}