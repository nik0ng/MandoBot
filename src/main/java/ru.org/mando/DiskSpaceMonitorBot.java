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
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class DiskSpaceMonitorBot extends TelegramLongPollingBot {

    @Autowired
    CalculatorService calculatorService;
    @Autowired
    KeyBoardService keyBoardService;
    public static final List<CommandEnum> CHECK_STORAGE_COMMAND = Arrays.asList(CommandEnum.STORAGE_IN_BACK_BACK, CommandEnum.FILESTORAGE,
            CommandEnum.BACKUP_FILESTORAGE);

    public static final List<CommandEnum> CRON_BUTTON_LIST = Arrays.asList(CommandEnum.START_CRON, CommandEnum.STOP_CRON, CommandEnum.BACK);

    public static Map<CommandEnum, Boolean> actionMap = CHECK_STORAGE_COMMAND.stream().collect(Collectors.toMap(command -> command, command -> true));

    public static void main(String[] args) {
        SpringApplication.run(DiskSpaceMonitorBot.class, args);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String command = update.getMessage().getText();
            if(Config.getUsersToSendNotification().contains(update.getMessage().getChatId())) {
                if (CommandEnum.CHECK_STORAGES.getId().equals(command)) {
                    makeChoiceDiskCheck(update.getMessage().getChatId());
                } else if (CommandEnum.STORAGE_IN_BACK_BACK.getId().equals(command)) {
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), Path.getBackBack(), command);
                } else if (CommandEnum.FILESTORAGE.getId().equals(command)) {
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), Path.getFilestorage(), command);
                } else if (CommandEnum.BACKUP_FILESTORAGE.getId().equals(command)) {
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), Path.getBackupFilestorage(), command);
                } else if (CommandEnum.ALL_STORAGE.getId().equals(command)) {
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), Path.getBackBack(), command);
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), Path.getFilestorage(), command);
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), Path.getBackupFilestorage(), command);
                } else if (CommandEnum.BACK.getId().equals(command)) {
                    sendCommandHints(update.getMessage().getChatId());
                } else if (CommandEnum.WORK_CRON.getId().equals(command)) {
                    initCronCommands(update.getMessage().getChatId());
                } else if (CommandEnum.START_CRON.getId().equals(command)) {
                    checkStopOrStartCronCommands(update.getMessage().getChatId(), "Start");
                } else if (CommandEnum.STOP_CRON.getId().equals(command)) {
                    checkStopOrStartCronCommands(update.getMessage().getChatId(), "Stop");
                } else if ((CommandEnum.STORAGE_IN_BACK_BACK.getId() + " Start").equals(command)) {
                    actionMap.put(CommandEnum.STORAGE_IN_BACK_BACK, true);
                } else if ((CommandEnum.FILESTORAGE.getId() + " Start").equals(command)) {
                    actionMap.put(CommandEnum.FILESTORAGE, true);
                } else if ((CommandEnum.BACKUP_FILESTORAGE.getId() + " Start").equals(command)) {
                    actionMap.put(CommandEnum.BACKUP_FILESTORAGE, true);
                } else if ((CommandEnum.ALL_STORAGE.getId() + " Start").equals(command)) {
                    actionMap.put(CommandEnum.STORAGE_IN_BACK_BACK, true);
                    actionMap.put(CommandEnum.FILESTORAGE, true);
                    actionMap.put(CommandEnum.BACKUP_FILESTORAGE, true);
                } else if ((CommandEnum.STORAGE_IN_BACK_BACK.getId() + " Stop").equals(command)) {
                    actionMap.put(CommandEnum.STORAGE_IN_BACK_BACK, false);
                } else if ((CommandEnum.FILESTORAGE.getId() + " Stop").equals(command)) {
                    actionMap.put(CommandEnum.FILESTORAGE, false);
                } else if ((CommandEnum.BACKUP_FILESTORAGE.getId() + " Stop").equals(command)) {
                    actionMap.put(CommandEnum.BACKUP_FILESTORAGE, false);
                } else if ((CommandEnum.ALL_STORAGE.getId() + " Stop").equals(command)) {
                    actionMap.put(CommandEnum.STORAGE_IN_BACK_BACK, false);
                    actionMap.put(CommandEnum.FILESTORAGE, false);
                    actionMap.put(CommandEnum.BACKUP_FILESTORAGE, false);
                } else if (("\uD83D\uDD19 back to cron work").equals(command)) {
                    initCronCommands(update.getMessage().getChatId());
                }
            }
        }
    }



    /**
     * check storages by cron
     */
    @Async
    @Scheduled(fixedRate = 60000) // Планирование выполнения каждую минуту
    public void checkBackBackDiskSpace() {
        boolean detector = true;
        detector = actionMap.get(CommandEnum.STORAGE_IN_BACK_BACK);
        if (detector) {
            String backback = makeMessageScheduler(Path.getBackBack());
            if (backback != null) {
                Config.getUsersToSendNotification().forEach(u->sendMessageToTelegram(backback, u));
            }
        }
        detector = actionMap.get(CommandEnum.FILESTORAGE);
        if (detector) {
            String fileStore = makeMessageScheduler(Path.getFilestorage());
            if (fileStore != null) {
                Config.getUsersToSendNotification().forEach(u->sendMessageToTelegram(fileStore, u));
            }
        }
        detector = actionMap.get(CommandEnum.BACKUP_FILESTORAGE);
        if (detector) {
            String backupFileStore = makeMessageScheduler(Path.getBackupFilestorage());
            if (backupFileStore != null) {
                Config.getUsersToSendNotification().forEach(u->sendMessageToTelegram(backupFileStore, u));
            }
        }
    }

    private String makeMessageScheduler(String path) {
        double usedPercentage = calculatorService.countBusyStoragePercent(path);
        return checkLimitStorageAndMakeMessage(usedPercentage, path);
    }


    private String checkLimitStorageAndMakeMessage(Double usedPercentage, String path) {
        String result = null;
        if (usedPercentage.isNaN() || !calculatorService.isEnoughSpace(usedPercentage)) {
            result = String.format("‼️‼️‼️Warning‼️‼️‼️\n %s Disk space usage is %.2f%%", path, usedPercentage);
        }
        return result;
    }


    public void checkDiskSpaceAndSendBack(Long chatId, String path, String command) {
        File file = new File(path);

        Double usedPercentage = calculatorService.countBusyStoragePercent(path);

        String message = String.format("Disk (%s) space usage: %.2f%%\nTotal space: %.2f GB\nFree space: %.2f GB",
                path, usedPercentage, file.getTotalSpace() / (1024.0 * 1024.0 * 1024.0), file.getFreeSpace() / (1024.0 * 1024.0 * 1024.0));

        sendMessageToTelegram(message, chatId);
        String result = null;
        result = checkLimitStorageAndMakeMessage(usedPercentage, path);
        if (result != null) {
            sendMessageToTelegram(result, chatId);
        } else {
            sendMessageToTelegram(String.format("%s  ✅", path), chatId);
        }

    }

    public void initCronCommands(Long chatId) {
        // Создание клавиатуры с кнопкой для подсказок команд
        ReplyKeyboardMarkup replyKeyboardMarkup = keyBoardService.initReplyKeyboardMarkup();
        List<KeyboardButton> keyboardButtonsRow = keyBoardService.initKeyboardButtonsRow(CRON_BUTTON_LIST, null);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = keyBoardService.initKeyboardRow(keyboardButtonsRow);
        keyboardRows.add(keyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        executeSendMessage(chatId, "Choice cron command", replyKeyboardMarkup);

    }

    public void sendCommandHints(Long chatId) {

        // Создание клавиатуры с кнопкой для подсказок команд
        ReplyKeyboardMarkup replyKeyboardMarkup = keyBoardService.initReplyKeyboardMarkup();

        List<KeyboardButton> keyboardButtonsRow = keyBoardService.initKeyboardButtonsRow(Arrays.asList(CommandEnum.CHECK_STORAGES, CommandEnum.WORK_CRON), null);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = keyBoardService.initKeyboardRow(keyboardButtonsRow);
        keyboardRows.add(keyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        executeSendMessage(chatId, "Available commands:", replyKeyboardMarkup);
    }

    private void makeChoiceDiskCheck(Long userId){
        ReplyKeyboardMarkup replyKeyboardMarkup = keyBoardService.initReplyKeyboardMarkup();
        List<CommandEnum> commandEnumList = new ArrayList<>(actionMap.keySet().stream().toList());
        List<KeyboardButton> keyboardButtonsRow = keyBoardService.initKeyboardButtonsRow(commandEnumList, null);
        List<KeyboardButton> keyboardOtherButtonsRow = keyBoardService.initKeyboardButtonsRow(Arrays.asList(CommandEnum.ALL_STORAGE, CommandEnum.BACK),
                null);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = keyBoardService.initKeyboardRow(keyboardButtonsRow);
        KeyboardRow keyboardRowOther = keyBoardService.initKeyboardRow(keyboardOtherButtonsRow);
        keyboardRows.add(keyboardRow);
        keyboardRows.add(keyboardRowOther);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        executeSendMessage(userId, "Who to check?", replyKeyboardMarkup);
    }

    public void checkStopOrStartCronCommands(Long chatId, String additionalWord) {
        // Создание клавиатуры с кнопкой для подсказок команд
        ReplyKeyboardMarkup replyKeyboardMarkup = keyBoardService.initReplyKeyboardMarkup();
        List<CommandEnum> commandEnumList = new ArrayList<>(actionMap.keySet().stream().toList());
        List<KeyboardButton> keyboardButtonsRow = keyBoardService.initKeyboardButtonsRow(commandEnumList, additionalWord);
        List<KeyboardButton> keyboardOtherButtonsRow = keyBoardService.initKeyboardButtonsRow(Arrays.asList(CommandEnum.ALL_STORAGE, CommandEnum.BACK),
                additionalWord);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = keyBoardService.initKeyboardRow(keyboardButtonsRow);
        KeyboardRow keyboardRowOther = keyBoardService.initKeyboardRow(keyboardOtherButtonsRow);
        keyboardRows.add(keyboardRow);
        keyboardRows.add(keyboardRowOther);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        executeSendMessage(chatId, "Who to " + additionalWord, replyKeyboardMarkup);

    }

    private void executeSendMessage(Long chatId, String text, ReplyKeyboardMarkup replyKeyboardMarkup) {
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

    public void sendMessageToTelegram(String message, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);
        executeTelegramMethod(sendMessage);
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