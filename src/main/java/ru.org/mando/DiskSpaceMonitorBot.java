package ru.org.mando;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.org.mando.classes.Command;
import ru.org.mando.classes.Config;
import ru.org.mando.classes.Path;
import ru.org.mando.services.CalculatorService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class DiskSpaceMonitorBot extends TelegramLongPollingBot {

    @Autowired
    CalculatorService calculatorService;

    public static void main(String[] args) {
        SpringApplication.run(DiskSpaceMonitorBot.class, args);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String command = update.getMessage().getText();

            if (Command.STORAGE_IN_BACK_BACK.equals(command)) {
                checkDiskSpaceAndSendBack(Config.getUsersToSendNotification(), Path.getBackBack(), command);
            } if(Command.FILESTORAGE.equals(command)){
                checkDiskSpaceAndSendBack(Config.getUsersToSendNotification(), Path.getBackBack(), command);
            }else if (Command.HELP.equals(command)) {
                sendCommandHints(update.getMessage().getChatId());
            }
        }
    }

    /**
     * check storage in backback in cron
     */
    @Scheduled(fixedDelay = 4 * 60 * 60 * 1000) // Проверка каждые 4 часа (время задается в миллисекундах)
    public void checkDiskSpace() {
        double usedPercentage = calculatorService.countBusyStoragePercent(Path.getBackBack());
        String message = checkLimitStorageAndMakeMessage(usedPercentage, Path.getBackBack());
        sendMessageToTelegram(message, Config.getUsersToSendNotification());
    }

    private String checkLimitStorageAndMakeMessage(Double usedPercentage, String command){
        if (usedPercentage > Config.getThreshold()) {
            return String.format("‼️‼️‼️Warning‼️‼️‼️\n %s Disk space usage is %.2f%%", command, usedPercentage);
        }else {
            return String.format("Storage in %s is OKey! And you must not forget about my existence and always wear a helmet!", command);
        }
    }

    public void checkDiskSpaceAndSendBack(List<Long> chatIds, String path, String command) {
        File file = new File(path);

        double usedPercentage = calculatorService.countBusyStoragePercent(path);

        String message = String.format("Disk (%s) space usage: %.2f%%\nTotal space: %.2f GB\nFree space: %.2f GB",
                command, usedPercentage, file.getTotalSpace() / (1024.0 * 1024.0 * 1024.0), file.getFreeSpace() / (1024.0 * 1024.0 * 1024.0));

        sendMessageToTelegram(message, chatIds);
        sendMessageToTelegram(checkLimitStorageAndMakeMessage(usedPercentage, command), chatIds);

    }

    public void sendCommandHints(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Available commands:");

        // Создание клавиатуры с кнопкой для подсказок команд
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardButton> keyboardButtonsRow = new ArrayList<>();
        keyboardButtonsRow.add(new KeyboardButton(Command.STORAGE_IN_BACK_BACK));
        keyboardButtonsRow.add(new KeyboardButton(Command.FILESTORAGE));


        List<KeyboardButton> keyboardButtons = new ArrayList<>();
        keyboardButtons.add(new KeyboardButton(Command.HELP));
        keyboardButtons.add(new KeyboardButton("/command2"));
        keyboardButtons.add(new KeyboardButton("/command3"));
        keyboardButtons.add(new KeyboardButton("/command4"));

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.addAll(keyboardButtonsRow);
        keyboardRows.add(keyboardRow);

        KeyboardRow commandsRow = new KeyboardRow();
        commandsRow.addAll(keyboardButtons);
        keyboardRows.add(commandsRow);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToTelegram(String message, List<Long> chatIds) {
        chatIds.forEach(chatId ->{
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