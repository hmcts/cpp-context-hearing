package uk.gov.moj.cpp.hearing.command.handler.util;

import java.security.SecureRandom;
import java.util.Random;

public class TokenGenerator {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RND = new SecureRandom();

    public static String generateAlphanumeric(int length) {
        return RND.ints(length, 0, CHARS.length())
                .mapToObj(CHARS::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
