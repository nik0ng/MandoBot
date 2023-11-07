package ru.org.mando;

import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ru.org.mando.services.CheckSiteService;
import ru.org.mando.services.InServerWorkerService;
import ru.org.mando.services.KeyBoardService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class DiskSpaceMonitorBot extends TelegramLongPollingBot {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CalculatorService calculatorService;
    @Autowired
    private KeyBoardService keyBoardService;
    @Autowired
    private CheckSiteService checkSiteService;
    @Autowired
    private InServerWorkerService serverWorkerService;

    public static final List<CommandEnum> CHECK_STORAGE_COMMAND = Arrays.asList(CommandEnum.STORAGE_IN_BACK_BACK, CommandEnum.FILESTORAGE,
            CommandEnum.BACKUP_FILESTORAGE, CommandEnum.MAIN_DISK_1TB);

    public static final List<CommandEnum> CRON_BUTTON_LIST = Arrays.asList(CommandEnum.START_CRON, CommandEnum.STOP_CRON, CommandEnum.BACK);

    public static Map<CommandEnum, Boolean> actionMap = CHECK_STORAGE_COMMAND.stream().collect(Collectors.toMap(command -> command, command -> true));

    public static void main(String[] args) {
        SpringApplication.run(DiskSpaceMonitorBot.class, args);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String command = update.getMessage().getText();
            if (Config.getUsersToSendNotification().contains(update.getMessage().getChatId())) {
                if (CommandEnum.CHECK_STORAGES.getId().equals(command)) {
                    makeChoiceDiskCheck(update.getMessage().getChatId());
                } else if (CommandEnum.STORAGE_IN_BACK_BACK.getId().equals(command)) {
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), Path.getBackBack(), command);
                } else if (CommandEnum.FILESTORAGE.getId().equals(command)) {
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), Path.getFilestorage(), command);
                } else if (CommandEnum.BACKUP_FILESTORAGE.getId().equals(command)) {
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), Path.getBackupFilestorage(), command);
                } else if (CommandEnum.MAIN_DISK_1TB.getId().equals(command)) {
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), Path.getMainDisk1tb(), command);
                } else if (CommandEnum.ALL_STORAGE.getId().equals(command)) {
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), Path.getBackBack(), command);
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), Path.getFilestorage(), command);
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), Path.getBackupFilestorage(), command);
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), Path.getMainDisk1tb(), command);
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
                } else if ((CommandEnum.MAIN_DISK_1TB.getId() + " Start").equals(command)) {
                    actionMap.put(CommandEnum.MAIN_DISK_1TB, true);
                } else if ((CommandEnum.ALL_STORAGE.getId() + " Start").equals(command)) {
                    actionMap.put(CommandEnum.STORAGE_IN_BACK_BACK, true);
                    actionMap.put(CommandEnum.FILESTORAGE, true);
                    actionMap.put(CommandEnum.BACKUP_FILESTORAGE, true);
                    actionMap.put(CommandEnum.MAIN_DISK_1TB, true);
                } else if ((CommandEnum.STORAGE_IN_BACK_BACK.getId() + " Stop").equals(command)) {
                    actionMap.put(CommandEnum.STORAGE_IN_BACK_BACK, false);
                } else if ((CommandEnum.FILESTORAGE.getId() + " Stop").equals(command)) {
                    actionMap.put(CommandEnum.FILESTORAGE, false);
                } else if ((CommandEnum.BACKUP_FILESTORAGE.getId() + " Stop").equals(command)) {
                    actionMap.put(CommandEnum.BACKUP_FILESTORAGE, false);
                } else if ((CommandEnum.MAIN_DISK_1TB.getId() + " Stop").equals(command)) {
                    actionMap.put(CommandEnum.MAIN_DISK_1TB, false);
                } else if ((CommandEnum.ALL_STORAGE.getId() + " Stop").equals(command)) {
                    actionMap.put(CommandEnum.STORAGE_IN_BACK_BACK, false);
                    actionMap.put(CommandEnum.FILESTORAGE, false);
                    actionMap.put(CommandEnum.BACKUP_FILESTORAGE, false);
                    actionMap.put(CommandEnum.MAIN_DISK_1TB, false);
                } else if (("\uD83D\uDD19 back to cron work").equals(command)) {
                    initCronCommands(update.getMessage().getChatId());
                }else if(CommandEnum.ADMINISTRATION.getId().equals(command)){
                    initAdminCommands(update.getMessage().getChatId());
                }else if(CommandEnum.KILL_YODA.getId().equals(command)){
                    String msg = serverWorkerService.killService("kill_yoda.sh");
                    Config.getUsersToSendNotification().forEach(u -> sendMessageToTelegram(msg, u));
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
        for (CommandEnum commandEnum : CHECK_STORAGE_COMMAND) {
            boolean detector = actionMap.get(commandEnum);
            if (detector) {
                String path = getPath(commandEnum);
                String message = makeMessageScheduler(path);
                if (message != null) {
                    Config.getUsersToSendNotification().forEach(u -> sendMessageToTelegram(message, u));
                }
            }
        }
        try {
            String checkYoda = checkSiteService.check("https://yoda.sec2.ru/");
            if (!checkYoda.equals("")) {
                Config.getUsersToSendNotification().forEach(u -> sendMessageToTelegram(checkYoda, u));
            }
        } catch (Exception e) {
            log.error("Bad try check YODA work status! \n" + e.getMessage());
        }
    }

    private String getPath(CommandEnum commandEnum) {
        return switch (commandEnum) {
            case STORAGE_IN_BACK_BACK -> Path.getBackBack();
            case FILESTORAGE -> Path.getFilestorage();
            case BACKUP_FILESTORAGE -> Path.getBackupFilestorage();
            case MAIN_DISK_1TB -> Path.getMainDisk1tb();
            default -> throw new IllegalArgumentException("Unknown command: " + commandEnum);
        };
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
        ReplyKeyboardMarkup replyKeyboardMarkup = keyBoardService.initReplyKeyboardMarkup();
        List<KeyboardButton> keyboardButtonsRow = keyBoardService.initKeyboardButtonsRow(CRON_BUTTON_LIST, null);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = keyBoardService.initKeyboardRow(keyboardButtonsRow);
        keyboardRows.add(keyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        executeSendMessage(chatId, "Choice cron command", replyKeyboardMarkup);

    }

    private void initAdminCommands(Long chatId){
        ReplyKeyboardMarkup replyKeyboardMarkup = keyBoardService.initReplyKeyboardMarkup();
        List<KeyboardButton> keyboardButtonsRow = keyBoardService.initKeyboardButtonsRow(Arrays.asList(CommandEnum.KILL_YODA, CommandEnum.BACK), null);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = keyBoardService.initKeyboardRow(keyboardButtonsRow);
        keyboardRows.add(keyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        executeSendMessage(chatId, "Choice ADMINISTRATION command", replyKeyboardMarkup);
    }

    public void sendCommandHints(Long chatId) {

        // Создание клавиатуры с кнопкой для подсказок команд
        ReplyKeyboardMarkup replyKeyboardMarkup = keyBoardService.initReplyKeyboardMarkup();

        List<KeyboardButton> keyboardButtonsRow = keyBoardService.initKeyboardButtonsRow(Arrays.asList(CommandEnum.CHECK_STORAGES,
                CommandEnum.WORK_CRON, CommandEnum.ADMINISTRATION), null);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = keyBoardService.initKeyboardRow(keyboardButtonsRow);
        keyboardRows.add(keyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        executeSendMessage(chatId, "Available commands:", replyKeyboardMarkup);
    }

    private void makeChoiceDiskCheck(Long userId) {
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