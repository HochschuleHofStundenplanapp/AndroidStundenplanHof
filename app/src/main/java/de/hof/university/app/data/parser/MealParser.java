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

package de.hof.university.app.data.parser;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hof.university.app.BuildConfig;
import de.hof.university.app.model.meal.Meal;

/**
 * Created by larsg on 17.06.2016.
 */
final public class MealParser implements Parser<Meal> {

	public final static String TAG = "MealParser";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
    private Integer tariff;


    public MealParser() {
    }

    @Override
    public final ArrayList<Meal> parse(String[] params) {
        ArrayList<Meal> result = new ArrayList<>();
        if (params.length == 2) {
            String xmlString = params[0];
            try {
                tariff = Integer.valueOf(params[1]);
            } catch (NumberFormatException nfe) {
                tariff = 1;
            }

            //Escape, if String is empty
            if (xmlString.isEmpty()) {
                return result;
            }

            try {
                XmlPullParser xmlParser = Xml.newPullParser();
                xmlParser.setInput(new StringReader(xmlString));
                Meal meal = null;
                String xmlText = "";
                String xmlCategory = "";
                Date xmlDay = new Date();
                String xmlWeekday = "";
                Integer xmlTariff = 0;
                String xmlGroup = "";

                int eventType = xmlParser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String xmlTag = xmlParser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (xmlTag.equalsIgnoreCase("gericht")) {
                                // create a new instance of employee
                                meal = new Meal(xmlDay, xmlWeekday, xmlCategory, xmlParser.getAttributeValue(null, "name"));
//                                Log.d(TAG, xmlParser.getAttributeValue(null, "name"));
                            }
                            if (xmlTag.equalsIgnoreCase("kategorie")) {
                                xmlCategory = xmlParser.getAttributeValue(null, "name");
                                if (xmlCategory.equalsIgnoreCase("Salat_suppe")) {
                                    xmlCategory = "Salat";
                                }
//                                Log.d(TAG, xmlParser.getAttributeValue(null, "name"));
                            }
                            if (xmlTag.equalsIgnoreCase("tag")) {
                                try {
                                    xmlDay = MealParser.sdf.parse(xmlParser.getAttributeValue(null, "datum"));
                                } catch (ParseException e) {
                                    if (BuildConfig.DEBUG) e.printStackTrace();
                                }
                                xmlWeekday = xmlParser.getAttributeValue(null, "wochentag");
//                                Log.d(TAG, xmlParser.getAttributeValue(null, "wochentag"));
                            }
                            if (xmlTag.equalsIgnoreCase("preis")) {
                                try {
                                    xmlTariff = Integer.valueOf(xmlParser.getAttributeValue(null, "gruppenId"));
                                } catch (NumberFormatException nfe) {
                                    xmlTariff = 0;
                                }
                                xmlGroup = xmlParser.getAttributeValue(null, "gruppe");
                            }
                            break;

                        case XmlPullParser.TEXT:
                            xmlText = xmlParser.getText();
                            break;

                        case XmlPullParser.END_TAG:
                            if (xmlTag.equalsIgnoreCase("preis")) {
                                if (tariff == xmlTariff) {
                                    assert meal != null;
                                    meal.setPrice(xmlText);
                                    meal.setTariff(xmlGroup);
                                    result.add(meal);
                                }
                            } else if (xmlTag.equalsIgnoreCase("gerichtAttribut")) {
                                assert meal != null;
                                meal.addAttribute(xmlText);
                            }
                            break;

                        default:
                            break;
                    }
                    eventType = xmlParser.next();
                }
            } catch (final XmlPullParserException | IOException e) {
                if (BuildConfig.DEBUG) e.printStackTrace();
                result.clear();
            }
        }
        return result;
    }

    /* JSON-Objekt in der Schnittstellen Version 2 deaktiviert
    @Override
    public ArrayList<Meal> parse(String[] params) {
        ArrayList<Meal> result = new ArrayList<>();
        if(params.length==2) {
            String jsonString = params[0];
            tariff=params[1];
            //Escape, if String is empty
            if(jsonString.isEmpty())
                return result;
            try {
                JSONArray jsonArray = new JSONObject(jsonString).getJSONArray("meal");
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject tmpObject = jsonArray.getJSONObject(i);
                    result.addAll(convertJsonObject(tmpObject));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    */

    /* JSON-Objekt in der Schnittstellen Version 2 deaktiviert
    private ArrayList<Meal> convertJsonObject(JSONObject jsonObject) {
        ArrayList<Meal> result = new ArrayList<>();

        try {
            String day = jsonObject.getString("day");
            String date = jsonObject.getString("date");
            String category = "";
            String name = "";
            String price = "";

            JSONArray dayArray = jsonObject.getJSONArray("categories");

            for (int i = 0; i < dayArray.length(); ++i) {
                category = dayArray.getJSONObject(i).getString("name");
                JSONArray mealArray = dayArray.getJSONObject(i).getJSONArray("meals");
                for(int j = 0; j<mealArray.length(); ++j){
                    name = mealArray.getJSONObject(j).getString("name");
                    JSONArray pricesArray = mealArray.getJSONObject(j).getJSONArray("prices");
                    for(int k = 0; k<pricesArray.length(); ++k){
                        if(pricesArray.getJSONObject(k).getString("groupId").equals(tariff)) {
                            price = pricesArray.getJSONObject(k).getString("price");
                            if (price.length() < 4)
                                price = price + "0";
                            price = price + " €";
                        }
                    }
                    result.add(new Meal(name, price, day, category, tariff, sdf.parse(date)));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }
    */
}
