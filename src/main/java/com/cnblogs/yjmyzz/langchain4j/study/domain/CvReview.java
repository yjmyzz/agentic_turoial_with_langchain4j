package com.cnblogs.yjmyzz.langchain4j.study.domain;

import dev.langchain4j.model.output.structured.Description;

/**
 * 评价简历
 */
public class CvReview {
    @Description("从0到1分你邀请这位应聘者参加面试的可能性")
    public double score;

    @Description("对简历的反馈，什么是好的，什么需要改进，什么技能缺失，什么危险信号……")
    public String feedback;

    public CvReview() {}

    public CvReview(double score, String feedback) {
        this.score = score;
        this.feedback = feedback;
    }

    @Override
    public String toString() {
        return "\nCvReview: " +
                " - score = " + score +
                "\n- feedback = \"" + feedback + "\"\n";
    }
}
