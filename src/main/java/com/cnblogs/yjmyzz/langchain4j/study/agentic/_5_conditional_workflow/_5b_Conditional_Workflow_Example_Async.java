package com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow;

import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._4_parallel_workflow.ManagerCvReviewer;
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

/**
 * 此示例演示了多个已满足的条件以及异步智能体，
 * 这些智能体将允许并行的连续调用以加快执行速度。
 * 在这个示例中：
 * - 条件1：如果HR评审良好，将简历传递给经理进行评审
 * - 条件2：如果HR评审显示缺少信息，则联系候选人获取更多信息
 * by 菩提树下的杨过(yjmyzz.cnblogs.com)
 */
@SpringBootApplication
public class _5b_Conditional_Workflow_Example_Async {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);

        // 1. 创建所有异步智能体
        ManagerCvReviewer managerCvReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(model)
                .async(true) // 异步智能体
                .outputKey("managerReview")
                .build();
        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(model)
                .async(true)
                .tools(new OrganizingTools())
                .outputKey("sentEmailId")
                .build();
        InfoRequester infoRequester = AgenticServices.agentBuilder(InfoRequester.class)
                .chatModel(model)
                .async(true)
                .tools(new OrganizingTools())
                .outputKey("sentEmailId")
                .build();

        // 2. 构建异步条件式工作流
        UntypedAgent candidateResponder = AgenticServices
                .conditionalBuilder()
                .subAgents(scope -> {
                    CvReview hrReview = (CvReview) scope.readState("cvReview");
                    return hrReview.score >= 0.8; // 如果HR通过，发送给经理评审
                }, managerCvReviewer)
                .subAgents(scope -> {
                    CvReview hrReview = (CvReview) scope.readState("cvReview");
                    return hrReview.score < 0.8; // 如果HR未通过，发送拒绝邮件
                }, emailAssistant)
                .subAgents(scope -> {
                    CvReview hrReview = (CvReview) scope.readState("cvReview");
                    return hrReview.feedback.toLowerCase().contains("missing information:");
                }, infoRequester) // 如果需要，向候选人请求更多信息
                .output(agenticScope ->
                        (agenticScope.readState("managerReview", new CvReview(0, "不需要经理评审"))).toString() +
                                "\n" + agenticScope.readState("sentEmailId", 0)
                ) // 最终输出是经理评审结果（如果有的话）
                .build();

        // 3. 输入参数
        String candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt");
        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");
        CvReview hrReview = new CvReview(
                0.85,
                """
                        可靠的候选人，薪资期望在范围内，能够在期望时间内入职。
                        缺失信息：关于在比利时工作许可状态的详细信息。
                        """
        );

        Map<String, Object> arguments = Map.of(
                "candidateCv", candidateCv,
                "candidateContact", candidateContact,
                "jobDescription", jobDescription,
                "cvReview", hrReview
        );


        // 4. 运行异步条件式工作流
        candidateResponder.invoke(arguments);

        System.out.println("=== 异步条件式工作流执行完成 ===");
    }
}
