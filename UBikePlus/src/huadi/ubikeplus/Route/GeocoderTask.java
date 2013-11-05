package huadi.ubikeplus.Route;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class GeocoderTask extends AsyncTask<String, Integer, Address>
{
	private ProgressDialog dialog;
	
	Context context;
	GoogleMap map;
	Marker marker;

	LatLng latLng;

	public GeocoderTask(Context _Context, GoogleMap googleMap, Marker _Marker)
	{
		context = _Context;
		map = googleMap;
		marker = _Marker;
		
		dialog = new ProgressDialog(context);
	}

	@Override
	protected void onPreExecute() 
	{
		super.onPreExecute();
		dialog.setTitle("提示");
        dialog.setMessage("搜尋中...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        //dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();
    }
	
	@Override
	protected Address doInBackground(String... locationName)
	{
		Address address;
		int count = 0;
		while (true)
		{
			count++;
			address = searchLocationByName(locationName[0]);
			
			if(address == null && count > 5) //搜尋不到或超過5次 就說失敗了
			{
				break;
			}
			else if(address == null)
				continue;
			else
				break;
		}
		if( address != null || count <= 5 )
		{
			return address;
		}
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress)
	{
		super.onProgressUpdate(progress);
		//dialog.setProgress(progress[0]); //回報進度
	}

	@Override
	protected void onPostExecute(Address address)
	{
		if (address == null)
		{
			Toast.makeText(context, "搜尋失敗", Toast.LENGTH_SHORT).show();
			//marker.setVisible(false);
		}
		else
		{
			marker.setVisible(true);
			marker.setPosition(latLng);
			marker.setTitle(address.getAddressLine(1));
			marker.setSnippet(address.getAddressLine(0));			
			
			map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
		}
		
		if (dialog.isShowing())
			dialog.dismiss(); //停止
	}
	
	private Address searchLocationByName(String addressName)
	{
		Geocoder geocoder = new Geocoder(context, Locale.TAIWAN);
		List<Address> addresses = null;

		try
		{
			addresses = geocoder.getFromLocationName(addressName, 1); //得到一個地址
			Address address_send = null;
			for (Address address : addresses)
			{
				latLng = new LatLng(address.getLatitude(), address.getLongitude());
				address.getAddressLine(1);
				address_send = address;
			}
			return address_send;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
