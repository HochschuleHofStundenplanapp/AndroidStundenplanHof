package de.hof.university.app.model.meal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import de.hof.university.app.model.SaveObject;

/**
 * Created by danie on 01.12.2016.
 */

public class Meals extends SaveObject {
	private ArrayList<Meal> meals;

	public Meals() {
		super();
		this.meals = new ArrayList<>();
	}

	public ArrayList<Meal> getMeals() {
		return meals;
	}

	public void setMeals(ArrayList<Meal> meals) {
		this.meals = meals;
	}

}
