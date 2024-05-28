package ru.org.mando.service;

import ru.org.mando.config.CommandConfig;
import ru.org.mando.config.YodaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.org.mando.classes.CommandEnum;
import ru.org.mando.config.AdminConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MandoBot extends TelegramLongPollingBot {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CalculatorService calculatorService;
    @Autowired
    private KeyBoardService keyBoardService;
    @Autowired
    private InServerWorkerService serverWorkerService;

    @Autowired
    private AdminConfig adminConfig;
    @Autowired
    private YodaConfig yodaConfig;



    public MandoBot() {
        log.info("MandoBot instantiated");
    }



    @Override
    public void onUpdateReceived(Update update) {

        if (adminConfig.getUsersToSendNotification().contains(update.getMessage().getChatId())) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String command = update.getMessage().getText();

                if (adminConfig.getNikanorov().equals(update.getMessage().getChatId())) {
                    if (command.toLowerCase().contains(CommandEnum.ANNOUNCEMENT.getId())) {
                        adminConfig.getUsersToSendNotification().forEach(u -> sendMessageToTelegram(command, u));
                    }
                }

                if (CommandEnum.CHECK_STORAGES.getId().equals(command)) {
                    makeChoiceDiskCheck(update.getMessage().getChatId());
                } else if (CommandEnum.STORAGE_IN_BACK_BACK.getId().equals(command)) {
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), yodaConfig.getBackback(), command);
                } else if (CommandEnum.FILESTORAGE.getId().equals(command)) {
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), yodaConfig.getFilestorage(), command);
                } else if (CommandEnum.BACKUP_FILESTORAGE.getId().equals(command)) {
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), yodaConfig.getBackupFileStorage(), command);
                } else if (CommandEnum.MAIN_DISK_1TB.getId().equals(command)) {
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), yodaConfig.getMainDisk1TB(), command);
                } else if (CommandEnum.ALL_STORAGE.getId().equals(command)) {
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), yodaConfig.getBackback(), command);
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), yodaConfig.getFilestorage(), command);
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), yodaConfig.getBackupFileStorage(), command);
                    checkDiskSpaceAndSendBack(update.getMessage().getChatId(), yodaConfig.getMainDisk1TB(), command);
                } else if (CommandEnum.BACK.getId().equals(command)) {
                    sendCommandHints(update.getMessage().getChatId());
                } else if (CommandEnum.WORK_CRON.getId().equals(command)) {
                    initCronCommands(update.getMessage().getChatId());
                } else if (CommandEnum.START_CRON.getId().equals(command)) {
                    checkStopOrStartCronCommands(update.getMessage().getChatId(), "Start");
                } else if (CommandEnum.STOP_CRON.getId().equals(command)) {
                    checkStopOrStartCronCommands(update.getMessage().getChatId(), "Stop");
                } else if ((CommandEnum.STORAGE_IN_BACK_BACK.getId() + " Start").equals(command)) {
                    CommandConfig.ACTION_MAP.put(CommandEnum.STORAGE_IN_BACK_BACK, true);
                    adminConfig.getUsersToSendNotification().forEach(u -> sendMessageToTelegram("Проверка BackBack по крону включена!", u));
                } else if ((CommandEnum.FILESTORAGE.getId() + " Start").equals(command)) {
                    CommandConfig.ACTION_MAP.put(CommandEnum.FILESTORAGE, true);
                    adminConfig.getUsersToSendNotification().forEach(u -> sendMessageToTelegram("Проверка FileStorage по крону включена!", u));
                } else if ((CommandEnum.BACKUP_FILESTORAGE.getId() + " Start").equals(command)) {
                    CommandConfig.ACTION_MAP.put(CommandEnum.BACKUP_FILESTORAGE, true);
                    adminConfig.getUsersToSendNotification().forEach(u -> sendMessageToTelegram("Проверка Backup_FileStorage по крону включена!", u));
                } else if ((CommandEnum.MAIN_DISK_1TB.getId() + " Start").equals(command)) {
                    CommandConfig.ACTION_MAP.put(CommandEnum.MAIN_DISK_1TB, true);
                    adminConfig.getUsersToSendNotification().forEach(u -> sendMessageToTelegram("Проверка основного диска Poseydon на 1 ТБ по крону включена!", u));
                } else if ((CommandEnum.ALL_STORAGE.getId() + " Start").equals(command)) {
                    CommandConfig.ACTION_MAP.put(CommandEnum.STORAGE_IN_BACK_BACK, true);
                    CommandConfig.ACTION_MAP.put(CommandEnum.FILESTORAGE, true);
                    CommandConfig.ACTION_MAP.put(CommandEnum.BACKUP_FILESTORAGE, true);
                    CommandConfig.ACTION_MAP.put(CommandEnum.MAIN_DISK_1TB, true);
                    adminConfig.getUsersToSendNotification().forEach(u -> sendMessageToTelegram("Все проверки дисков по крону включены!", u));
                } else if ((CommandEnum.STORAGE_IN_BACK_BACK.getId() + " Stop").equals(command)) {
                    CommandConfig.ACTION_MAP.put(CommandEnum.STORAGE_IN_BACK_BACK, false);
                    adminConfig.getUsersToSendNotification().forEach(u -> sendMessageToTelegram("Проверка BackBack по крону отключена!", u));
                } else if ((CommandEnum.FILESTORAGE.getId() + " Stop").equals(command)) {
                    CommandConfig.ACTION_MAP.put(CommandEnum.FILESTORAGE, false);
                    adminConfig.getUsersToSendNotification().forEach(u -> sendMessageToTelegram("Проверка FileStorage по крону отключена!", u));
                } else if ((CommandEnum.BACKUP_FILESTORAGE.getId() + " Stop").equals(command)) {
                    CommandConfig.ACTION_MAP.put(CommandEnum.BACKUP_FILESTORAGE, false);
                    adminConfig.getUsersToSendNotification().forEach(u -> sendMessageToTelegram("Проверка Backup_FileStorage по крону отключена!", u));
                } else if ((CommandEnum.MAIN_DISK_1TB.getId() + " Stop").equals(command)) {
                    CommandConfig.ACTION_MAP.put(CommandEnum.MAIN_DISK_1TB, false);
                    adminConfig.getUsersToSendNotification().forEach(u -> sendMessageToTelegram("Проверка основного диска Poseydon на 1 ТБ по крону отключена!", u));
                } else if ((CommandEnum.ALL_STORAGE.getId() + " Stop").equals(command)) {
                    CommandConfig.ACTION_MAP.put(CommandEnum.STORAGE_IN_BACK_BACK, false);
                    CommandConfig.ACTION_MAP.put(CommandEnum.FILESTORAGE, false);
                    CommandConfig.ACTION_MAP.put(CommandEnum.BACKUP_FILESTORAGE, false);
                    CommandConfig.ACTION_MAP.put(CommandEnum.MAIN_DISK_1TB, false);
                    adminConfig.getUsersToSendNotification().forEach(u -> sendMessageToTelegram("Все проверки дисков по крону отключены!", u));
                } else if (("\uD83D\uDD19 back to cron work").equals(command)) {
                    initCronCommands(update.getMessage().getChatId());
                } else if (CommandEnum.ADMINISTRATION.getId().equals(command)) {
                    initAdminCommands(update.getMessage().getChatId());
                } else if (CommandEnum.KILL_YODA.getId().equals(command)) {
                    String msg = serverWorkerService.killService("kill_yoda.sh");
                    adminConfig.getUsersToSendNotification().forEach(u -> sendMessageToTelegram(msg, u));
                    log.info(msg);
                } else if (CommandEnum.CONFIGURE.getId().equals(command)) {
                    sendCommandHints(update.getMessage().getChatId());
                }
            }
        } else {
            User user = update.getMessage().getFrom();

            String msg = "\nВНИМАНИЕ! НЕСАНКЦИОНИРОВАННАЯ ПОПЫТКА ОТПРАВЛЯТЬ КОМАНДЫ! \nID пользователя: " + user.getId() + "\nФИО пользователя: " +
                    (user.getLastName() != null ? (user.getLastName() + " ") : "")
                    + user.getFirstName() + (user.getUserName() != null ? ("\nlogin: " + user.getUserName()) : "");
            adminConfig.getUsersToSendNotification().forEach(u -> sendMessageToTelegram(msg, u));
            log.error(msg);
        }

    }

    public void checkDiskSpaceAndSendBack(Long chatId, String path, String command) {
        File file = new File(path);

        Double usedPercentage = calculatorService.countBusyStoragePercent(path);

        String message = String.format("Disk (%s) space usage: %.2f%%\nTotal space: %.2f GB\nFree space: %.2f GB",
                path, usedPercentage, file.getTotalSpace() / (1024.0 * 1024.0 * 1024.0), file.getFreeSpace() / (1024.0 * 1024.0 * 1024.0));

        sendMessageToTelegram(message, chatId);
        String result = null;
        result = calculatorService.checkLimitStorageAndMakeMessage(usedPercentage, path);
        if (result != null) {
            sendMessageToTelegram(result, chatId);
        } else {
            sendMessageToTelegram(String.format("%s  ✅", path), chatId);
        }

    }

    public void initCronCommands(Long chatId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = keyBoardService.initReplyKeyboardMarkup();
        List<KeyboardButton> keyboardButtonsRow = keyBoardService.initKeyboardButtonsRow(CommandConfig.CRON_BUTTON_LIST, null);
        List<KeyboardButton> backKeyboardButtonsRow = keyBoardService.initKeyboardButtonsRow(List.of(CommandEnum.BACK), null);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = keyBoardService.initKeyboardRow(keyboardButtonsRow);
        KeyboardRow backKeyBoardRow = keyBoardService.initKeyboardRow(backKeyboardButtonsRow);
        keyboardRows.add(keyboardRow);
        keyboardRows.add(backKeyBoardRow);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        executeSendMessage(chatId, "Choice cron command", replyKeyboardMarkup);

    }

    private void initAdminCommands(Long chatId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = keyBoardService.initReplyKeyboardMarkup();
        List<KeyboardButton> keyboardButtonsRow = keyBoardService.initKeyboardButtonsRow(List.of(CommandEnum.KILL_YODA), null);

        List<KeyboardButton> backKeyboardButtonsRow = keyBoardService.initKeyboardButtonsRow(List.of(CommandEnum.BACK), null);


        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow keyboardRow = keyBoardService.initKeyboardRow(keyboardButtonsRow);
        KeyboardRow backKeyboardRow = keyBoardService.initKeyboardRow(backKeyboardButtonsRow);
        keyboardRows.add(keyboardRow);
        keyboardRows.add(backKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        executeSendMessage(chatId, "Choice ADMINISTRATION command", replyKeyboardMarkup);
    }


    public void sendCommandHints(Long chatId) {

        // Создание клавиатуры с кнопкой для подсказок команд
        ReplyKeyboardMarkup replyKeyboardMarkup = keyBoardService.initReplyKeyboardMarkup();

        List<KeyboardButton> keyboardButtonsRow = keyBoardService.initKeyboardButtonsRow(Arrays.asList(CommandEnum.CHECK_STORAGES,
                CommandEnum.WORK_CRON), null);

        List<KeyboardButton> adminKeyboardButtonRow = keyBoardService.initKeyboardButtonsRow(List.of(CommandEnum.ADMINISTRATION), null);

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = keyBoardService.initKeyboardRow(keyboardButtonsRow);
        keyboardRows.add(keyboardRow);

        KeyboardRow adminKeyboardRow = keyBoardService.initKeyboardRow(adminKeyboardButtonRow);
        keyboardRows.add(adminKeyboardRow);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        executeSendMessage(chatId, "Available commands:", replyKeyboardMarkup);
    }

    private void makeChoiceDiskCheck(Long userId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = keyBoardService.initReplyKeyboardMarkup();
        List<CommandEnum> commandEnumList = new ArrayList<>(CommandConfig.ACTION_MAP.keySet().stream().toList());
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
        List<CommandEnum> commandEnumList = new ArrayList<>(CommandConfig.ACTION_MAP.keySet().stream().toList());
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
        if (text != null && !text.equals("")) {
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
        return adminConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return adminConfig.getBotToken();
    }
}