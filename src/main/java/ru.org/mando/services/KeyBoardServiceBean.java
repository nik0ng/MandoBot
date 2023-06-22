package ru.org.mando.services;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.org.mando.classes.CommandEnum;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeyBoardServiceBean implements KeyBoardService{
    @Override
    public ReplyKeyboardMarkup initReplyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        return replyKeyboardMarkup;
    }

    @Override
    public KeyboardRow initKeyboardRow(List<KeyboardButton> keyboardButtonsRow) {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.addAll(keyboardButtonsRow);
        return keyboardRow;
    }

    @Override
    public List<KeyboardButton> initKeyboardButtonsRow(List<CommandEnum> commandEnumList) {
        List<KeyboardButton> keyboardButtonsRow = new ArrayList<>();
        commandEnumList.forEach(t->{
            keyboardButtonsRow.add(new KeyboardButton(t.getId()));
        });
        return keyboardButtonsRow;
    }
}
