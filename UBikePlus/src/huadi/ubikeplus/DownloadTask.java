package huadi.ubikeplus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

public class DownloadTask extends AsyncTask<String, Integer, String>
{
	private Context context;
	GoogleMap map;
	String fileName;
	boolean isAddPoint;

	Runnable runnable;
	ProgressDialog dialog;
	
	String rootPath;

	public DownloadTask(Context context, GoogleMap map, String fileName, boolean isAddPoint)
	{
		this.context = context;
		this.map = map;
		this.fileName = fileName;
		this.isAddPoint = isAddPoint;
		dialog = new ProgressDialog(context);
		rootPath = Environment.getExternalStorageDirectory() + "/UBikePlus/";
	}

	@Override
	protected String doInBackground(String... sUrl)
	{		
		try
		{
			InputStream input = null;
			OutputStream output = null;
			HttpURLConnection connection = null;
			try
			{
				URL url = new URL(sUrl[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();

				// expect HTTP 200 OK, so we don't mistakenly save error report 
				// instead of the file
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
					return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();

				// this will be useful to display download percentage
				// might be -1: server did not report the length
				int fileLength = connection.getContentLength();

				// download the file
				input = connection.getInputStream();
				File folder = new File(Environment.getExternalStorageDirectory(), "/UBikePlus");
				if(!folder.exists())
					folder.mkdir();
				String filePath = rootPath + fileName;
				File file = new File(filePath);
				output = new FileOutputStream(file);

				byte data[] = new byte[4096];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1)
				{					
					total += count;
					// publishing the progress....
					if (fileLength > 0) // only if total length is known
						publishProgress((int) (total * 100 / fileLength));
					output.write(data, 0, count);
				}
			}
			catch (Exception e)
			{
				return e.toString();
			}
			finally
			{
				try
				{
					if (output != null)
						output.close();
					if (input != null)
						input.close();
				}
				catch (IOException ignored)
				{
				}

				if (connection != null)
					connection.disconnect();
			}
		}
		finally
		{
			//			wl.release();
		}
		return null;
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		dialog.setCancelable(false);
	}

	@Override
	protected void onProgressUpdate(Integer... progress)
	{
		super.onProgressUpdate(progress);
		// if we get here, length is known, now set indeterminate to false
		dialog.setIndeterminate(false);
		dialog.setProgress(progress[0]);
	}

	@Override
	protected void onPostExecute(String result)
	{
		if (dialog.isShowing())
			dialog.dismiss();
		
		SharedPreferences settings = context.getSharedPreferences("Preference", 0);
		
		if (result != null)
		{
			Toast.makeText(context, "租車站點資料下載失敗 ", Toast.LENGTH_LONG).show();
			if(isAddPoint)
				settings.edit().putInt("UpdateTime", 0).commit();
		}
		else
		{
			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR); //民國
			int month = calendar.get(Calendar.MONTH) + 1; //Calendar.MONTH 從0開始...
			int day = calendar.get(Calendar.DATE);
			int currentDateTime = Integer.parseInt(String.format("%d%02d%02d", year, month, day));
			settings.edit().putInt("UpdateTime", currentDateTime).commit(); //紀錄更新日期
			
			//Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show();
			if(isAddPoint) //若是下載站點資訊才增加點
			try
			{
				String filePath = rootPath + fileName;
				final File file = new File(filePath);
				final FileReader fr = new FileReader(file);						
				final BufferedReader br = new BufferedReader(fr);
				
				final Handler handler = new Handler();
				runnable = new Runnable()
				{
					@Override
					public void run()
					{
						String line = "";
						try
						{
							if ((line = br.readLine()) != null)
							{
								if(!line.split(",")[0].equals("0000")) //跟0001合併了
								{
									map.addMarker(new MarkerOptions()
									.position(new LatLng(Double.parseDouble(line.split(",")[1]), Double.parseDouble(line.split(",")[2])))
									.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_havebike))
									.title(line.split(",")[3])
									.snippet(line.split(",")[4]));//0代號 5區 6英區 7 英位址
								}
							}
							else
							{
								handler.removeCallbacks(runnable);
//								fr.close();
//								br.close();
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

						handler.postDelayed(this, 10);
					}
				};
				handler.postDelayed(runnable, 10);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}