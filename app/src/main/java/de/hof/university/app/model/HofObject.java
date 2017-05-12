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

package de.hof.university.app.model;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import de.hof.university.app.Util.Define;
import de.hof.university.app.Util.Log;

/**
 * Created by Daniel on 01.12.2016.
 */

public class HofObject implements Serializable {
	public final static String TAG = "SaveObject";

	private Date lastSaved = null;

	public HofObject() {
	}

	public Date getLastSaved() {
		return lastSaved;
	}

	public void setLastSaved(final Date lastSaved) {
		this.lastSaved = lastSaved;
	}

}
