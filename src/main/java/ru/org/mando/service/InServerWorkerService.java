package ru.org.mando.service;

import com.jcraft.jsch.JSchException;

import java.io.IOException;

public interface InServerWorkerService {

    /**
     * Kill service, screen and process, connect by ssh to server and start sh script
     * @return result
     */
    String killService(String script);
}
