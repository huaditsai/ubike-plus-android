package huadi.ubikeplus.Drawer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import huadi.ubikeplus.R;

public class DrawerItemAdapter extends ArrayAdapter<String>
{
	private final Context context;
	private final String[] Ids;
	private final int rowResourceId;

	public DrawerItemAdapter(Context context, int textViewResourceId, String[] objects)
	{
		super(context, textViewResourceId, objects);

		this.context = context;
		this.Ids = objects;
		this.rowResourceId = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(rowResourceId, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.textView);

		int id = Integer.parseInt(Ids[position]);
		int icon = DrawerListMoedel.GetbyId(id).Icon;

		textView.setText(DrawerListMoedel.GetbyId(id).Name);
		
		textView.setCompoundDrawablesWithIntrinsicBounds(0, //left
			icon, //top
			0, //right
			0);//bottom
	
		return rowView;

	}

}
