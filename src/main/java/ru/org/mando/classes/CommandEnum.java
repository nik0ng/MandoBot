package ru.org.mando.classes;


import javax.annotation.Nullable;

public enum CommandEnum implements EnumClass<String>{

    CHECK_STORAGES("\uD83D\uDCBE Check storages"),
    STORAGE_IN_BACK_BACK("BackBack"),
    FILESTORAGE("FileStorage"),
    BACKUP_FILESTORAGE("Backup_FileStorage"),
    MAIN_DISK_1TB("main disk poseydon on 1TB"),
    ALL_STORAGE("\uD83D\uDD4B ALL"),
    WORK_CRON("⏰ Cron work"),
    STOP_CRON("⛔️ STOP cron"),
    START_CRON("✅ START cron"),
    BACK("\uD83D\uDD19 BACK");
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
