package com.cnblogs.yjmyzz.langchain4j.study.agentic._9_human_in_the_loop;

import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow.EmailAssistant;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow.InterviewOrganizer;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow.OrganizingTools;
import com.cnblogs.yjmyzz.langchain4j.study.agentic._5_conditional_workflow.RagProvider;
import com.cnblogs.yjmyzz.langchain4j.study.domain.CvReview;
import com.cnblogs.yjmyzz.langchain4j.study.util.StringLoader;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.workflow.HumanInTheLoop;
import dev.langchain4j.model.chat.ChatModel;
import io.micrometer.common.util.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

@SpringBootApplication
public class _9a_HumanInTheLoop_Simple_Validator {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);
        RagProvider ragProvider = context.getBean("ragProvider", RagProvider.class);

        // 3. 创建相关智能体
        HiringDecisionProposer decisionProposer = AgenticServices.agentBuilder(HiringDecisionProposer.class)
                .chatModel(model)
                .outputKey("modelDecision")
                .build();

        InterviewOrganizer interviewOrganizer = AgenticServices.agentBuilder(InterviewOrganizer.class)
                .chatModel(model)
                .tools(new OrganizingTools())
                .contentRetriever(ragProvider.loadHouseRulesRetriever())
                .build();

        EmailAssistant emailAssistant = AgenticServices.agentBuilder(EmailAssistant.class)
                .chatModel(model)
                .tools(new OrganizingTools())
                .build();

        // 2. 定义人工验证环节
        HumanInTheLoop humanValidator = AgenticServices.humanInTheLoopBuilder()
                .description("验证模型提出的招聘决策")
                .inputKey("modelDecision")
                .outputKey("finalDecision") // 由人工检查
                .requestWriter(request -> {
                    System.out.println("AI招聘助手建议: " + request);
                    System.out.println("请确认最终决定。");
                    System.out.println("选项: 邀请现场面试 (I), 拒绝 (R), 暂缓 (H)");
                    System.out.print("> "); // 在实际系统中需要输入验证和错误处理
                })
                .responseReader(() -> new Scanner(System.in).nextLine())
                .build();

        // 3. 将智能体链接成工作流
        UntypedAgent hiringDecisionWorkflow = AgenticServices.sequenceBuilder()
                .subAgents(decisionProposer, humanValidator)
                .outputKey("finalDecision")
                .build();

        // 4. 准备输入参数
        Map<String, Object> input = Map.of(
                "cvReview", new CvReview(0.85,
                        """
                                技术能力强，但缺乏所需的React经验。
                                似乎是快速独立学习者。文化契合度良好。
                                工作许可可能存在潜在问题，但似乎可以解决。
                                薪资期望略高于计划预算。
                                决定继续进行现场面试。
                                """)
        );

        // 5. 运行工作流
        String finalDecision = (String) hiringDecisionWorkflow.invoke(input);

        System.out.println("\n=== 人工最终决定 ===");
        System.out.println("(邀请现场面试 (I), 拒绝 (R), 暂缓 (H))\n");
        System.out.println(finalDecision);

        UntypedAgent candidateResponder = AgenticServices
                .conditionalBuilder()
                .subAgents(agenticScope -> finalDecision.contains("I"), interviewOrganizer)
                .subAgents(agenticScope -> finalDecision.contains("R"), emailAssistant)
                .subAgents(agenticScope -> finalDecision.contains("H"), new HoldOnAssist())
                .build();

        String candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt");
        String jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt");


        Map<String, Object> arguments = Map.of(
                "candidateContact", candidateContact,
                "jobDescription", jobDescription
        );

        // 6. 根据人工最终决定，进行下一步操作
        candidateResponder.invoke(arguments);

        // 注意：人工参与和人工验证通常需要较长时间等待用户响应。
        // 在这种情况下，建议使用异步智能体，这样它们不会阻塞工作流的其余部分，
        // 这些部分可以在用户回答之前潜在执行。
    }
}
