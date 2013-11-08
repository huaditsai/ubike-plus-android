package huadi.ubikeplus.Marker;

import huadi.ubikeplus.Route.GoogleDirectionTask;
import huadi.ubikeplus.Route.GoogleDistanceMatrixTask;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class InfoWindowClicked
{
	Context context;
	GoogleMap map;
	Polyline directionPolyline;
	String myLocationString;	
	
	boolean isDrection = false;
	
	public InfoWindowClicked(Context context, GoogleMap map, Polyline directionPolyline, String myLocation)
	{
		this.context = context;
		this.map = map;
		this.directionPolyline = directionPolyline;
		myLocationString = myLocation;
	}
	
	public boolean ClickMarker(final String directionPoint) //點marker
	{		
		List<String> distanceMatrix = new ArrayList<String>();
		int cost = 0; //預計花費
		try
		{
			distanceMatrix = new GoogleDistanceMatrixTask().execute(myLocationString, directionPoint).get();
			int timeMin = Integer.parseInt(distanceMatrix.get(3)) / 60; //預計時間(走路)
			if (timeMin < 30)
				cost = 0;
			else if (timeMin >= 30 && timeMin < 240)
			{
				cost = timeMin / 30 * 10;
			}
			else if (timeMin >= 240 && timeMin < 480)
			{
				cost = timeMin / 30 * 20;
			}
			else if (timeMin >= 480)
			{
				cost = timeMin / 30 * 40;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		new AlertDialog.Builder(context).setTitle("路徑規劃 , 將此點設為路徑終點?").setMessage("與此地距離 " + distanceMatrix.get(0) + "\n走路花費時間 約 " + distanceMatrix.get(1) + "\n騎YouBike會員花費 約 " + cost + " 元").setPositiveButton("確定", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				new GoogleDirectionTask(map, directionPolyline).execute(myLocationString, directionPoint);
				isDrection = true;
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
			}
		}).show();
		
		return isDrection;
	}
}
