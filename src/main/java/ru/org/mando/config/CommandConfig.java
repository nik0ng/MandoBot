package ru.org.mando.config;

import org.springframework.context.annotation.Configuration;
import ru.org.mando.classes.CommandEnum;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class CommandConfig {

    public static final List<CommandEnum> CHECK_STORAGE_COMMAND = Arrays.asList(CommandEnum.STORAGE_IN_BACK_BACK, CommandEnum.FILESTORAGE,
            CommandEnum.BACKUP_FILESTORAGE, CommandEnum.MAIN_DISK_1TB);

    public static final List<CommandEnum> CRON_BUTTON_LIST = Arrays.asList(CommandEnum.START_CRON, CommandEnum.STOP_CRON);

    public static Map<CommandEnum, Boolean> ACTION_MAP = CommandConfig.CHECK_STORAGE_COMMAND.stream().collect(Collectors.toMap(command -> command, command -> true));

}
