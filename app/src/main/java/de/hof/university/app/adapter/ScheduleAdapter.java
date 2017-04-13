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
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.hof.university.app.R;
import de.hof.university.app.model.BigListItem;
import de.hof.university.app.model.LastUpdated;
import de.hof.university.app.model.schedule.LectureItem;

/**
 * Created by Lars on 29.11.2015.
 */
public final class ScheduleAdapter extends ArrayAdapter<Object> {

    public final static String TAG = "ScheduleAdapter";

    private final ArrayList<Object> items;
    private final LayoutInflater vi;

    public ScheduleAdapter(final Context context, final ArrayList<Object> items) {
        super(context, 0, items);

        this.items = items;
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public final View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
        View v = convertView;

        final Object obj = items.get(position);
        if (obj != null) {
            if (obj instanceof LectureItem ) {
                final LectureItem st = (LectureItem) obj;
                v = vi.inflate(R.layout.list_item_stundenplan, null);
                final TextView tvTime = (TextView) v.findViewById(R.id.stundenplan_time);
                final TextView tvRoom = (TextView) v.findViewById(R.id.stundenplan_raum);
                final TextView tvDetails = (TextView) v.findViewById(R.id.stundenplan_details);
                tvTime.setText(st.getTime());
                tvRoom.setText(st.getRoom());
                tvDetails.setText(st.getDetails());

            } else if (obj instanceof BigListItem) {
                final BigListItem tag = (BigListItem) obj;
                v = vi.inflate(R.layout.list_item_big, null);
                final TextView sectionView = (TextView) v.findViewById(R.id.list_item_big_text);
                sectionView.setText(tag.getTitle());

            } else if (obj instanceof LastUpdated) {
                final LastUpdated lastUpdated = (LastUpdated) obj;
                v = vi.inflate(R.layout.list_item_last_updated, null);
                final TextView text = (TextView) v.findViewById(R.id.list_item_last_updated);
                text.setText(lastUpdated.getTitle());

            }
        }
        return v;
    }

}
