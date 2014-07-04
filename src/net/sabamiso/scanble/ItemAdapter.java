package net.sabamiso.scanble;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ItemAdapter extends ArrayAdapter<Item> {
	private LayoutInflater inflater;

	public ItemAdapter(Context context, List<Item> list) {
		super(context, 0, list);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int p, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem, null);
		}

		Item item = (Item) getItem(p);

		TextView tv;
		tv = (TextView) convertView.findViewById(R.id.name);
		tv.setText(item.getName());

		tv = (TextView) convertView.findViewById(R.id.address);
		tv.setText(item.getAddress());

		tv = (TextView) convertView.findViewById(R.id.rssi);
		tv.setText(item.getRssi());

		return convertView;
	}
}