package ru.org.mando.service;

public interface CalculatorService {

    Double countBusyStoragePercent(String path);

    Boolean isEnoughSpace(Double usedPercentage);

    String checkLimitStorageAndMakeMessage(Double usedPercentage, String path);
}
