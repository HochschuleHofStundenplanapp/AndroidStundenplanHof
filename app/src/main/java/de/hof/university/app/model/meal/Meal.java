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

package de.hof.university.app.model.meal;

import android.util.Log;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;
import de.hof.university.app.util.Define;

/**
 * Created by Lukas on 25.11.2015.
 */
public class Meal implements Serializable {
	private static final long serialVersionUID = Define.serialVersionUIDv1;

	final static String TAG = "Meal";
	
	private String name;
	private String price;
	private final String weekDay;
	//TODO was ist eine Kategorie?
	private final String category;
	private String tariff;
	private final Date day;
	// TODO welche Attribute sind hier gemeint?
	final private ArrayList<Integer> attributes;


	public Meal(final Date tag, final String weekDay, final String category, final String name) {
		super();
		
		junit.framework.Assert.assertTrue( !weekDay.isEmpty() );
		junit.framework.Assert.assertTrue( !category.isEmpty() );
		junit.framework.Assert.assertTrue( !name.isEmpty() );
		
		this.weekDay = weekDay;
		this.day = tag;
		this.category = category;
		this.name = name.replace("\\", "");
		this.attributes = new ArrayList<>();
	}


	public final Date getDay() {
		return day;
	}

	public final void setName(final String name) {
		this.name = name;
	}

	public final void setPrice(final String price) {
		this.price = price;
	}

	public final String getWeekDay() {
		return weekDay;
	}

	public final String getCategory() {
		return category;
	}

	public final void setTariff(final String tariff) {
		this.tariff = tariff;
	}

	public final String getName() {
	
		String result = this.name;
		String tmpAttributes = "";


		if ( !tmpAttributes.isEmpty() ) {
			result += " (" + tmpAttributes + ")";
		}
		return result;
	}

	public ArrayList<Integer> getAttributes(){
		return attributes;
	}

	public ArrayList<String> getParsedAttributes() {
		ArrayList<String> myAttributes = new ArrayList<>();

        for ( int a : attributes ) {
            switch (a) {
                case 1:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_hausgemacht));
                    break;
                case 2:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_Wild));
                    break;
                case 3:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_Geflügel));
                    break;
                case 4:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_regional));
                    break;
                case 5:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_Schwein));
                    break;
                case 6:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_Fisch));
                    break;
                case 7:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_vegetarisch));
                    break;
                case 8:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_Rind));
                    break;
                case 9:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_nachhaltigerFang));
                    break;
                case 10:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_vegan));
                    break;
                case 11:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_Lamm));
                    break;
                case 12:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_Meeresfruechte));
                    break;
                case 13:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_MensaVital));
                    break;
                case 14:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_nichtvegetarisch));
                    break;
                case 15:
                    myAttributes.add(MainActivity.getAppContext().getResources().getString(R.string.MEAL_Kraueterkueche));
                    break;
            }
        }
		return myAttributes;
	}

	//TODO stimmt das? Kann man doch weglassen?
	public final String getPrice() {
		try {
			final Double number = Double.valueOf(price);
			DecimalFormat decimalFormat = new DecimalFormat("#0.00");
			return decimalFormat.format(number);
		} catch ( final NumberFormatException nfe ) {
			return price;
		}
	}

	public void addAttribute(final String xmlText) {
		
		int aNumber = 0;
		try {
			aNumber = Integer.parseInt(xmlText);
		} catch ( final NumberFormatException e )
		{
			Log.e( TAG, "addAttibute: kein Integer: "+xmlText, e ) ;
		}
		
		attributes.add( aNumber );
	}
}
