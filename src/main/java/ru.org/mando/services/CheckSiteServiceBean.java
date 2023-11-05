package ru.org.mando.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
@Service
public class CheckSiteServiceBean implements CheckSiteService{
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final long MAX_REQUEST_TIME_MS = 5000; // 5 секунд

    @Override
    public String check(String siteName) throws IOException{

        // Получаем URL сайта
        URL url = new URL(siteName);

        // Создаем HTTP соединение
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        long startTime = System.currentTimeMillis();

        StringBuilder result = new StringBuilder();
        // Получаем код ответа
        connection.setReadTimeout((int) MAX_REQUEST_TIME_MS);
        int responseCode = 0;
        try {
            responseCode = connection.getResponseCode();
        } catch (SocketTimeoutException e) {
            result.append("Сайт yoda.sec2.ru недоступен. Время запроса: ").append(MAX_REQUEST_TIME_MS).append(" мс.");
            log.info(result.toString());
            return result.toString();
        }

        // Проверяем время ответа
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        if (responseTime > MAX_REQUEST_TIME_MS) {
            result.append("Сайт yoda.sec2.ru недоступен. Время ответа: ").append(responseTime).append(" мс.");
            log.info(result.toString());
            return result.toString();
        }

        // Проверяем, что сайт все еще доступен
        if (responseCode != 200) {
            result.append("Сайт yoda.sec2.ru недоступен. Код ответа: ").append(responseCode);
            log.info(result.toString());
            return result.toString();
        }
        return result.toString();
    }
}
