/*
 * Copyright (c) 2017 Daniel Glaser
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

import java.util.ArrayList;

import de.hof.university.app.util.Define;
import de.hof.university.app.model.HofObject;

/**
 * Created by danie on 01.12.2016.
 */

public class Meals extends HofObject {
	private static final long serialVersionUID = Define.serialVersionUIDv1;

	private ArrayList<Meal> meals;
	private ArrayList<Meal> nextWeekMeals;
	private ArrayList<Meal> in3Weekmeal;

	public Meals() {
		super();
		this.meals = new ArrayList<>();
		this.nextWeekMeals = new ArrayList<>();
		this.in3Weekmeal = new ArrayList<>();
	}

	public ArrayList<Meal> getMeals() {
		return meals;
	}

	public ArrayList<Meal> getNextWeekMeals() {return nextWeekMeals;}

	public ArrayList<Meal> getIn3Weekmeal() {return in3Weekmeal;}

	public void setMeals(ArrayList<Meal> meals) {
		this.meals = meals;
	}

	public void setNextWeekMeals(ArrayList<Meal> meals){this.nextWeekMeals = meals;}

	public void setIn3Weekmeal(ArrayList<Meal> meals){this.in3Weekmeal = meals;}

}
