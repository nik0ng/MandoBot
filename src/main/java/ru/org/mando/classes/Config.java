package ru.org.mando.classes;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Config {

    private static final String BOT_TOKEN = "6201461209:AAFbCcYZhQgK_aX0n7CfQhUEOmyBKnXidQM";
    // admins
    private static final long NIK0NG = 567461715;
    private static final long MARAT = 594333443;
    private static final List<Long> USERS_TO_SEND_NOTIFICATION = List.of(NIK0NG, MARAT);

    private static final double THRESHOLD = 90.0;
    //test
//    private static final double THRESHOLD = 50.0;


    public static String getBotToken(){
        return BOT_TOKEN;
    }

    public static Double getThreshold(){
        return THRESHOLD;
    }

    public static List<Long> getUsersToSendNotification(){
        return USERS_TO_SEND_NOTIFICATION;
    }

}
