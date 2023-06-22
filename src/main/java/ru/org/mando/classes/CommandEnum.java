package ru.org.mando.classes;


import javax.annotation.Nullable;

public enum CommandEnum implements EnumClass<String>{
    STORAGE_IN_BACK_BACK("Storage in BackBack"),
    FILESTORAGE("Storage in FileStorage"),
    BACKUP_FILESTORAGE("Storage in Backup_FileStorage"),
    WORK_CRON("Cron work"),
    STOP_CRON("⛔️ STOP cron"),
    START_CRON("✅ START cron"),
    BACK("\uD83D\uDD19 BACK"),
    HELP("\uD83C\uDD98 HELP");
    private String id;
    CommandEnum(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static CommandEnum fromId(String id) {
        for (CommandEnum at : CommandEnum.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
