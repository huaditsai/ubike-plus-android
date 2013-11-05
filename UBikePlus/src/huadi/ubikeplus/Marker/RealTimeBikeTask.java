package huadi.ubikeplus.Marker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class RealTimeBikeTask extends AsyncTask<List<String>, Integer, List<String>> // <肚把计, 矪瞶い穝ざ把计, 矪瞶肚把计>
{
	// private final String bikeurl =
	// "http://its.taipei.gov.tw/aspx/Youbike.aspx?Mode=1"; //カユ硄Ы5だ牧穝(侣)
	List<String> info = new ArrayList<String>();

	Activity activity;
	Context context;
	GoogleMap map;
	List<Marker> markers;
	ProgressDialog dialog;

	String sno = ""; //0001
	String sarea = ""; //

	public RealTimeBikeTask(Activity activity, GoogleMap map, List<Marker> markers)
	{
		this.activity = activity;
		this.context = activity;
		this.map = map;
		this.markers = markers;
		dialog = new ProgressDialog(context);
	}

	@Override
	protected List<String> doInBackground(List<String>... params)
	{
		//		if (params.length < 0)
		//			return null;
		for (int i = 0; i < params[0].size(); i++)
		{
			sno = params[0].get(i);
			sarea = params[1].get(i);

			HttpURLConnection con = null;
			try
			{
				URL url = new URL("http://www.youbike.com.tw/info3b.php?sno=" + sno); // String.format("%04d",));
				con = (HttpURLConnection) url.openConnection();
				con.setReadTimeout(10000);
				con.setConnectTimeout(15000);
				con.setRequestMethod("GET");
				con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.2; en-GB; rv:1.9.2.9) Gecko/20100824 Firefox/3.6.9");
				con.setDoInput(true);
				con.connect();

				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

				String n, result = "";
				StringBuilder htmlContent = new StringBuilder();
				while ((n = reader.readLine()) != null)
					htmlContent.append(n);

				result = htmlContent.toString();

				String pattern;
				pattern = "sbi\\s*\\=\\s*'([0-9]+)?'.*sus\\s*\\=\\s*'([0-9]+)?';";
				Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
				Matcher m = p.matcher(result);
				while (m.find())
				{
					info.add(m.group(1) + "," + m.group(2));
				}
			}
			catch (Exception e)
			{
				Log.e("Exception", e.toString());
				info.add("代刚┪蝴い...");
			}
			finally
			{
				if (con != null)
					con.disconnect();
			}
		}

		return info;
	}

	@Override
	protected void onPostExecute(final List<String> results)
	{
		super.onPostExecute(results);

		if (dialog.isShowing())
			dialog.dismiss();

		for (int i = 0; i < results.size(); i++)
		{
			map.setInfoWindowAdapter(new MyInfoWindowAdapter(activity, markers.get(i).getTitle(), results.get(i), sarea));

			//if(results.size() == 1)
			markers.get(i).showInfoWindow();
		}

	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();

		//dialog.setTitle("穝");
		dialog.setMessage("戈穝い...");
		dialog.setIndeterminate(true);
		//dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setCancelable(false);
		//dialog.setMax(100);
		dialog.show();
	}

	@Override
	protected void onProgressUpdate(Integer... progress)
	{
		super.onProgressUpdate(progress);
		// if we get here, length is known, now set indeterminate to false
		//dialog.setIndeterminate(false);
		//dialog.setProgress(progress[0]);
	}
}
