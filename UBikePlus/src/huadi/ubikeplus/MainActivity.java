package huadi.ubikeplus;

import huadi.ubikeplus.Drawer.DrawerItemAdapter;
import huadi.ubikeplus.Drawer.DrawerListMoedel;
import huadi.ubikeplus.Marker.MyInfoWindowAdapter;
import huadi.ubikeplus.Marker.RealTimeBikeTask;
import huadi.ubikeplus.Route.GeocoderTask;
import huadi.ubikeplus.Route.GoogleDirectionTask;
import huadi.ubikeplus.Route.GoogleDistanceMatrixTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends Activity
{
	private static final LatLng NTUE = new LatLng(25.024465, 121.544514);

	private GoogleMap map;
	private List<Marker> allMarkers = new ArrayList<Marker>(); //所有點
	ArrayList<BikeList> bikeLists = new ArrayList<BikeList>(); //站點列表

	private ImageButton compass;
	private float currentDegree;

	private ImageButton myLocationButton; //自己的位置
	private LatLng myLocateLatLng;
	private LocationClient locationClient;
	private static final LocationRequest REQUEST = LocationRequest.create().setInterval(1000) // 1 seconds
	.setFastestInterval(16) // 16ms = 60fps
	.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	//drawer
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ImageButton drawerHomeButton; //按下開啟drawer

	private boolean isUpdateBikeTxt = false;
	private SearchView searchView;
	private Marker marker; //搜尋完, 或長按地圖時用
	private Polyline directionPolyline; //導航用
	private String directionPoint = ""; //目的地
	private boolean isDrection = false;

	private Runnable runnable; //慢慢把mark畫上

	private static final String TXT_YOUBIKENAME = "_YouBikeStation.txt";
	private static final String TXT_BIKEUPDATEINFO = "_BikeUpdateInfo.txt";
	private static final String TXT_NEWBIKENAME = "_NewBikeStation.txt";

	private File youBikeFile;
	private File infoFile;
	String rootPath;
	String serverPath = "http://120.127.14.60/Download/";

	private LinearLayout layout_exercise;
	private static TextView txt_timer, txt_speed, txt_caloric, txt_distance;
	private Button btn_pause;
	private int timerSec = 0; //計時秒數
	private Handler timeHandler = new Handler();
	private float myTracesDistance = 0f;
	private Polyline myTracesPolyline; //走過的痕跡
	private List<LatLng> myTracesPoints; //走過的點
	private boolean isPauseExe = true;
	private boolean isStartExe = false;
	private float speed = 0f; //速率
	private int currentTimeSec = 0; //上個位置到此的時間
	private float currentDistance = 0f; //與上一個位置的距離
	private LatLng currentPoint; //上一個位置
	private float currentAltitude = 0f; //上一個位置海拔(feet)
	private float caloric = 0; //消耗熱量
	private float weight = 60 * 2.204623f; //體重(pb)
	private int count = 0; //是否暫停的重複次數(沒動)
	private int maxCount = 3; //最大嘗試次數

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE); //fullScreen
		setContentView(R.layout.activity_main);

		rootPath = Environment.getExternalStorageDirectory() + "/UBikePlus/";

		String youBikeFilePath = rootPath + TXT_YOUBIKENAME;
		youBikeFile = new File(youBikeFilePath);

		String infoFilePath = rootPath + TXT_BIKEUPDATEINFO;
		infoFile = new File(infoFilePath);

		SetDrawer();

		InitExercise();

		SharedPreferences settings = getSharedPreferences("Preference", 0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		int currentDateTime = Integer.parseInt(sdf.format(new Date()));
		if (settings.contains("UpdateTime"))
		{
			if (settings.getInt("UpdateTime", 0) < currentDateTime) //該更新了
				isUpdateBikeTxt = true;
			else
				isUpdateBikeTxt = false;
		}

	}

	private void setUpMapIfNeeded()
	{
		if (map == null)
		{
			map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			if (map != null)
			{
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(NTUE, 16));
				map.setMyLocationEnabled(true); //定位

				UiSettings uiSettings = map.getUiSettings();
				uiSettings.setZoomControlsEnabled(false);
				uiSettings.setCompassEnabled(false);
				uiSettings.setMyLocationButtonEnabled(false);

				SetCompassBtn();
				SetMyLocationBtn();
				SetSearchView();

				//更新站點資料
				if (!youBikeFile.exists() || isUpdateBikeTxt) //檔案不再 或 需要下載更新
					new DownloadTask(MainActivity.this, map, TXT_YOUBIKENAME, true).execute(serverPath + TXT_YOUBIKENAME);
				else
				{
					InitMapItems(youBikeFile, false);
				}

				if (!infoFile.exists())
					new DownloadTask(MainActivity.this, map, TXT_BIKEUPDATEINFO, true).execute(serverPath + TXT_BIKEUPDATEINFO);

				map.setOnMarkerClickListener(new OnMarkerClickListener() //MarkerClic
				{
					@Override
					public boolean onMarkerClick(Marker marker)
					{
						marker.hideInfoWindow();
						map.setInfoWindowAdapter(new MyInfoWindowAdapter(MainActivity.this, marker.getTitle(), marker.getSnippet()));

						try
						{
							FileReader fr = new FileReader(youBikeFile);
							final BufferedReader br = new BufferedReader(fr);
							String temp = br.readLine(); //readLine()讀取一整行
							while (temp != null)
							{
								if (marker.getTitle().equals(temp.split(",")[3])) //跟0001合併了
								{
									List<Marker> markers = new ArrayList<Marker>();
									markers.add(marker);

									List<String> sno = new ArrayList<String>();
									List<String> sarea = new ArrayList<String>();

									sno.add(temp.split(",")[0]);
									sarea.add(temp.split(",")[4]);

									new RealTimeBikeTask(MainActivity.this, map, markers).execute(sno, sarea);
									break;
								}
								//Log.e("marker.getTitle()", "" + marker.getTitle() + "," + temp.split(",")[3]);
								temp = br.readLine();
							}
							fr.close();
							br.close();
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

						return false;
					}
				});

				directionPolyline = map.addPolyline(new PolylineOptions() //劃一條導航用的線
				.width(5).color(Color.argb(120, 70, 50, 200)).geodesic(true));

				myTracesPolyline = map.addPolyline(new PolylineOptions() //走過的線
				.width(7).color(Color.argb(150, 255, 85, 18)).geodesic(true));

				map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() //InfoWindowClick
				{
					@Override
					public void onInfoWindowClick(final Marker marker)
					{
						directionPoint = marker.getPosition().latitude + "," + marker.getPosition().longitude;
						ClickMarker(directionPoint);
					}
				});

			}

		}
	}
	
	public void ClickMarker(final String directionPoint)
	{		
		List<String> distanceMatrix = new ArrayList<String>();
		int cost = 0; //預計花費
		try
		{
			distanceMatrix = new GoogleDistanceMatrixTask().execute(locationClient.getLastLocation().getLatitude() + "," + locationClient.getLastLocation().getLongitude(), directionPoint).get();
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

		new AlertDialog.Builder(MainActivity.this).setTitle("路徑規劃 , 將此點設為路徑終點?").setMessage("與此地距離 " + distanceMatrix.get(0) + "\n走路花費時間 約 " + distanceMatrix.get(1) + "\nYouBike會員花費 約 " + cost + " 元").setPositiveButton("確定", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				new GoogleDirectionTask(map, directionPolyline).execute(locationClient.getLastLocation().getLatitude() + "," + locationClient.getLastLocation().getLongitude(), directionPoint);
				isDrection = true;
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
			}
		}).show();
	}

	public void InitMapItems(File file, boolean isGetMarker)
	{
		map.clear();
		allMarkers = new ArrayList<Marker>();

		directionPolyline = map.addPolyline(new PolylineOptions() //劃一條導航用的線
		.width(5).color(Color.argb(120, 70, 50, 200)).geodesic(true));

		marker = map.addMarker(new MarkerOptions().position(NTUE).visible(false)); //searchView

		try
		{
			final FileReader fr = new FileReader(file);
			final BufferedReader br = new BufferedReader(fr);

			if (isGetMarker)
			{
				String line = br.readLine();
				while (line != null)
				{
					if (!line.split(",")[0].equals("0000"))
					{
						Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(line.split(",")[1]), Double.parseDouble(line.split(",")[2]))).icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_pin_green)).title(line.split(",")[3]).snippet(line.split(",")[4]));//0代號 3名稱 4位置 5區 6英區 7 英位址

						allMarkers.add(marker); //新增到所有點中						
					}
					line = br.readLine();
				}
				fr.close();
				br.close();
			}
			else
			{
				final Handler handler = new Handler();
				runnable = new Runnable()
				{
					@Override
					public void run()
					{
						String line = "";
						try
						{
							line = br.readLine();
							if (line != null)
							{
								//Log.e("br.readLine()", line);
								if (!line.split(",")[0].equals("0000"))
								{
									Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(line.split(",")[1]), Double.parseDouble(line.split(",")[2]))).icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_pin_green)).title(line.split(",")[3]).snippet(line.split(",")[4]));//0代號 3名稱 4位置 5區 6英區 7 英位址

									allMarkers.add(marker); //新增到所有點中
								}
							}
							else
							{
								handler.removeCallbacks(runnable);
								//fr.close();
								//br.close();
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

						handler.postDelayed(this, 10);
					}
				};
				handler.postDelayed(runnable, 10);//每100ms執行一次runnable.
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void SetCompassBtn() //旋轉到地圖時才出現
	{
		compass = (ImageButton) findViewById(R.id.btn_compass);
		compass.setVisibility(ImageButton.INVISIBLE);

		map.setOnCameraChangeListener(new OnCameraChangeListener()
		{
			@Override
			public void onCameraChange(CameraPosition cameraPosition)
			{
				if (cameraPosition.bearing != 0)
				{
					compass.setVisibility(ImageButton.VISIBLE);

					RotateAnimation ra = new RotateAnimation(currentDegree, -cameraPosition.bearing, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					ra.setDuration(100);
					ra.setFillAfter(true);
					compass.startAnimation(ra);
					currentDegree = -cameraPosition.bearing;
				}
				else
					compass.setVisibility(ImageButton.INVISIBLE);
			}
		});
		compass.setOnClickListener(new OnClickListener() //轉正地圖
		{
			@Override
			public void onClick(View v)
			{
				CameraPosition cameraPosition = new CameraPosition.Builder().target(map.getCameraPosition().target).bearing(0).tilt(map.getCameraPosition().tilt).zoom(map.getCameraPosition().zoom).build();
				map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
				compass.setVisibility(ImageButton.GONE);
				compass.clearAnimation();
			}
		});
	}

	public void SetMyLocationBtn()
	{
		myLocationButton = (ImageButton) findViewById(R.id.btn_myLocat);
		myLocationButton.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				switch (event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
						myLocationButton.setImageResource(R.drawable.loction_b);
						break;
					case MotionEvent.ACTION_UP:
						if (locationClient.getLastLocation() != null && locationClient.isConnected())
						{
							myLocateLatLng = new LatLng(locationClient.getLastLocation().getLatitude(), locationClient.getLastLocation().getLongitude());
							map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocateLatLng, 16));
						}
						else
						{
							setUpLocationClientIfNeeded();
							if (locationClient.getLastLocation() == null || !locationClient.isConnected())
							{
								new AlertDialog.Builder(MainActivity.this).setTitle("已停用定位服務").setMessage("需要存取您的位置, 請開啟定位服務").setPositiveButton("啟用", new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
									}
								}).setNegativeButton("取消", new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int which)
									{

									}
								}).show();
							}
						}
						myLocationButton.setImageResource(R.drawable.loction_g);
						break;
				}
				return false;
			}
		});
	}

	public void SetSearchView()
	{
		marker = map.addMarker(new MarkerOptions().position(NTUE).visible(false));

		searchView = (SearchView) findViewById(R.id.searchView1);
		searchView.setOnQueryTextListener(new OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextChange(String query)
			{
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String query)
			{
				//Log.e("query",query);
				marker.setVisible(false);
				new GeocoderTask(MainActivity.this, map, marker).execute(query);
				searchView.setQuery("", false);
				searchView.clearFocus();
				return false;
			}
		});
	}

	protected void ActionAlertDialog() //彈出站點列表
	{
		//ArrayList<PP> list = initData();
		AlertDialog.Builder builder;
		AlertDialog alertDialog;

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.bike_listview, (ViewGroup) findViewById(R.id.layout_myview));

		ListView myListView = (ListView) layout.findViewById(R.id.mylistview);
		BikeListAdapter adapter = new BikeListAdapter(MainActivity.this, bikeLists);
		myListView.setAdapter(adapter);
		myListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

		DisplayMetrics dm = new DisplayMetrics(); // 建立一個DisplayMetrics物件
		this.getWindowManager().getDefaultDisplay().getMetrics(dm); // 取得裝置的資訊
		int Width = dm.widthPixels;
		int Height = dm.heightPixels;
		LayoutParams lp = (LayoutParams) myListView.getLayoutParams();
		lp.width = (int) (Width * 0.8);
		lp.height = (int) (Height * 0.8);
		myListView.setLayoutParams(lp);

		myListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> a, View v, int which, long id)
			{
				String directionPoint = bikeLists.get(which).marker.getPosition().latitude + "," + bikeLists.get(which).marker.getPosition().longitude;
				ClickMarker(directionPoint);
			}
		});

		builder = new AlertDialog.Builder(MainActivity.this);
		builder.setNegativeButton("CLOSE", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.setView(layout);

		alertDialog = builder.create();
		alertDialog.show();
	}

	protected ArrayList<BikeList> initData(List<Marker> markers, List<String> info, List<String> sareas)
	{ //站點列表的內容
		ArrayList<BikeList> list = new ArrayList<BikeList>();
		BikeList p;
		List<String> distanceMatrix = new ArrayList<String>();
		for (int i = 0; i < markers.size(); i++)
		{
			try
			{
				distanceMatrix = new GoogleDistanceMatrixTask().execute(locationClient.getLastLocation().getLatitude() + "," + locationClient.getLastLocation().getLongitude(), markers.get(i).getPosition().latitude + "," + markers.get(i).getPosition().longitude).get();
			}
			catch (Exception e)
			{
				// TODO 自動產生的 catch 區塊
				e.printStackTrace();
			}
			p = new BikeList();

			p.marker = markers.get(i);
			p.img_pin = R.drawable.bike_pin_green;
			p.txt_title = markers.get(i).getTitle();
			p.txt_area = sareas.get(i);

			p.txt_bike = info.get(i).split(",")[0];
			if (p.txt_bike.equals("0"))
				p.img_pin = R.drawable.bike_pin_orange;

			p.txt_stop = info.get(i).split(",")[1];
			if (p.txt_stop.equals("0"))
				p.img_pin = R.drawable.bike_pin_red;

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

	public void UpdateBike(boolean isAll, boolean isShowList)
	{
		InitMapItems(youBikeFile, true);

		try
		{
			FileReader fr = new FileReader(youBikeFile);
			final BufferedReader br = new BufferedReader(fr);
			String temp = br.readLine(); //readLine()讀取一整行
			List<Marker> markers = new ArrayList<Marker>();
			List<String> snos = new ArrayList<String>(); //所有編號
			List<String> sareas = new ArrayList<String>(); //所有地址
			int i = 0;
			if (!isAll) //附近
				while (temp != null)
				{
					if (!temp.split(",")[0].equals("0000"))
					{
						float[] distance = new float[1];
						Location.distanceBetween(locationClient.getLastLocation().getLatitude(), locationClient.getLastLocation().getLongitude(), allMarkers.get(i).getPosition().latitude, allMarkers.get(i).getPosition().longitude, distance);

						if (distance[0] <= 1000) //半徑1KM
						{
							markers.add(allMarkers.get(i));
							snos.add(temp.split(",")[0]);
							sareas.add(temp.split(",")[4]);
						}
						i++;
					}
					temp = br.readLine();
				}
			else
				//全部				
				while (temp != null)
				{
					if (!temp.split(",")[0].equals("0000"))
					{
						snos.add(temp.split(",")[0]);
						sareas.add(temp.split(",")[4]);
					}
					temp = br.readLine();
				}

			fr.close();
			br.close();
			if (!isAll && markers.size() > 0)
			{
				List<String> info = new RealTimeBikeTask(MainActivity.this, map, markers).execute(snos, sareas).get();

				if (isShowList)
				{
					bikeLists = initData(markers, info, sareas);
					ActionAlertDialog();
				}
			}
			if (isAll && allMarkers.size() > 0)
			{
				List<String> info = new RealTimeBikeTask(MainActivity.this, map, allMarkers).execute(snos, sareas).get();
				if (isShowList)
				{
					bikeLists = initData(allMarkers, info, sareas);
					ActionAlertDialog();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void SetDrawer()
	{
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);

		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		DrawerListMoedel.LoadModel();
		String[] ids = new String[DrawerListMoedel.Items.size()];
		for (int i = 0; i < ids.length; i++)
			ids[i] = Integer.toString(i + 1);
		DrawerItemAdapter adapter = new DrawerItemAdapter(this, R.layout.drawer_list_item, ids);
		drawerList.setAdapter(adapter);

		drawerList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				switch (position)
				{
					case 0: //個人資訊
						Intent intent = new Intent(MainActivity.this, MyInfoActivity.class);
						startActivity(intent);
						break;

					case 1: //更新附近(1KM)
						UpdateBike(false, true);
						break;
					case 2: //更新全部
						UpdateBike(true, true);
						break;
					case 3: //運動計時
						if (isStartExe)
						{
							isStartExe = false;
							isPauseExe = true;
							layout_exercise.setVisibility(View.INVISIBLE);
						}
						else
						{
							isStartExe = true;
							isPauseExe = false;
							layout_exercise.setVisibility(View.VISIBLE);
							myTracesPoints = new ArrayList<LatLng>();
							timerSec = 0;
							btn_pause.setText("暫停");
							count = 0;
							myTracesPoints = new ArrayList<LatLng>();
						}

						break;
					case 4: //社群分享

						break;
					case 5: //設定
						Intent intent1 = new Intent(MainActivity.this, SettingActivity.class);
						startActivity(intent1);
						break;
					default:
						break;
				}
				drawerLayout.closeDrawer(drawerList);
			}
		});

		getActionBar().hide();

		drawerHomeButton = (ImageButton) findViewById(R.id.drawerHomeButton);
		drawerHomeButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				drawerLayout.openDrawer(Gravity.LEFT);
			}
		});
	}

	public void InitExercise() //運動
	{
		//TODO
		//		layout_exercise = (LinearLayout) findViewById(R.id.layout_exercise);
		//		layout_exercise.setVisibility(View.INVISIBLE);
		//		layout_exercise.setOnTouchListener(new OnTouchListener() //防止點到底下
		//		{
		//			public boolean onTouch(View v, MotionEvent event)
		//			{
		//				return true;
		//			}
		//		});
		//
		//		txt_timer = (TextView) findViewById(R.id.txt_timer);
		//		txt_speed = (TextView) findViewById(R.id.txt_speed);
		//		txt_caloric = (TextView) findViewById(R.id.txt_caloric);
		//		txt_distance = (TextView) findViewById(R.id.txt_distance);
		//
		//		btn_pause = (Button) findViewById(R.id.btn_pause);
		//		btn_pause.setOnClickListener(new OnClickListener()
		//		{
		//			@Override
		//			public void onClick(View v)
		//			{
		//				if (!isPauseExe)
		//				{
		//					if (count <= maxCount)
		//					{
		//						isPauseExe = true;
		//						btn_pause.setText("繼續");
		//					}
		//					else
		//					{
		//						btn_pause.setText("暫停");
		//						count = 0;
		//					}
		//				}
		//				else
		//				//暫停中, 按下表示開始
		//				{
		//					isPauseExe = false;
		//					btn_pause.setText("暫停");
		//					count = 0;
		//				}
		//			}
		//		});
		//
		//		timerSec = 0;
		//		myTracesPoints = new ArrayList<LatLng>();
		//
		//		SharedPreferences settings = getSharedPreferences("Preference", 0);
		//		weight = settings.getFloat("Weight", 60 * 2.204623f);
		//
		//		timeHandler.postDelayed(timerRun, 1000); //Timer
	}

	private final Runnable timerRun = new Runnable() //運動計時
	{
		public void run()
		{
			if (!isPauseExe)
			{
				if (count <= maxCount)
					timerSec++; // 經過的秒數 + 1
				else
					btn_pause.setText("繼續");
			}

			String time = String.format("%02d:%02d:%02d", timerSec / 60 / 60, timerSec / 60 % 60, timerSec % 60 % 60);
			txt_timer.setText(time);
			timeHandler.postDelayed(this, 1000);
		}
	};

	public void DoExercise() //於locationChange
	{
		if (isStartExe && locationClient.getLastLocation() != null && locationClient.isConnected())
		{
			float[] distance = new float[] { 0 };
			if (currentPoint != null)
				Location.distanceBetween(locationClient.getLastLocation().getLatitude(), locationClient.getLastLocation().getLongitude(), currentPoint.latitude, currentPoint.longitude, distance);

			if (locationClient.getLastLocation().hasSpeed())
			{
				count = 0;
				btn_pause.setText("暫停");
			}
			else if (count <= maxCount)
			{
				count++;
				speed = 0f;
			}

			//Log.e("distance", distance[0] + ", " + count);

			if (!isPauseExe && count <= maxCount)
			{
				if (locationClient.getLastLocation().hasSpeed())
				{
					speed = locationClient.getLastLocation().getSpeed() * 3.6f;// (currentDistance / currentTimeSec) * 3.6f; //時速
					//大於30公分才紀錄
					myTracesPoints.add(new LatLng(locationClient.getLastLocation().getLatitude(), locationClient.getLastLocation().getLongitude()));
				}
				txt_speed.setText(String.format("%02.1f", speed));

				myTracesDistance = 0;
				float[] totalDistance = new float[1];
				myTracesPolyline.setPoints(myTracesPoints);
				for (int i = 0; i < myTracesPoints.size() - 1; i++)
				{
					Location.distanceBetween(myTracesPoints.get(i).latitude, myTracesPoints.get(i).longitude, myTracesPoints.get(i + 1).latitude, myTracesPoints.get(i + 1).longitude, totalDistance);
					myTracesDistance += totalDistance[0] / 1000;
					txt_distance.setText(String.format("%02.2f", myTracesDistance));
				}

				//http://www.infinitnutrition.us/library/Calculating%20Cycling%20Calories.pdf
				float aveSpeed = speed * 1.609344f; //(myTracesDistance / timerSec) * 0.00044704f; //mph
				float coefficient = 0;
				if (0 < aveSpeed && aveSpeed <= 15)
					coefficient = 0.06f * (aveSpeed / 15);
				else if (15 < aveSpeed && aveSpeed <= 16)
					coefficient = 0.0615f;
				else if (16 < aveSpeed && aveSpeed <= 17)
					coefficient = 0.0675f;
				else if (17 < aveSpeed && aveSpeed <= 18)
					coefficient = 0.0740f;
				else if (18 < aveSpeed && aveSpeed <= 19)
					coefficient = 0.0811f;
				else if (19 < aveSpeed && aveSpeed <= 20)
					coefficient = 0.0891f;
				else if (20 < aveSpeed && aveSpeed <= 21)
					coefficient = 0.0975f;
				else if (21 < aveSpeed && aveSpeed <= 23)
					coefficient = 0.1173f;
				else if (23 < aveSpeed && aveSpeed <= 25)
					coefficient = 0.14f;

				caloric += (coefficient * weight * ((timerSec - currentTimeSec) / 60) + currentAltitude / (100 * 0.3048f)) * 0.4; //min*0.3, max*0.5
				txt_caloric.setText(String.format("%02.1f", caloric));
				currentAltitude = (float) locationClient.getLastLocation().getAltitude();

				currentDistance = distance[0];
				currentTimeSec = timerSec;
			}
			currentPoint = new LatLng(locationClient.getLastLocation().getLatitude(), locationClient.getLastLocation().getLongitude());
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		setUpMapIfNeeded();
		setUpLocationClientIfNeeded();
		locationClient.connect();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (locationClient != null && !isStartExe)
		{
			locationClient.disconnect();
		}
	}

	private void setUpLocationClientIfNeeded()
	{
		if (locationClient == null)
		{
			locationClient = new LocationClient(getApplicationContext(), new ConnectionCallbacks()
			{
				@Override
				public void onDisconnected()
				{
				}

				@Override
				public void onConnected(Bundle arg0)
				{
					locationClient.requestLocationUpdates(REQUEST, new LocationListener()
					{
						@Override
						public void onLocationChanged(Location location)
						{
							if (isDrection)
								new GoogleDirectionTask(map, directionPolyline).execute(locationClient.getLastLocation().getLatitude() + "," + locationClient.getLastLocation().getLongitude(), directionPoint);

							DoExercise();
						}
					});
				}
			}, new OnConnectionFailedListener()
			{
				@Override
				public void onConnectionFailed(ConnectionResult arg0)
				{
				}
			});
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) //縮小APP instead pause
		{
			moveTaskToBack(true); //背景執行
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
