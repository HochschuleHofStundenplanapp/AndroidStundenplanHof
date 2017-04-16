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
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.hof.university.app.R;
import de.hof.university.app.model.LastUpdated;
import de.hof.university.app.model.schedule.LectureChange;

/**
 *
 */
public final class ChangesAdapter extends ArrayAdapter<Object> {

    public final static String TAG = "ChangesAdapter";

    private final ArrayList<Object> items;
    private final LayoutInflater vi;

    public ChangesAdapter(final Context context, final ArrayList<Object> itemsParam) {
        super(context, 0, itemsParam);
        this.items = itemsParam;
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public final View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
        View v = convertView;
        final Object obj = items.get(position);

        if (obj instanceof LectureChange ) {

            final LectureChange lectureChange = (LectureChange) items.get(position);
            v = vi.inflate(R.layout.list_item_aenderung, null);

            v.setOnClickListener(null);
            v.setOnLongClickListener(null);
            v.setLongClickable(false);

            final TextView tvOldDate = (TextView) v.findViewById(R.id.aenderung_oldDate);
            final TextView tvNewDate = (TextView) v.findViewById(R.id.aenderung_newDate);
            final TextView tvDetails = (TextView) v.findViewById(R.id.aenderung_details);

            tvOldDate.setText(lectureChange.getOld());
            tvOldDate.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            tvNewDate.setText(lectureChange.getNew());
            tvDetails.setText(lectureChange.getDetails());

        } else if (obj instanceof LastUpdated) {
            final LastUpdated lastUpdated = (LastUpdated) obj;
            v = vi.inflate(R.layout.list_item_last_updated, null);
            final TextView text = (TextView) v.findViewById(R.id.list_item_last_updated);
            text.setText(lastUpdated.getTitle());

        }
        return v;
    }
}
