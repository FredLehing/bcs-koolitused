package ee.bcskoolitus.service;

import ee.bcskoolitus.controller.ask.dto.AskResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class NlToSqlService {

    private static final String SQL_SYSTEM_PROMPT_TEMPLATE = """
            You are a %s query generator for an e-commerce database.
            
            SCHEMA:
            customers(id UUID PK, email TEXT, first_name TEXT, last_name TEXT, phone TEXT)
            products(id UUID PK, name TEXT, price NUMERIC, stock INTEGER, category TEXT)
            orders(id UUID PK, customer_id UUID, status TEXT, total_amount NUMERIC)
            order_items(id UUID PK, order_id UUID, product_id UUID, quantity INTEGER, unit_price NUMERIC)
            
            RULES:
            1.Return only raw SQL, without any long explanation
            2.Don't return customer personal data information
            3.Don't speculate if you dont get an answer/data return CANNOT_ANSWER
            4. Use only %s SQL syntax and functions.
            """;

    private static final String SQL_USER_PROMPT_TEMPLATE = """
            
            %s
            """;

    private static final String SUMMARY_SYSTEM_PROMPT = """
            
            """;

    private static final String SUMMARY_USER_PROMPT_TEMPLATE = """
            
            Question: %s
            Results (%d rows): %s
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ChatClient chatClient;
    private final String sqlSystemPrompt;

    public NlToSqlService(JdbcTemplate jdbcTemplate, ChatClient.Builder builder,
                           @Value("${nlsql.dialect}") String dialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.chatClient = builder.build();
        this.sqlSystemPrompt = SQL_SYSTEM_PROMPT_TEMPLATE.formatted(dialect, dialect);
    }

    public AskResponse ask(String userQuestion) {

        return callLlm(sqlSystemPrompt, userQuestion);
    }

    private AskResponse callLlm(String systemPrompt, String userPrompt) {
        AskResponse response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .responseEntity(AskResponse.class)
                .getEntity();

        if (response == null) {
            throw new IllegalStateException("AI model returned an empty response");
        }

        return response;
    }
}
