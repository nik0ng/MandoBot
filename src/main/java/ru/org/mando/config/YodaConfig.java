package ru.org.mando.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class YodaConfig {

    @Value("${yoda.war}")
    private String yodaSite;

    @Value("${yoda.ip}")
    private String yodaIp;

    @Value("${yoda.port}")
    private String yodaPort;

    @Value("${yoda.userName}")
    private String yodaUserName;

    @Value("${yoda.path.bashScripts}")
    private String yodaWorkScripts;

    @Value("${yoda.path.backback}")
    private String backback;
    @Value("${yoda.path.filestorage}")
    private String filestorage;
    @Value("${yoda.path.backupFileStorage}")
    private String backupFileStorage;
    @Value("${yoda.path.mainDisk1TB}")
    private String mainDisk1TB;
}
