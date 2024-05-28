package ru.org.mando.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.org.mando.service.CalculatorService;

import java.io.File;
@Service
public class CalculatorServiceBean implements CalculatorService {

    @Value("${yoda.maxCompletedSpaceDisk}")
    private int maxCompletedSpaceDisk;

    @Override
    public Double countBusyStoragePercent(String path) {
        File file = new File(path);
        long totalSpace = file.getTotalSpace();
        long freeSpace = file.getFreeSpace();
        return ((double) (totalSpace - freeSpace) / totalSpace) * 100;
    }

    @Override
    public Boolean isEnoughSpace(Double usedPercentage) {
        return usedPercentage < maxCompletedSpaceDisk;
    }

    @Override
    public String checkLimitStorageAndMakeMessage(Double usedPercentage, String path) {
        String result = null;
        if (usedPercentage.isNaN() || !isEnoughSpace(usedPercentage)) {
            result = String.format("‼️‼️‼️Warning‼️‼️‼️\n %s Disk space usage is %.2f%%", path, usedPercentage);
        }
        return result;
    }
}
