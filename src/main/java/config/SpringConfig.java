package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.org.mando.DiskSpaceMonitorBot;
import ru.org.mando.services.CalculatorService;
import ru.org.mando.services.CalculatorServiceBean;

@Configuration
@ComponentScan("ru.org.mando")
public class SpringConfig{
    @Bean
    public DiskSpaceMonitorBot diskSpaceMonitorBot(CalculatorServiceBean calculatorServiceBean) {
        return new DiskSpaceMonitorBot();
    }

    @Bean
    public CalculatorServiceBean calculatorService() {
        return new CalculatorServiceBean();
    }
}
