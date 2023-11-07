package ru.org.mando.services;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class InServerWorkerServiceBean implements InServerWorkerService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public String killService(String script) {
        String result = "";
        try {

            ProcessBuilder processBuilder = new ProcessBuilder("bash", script);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            process.waitFor();
            result += "YODA успешно остановлена!";

        } catch (InterruptedException | IOException e) {
            result += "Не получилось остановить YODA!";
            throw new RuntimeException(e);
        }

        return result;
    }
}
