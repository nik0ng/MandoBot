package ru.org.mando.service.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.org.mando.service.InServerWorkerService;


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
            log.error(result + " по причине: \n" + e.getMessage());
        }
        return result;
    }
}
