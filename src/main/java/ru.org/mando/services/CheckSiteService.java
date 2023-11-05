package ru.org.mando.services;

import java.io.IOException;
import java.net.MalformedURLException;

public interface CheckSiteService {

    String check(String siteName) throws IOException;
}
