package com.cnblogs.yjmyzz.langchain4j.study.agentic._9_human_in_the_loop;

import com.cnblogs.yjmyzz.langchain4j.study.domain.CvReview;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface HiringDecisionProposer {

    @Agent("汇总招聘决策以供最终确认")
    @SystemMessage("""
        你汇总招聘理由，最多3行，供人类做出最终决定是否继续。
        """)
    @UserMessage("""
        招聘过程中所有相关方的反馈：{{cvReview}}
        """)
    String propose(@V("cvReview") CvReview cvReview);
}