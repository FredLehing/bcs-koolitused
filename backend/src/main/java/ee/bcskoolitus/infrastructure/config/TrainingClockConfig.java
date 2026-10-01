package ee.bcskoolitus.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TrainingClockConfig {
    @Bean
    public Clock trainingClock() {
        return Clock.system(ZoneId.of("Europe/Tallinn"));
    }
}
