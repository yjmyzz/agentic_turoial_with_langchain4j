package com.cnblogs.yjmyzz.langchain4j.study.agentic._3_loop_workflow;

import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
import com.cnblogs.yjmyzz.langchain4j.study.domain.CvReview;
import com.cnblogs.yjmyzz.langchain4j.study.util.StringLoader;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.Map;

/**
 * 【迭代工作流示例】(改编自<a href="https://github.com/langchain4j/langchain4j-examples">langchain4j官网示例</a>)
 * 此示例演示了如何实现一个CvReviewer Agent，我们可以将其与CvTailor Agent添加到循环中。
 * 我们将实现两个智能体：
 * - ScoredCvTailor (接收简历并按照CvReview（反馈/指示+分数）进行定制优化)
 * - CvReviewer (接收优化后的简历和职位描述，返回CvReview对象（反馈+分数）
 * 此外，当分数超过特定阈值（例如0.7）时循环结束（退出条件）
 */
public class _3a_Loop_Agent_Example {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);

        CvReviewer cvReviewer = AgenticServices.agentBuilder(CvReviewer.class)
                .chatModel(model)
                // 每次迭代中都会用新的反馈更新，用于下一次简历优化
                .outputKey("cvReview")
                .build();

        ScoredCvTailor scoredCvTailor = AgenticServices.agentBuilder(ScoredCvTailor.class)
                .chatModel(model)
                // 每次迭代中都会更新，持续改进简历
                .outputKey("cv")
                .build();

        // 除非定义了组合Agent，否则使用UntypedAgent，参见_2_Sequential_Agent_Example
        UntypedAgent reviewedCvGenerator = AgenticServices
                // 可以添加任意数量的Agent，顺序很重要
                .loopBuilder().subAgents(cvReviewer, scoredCvTailor)
                // 想要观察的最终输出（改进后的简历）
                .outputKey("cv")
                .exitCondition(agenticScope -> {
                    CvReview review = (CvReview) agenticScope.readState("cvReview");
                    // 记录中间分数
                    System.out.println("检查退出条件，当前分数=" + review.score);
                    return review.score > 0.8;
                })
                // 基于CvReviewer Agent给出的分数的退出条件，当>0.8时表示结果满意
                // 注意：退出条件在每次Agent调用后检查，而不仅仅在整个循环之后
                // 安全机制以避免无限循环，以防退出条件永远无法满足
                .maxIterations(3)
                .build();

        // 从resources/documents/中的文本文件加载原始参数：
        // - master_cv.txt
        // - job_description_backend.txt
        String masterCv = StringLoader.loadFromResource("/documents/master_cv.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");

        // 因为我们使用无类型智能体，需要传递参数映射
        Map<String, Object> arguments = Map.of(
                // 从原始简历开始，持续改进
                "cv", masterCv,
                "jobDescription", jobDescription
        );

        // 调用组合 Agent 生成定制的简历
        String tailoredCv = (String) reviewedCvGenerator.invoke(arguments);

        // 打印生成的简历
        System.out.println("=== 已Review的简历（无类型）===");
        System.out.println((String) tailoredCv);

        // 这份简历可能在第一次定制+Review轮次后就通过了
        // 如果想看到失败的情况，可以尝试使用长笛教师的职位描述，例如：
        // String fluteJobDescription = "我们正在寻找一位充满热情的长笛教师加入我们的音乐学院。";
        // 如示例3b所示，也会检查简历的中间状态
        // 并检索最终的审阅意见和分数。

    }
}