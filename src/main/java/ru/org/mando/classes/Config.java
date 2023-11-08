package ru.org.mando.classes;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Config {

    private static final String BOT_TOKEN = "6201461209:AAFbCcYZhQgK_aX0n7CfQhUEOmyBKnXidQM";
    //test
//    private static final String BOT_TOKEN = "1785456855:AAEl16uUutXmMTSxM0tKaBJeH-tcvTlOqbY";
    private static final String YODA_IP = "192.1.0.221";
    private static final int YODA_PORT = 5465;
    private static final String YODA_USERNAME = "yodalxc";
    private static final String YODA_PATH = "/home/yodalxc/workscripts";

    // admins
    private static final long NIK0NG = 567461715;
    private static final long MARAT = 594333443;
    private static final List<Long> USERS_TO_SEND_NOTIFICATION = List.of(NIK0NG, MARAT);


    //for test (yulia)
//    private static final long MARAT = 460081308;

    //test
//    private static final List<Long> USERS_TO_SEND_NOTIFICATION = List.of(NIK0NG);

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

    public static String getYodaIp(){
        return YODA_IP;
    }
    public static Integer getYodaPort(){
        return YODA_PORT;
    }
    public static String getYodaUsername(){
        return YODA_USERNAME;
    }
    public static String getYodaPath(){
        return YODA_PATH;
    }


}
