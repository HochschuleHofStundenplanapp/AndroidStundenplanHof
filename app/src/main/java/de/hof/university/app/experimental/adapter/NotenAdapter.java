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

package de.hof.university.app.experimental.adapter;

/**
 * Created by Lukas on 05.07.2016.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.hof.university.app.R;
import de.hof.university.app.experimental.model.Noten;


/**
 * Created by Lukas on 26.11.2015.
 */
public class NotenAdapter extends ArrayAdapter<Noten> {

    public final static String TAG = "NotenAdapter";

    private final ArrayList<Noten> items;
    private final LayoutInflater vi;

    public NotenAdapter(final Context context, final ArrayList<Noten> items) {
        super(context, 0, items);

        this.items = items;

        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public final View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        Noten note = items.get(position);

        if (note != null) {
            v = vi.inflate(R.layout.notenbekanntgabe_item, null);

            v.setOnClickListener(null);
            v.setOnLongClickListener(null);
            v.setLongClickable(false);

            final TextView fachView = (TextView) v.findViewById(R.id.notenbekanntgabe_item_fach);
            fachView.setText(note.getFach());

            final TextView notenView = (TextView) v.findViewById(R.id.notenbekanntgabe_item_note);
            notenView.setText("");
            String strNote = note.getNote();
            switch (strNote) {
                case "erfolgreich":
                    notenView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_checked, 0);
                    break;
                case "nicht erfolgreich":
                    notenView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_unchecked, 0);
                    break;
                case "Korrektur noch nicht abgeschlossen":
                    notenView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_pen, 0);
                    break;
                default:
                    notenView.setText(strNote);
                    break;
            }
        }
        return v;
    }
}
