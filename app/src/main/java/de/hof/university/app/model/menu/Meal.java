/*
 * Copyright (c) 2016 Lars Gaidzik & Lukas Mahr
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

package de.hof.university.app.model.menu;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by Lukas on 25.11.2015.
 */
public class Meal {
    private String name;
    private String price;
    private final String weekDay;
    private final String category;
    private String tariff;
    private final Date day;

// --Commented out by Inspection START (17.07.2016 20:11):
//    public Meal(final Meal meal) {
//        name= meal.name;
//        price = meal.price;
//        weekDay = meal.weekDay;
//        category = meal.category;
//        tariff = meal.tariff;
//        day= meal.day;
//    }
// --Commented out by Inspection STOP (17.07.2016 20:11)


    // public Meal(String name, String price) {
    //    this.name = name;
    //    this.price = price;
    //}

// --Commented out by Inspection START (17.07.2016 20:11):
//    protected Meal(final Parcel in) {
//        name = in.readString();
//        price = in.readString();
//        weekDay = in.readString();
//        category = in.readString();
//        tariff = in.readString();
//    }
// --Commented out by Inspection STOP (17.07.2016 20:11)

    public Meal(final Date tag, final String weeekDay, final String category, final String name){
        this.weekDay = weeekDay;
        this.day = tag;
        this.category =category;
        this.name = name;
    }

// --Commented out by Inspection START (17.07.2016 20:11):
//    public Meal(final String name, final String preis, final String weekDay, final String kategorie,
//                final String tarif, final Date day) {
//        this.name = name;
//        setPrice(preis);
//        this.weekDay = weekDay;
//        this.category = kategorie;
//        this.tariff = tarif;
//        this.day = day;
//    }
// --Commented out by Inspection STOP (17.07.2016 20:11)

    public final Date getDay() {
        return day;
    }

// --Commented out by Inspection START (17.07.2016 20:11):
//    public void setDay(Date day) {
//        this.day = day;
//    }
// --Commented out by Inspection STOP (17.07.2016 20:11)

    public final void setName(String name) {
        this.name = name;
    }

    public final void setPrice(String price) {
        this.price = price;
    }

    public final String getWeekDay() {
        return weekDay;
    }

// --Commented out by Inspection START (17.07.2016 20:11):
//    public void setWeekDay(String weeekDay) {
//        this.weekDay = weeekDay;
//    }
// --Commented out by Inspection STOP (17.07.2016 20:11)

    public final String getCategory() {
        return category;
    }

// --Commented out by Inspection START (17.07.2016 20:11):
//    public void setCategory(String category) {
//        this.category = category;
//    }
// --Commented out by Inspection STOP (17.07.2016 20:11)

// --Commented out by Inspection START (17.07.2016 20:11):
//    public String getTariff() {
//        return tariff;
//    }
// --Commented out by Inspection STOP (17.07.2016 20:11)

    public final void setTariff(String tariff) {
        this.tariff = tariff;
    }

    public final String getName() {
        return name;
    }

    public final String getPrice() {
        try{
            final Double number = Double.valueOf(price);
            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            return decimalFormat.format(number);
        }catch (final NumberFormatException nfe) {
            return price;
        }
    }
}
