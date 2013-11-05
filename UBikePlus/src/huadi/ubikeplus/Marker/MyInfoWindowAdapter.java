package huadi.ubikeplus.Marker;

import huadi.ubikeplus.R;
import android.app.Activity;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
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

			if (!snippet.equals("代刚┪蝴い..."))
			{
				SpannableString snippetText = new SpannableString(sarea + "\r\nó进 : " + snippet.split(",")[0] + "\r\n氨 : " + snippet.split(",")[1]);
				String bikeHeadString = sarea + "\r\nó进 : ";
				String stopHeadString = sarea + "\r\nó进 : " + snippet.split(",")[0] + "\r\n氨 : ";
				
				marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bike_pin_green));
				
				if (snippet.split(",")[0].equals("0"))
				{
					snippetText.setSpan(new ForegroundColorSpan(Color.RED), bikeHeadString.length(), bikeHeadString.length() + snippet.split(",")[0].length(), 0);
					marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bike_pin_orange));
				}
				else if (snippet.split(",")[1].equals("0"))
				{
					snippetText.setSpan(new ForegroundColorSpan(Color.RED), stopHeadString.length(), snippetText.length(), 0);
					marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bike_pin_red));
				}
				else if(snippet.split(",")[1].equals("null") && snippet.split(",")[1].equals("null"))
					marker.setVisible(false);

				info_snippet.setText(snippetText);
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
