package ru.org.mando.classes;

import org.springframework.stereotype.Component;

@Component
public class Path {

    private static final String BACKBACK = "/";

    public static String getBackBack(){
        return BACKBACK;
    }
}
