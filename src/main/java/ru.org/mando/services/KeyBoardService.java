package ru.org.mando.services;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.org.mando.classes.CommandEnum;

import java.util.List;

public interface KeyBoardService {
    public ReplyKeyboardMarkup initReplyKeyboardMarkup();

    public KeyboardRow initKeyboardRow(List<KeyboardButton> keyboardButtonsRow);

    public List<KeyboardButton> initKeyboardButtonsRow(List<CommandEnum> commandEnumList, String additionalWord);
}
