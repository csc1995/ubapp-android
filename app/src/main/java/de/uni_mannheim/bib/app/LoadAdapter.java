/*
 * Copyright (C) 2014 Universitätsbibliothek Mannheim
 *
 * Author:
 *    Universitätsbibliothek Mannheim <sysadmin@bib.uni-mannheim.de>
 *    Last modified on 2016-03-15
 * 
 * 
 * This is free software licensed under the terms of the GNU GPL, 
 * version 3, or (at your option) any later version.
 * See <http://www.gnu.org/licenses/> for more details.
 *
 *
 * Turns list content into a structured (xml formatted) list.
 * Is used by LoadActivity.
 * 
 * 
 */

package de.uni_mannheim.bib.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class LoadAdapter extends BaseAdapter {

	private boolean log_enabled = false;

	String[] loads;
	String[] resids;
	String[] names;

	Context context;

	private static LayoutInflater inflater = null;

	public LoadAdapter(LoadActivity loadActivity, String[] loadList,
			String[] residList, String[] nameList) {

		// Log Message
		if (log_enabled) {
			Log.e("LoadAdapter", "constructed");
		}

		context = loadActivity;
		loads = loadList;
		resids = residList;
		names = nameList;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return names.length;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public class Holder {
		TextView tv_load;
		ImageView iv_signal;
		TextView tv_name;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		Holder holder = new Holder();
		View rowView;

		rowView = inflater.inflate(R.layout.load_list, null);
		holder.tv_load = (TextView) rowView.findViewById(R.id.textView1);
		holder.iv_signal = (ImageView) rowView.findViewById(R.id.imageView1);
		holder.tv_name = (TextView) rowView.findViewById(R.id.textView2);

		holder.tv_load.setText(addEmptyChars(loads[position]) + " %  ");
		holder.iv_signal.setBackgroundResource(Integer
				.valueOf(resids[position]));
		// holder.tv_name.setText(names[position]);

		// Log.v("Logging", "Position: " + position + " Name: " + names[position]);

		switch(position) {
			case 0: holder.tv_name.setText(R.string.free_seats_activity_label_schneckenhof);
				break;
			case 1: holder.tv_name.setText(R.string.free_seats_activity_label_learningcenter);
				break;
			case 2: holder.tv_name.setText(R.string.free_seats_activity_label_schneckenhofsued);
				break;
			case 3: holder.tv_name.setText(R.string.free_seats_activity_label_ehrenhof);
				break;
			case 4: holder.tv_name.setText(R.string.free_seats_activity_label_a3);
				break;
			case 5: holder.tv_name.setText(R.string.free_seats_activity_label_a5);
				break;
			default: holder.tv_name.setText("undefined");
				break;
		}

		return rowView;
	}
	
	protected String addEmptyChars(String workload) {
		// Function that sums up load digits to 3 chars
		
		String load = "";
		
		if(Integer.valueOf(workload)<100) {
			load = "  "; 
		} 
		
		if(Integer.valueOf(workload)<10) {
			load = "    "; 
		}
		
		load += String.valueOf(workload);
		
		return load;
	}

}