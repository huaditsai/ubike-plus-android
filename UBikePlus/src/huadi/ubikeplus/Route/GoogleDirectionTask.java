package huadi.ubikeplus.Route;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

public class GoogleDirectionTask extends AsyncTask<String, Integer, List<LatLng>>
{
	private final String mapAPI = "http://maps.googleapis.com/maps/api/directions/json?" //Google Directions API
		+ "origin={0}"
		+ "&destination={1}"
		+ "&mode=walking"
		+ "&language=zh-TW"		
		+ "&units=metric"
		+ "&sensor=true";

	private String from;
	private String desti;
	private List<LatLng> latLngs = new ArrayList<LatLng>();

	GoogleMap map;
	Polyline directionPolyline;

	public GoogleDirectionTask(GoogleMap googleMap, Polyline pLine)
	{
		map = googleMap;
		directionPolyline = pLine;
	}

	@Override
	protected List<LatLng> doInBackground(String... params)
	{
		if (params.length < 0)
			return null;

		from = params[0];
		desti = params[1];

		String url = MessageFormat.format(mapAPI, from, desti);
		//Log.i("map", url);
		HttpGet get = new HttpGet(url);
		String strResult = "";

		try
		{
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
			HttpClient httpClient = new DefaultHttpClient(httpParameters);

			HttpResponse httpResponse = null;
			httpResponse = httpClient.execute(get);

			if (httpResponse.getStatusLine().getStatusCode() == 200)//判斷網路連接是否成功
			{
				strResult = EntityUtils.toString(httpResponse.getEntity());
				//Log.e("strResult", strResult);

				JSONObject jsonObject = new JSONObject(strResult);
				JSONArray routeObject = jsonObject.getJSONArray("routes");//route 會指出具名的路線 (例如「US 101」)
				String polyline = routeObject.getJSONObject(0).getJSONObject("overview_polyline").getString("points");

				if (polyline.length() > 0)
				{
					DecodePolylines(polyline);
				}
			}
		}
		catch (Exception e)
		{
			Log.e("map", e.toString());
		}

		return latLngs;
	}

	//https://developers.google.com/maps/documentation/utilities/polylinealgorithm?hl=zh-TW
	private void DecodePolylines(String poly)//對類似這樣 myywCkv~dVQzSIlDA~DEvIMlPAvAf@@ 的東西解碼
	{
		int len = poly.length();
		int index = 0;
		int lat = 0;//緯度
		int lng = 0;//經度

		while (index < len)
		{
			int b, shift = 0, result = 0;
			do
			{
				b = poly.charAt(index++) - 63;
				result |= (b & 0x1f) << shift; //轉為2進位 且 每 5 個位元分成一個區塊,前後順序也順便顛倒了
				shift += 5; //每 5 個位元分成一個區塊
			}
			while (b >= 0x20); //0x20 的2進位=100000

			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1)); //向右移一位元,1的補數(1,0相反)
			lat += dlat;

			shift = 0;
			result = 0;

			do
			{
				b = poly.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			}
			while (b >= 0x20);

			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
			latLngs.add(p);
		}
	}

	protected void onPostExecute(List<LatLng> points)
	{		
		if (points.size() > 0)
		{			
			directionPolyline.setPoints(points);
		}
	}

}
