package huadi.ubikeplus.Marker;

import huadi.ubikeplus.R;
import android.app.Activity;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

public class MyInfoWindowAdapter implements InfoWindowAdapter
{
	Activity activity;
	String title;
	String snippet;
	boolean isBikePin;
	String sarea;

	public MyInfoWindowAdapter(Activity activity, String title, String snippet, String sarea)
	{
		this.activity = activity;
		this.title = title;
		this.snippet = snippet;
		this.sarea = sarea;
		isBikePin = true;
	}

	public MyInfoWindowAdapter(Activity activity, String title, String snippet)
	{
		this.activity = activity;
		this.title = title;
		this.snippet = snippet;
		isBikePin = false;
	}

	@Override
	public View getInfoContents(Marker arg0)
	{
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker)
	{
		View v = activity.getLayoutInflater().inflate(R.layout.custom_info_window, null);

		if (!isBikePin)
		{
			//title = marker.getTitle();
			TextView info_title = (TextView) v.findViewById(R.id.info_title);
			if (title != null)
				info_title.setText(title);
			else
				info_title.setText("");

			//snippet = marker.getSnippet();
			TextView info_snippet = (TextView) v.findViewById(R.id.info_snippet);
			if (snippet != null)
			{
				info_snippet.setText(snippet);
			}
			else
				info_snippet.setText("");
		}
		else
		{
			title = marker.getTitle();
			TextView info_title = (TextView) v.findViewById(R.id.info_title);
			if (title != null)
			{
				// Spannable string allows us to edit the formatting of the text.
				SpannableString titleText = new SpannableString(title);
				//titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
				info_title.setText(titleText);
			}
			else
			{
				info_title.setText("");
			}

			TextView info_snippet = (TextView) v.findViewById(R.id.info_snippet);
			ImageView myinfo_parking = (ImageView) v.findViewById(R.id.myinfo_parking);
			ImageView myinfo_bike = (ImageView) v.findViewById(R.id.myinfo_bike);
			TextView myinfo_parking_text = (TextView) v.findViewById(R.id.myinfo_parking_text);
			TextView myinfo_bike_text = (TextView) v.findViewById(R.id.myinfo_bike_text);

			if (!snippet.equals("代刚┪蝴い..."))
			{
				SpannableString snippetText = new SpannableString(sarea + "\r\nó进 : " + snippet.split(",")[0] + "\r\n氨 : " + snippet.split(",")[1]);
				String bikeHeadString = sarea + "\r\nó进 : ";
				String stopHeadString = sarea + "\r\nó进 : " + snippet.split(",")[0] + "\r\n氨 : ";
								
				marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_havebike));
				
				if (snippet.split(",")[0].equals("0"))
				{
					snippetText.setSpan(new ForegroundColorSpan(Color.RED), bikeHeadString.length(), bikeHeadString.length() + snippet.split(",")[0].length(), 0);
					myinfo_parking_text.setTextColor(Color.RED);
					marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_nobike));
				}
				else if (snippet.split(",")[1].equals("0"))
				{
					snippetText.setSpan(new ForegroundColorSpan(Color.RED), stopHeadString.length(), snippetText.length(), 0);
					myinfo_bike_text.setTextColor(Color.RED);
					marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_nopark));
				}
				else if(snippet.split(",")[1].equals("null") && snippet.split(",")[1].equals("null"))
					marker.setVisible(false);

//				info_snippet.setText(snippetText);
				info_snippet.setText(sarea);
				myinfo_parking_text.setText(snippet.split(",")[0]);
				myinfo_bike_text.setText(snippet.split(",")[1]);
			}
			else
			{
				SpannableString snippetText = new SpannableString(snippet);
				snippetText.setSpan(new ForegroundColorSpan(Color.RED), 0, snippetText.length(), 0);
				info_snippet.setText(marker.getSnippet() + "\r\n" + snippetText);
			}
		}
		return v;
	}

}
