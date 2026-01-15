package com.cnblogs.yjmyzz.langchain4j.study.agentic._9_human_in_the_loop;

import com.cnblogs.yjmyzz.langchain4j.study.AgentDesignPatternApplication;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.workflow.HumanInTheLoop;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;
import java.util.Scanner;

/**
 * 这个示例演示了带有人工参与的来回交互循环，
 * 直到达到最终目标（退出条件），之后工作流的其余部分可以继续。
 * 循环持续直到人工确认可用性，这由AiService验证。
 * 当没有找到可用时间段时，循环在5次迭代后结束。
 */
@SpringBootApplication
public class _9b_HumanInTheLoop_Chatbot_With_Memory {

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(AgentDesignPatternApplication.class, args);
        ChatModel model = context.getBean("ollamaChatModel", ChatModel.class);

        // 1. 定义子智能体
        MeetingProposer proposer = AgenticServices
                .agentBuilder(MeetingProposer.class)
                .chatModel(model)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(15)) // 使智能体能记住已提出的建议
                .outputKey("proposal")
                .build();

        // 2. 添加AiService来判断是否已达成决策（这可以是小型本地模型，因为任务很简单）
        DecisionsReachedService decisionService = AiServices.create(DecisionsReachedService.class, model);

        // 2. 定义人工参与智能体
        HumanInTheLoop humanInTheLoop = AgenticServices
                .humanInTheLoopBuilder()
                .description("向用户请求输入的智能体")
                .outputKey("candidateAnswer") // 匹配proposer智能体的输入变量名之一
                .inputKey("proposal") // 必须匹配proposer智能体的输出
                .requestWriter(request -> {
                    System.out.println(request);
                    System.out.print("> ");
                })
                .responseReader(() -> new Scanner(System.in).nextLine())
                .async(true) // 等待用户输入时不需要阻塞整个程序
                .build();

        // 3. 构建循环
        // 这里我们希望每个循环只检查一次退出条件，而不是在每次智能体调用后都检查，
        // 所以我们将两个智能体捆绑在一个序列中，并将其作为一个智能体提供给循环
        UntypedAgent agentSequence = AgenticServices
                .sequenceBuilder()
                .subAgents(proposer, humanInTheLoop)
                .output(agenticScope -> Map.of(
                        "proposal", agenticScope.readState("proposal"),
                        "candidateAnswer", agenticScope.readState("candidateAnswer")
                ))
                .outputKey("proposalAndAnswer")
                // 这个输出包含最后的日期建议 + 候选人的回答，对于后续智能体安排会议（或放弃尝试）应该足够
                .build();

        UntypedAgent schedulingLoop = AgenticServices
                .loopBuilder()
                .subAgents(agentSequence)
                .exitCondition(scope -> {
                    System.out.println("--- 检查退出条件 ---");
                    String response = (String) scope.readState("candidateAnswer");
                    String proposal = (String) scope.readState("proposal");
                    return response != null && decisionService.isDecisionReached(proposal, response);
                })
                .outputKey("proposalAndAnswer")
                .maxIterations(5)
                .build();

        // 4. 运行日程安排循环
        Map<String, Object> input = Map.of("meetingTopic", "现场访问",
                "candidateAnswer", "你好", // 这个变量需要提前出现在AgenticScope中，因为MeetingProposer将其作为输入
                "memoryId", "user-1234"); // 如果不提供memoryId，proposer智能体将不会记住已提出的建议

        var lastProposalAndAnswer = schedulingLoop.invoke(input);

        System.out.println("=== 结果：最后的建议和回答 ===");
        System.out.println(lastProposalAndAnswer);
    }
}
