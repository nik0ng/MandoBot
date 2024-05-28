package ru.org.mando.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@Configuration
public class AdminConfig {

    @Value("${bot.war.token}")
    private String botToken;
    @Value("${bot.name}")
    private String botName;

    @Value("${bot.nikanorov}")
    private Long nikanorov;
    @Value("${bot.marat}")
    private Long marat;
    @Value("${bot.nekrasov}")
    private Long nekrasov;


    public List<Long> getUsersToSendNotification(){
//        return Collections.singletonList(getNikanorov());
        return Arrays.asList(getNikanorov(), getMarat(), getNekrasov());
    }

}
