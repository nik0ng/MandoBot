package ru.org.mando.service;

import java.io.IOException;

public interface CheckSiteService {

    void check(String siteName) throws IOException, InterruptedException;
}
