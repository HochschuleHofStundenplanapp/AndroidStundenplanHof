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

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hof.university.app.Util.Define;

/**
 * Created by Lukas on 25.11.2015.
 */
public class Meal implements Serializable {
	private static final long serialVersionUID = Define.serialVersionUIDv1;

	private String name;
	private String price;
	private final String weekDay;
	//TODO was ist eine Kategorie?
	private final String category;
	private String tariff;
	private final Date day;
	// TODO welche Attribute sind hier gemeint?
	final private ArrayList<Integer> attributes;


	public Meal(final Date tag, final String weeekDay, final String category, final String name) {
		this.weekDay = weeekDay;
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
		String result;
		result = name;
		String tmpAttributes = "";
		for ( int a : attributes
				) {
			if ( !tmpAttributes.isEmpty() ) {
				tmpAttributes += ", ";
			}
			if ( a == 1 ) {                          // TODO Schöner machen nicht hardcoded
				tmpAttributes += "hausgemacht";
			} else if ( a == 2 ) {
				tmpAttributes += "Wild";
			} else if ( a == 3 ) {
				tmpAttributes += "Geflügel";
			} else if ( a == 4 ) {
				tmpAttributes += "regional";
			} else if ( a == 5 ) {
				tmpAttributes += "Schwein";
			} else if ( a == 6 ) {
				tmpAttributes += "Fisch";
			} else if ( a == 7 ) {
				tmpAttributes += "vegetarisch";
			} else if ( a == 8 ) {
				tmpAttributes += "Rind";
			} else if ( a == 9 ) {
				tmpAttributes += "nachhaltiger Fang";
			} else if ( a == 10 ) {
				tmpAttributes += "vegan";
			} else if ( a == 11 ) {
				tmpAttributes += "Lamm";
			} else if ( a == 12 ) {
				tmpAttributes += "Meeresfrüchte";
			} else if ( a == 13 ) {
				tmpAttributes += "Mensa Vital";
			} else if ( a == 14 ) {
				tmpAttributes += "nicht vegetarisch";
			} else if ( a == 15 ) {
				tmpAttributes += "Kräuterküche";
			}
		}
		if ( !tmpAttributes.isEmpty() ) {
			result += " (" + tmpAttributes + ")";
		}
		return result;
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

		attributes.add(Integer.parseInt(xmlText));
	}
}
