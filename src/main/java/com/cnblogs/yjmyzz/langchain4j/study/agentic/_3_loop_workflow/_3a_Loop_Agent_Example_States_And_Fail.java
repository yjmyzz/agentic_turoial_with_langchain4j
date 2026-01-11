
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * (改编自<a href="https://github.com/langchain4j/langchain4j-examples">langchain4j官网示例</a>)
 * 本例构建与3a中相同的循环智能体，但这次通过尝试将简历定制到不合适的职位描述来观察失败情况。
 * 除了最终简历外，还将返回最新的分数和反馈，
 * 这将允许检查是否获得了良好的分数以及提交此简历是否值得。
 * 此外，还展示了一种技巧来检查审阅的中间状态（每次循环都会被覆盖）：
 * 通过在每次检查退出条件时（即每次智能体调用后）将其存储在列表中。
 */
public class _3a_Loop_Agent_Example_States_And_Fail {

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

        // 构建序列并在每次退出条件检查时存储审阅结果
        // 了解退出条件是实际满足还是仅达到最大迭代次数可能很重要
        // （例如，约翰可能甚至不想申请这份工作）。
        // 可以更改输出变量以包含最后的分数和反馈，并在循环结束后自行检查。
        // 还可以将中间值存储在可变列表中供后续检查。
        // 下面的代码同时完成这两件事。
        List<CvReview> reviewHistory = new ArrayList<>();

        // 除非您定义了组合Agent，否则使用UntypedAgent，参见下面的示例
        UntypedAgent reviewedCvGenerator = AgenticServices
                // 可以添加任意数量的Agent，顺序很重要
                .loopBuilder().subAgents(cvReviewer, scoredCvTailor)
                // 要观察的最终输出
                .outputKey("cvAndReview")
                .output(agenticScope -> {
                    return Map.of(
                            "cv", agenticScope.readState("cv"),
                            "finalReview", agenticScope.readState("cvReview")
                    );
                })
                .exitCondition(scope -> {
                    CvReview review = (CvReview) scope.readState("cvReview");
                    // 在每次智能体调用时捕获分数+反馈
                    reviewHistory.add(review);
                    System.out.println("退出条件检查，当前分数=" + review.score);
                    return review.score >= 0.8;
                })
                // 安全机制以避免无限循环，以防退出条件永远无法满足
                .maxIterations(3)
                .build();

        // 从resources/documents/中的文本文件加载原始参数：
        // - master_cv.txt
        // - job_description_backend.txt
        String masterCv = StringLoader.loadFromResource("/documents/master_cv.txt");
        String jobDescription = "我们正在寻找一位充满热情的长笛教师加入我们的音乐学院。";

        // 因为使用UntypedAgent，需要传递参数映射
        Map<String, Object> arguments = Map.of(
                // 从原始简历开始，持续改进
                "cv", masterCv,
                "jobDescription", jobDescription
        );

        // 调用组合 Agent 生成定制的简历
        Map<String, Object> cvAndReview = (Map<String, Object>) reviewedCvGenerator.invoke(arguments);

        // 可以在日志中观察步骤，例如：
        // 第1轮输出："content": "{\n  \"score\": 0.0,\n  \"feedback\": \"此简历不适合我们音乐学院的长笛教师职位..."
        // 第2轮输出："content": "{\n  \"score\": 0.3,\n  \"feedback\": \"约翰的简历展示了强大的软技能，如沟通、耐心和适应能力，这些在教学中很重要。然而，缺少正式的音乐培训或..."
        // 第3轮输出："content": "{\n  \"score\": 0.4,\n  \"feedback\": \"约翰·道展示了强大的软技能和指导经验..."

        System.out.println("=== 长笛教师职位定制简历 ===");
        // 循环后的最终简历
        System.out.println(cvAndReview.get("cv"));

        // 从输出映射中获取finalReview，以便检查
        // 最终分数和反馈是否满足要求
        CvReview review = (CvReview) cvAndReview.get("finalReview");
        System.out.println("=== 长笛教师职位最终审阅结果 ===");
        System.out.println("简历" + (review.score >= 0.8 ? "通过" : "未通过") + "，得分=" + review.score);
        System.out.println("最终反馈：" + review.feedback);

        // 在 reviewHistory 中可以找到完整的审阅历史
        System.out.println("=== 长笛教师职位完整审阅历史 ===");
        System.out.println(reviewHistory);

    }
}
