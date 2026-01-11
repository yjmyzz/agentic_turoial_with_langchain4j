package com.cnblogs.yjmyzz.langchain4j.study.agentic._3_loop_workflow;

import com.cnblogs.yjmyzz.langchain4j.study.domain.CvReview;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ScoredCvTailor {

    @Agent("根据特定指示定制简历")
    @SystemMessage("""
            这里有一份需要根据特定职位描述、反馈或其他指示进行定制的简历。
            您可以优化简历使其满足要求，但请不要编造事实。
            如果删除无关内容能使简历更符合指示要求，您可以这样做。
            目标是让申请人获得面试机会，并且能够在面试中兑现简历中的描述。
            当前简历：{{cv}}
            """)
    @UserMessage("""
            以下是定制简历的指示和反馈：
            （再次强调，请勿编造原始简历中不存在的事实。
            如果申请人不完全符合要求，请突出展示他最匹配的现有特点，
            但不要捏造事实）
            审阅意见：{{cvReview}}
            """)
    String tailorCv(@V("cv") String cv, @V("cvReview") CvReview cvReview);
}
