package de.hof.university.app.chat.Helper;

import java.util.Random;

public class UserGenerator {

    public String generateUser(){

        Random random = new Random();
        int number = random.nextInt(100000);
        String username = "Guest" + number;
        return username;
    }


}
