package ru.org.mando.classes;

import org.springframework.stereotype.Component;

@Component
public class Path {

    private static final String BACKBACK = "/mnt/backback";
    //test
//    private static final String BACKBACK = "/";

    private static final String FILESTORAGE = "/mnt/filestorage";
    private static final String BACKUP_FILESTORAGE = "/mnt/backup_filestorage";


    public static String getBackBack(){
        return BACKBACK;
    }
    public static String getFilestorage(){
        return FILESTORAGE;
    }

    public static String getBackupFilestorage(){
        return BACKUP_FILESTORAGE;
    }
}
