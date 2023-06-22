package ru.org.mando.services;

import org.springframework.stereotype.Service;
public interface CalculatorService {

    public Double countBusyStoragePercent(String path);

    public Boolean isEnoughSpace(Double usedPercentage);
}
