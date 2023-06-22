package ru.org.mando;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.org.mando.classes.CommandEnum;
import ru.org.mando.classes.Config;
import ru.org.mando.classes.Path;
import ru.org.mando.services.CalculatorService;
import ru.org.mando.services.KeyBoardService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class DiskSpaceMonitorBot extends TelegramLongPollingBot {

    @Autowired
    CalculatorService calculatorService;
    @Autowired
    KeyBoardService keyBoardService;

    public static void main(String[] args) {
        SpringApplication.run(DiskSpaceMonitorBot.class, args);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String command = update.getMessage().getText();

            if (CommandEnum.STORAGE_IN_BACK_BACK.getId().equals(command)) {
                checkDiskSpaceAndSendBack(Config.getUsersToSendNotification(), Path.getBackBack(), command);
            }else if (CommandEnum.FILESTORAGE.getId().equals(command)) {
                checkDiskSpaceAndSendBack(Config.getUsersToSendNotification(), Path.getFilestorage(), command);
            } else if (CommandEnum.BACKUP_FILESTORAGE.getId().equals(command)) {
                checkDiskSpaceAndSendBack(Config.getUsersToSendNotification(), Path.getBackupFilestorage(), command);
            } else if (CommandEnum.HELP.getId().equals(command) || CommandEnum.BACK.getId().equals(command)) {
                sendCommandHints(update.getMessage().getChatId());
            }else if (CommandEnum.WORK_CRON.getId().equals(command)){
                initCronCommands(update.getMessage().getChatId());
            }else if (CommandEnum.START_CRON.getId().equals(command)){
                Config.setIsWork(true);
            }else if (CommandEnum.STOP_CRON.getId().equals(command)){
                Config.setIsWork(false);
            }
        }
    }

    /**
     * check storages by cron
     */
    @Async
    @Scheduled(fixedRate = 60000) // Планирование выполнения каждую минуту
    public void checkBackBackDiskSpace() {
        if(Config.isWork) {
            String backback = makeMessageScheduler(Path.getBackBack());
            if (backback != null) {
                sendMessageToTelegram(backback, Config.getUsersToSendNotification());
            }
            String fileStore = makeMessageScheduler(Path.getFilestorage());
            if (fileStore != null) {
                sendMessageToTelegram(fileStore, Config.getUsersToSendNotification());
            }
            String backupFileStore = makeMessageScheduler(Path.getBackupFilestorage());
            if (backupFileStore != null) {
                sendMessageToTelegram(backupFileStore, Config.getUsersToSendNotification());
            }
        }
    }

    private String makeMessageScheduler(String path) {
        double usedPercentage = calculatorService.countBusyStoragePercent(path);
        return checkLimitStorageAndMakeMessage(usedPercentage, path);
    }


    private String checkLimitStorageAndMakeMessage(Double usedPercentage, String command) {
        String result = null;
        if (usedPercentage.equals("NaN") && !calculatorService.isEnoughSpace(usedPercentage)) {
            result = String.format("‼️‼️‼️Warning‼️‼️‼️\n %s Disk space usage is %.2f%%", command, usedPercentage);
        }
        return result;

    }


    public void checkDiskSpaceAndSendBack(List<Long> chatIds, String path, String command) {
        File file = new File(path);

        Double usedPercentage = calculatorService.countBusyStoragePercent(path);

        String message = String.format("Disk (%s) space usage: %.2f%%\nTotal space: %.2f GB\nFree space: %.2f GB",
                command, usedPercentage, file.getTotalSpace() / (1024.0 * 1024.0 * 1024.0), file.getFreeSpace() / (1024.0 * 1024.0 * 1024.0));

        sendMessageToTelegram(message, chatIds);
        String result = null;
        result = checkLimitStorageAndMakeMessage(usedPercentage, command);
        if (result != null) {
            sendMessageToTelegram(result, chatIds);
        } else {
            sendMessageToTelegram(String.format("%s is OKey! And you must not forget about my existence and always wear a helmet!",
                    command), chatIds);
        }

    }

    public static final List<CommandEnum> CRON_BUTTON_LIST = Arrays.asList(CommandEnum.START_CRON, CommandEnum.STOP_CRON, CommandEnum.BACK);
    public void initCronCommands(Long chatId){
        // Создание клавиатуры с кнопкой для подсказок команд
        ReplyKeyboardMarkup replyKeyboardMarkup = keyBoardService.initReplyKeyboardMarkup();
        List<KeyboardButton> keyboardButtonsRow = keyBoardService.initKeyboardButtonsRow(CRON_BUTTON_LIST);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = keyBoardService.initKeyboardRow(keyboardButtonsRow);
        keyboardRows.add(keyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        executeSendMessage(chatId, "Choice cron command", replyKeyboardMarkup);

    }

    public static final List<CommandEnum> CHECK_STORAGE_COMMAND = Arrays.asList(CommandEnum.STORAGE_IN_BACK_BACK, CommandEnum.FILESTORAGE,
            CommandEnum.BACKUP_FILESTORAGE);
    public static final List<CommandEnum> ADDITIONAL_COMMAND = Arrays.asList(CommandEnum.HELP, CommandEnum.WORK_CRON);

    public void sendCommandHints(Long chatId) {

        // Создание клавиатуры с кнопкой для подсказок команд
        ReplyKeyboardMarkup replyKeyboardMarkup = keyBoardService.initReplyKeyboardMarkup();

        List<KeyboardButton> keyboardButtonsRow = keyBoardService.initKeyboardButtonsRow(CHECK_STORAGE_COMMAND);
        List<KeyboardButton> keyboardButtons = keyBoardService.initKeyboardButtonsRow(ADDITIONAL_COMMAND);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = keyBoardService.initKeyboardRow(keyboardButtonsRow);
        keyboardRows.add(keyboardRow);
        KeyboardRow commandsRow = keyBoardService.initKeyboardRow(keyboardButtons);
        keyboardRows.add(commandsRow);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        executeSendMessage(chatId, "Available commands:", replyKeyboardMarkup);
    }

    private void executeSendMessage(Long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(text);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToTelegram(String message, List<Long> chatIds) {
        chatIds.forEach(chatId -> {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText(message);
            executeTelegramMethod(sendMessage);
        });
    }

    public void executeTelegramMethod(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "YOUR_BOT_USERNAME";
    }

    @Override
    public String getBotToken() {
        return Config.getBotToken();
    }
}