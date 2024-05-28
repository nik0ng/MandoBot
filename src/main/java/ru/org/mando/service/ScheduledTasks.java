package ru.org.mando.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.org.mando.classes.CommandEnum;
import ru.org.mando.config.AdminConfig;
import ru.org.mando.config.CommandConfig;
import ru.org.mando.config.YodaConfig;

import java.util.concurrent.RejectedExecutionException;

@Component
public class ScheduledTasks {
    private final Logger log = LoggerFactory.getLogger(getClass());


    @Autowired
    private AdminConfig adminConfig;
    @Autowired
    private YodaConfig yodaConfig;
    @Autowired
    private CheckSiteService checkSiteService;
    @Autowired
    private CalculatorService calculatorService;

    @Lazy
    @Autowired
    private MandoBot mandoBot;


    /**
     * check storages by cron
     */
    @Scheduled(cron = "0 * * * * *", zone = "Europe/Moscow")
    public void checkSite() {
        try {
            checkSiteService.check(yodaConfig.getYodaSite());
        } catch (RejectedExecutionException rejectExcept) {
            log.error(rejectExcept.getMessage());
        } catch (Exception e) {
            String msg = "Что то не так с соединением, не получилось проверить " + yodaConfig.getYodaSite() + " !";
            adminConfig.getUsersToSendNotification().forEach(u -> mandoBot.sendMessageToTelegram(msg, u));
            log.error("Bad try check YODA work status! \n" + e.getMessage());
        }
    }

    @Async
    @Scheduled(cron = "0 * * * * *", zone = "Europe/Moscow")
    public void checkDiskSpace() {
        for (CommandEnum commandEnum : CommandConfig.CHECK_STORAGE_COMMAND) {
            boolean detector = CommandConfig.ACTION_MAP.get(commandEnum);
            if (detector) {
                String path = getPath(commandEnum);
                String message = makeMessageScheduler(path);
                if (message != null) {
                    adminConfig.getUsersToSendNotification().forEach(u -> mandoBot.sendMessageToTelegram(message, u));
                }
            }
        }
    }

    private String makeMessageScheduler(String path) {
        double usedPercentage = calculatorService.countBusyStoragePercent(path);
        return calculatorService.checkLimitStorageAndMakeMessage(usedPercentage, path);
    }

    private String getPath(CommandEnum commandEnum) {
        return switch (commandEnum) {
            case STORAGE_IN_BACK_BACK -> yodaConfig.getBackback();
            case FILESTORAGE -> yodaConfig.getFilestorage();
            case BACKUP_FILESTORAGE -> yodaConfig.getBackupFileStorage();
            case MAIN_DISK_1TB -> yodaConfig.getMainDisk1TB();
            default -> throw new IllegalArgumentException("Unknown command: " + commandEnum);
        };
    }
}
