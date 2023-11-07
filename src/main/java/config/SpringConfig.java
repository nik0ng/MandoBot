package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.org.mando.DiskSpaceMonitorBot;
import ru.org.mando.services.CalculatorServiceBean;
import ru.org.mando.services.CheckSiteServiceBean;
import ru.org.mando.services.InServerWorkerServiceBean;

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

    @Bean
    public CheckSiteServiceBean checkSiteService() {return new CheckSiteServiceBean();}

    @Bean
    public InServerWorkerServiceBean inServerWorkerService(){ return new InServerWorkerServiceBean();}
}
