package ru.org.mando.services;

import org.springframework.stereotype.Service;

import java.io.File;
@Service
public class CalculatorServiceBean implements CalculatorService {
    @Override
    public Double countBusyStoragePercent(String path) {
        File file = new File(path);
        long totalSpace = file.getTotalSpace();
        long freeSpace = file.getFreeSpace();
        return ((double) (totalSpace - freeSpace) / totalSpace) * 100;
    }
}
