package ru.org.mando.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.org.mando.config.AdminConfig;
import ru.org.mando.service.CheckSiteService;
import ru.org.mando.service.MandoBot;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CheckSiteServiceBean implements CheckSiteService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Lazy
    @Autowired
    private MandoBot mandoBot;
    @Autowired
    private AdminConfig adminConfig;

    private static final long MAX_REQUEST_TIME_MS = 600000; // 10 минут
    private static final long FIVE_MS = 5000; // 5 секунд
    private static final long FORTY_MS = 40000; // 40 секунд

    private static final long SEC30 = 30000; // 30 секунд
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void check(String siteName) throws IOException, InterruptedException {
        lock.lock();
        try {
            int responseCode = 0;
            long fullAnswerTime = 0;
            boolean badAnswer = false;

            while (responseCode != 200) {
                URL url = new URL(siteName);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout((int) MAX_REQUEST_TIME_MS);

                long startTime = System.currentTimeMillis();
                try {
                    responseCode = connection.getResponseCode();

                    long endTime = System.currentTimeMillis();
                    long ms = endTime - startTime;
                    fullAnswerTime += ms;

                    if (responseCode == 200) {
                        if (badAnswer || (ms > FIVE_MS)) {
                            String msg = String.format("Ответ 200. Всё ОК! Но время ответа заняло %s", convertMilliseconds(fullAnswerTime));
                            log.info(msg);
                            adminConfig.getUsersToSendNotification().forEach(u -> mandoBot.sendMessageToTelegram(msg, u));
                        }
                        return;
                    } else {
                        String msg = "Сайт yoda.sec2.ru недоступен. Код ответа: " + responseCode + ". Время ответа: " + fullAnswerTime;
                        adminConfig.getUsersToSendNotification().forEach(u -> mandoBot.sendMessageToTelegram(msg, u));
                        log.error(msg);
                        break;
                    }

                } catch (Exception e) {
                    long endTime = System.currentTimeMillis();
                    long stayTime = endTime - startTime;

                    fullAnswerTime += stayTime;

                    String resultTimeWait = convertMilliseconds(fullAnswerTime);

                    if (fullAnswerTime >= MAX_REQUEST_TIME_MS) {
                        String msg = "Время ответа превысило " + resultTimeWait
                                + "\nYODA не работает! Подсчет времени ответа прекращен до следующего запуска крона!";
                        adminConfig.getUsersToSendNotification().forEach(u -> mandoBot.sendMessageToTelegram(msg, u));
                        log.error("YODA DON'T WORKING! Время ответа {}", resultTimeWait);
                        break;
                    } else if (fullAnswerTime >= FIVE_MS && fullAnswerTime < FORTY_MS) {
                        String msg = "Время ответа превысило " + resultTimeWait;
                        adminConfig.getUsersToSendNotification().forEach(u -> mandoBot.sendMessageToTelegram(msg, u));
                        log.error("Время ответа превысило {} ", resultTimeWait);
                        badAnswer = true;
                        Thread.sleep(SEC30);
                        fullAnswerTime += SEC30;
                    } else if (fullAnswerTime >= FORTY_MS) {
                        String msg = "Время ответа превысило " + resultTimeWait;
                        adminConfig.getUsersToSendNotification().forEach(u -> mandoBot.sendMessageToTelegram(msg, u));
                        log.error(msg);
                        badAnswer = true;
                        Thread.sleep(SEC30);
                        fullAnswerTime += SEC30;
                    }
                } finally {
                    connection.disconnect();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private String convertMilliseconds(long milliseconds) {
        if (milliseconds >= 60000) {
            long minutes = milliseconds / 60000;
            long seconds = (milliseconds % 60000) / 1000;
            return minutes + " мин " + seconds + " сек";
        } else if (milliseconds >= 1000) {
            float seconds = milliseconds / 1000.0f;
            return String.format("%.2f сек", seconds);
        } else {
            return milliseconds + " мс";
        }
    }
}

