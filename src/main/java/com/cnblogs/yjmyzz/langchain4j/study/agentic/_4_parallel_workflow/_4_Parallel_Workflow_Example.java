package com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow;

import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
import com.cnblogs.yjmyzz.langchain4j.study.domain.CvReview;
import com.cnblogs.yjmyzz.langchain4j.study.util.StringLoader;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 这个示例演示了如何实现3个并行的CvReviewer智能体，
 * 它们将同时评估简历。我们将实现三个智能体：
 * - ManagerCvReviewer（评估候选人胜任工作的可能性）
 *      输入：简历和职位描述
 * - TeamMemberCvReviewer（评估候选人融入团队的程度）
 *      输入：简历
 * - HrCvReviewer（从HR角度检查候选人是否符合要求）
 *      输入：简历、HR要求
 * by 菩提树下的杨过(yjmyzz.cnblogs.com)
 */
@SpringBootApplication
public class _4_Parallel_Workflow_Example {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);

        // 2. 在本包中定义三个子智能体：
        //      - HrCvReviewer.java
        //      - ManagerCvReviewer.java
        //      - TeamMemberCvReviewer.java

        // 3. 使用AgenticServices创建所有智能体
        HrCvReviewer hrCvReviewer = AgenticServices.agentBuilder(HrCvReviewer.class)
                .chatModel(model)
                .outputKey("hrReview") // 这将在每次迭代中被覆盖，同时也作为我们想要观察的最终输出
                .build();

        ManagerCvReviewer managerCvReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(model)
                .outputKey("managerReview") // 这会覆盖原始输入指令，并在每次迭代中被覆盖，用作CvTailor的新指令
                .build();

        TeamMemberCvReviewer teamMemberCvReviewer = AgenticServices.agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(model)
                .outputKey("teamMemberReview") // 这会覆盖原始输入指令，并在每次迭代中被覆盖，用作CvTailor的新指令
                .build();

        // 4. 构建执行流程
        var executor = Executors.newFixedThreadPool(3);  // 保留引用以便后续关闭

        UntypedAgent cvReviewGenerator = AgenticServices // 使用UntypedAgent，除非你定义了结果组合智能体，参见_2_Sequential_Agent_Example
                .parallelBuilder()
                .subAgents(hrCvReviewer, managerCvReviewer, teamMemberCvReviewer) // 可以添加任意多个
                .executor(executor) // 可选，默认使用内部缓存的线程池，执行完成后会自动关闭
                .outputKey("fullCvReview") // 这是我们想要观察的最终输出
                .output(agenticScope -> {
                    // 从智能体作用域读取每个评审者的输出
                    CvReview hrReview = (CvReview) agenticScope.readState("hrReview");
                    CvReview managerReview = (CvReview) agenticScope.readState("managerReview");
                    CvReview teamMemberReview = (CvReview) agenticScope.readState("teamMemberReview");
                    // 返回汇总的评审结果，包含平均分（或你想要的任何其他聚合方式）
                    String feedback = String.join("\n",
                            "HR评审: " + hrReview.feedback,
                            "经理评审: " + managerReview.feedback,
                            "团队成员评审: " + teamMemberReview.feedback
                    );
                    double avgScore = (hrReview.score + managerReview.score + teamMemberReview.score) / 3.0;

                    return new CvReview(avgScore, feedback);
                })
                .build();

        // 5. 从resources/documents/目录下的文本文件加载原始参数
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        String hrRequirements = StringLoader.loadFromResource("/documents/hr_requirements.txt");
        String phoneInterviewNotes = StringLoader.loadFromResource("/documents/phone_interview_notes.txt");

        // 6. 由于我们使用了无类型智能体，需要传递参数映射
        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "jobDescription", jobDescription
                , "hrRequirements", hrRequirements
                , "phoneInterviewNotes", phoneInterviewNotes
        );

        // 7. 调用组合智能体生成定制的简历
        var review = cvReviewGenerator.invoke(arguments);

        // 8. 打印生成的简历
        System.out.println("=== 已评审的简历 ===");
        System.out.println(review);

        // 9. 关闭执行器
        executor.shutdown();

    }


}