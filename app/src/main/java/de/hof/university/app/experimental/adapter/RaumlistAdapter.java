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

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.hof.university.app.R;
import de.hof.university.app.experimental.model.Level;
import de.hof.university.app.experimental.model.Raum;
import de.hof.university.app.experimental.model.Raumkategorie;
import de.hof.university.app.experimental.model.Suchdetails;

/**
 * Created by Lukas on 26.11.2015.
 */
public class RaumlistAdapter extends ArrayAdapter<Level> {

    public final static String TAG = "RaumlistAdapter";

    private final ArrayList<Level> items;
    private final LayoutInflater vi;


    public RaumlistAdapter(final Context context, final ArrayList<Level> items) {
        super(context, 0, items);

        this.items = items;
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public final View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        switch (items.get(position).getLevel()) {
            case 0:
                Raum raum = (Raum) items.get(position);
                if (raum != null) {
                    v = vi.inflate(R.layout.list_item_raum, null);

                    v.setOnClickListener(null);
                    v.setOnLongClickListener(null);
                    v.setLongClickable(false);

                    final TextView raumView = (TextView) v.findViewById(R.id.raumlist_left_text);
                    raumView.setText(raum.getName());
                }
                break;
            case 1:
                Raumkategorie category = (Raumkategorie) items.get(position);
                if (category != null) {
                    v = vi.inflate(R.layout.list_item_big, null);

                    v.setOnClickListener(null);
                    v.setOnLongClickListener(null);
                    v.setLongClickable(false);

                    final TextView raumView = (TextView) v.findViewById(R.id.list_item_big_text);
                    raumView.setText(category.getTitle());
                }
                break;
            case 2:
                Suchdetails details = (Suchdetails) items.get(position);
                if (details != null) {
                    v = vi.inflate(R.layout.list_item_medium, null);

                    v.setOnClickListener(null);
                    v.setOnLongClickListener(null);
                    v.setLongClickable(false);

                    final TextView raumView = (TextView) v.findViewById(R.id.list_item_medium_text);
                    //TODO AUSGABE formatieren
                    final String outputText = details.getDate() + '\n' + details.getTimeFrom() + '\n' + details.getTimeTo();
                    raumView.setText(outputText);
                }
                break;
            default:
                //TODO
                break;
        }

        //Raum raum = items.get(position);


        return v;
    }
}
