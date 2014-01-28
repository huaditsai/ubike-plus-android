package huadi.ubikeplus.Marker;

import huadi.ubikeplus.BikeList;
import huadi.ubikeplus.BikeListAdapter;
import huadi.ubikeplus.MainActivity;
import huadi.ubikeplus.R;
import huadi.ubikeplus.Route.GoogleDistanceMatrixTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class RealTimeBikeTask extends AsyncTask<List<String>, Integer, List<String>> // <傳入參數, 處理中更新介面參數, 處理後傳出參數>
{
	// private final String bikeurl =
	// "http://its.taipei.gov.tw/aspx/Youbike.aspx?Mode=1"; //台北市交通局5分鐘更新(舊)
	List<String> info = new ArrayList<String>();

	Activity activity;
	Context context;
	GoogleMap map;
	List<Marker> markers;
	Polyline directionPolyline;
	String myLocationString;

	ProgressDialog dialog;

	String sno = ""; //0001
	String sarea = ""; //地址

	List<String> sareas = new ArrayList<String>();
	ArrayList<BikeList> bikeLists = new ArrayList<BikeList>(); //站點列表
	boolean isShowList = false;

	public RealTimeBikeTask(Activity activity, GoogleMap map, List<Marker> markers)
	{
		this.activity = activity;
		this.context = activity;
		this.map = map;
		this.markers = markers;

		isShowList = false;

		dialog = new ProgressDialog(context);
	}

	public RealTimeBikeTask(Activity activity, GoogleMap map, List<Marker> markers, Polyline directionPolyline,
		String myLocationString)
	{
		this.activity = activity;
		this.context = activity;
		this.map = map;
		this.markers = markers;
		this.directionPolyline = directionPolyline;
		this.myLocationString = myLocationString;

		isShowList = true;

		dialog = new ProgressDialog(context);
	}

	@Override
	protected List<String> doInBackground(List<String>... params)
	{
		sareas = params[1];
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
					info.add(m.group(1) + "," + m.group(2)); //車輛, 車位
				}
			}
			catch (Exception e)
			{
				Log.e("Exception", e.toString());
				info.add("測試或維修中...");
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

		if (isShowList)
		{
			bikeLists = initData(markers, results, sareas);
			ActionAlertDialog();
		}

	}

	protected void ActionAlertDialog() //彈出站點列表
	{
		//ArrayList<PP> list = initData();
		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.bike_listview, (ViewGroup) activity.findViewById(R.id.layout_myview));

		ListView myListView = (ListView) layout.findViewById(R.id.mylistview);
		BikeListAdapter adapter = new BikeListAdapter(context, bikeLists);
		myListView.setAdapter(adapter);
		myListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

		DisplayMetrics dm = new DisplayMetrics(); // 建立一個DisplayMetrics物件
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm); // 取得裝置的資訊
		int Width = dm.widthPixels;
		int Height = dm.heightPixels;
		LayoutParams lp = (LayoutParams) myListView.getLayoutParams();
		lp.width = (int) (Width * 0.85);
		lp.height = (int) (Height * 0.8);
		myListView.setLayoutParams(lp);

		myListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> a, View v, int which, long id)
			{
				String directionPoint = bikeLists.get(which).marker.getPosition().latitude + "," + bikeLists.get(which).marker.getPosition().longitude;
				InfoWindowClicked infoWindowClicked = new InfoWindowClicked(context, map, directionPolyline, myLocationString);
				infoWindowClicked.ClickMarker(directionPoint);
			}
		});

		builder = new AlertDialog.Builder(context);
		builder.setNegativeButton("CLOSE", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.setView(layout);
		builder.setCancelable(false);

		alertDialog = builder.create();
		alertDialog.show();
	}

	protected ArrayList<BikeList> initData(List<Marker> markers, List<String> info, List<String> sareas) //站點列表的內容
	{
		ArrayList<BikeList> list = new ArrayList<BikeList>();
		BikeList p;
		List<String> distanceMatrix = new ArrayList<String>();
		for (int i = 0; i < markers.size(); i++)
		{
			try
			{
				distanceMatrix = new GoogleDistanceMatrixTask().execute(myLocationString, markers.get(i).getPosition().latitude + "," + markers.get(i).getPosition().longitude).get();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			p = new BikeList();

			p.marker = markers.get(i);
			p.img_pin = R.drawable.map_havebike;
			p.txt_title = markers.get(i).getTitle();
			p.txt_area = sareas.get(i);

			p.txt_bike = info.get(i).split(",")[0];
			if (p.txt_bike.equals("0"))
				p.img_pin = R.drawable.map_nobike;

			p.txt_stop = info.get(i).split(",")[1];
			if (p.txt_stop.equals("0"))
				p.img_pin = R.drawable.map_nopark;

			p.txt_distance = distanceMatrix.get(0);
			p.distanceValue = Float.parseFloat(distanceMatrix.get(2));
			p.txt_time = "走路約 " + distanceMatrix.get(1);

			list.add(p);
		}

		Collections.sort(list, new Comparator<BikeList>() //依距離排續
		{
			public int compare(BikeList o1, BikeList o2)
			{
				return Float.valueOf(o1.distanceValue).compareTo(Float.valueOf(o2.distanceValue));
			}
		});
		return list;
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();

		//dialog.setTitle("更新");
		dialog.setMessage("資料更新中...");
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
