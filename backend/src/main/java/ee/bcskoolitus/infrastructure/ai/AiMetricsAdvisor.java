package ee.bcskoolitus.infrastructure.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class AiMetricsAdvisor implements CallAdvisor {
    private static final BigDecimal INPUT_TOKEN_COST = new BigDecimal("0.30").divide(new BigDecimal(1_000_000));
    private static final BigDecimal OUTPUT_TOKEN_COST = new BigDecimal("2.50").divide(new BigDecimal(1_000_000));

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        long start = System.currentTimeMillis();
        ChatClientResponse response = callAdvisorChain.nextCall(chatClientRequest);
        ChatResponse chatResponse = response.chatResponse();
        Usage usage = chatResponse.getMetadata().getUsage();
        Integer inputTokens = chatResponse.getMetadata().getUsage().getPromptTokens();
        Integer outputTokens = chatResponse.getMetadata().getUsage().getCompletionTokens();
        log.info("[AiMetrics] latencyMs={} in={} out={} inputCost={} outputCost={}",
                System.currentTimeMillis() - start,
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                INPUT_TOKEN_COST.multiply(new BigDecimal(inputTokens)),
                OUTPUT_TOKEN_COST.multiply(new BigDecimal(outputTokens)));

        return response;
    }

    @Override
    public String getName() {
        return "Ai-Metrics";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
