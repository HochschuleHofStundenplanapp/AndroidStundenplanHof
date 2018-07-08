package de.hof.university.app.chat.Helper;

import java.util.Random;

public class UserGenerator {

    public String generateUser(){

        Random random = new Random();
        int number = random.nextInt(100000);
        return "Guest" + number;
    }

    public String generatePassword(){
        Random random = new Random();
        int number = random.nextInt(10000000);

        while (number < 1000)
            number = random.nextInt(10000000);

        return Integer.toString(number);
    }


}
