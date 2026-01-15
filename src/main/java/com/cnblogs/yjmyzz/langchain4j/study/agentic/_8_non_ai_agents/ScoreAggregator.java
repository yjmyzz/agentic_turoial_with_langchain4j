package com.cnblogs.yjmyzz.langchain4j.study.agentic._8_non_ai_agents;

import com.cnblogs.yjmyzz.langchain4j.study.domain.CvReview;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;

/**
 * 非AI智能体，将多个简历评审聚合成一个综合评审。
 * 这演示了普通Java操作符如何作为一等智能体用于智能体工作流，
 * 使它们能够与AI驱动的智能体互换使用。
 */
public class ScoreAggregator {

    @Agent(description = "将HR/经理/团队评审聚合成综合评审", outputKey = "combinedCvReview")
    public CvReview aggregate(@V("hrReview") CvReview hr,
                              @V("managerReview") CvReview mgr,
                              @V("teamMemberReview") CvReview team) {

        System.out.println("ScoreAggregator被调用，参数：hrReview=" + hr +
                ", managerReview=" + mgr +
                ", teamMemberReview=" + team);

        double avgScore = (hr.score + mgr.score + team.score) / 3.0;

        String combinedFeedback = String.join("\n\n",
                "HR评审: " + hr.feedback,
                "经理评审: " + mgr.feedback,
                "团队成员评审: " + team.feedback
        );

        return new CvReview(avgScore, combinedFeedback);
    }
}
