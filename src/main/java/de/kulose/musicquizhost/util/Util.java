package de.kulose.musicquizhost.util;

import java.util.Random;

public class Util {
    private static String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVXYZ0123456789";
    private static int nameLength = 4;
    private static Random random = new Random();

    public static String getRandomRoomName() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < nameLength; i++) {
            char randomChar = allowedChars.charAt(random.nextInt(allowedChars.length()));
            stringBuilder.append(randomChar);
        }

        return stringBuilder.toString();
    }
}
