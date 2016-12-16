package de.hof.university.app.model.meal;

import java.util.ArrayList;

import de.hof.university.app.Util.Define;
import de.hof.university.app.model.HofObject;

/**
 * Created by danie on 01.12.2016.
 */

public class Meals extends HofObject {
	private static final long serialVersionUID = Define.serialVersionUID;
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
