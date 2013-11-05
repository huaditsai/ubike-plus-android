package huadi.ubikeplus;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BikeListAdapter extends BaseAdapter
{
	Context context;
	ArrayList<BikeList> list;
	private LayoutInflater inflater;

	public BikeListAdapter(Context context, ArrayList<BikeList> list)
	{
		this.context = context;
		this.list = list;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount()
	{
		return list.size();
	}

	@Override
	public BikeList getItem(int position)
	{
		return list.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		Holder holder;

		if (convertView == null)
		{
			holder = new Holder();
			convertView = inflater.inflate(R.layout.bike_item_listview, null);
			holder.img_pin = (ImageView) convertView.findViewById(R.id.item_img_pin);
			holder.txt_title = (TextView) convertView.findViewById(R.id.item_txt_title);
			holder.txt_area = (TextView) convertView.findViewById(R.id.item_txt_area);
			holder.txt_bike = (TextView) convertView.findViewById(R.id.item_txt_bike);
			holder.txt_stop = (TextView) convertView.findViewById(R.id.item_txt_stop);
			holder.txt_distance = (TextView) convertView.findViewById(R.id.item_txt_distance);
			holder.txt_time = (TextView) convertView.findViewById(R.id.item_txt_time);

			convertView.setTag(holder);
		}
		else
		{
			holder = (Holder) convertView.getTag();
		}
		holder.img_pin.setImageResource(list.get(position).img_pin);
		holder.txt_title.setText(list.get(position).txt_title);
		holder.txt_title.getPaint().setFakeBoldText(true);		
		
		holder.txt_area.setText(list.get(position).txt_area);
		
		holder.txt_bike.setText(list.get(position).txt_bike);
		if(list.get(position).txt_bike.equals("0"))
			holder.txt_bike.setTextColor(Color.RED);
		else
			holder.txt_bike.setTextColor(0xff003399);
		
		holder.txt_stop.setText(list.get(position).txt_stop);
		if(list.get(position).txt_stop.equals("0"))
			holder.txt_stop.setTextColor(Color.RED);
		else
			holder.txt_stop.setTextColor(0xff003399);
		
		holder.txt_distance.setText(list.get(position).txt_distance);
		holder.txt_time.setText(list.get(position).txt_time);

		return convertView;
	}

	protected class Holder
	{
		ImageView img_pin;
		TextView txt_title;
		TextView txt_area;
		TextView txt_bike;
		TextView txt_stop;
		TextView txt_distance;
		TextView txt_time;
	}
}
