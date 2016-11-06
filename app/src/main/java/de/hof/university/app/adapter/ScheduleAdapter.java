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
import de.hof.university.app.model.schedule.Schedule;

/**
 * Created by Lars on 29.11.2015.
 */
public class ScheduleAdapter extends ArrayAdapter<Object> {

    // --Commented out by Inspection (17.07.2016 20:13):private final Context context;
    private final ArrayList<Object> items;
    private final LayoutInflater vi;

    public ScheduleAdapter(final Context context, final ArrayList<Object> items) {
        super(context, 0,items);

        this.items = items;
        // TODO möglich Lösung für einen Absturz mit java.lang.IllegalStateException
        this.notifyDataSetChanged();
        vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public final View getView(final int position, final View convertView, final ViewGroup parent) {
        View v = convertView;

        final Object obj = items.get(position);
        if (obj != null) {
            if(obj instanceof Schedule){
                Schedule st = (Schedule) obj;
                v = vi.inflate(R.layout.list_item_stundenplan, null);
                final TextView tvTime = (TextView) v.findViewById(R.id.stundenplan_time);
                final TextView tvRoom = (TextView) v.findViewById(R.id.stundenplan_raum);
                final TextView tvDetails = (TextView) v.findViewById(R.id.stundenplan_details);
                tvTime.setText(st.getTime());
                tvRoom.setText(st.getRoom());
                tvDetails.setText(st.getDetails());

            } else if(obj instanceof BigListItem){
                BigListItem tag = (BigListItem) obj;
                v = vi.inflate(R.layout.list_item_big, null);
                final TextView sectionView = (TextView) v.findViewById(R.id.list_item_big_text);
                sectionView.setText(tag.getTitle());
            }
        }
        return v;
    }

}
