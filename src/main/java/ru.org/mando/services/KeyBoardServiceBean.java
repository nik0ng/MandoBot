package ru.org.mando.services;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.org.mando.classes.CommandEnum;
import ru.org.mando.classes.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class KeyBoardServiceBean implements KeyBoardService {
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
    public List<KeyboardButton> initKeyboardButtonsRow(List<CommandEnum> commandEnumList, String additionalWord) {
        List<KeyboardButton> keyboardButtonsRow = new ArrayList<>();
        commandEnumList.forEach(t -> {
            String result = "";
            if (Objects.equals(t, CommandEnum.BACK) && additionalWord != null
                    && (additionalWord.equalsIgnoreCase("stop") || additionalWord.equalsIgnoreCase("start"))) {
                result += "\uD83D\uDD19 back to cron work";
            } else if (additionalWord != null) {
                result += t.getId() + " " + additionalWord;
            } else {
                result += t.getId();
            }
            keyboardButtonsRow.add(new KeyboardButton(result));
        });
        return keyboardButtonsRow;
    }
}
