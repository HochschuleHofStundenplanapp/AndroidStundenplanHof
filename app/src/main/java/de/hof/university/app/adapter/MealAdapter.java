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

package de.hof.university.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.hof.university.app.R;
import de.hof.university.app.model.BigListItem;
import de.hof.university.app.model.MediumListItem;
import de.hof.university.app.model.meal.Meal;

/**
 * Created by Lukas on 25.11.2015.
 */
public class MealAdapter extends ArrayAdapter<Object> {

    private final ArrayList<Object> items;
    private final LayoutInflater vi;

    public MealAdapter(final Context context, final ArrayList<Object> items) {
        super(context, 0, items);

        this.items = items;
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        final Object obj = items.get(position);
        if (obj != null) {
            if (obj instanceof BigListItem) {
                final BigListItem st = (BigListItem) obj;
                v = vi.inflate(R.layout.list_item_big, null);

                v.setOnClickListener(null);
                v.setOnLongClickListener(null);
                v.setLongClickable(false);

                final TextView sectionView = (TextView) v.findViewById(R.id.list_item_big_text);
                sectionView.setText(st.getTitle());

            } else if (obj instanceof MediumListItem) {
                final MediumListItem sk = (MediumListItem) obj;
                v = vi.inflate(R.layout.list_item_medium, null);

                v.setOnClickListener(null);
                v.setOnLongClickListener(null);
                v.setLongClickable(false);

                final TextView sectionView = (TextView) v.findViewById(R.id.list_item_medium_text);
                sectionView.setText(sk.getTitle());
            } else {
                final Meal sp = (Meal) obj;
                v = vi.inflate(R.layout.list_item_speiseplan, null);
                final TextView title = (TextView) v.findViewById(R.id.list_item_level2_title);
                final TextView subtitle = (TextView) v.findViewById(R.id.list_item_level2_price);


                if (title != null) {
                    title.setText(sp.getName());
                }
                if (subtitle != null) {
                    subtitle.setText(sp.getPrice());
                }
            }
        }

        return v;
    }

}
