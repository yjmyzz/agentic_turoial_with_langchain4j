package com.cnblogs.yjmyzz.langchain4j.study.agentic._8_non_ai_agents;

import com.cnblogs.yjmyzz.langchain4j.study.domain.CvReview;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;

/**
 * 非AI智能体，根据评分更新申请状态。
 * 这演示了普通Java操作符如何作为一等智能体用于智能体工作流，
 * 使它们能够与AI驱动的智能体互换使用。
 */
public class StatusUpdate {

    @Agent(description = "根据评分更新申请状态")
    public void update(@V("combinedCvReview") CvReview aggregateCvReview) {
        double score = aggregateCvReview.score;
        System.out.println("StatusUpdate被调用，评分: " + score);

        if (score >= 8.0) {
            // 示例用的模拟数据库更新
            System.out.println("申请状态更新为: 已邀请面试");
        } else {
            // 示例用的模拟数据库更新
            System.out.println("申请状态更新为: 已拒绝");
        }
    }
}

