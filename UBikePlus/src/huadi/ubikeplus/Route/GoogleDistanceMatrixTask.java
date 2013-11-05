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

public class GoogleDistanceMatrixTask extends AsyncTask<String, Integer, List<String>>
{
	//http://maps.googleapis.com/maps/api/distancematrix/json?
	//origins=25.025729,121.543329&destinations=25.033039,121.548779&mode=walking&language=zh-TW&units=metric&sensor=false
	private final String mapAPI = "http://maps.googleapis.com/maps/api/distancematrix/json?" //Google Directions API
		+ "origins={0}"
		+ "&destinations={1}"
		+ "&mode=walking"
		+ "&language=zh-TW"		
		+ "&units=metric"
		+ "&sensor=true";

	private String from;
	private String desti;
	private List<String> elements = new ArrayList<String>();


	public GoogleDistanceMatrixTask()
	{

	}

	@Override
	protected List<String> doInBackground(String... params)
	{
		if (params.length < 0)
			return null;

		from = params[0];
		desti = params[1];

		String url = MessageFormat.format(mapAPI, from, desti);
		//Log.e("map", url);
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
				JSONArray elementsArray = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");
				String distanceText = elementsArray.getJSONObject(0).getJSONObject("distance").getString("text"); //距離
				String durationText = elementsArray.getJSONObject(0).getJSONObject("duration").getString("text"); //時間
				
				String distanceValue = elementsArray.getJSONObject(0).getJSONObject("distance").getString("value"); //距離(m)
				String durationValue = elementsArray.getJSONObject(0).getJSONObject("duration").getString("value"); //時間(s)
				
				elements.add(distanceText);
				elements.add(durationText);
				elements.add(distanceValue);
				elements.add(durationValue);
				
			}
		}
		catch (Exception e)
		{
			Log.e("map", e.toString());
		}

		return elements;
	}
	

	protected void onPostExecute(List<String> result)
	{		
		
	}

}
