/*
 * Copyright (c) 2018 Hof University
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
