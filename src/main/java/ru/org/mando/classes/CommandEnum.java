package ru.org.mando.classes;


import javax.annotation.Nullable;

public enum CommandEnum implements EnumClass<String>{

    CONFIGURE("/configure"),
    EMPTY(""),
    CHECK_STORAGES("\uD83D\uDCBE Check storages"),
    STORAGE_IN_BACK_BACK("BackBack"),
    FILESTORAGE("FileStorage"),
    BACKUP_FILESTORAGE("Backup_FileStorage"),
    MAIN_DISK_1TB("main disk poseydon on 1TB"),
    ALL_STORAGE("\uD83D\uDD4B ALL"),
    WORK_CRON("⏰ Cron work"),
    STOP_CRON("⛔️ STOP cron"),
    START_CRON("✅ START cron"),
    BACK("\uD83D\uDD19 BACK"),
    ADMINISTRATION("\uD83D\uDC8A Administration"),
    KILL_YODA("\uD83D\uDC94 kill YODA"),
    ANNOUNCEMENT("#объявление");
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
