package huadi.ubikeplus;

import huadi.ubikeplus.Drawer.DrawerItemAdapter;
import huadi.ubikeplus.Drawer.DrawerListMoedel;
import huadi.ubikeplus.Marker.InfoWindowClicked;
import huadi.ubikeplus.Marker.MyInfoWindowAdapter;
import huadi.ubikeplus.Marker.RealTimeBikeTask;
import huadi.ubikeplus.Route.GeocoderTask;
import huadi.ubikeplus.Route.GoogleDirectionTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Contacts;
import android.provider.Settings;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.SearchView.OnQueryTextListener;

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
	private Runnable runnable; //慢慢把mark畫上
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

	private SearchView searchView;
	private Marker marker; //搜尋完, 或長按地圖時用
	private Polyline directionPolyline; //導航用
	private String directionPoint = ""; //目的地
	private boolean isDrection = false; //是否導航	

	private boolean isUpdateBikeTxt = false; //是否下載bike文件
	private static final String TXT_YOUBIKENAME = "_YouBikeStation.txt";
	private static final String TXT_BIKEUPDATEINFO = "_BikeUpdateInfo.txt";
	private File youBikeFile;
	private File infoFile;
	private String rootPath; //硬碟位置
	private String serverPath = "http://120.127.14.60/Download/";

	private boolean isAutoUpdate = false; //定時更新
	private boolean isNearUpdate = false; //定時只更新附近
	private Handler udateHandler = new Handler();
	private Runnable udateRunnable; //定時更新用

	private boolean isStartExe = false;

	ExerciseTimerView exerciseTimerView; //計時視窗
	private float myTracesDistance = 0f;
	private Polyline myTracesPolyline; //走過的痕跡
	private List<LatLng> myTracesPoints; //走過的點

	private float caloric = 0; //消耗熱量
	private float weight = 0; //體重(pb)

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

		SharedPreferences settings = getSharedPreferences("Preference", 0);

		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR); //民國
		int month = calendar.get(Calendar.MONTH) + 1; //Calendar.MONTH 從0開始...
		int day = calendar.get(Calendar.DATE);
		int currentDateTime = Integer.parseInt(String.format("%d%02d%02d", year, month, day));
		//Log.e("123", "" + currentDateTime);		
		if (settings.getInt("UpdateTime", 0) < currentDateTime) //該更新了
			isUpdateBikeTxt = true;
		else
			isUpdateBikeTxt = false;

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

				//設定google map 本身的UI
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

				map.setOnMarkerClickListener(new OnMarkerClickListener() //MarkerClick
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
							String line = br.readLine(); //readLine()讀取一整行
							while (line != null)
							{
								if (marker.getTitle().equals(line.split(",")[3]))
								{
									List<Marker> markers = new ArrayList<Marker>();
									markers.add(marker);

									List<String> sno = new ArrayList<String>();
									List<String> sarea = new ArrayList<String>();

									sno.add(line.split(",")[0]);
									sarea.add(line.split(",")[4]);

									//Log.e("marker.getTitle()", "" + sno + "," + sarea);
									new RealTimeBikeTask(MainActivity.this, map, markers).execute(sno, sarea);
									break;
								}

								line = br.readLine();
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
				.width(10).color(Color.argb(120, 70, 50, 200)).geodesic(true));

				myTracesPolyline = map.addPolyline(new PolylineOptions() //走過的線
				.width(10).color(Color.argb(150, 255, 85, 18)).geodesic(true));

				map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() //InfoWindowClick
				{
					@Override
					public void onInfoWindowClick(final Marker marker)
					{
						String myLocationString = locationClient.getLastLocation().getLatitude() + "," + locationClient.getLastLocation().getLongitude();
						directionPoint = marker.getPosition().latitude + "," + marker.getPosition().longitude;
						InfoWindowClicked infoWindowClicked = new InfoWindowClicked(MainActivity.this, map, directionPolyline, myLocationString);
						infoWindowClicked.ClickMarker(directionPoint);
					}
				});

			}
		}

	}

	public void InitMapItems(File file, boolean isGetMarker)
	{
		map.clear();
		allMarkers = new ArrayList<Marker>();

		directionPolyline = map.addPolyline(new PolylineOptions() //劃一條導航用的線
		.width(10).color(Color.argb(120, 70, 50, 200)).geodesic(true));

		myTracesPolyline = map.addPolyline(new PolylineOptions() //走過的線
		.width(10).color(Color.argb(150, 255, 85, 18)).geodesic(true));

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

					Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(line.split(",")[1]), Double.parseDouble(line.split(",")[2]))).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_havebike)).title(line.split(",")[3]).snippet(line.split(",")[4]));//0代號 3名稱 4位置 5區 6英區 7 英位址

					allMarkers.add(marker); //新增到所有點中						

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

								Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(line.split(",")[1]), Double.parseDouble(line.split(",")[2]))).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_havebike)).title(line.split(",")[3]).snippet(line.split(",")[4]));//0代號 3名稱 4位置 5區 6英區 7 英位址

								allMarkers.add(marker); //新增到所有點中
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
		DisplayMetrics dm = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(dm);

		compass = (ImageButton) findViewById(R.id.btn_compass);
		compass.setVisibility(ImageButton.GONE);

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
					compass.setVisibility(ImageButton.GONE);
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

	public void SetMyLocationBtn() //我的位置按鈕
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
						myLocationButton.setImageResource(R.drawable.map_location_press);
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
								}).setNegativeButton("取消", null).show();
							}
						}
						myLocationButton.setImageResource(R.drawable.map_location);
						break;
				}
				return false;
			}
		});
	}

	public void SetSearchView() //搜尋
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

	private void AutoUpdate() //定時自動更新站點
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		isAutoUpdate = settings.getBoolean("isAutoUpdate", false);
		isNearUpdate = settings.getBoolean("isNearUpdate", false);

		if (isAutoUpdate)
		{
			udateRunnable = new Runnable()
			{
				@Override
				public void run()
				{
					if (isNearUpdate)
						UpdateBike(false, false);
					else
						UpdateBike(true, false);

					udateHandler.postDelayed(this, 60 * 1000);
				}
			};
			udateHandler.postDelayed(udateRunnable, 60 * 1000);//每 1min
		}
	}

	@SuppressWarnings("unchecked")
	public void UpdateBike(boolean isAll, boolean isShowList) //更新站點資訊
	{
		InitMapItems(youBikeFile, true);

		try
		{
			FileReader fr = new FileReader(youBikeFile);
			final BufferedReader br = new BufferedReader(fr);

			String line = br.readLine(); //readLine()讀取一整行

			List<Marker> markers = new ArrayList<Marker>();
			List<String> snos = new ArrayList<String>(); //所有編號
			List<String> sareas = new ArrayList<String>(); //所有地址

			int i = 0;
			if (!isAll) //附近
				while (line != null)
				{
					float[] distance = new float[1];
					Location.distanceBetween(locationClient.getLastLocation().getLatitude(), locationClient.getLastLocation().getLongitude(), allMarkers.get(i).getPosition().latitude, allMarkers.get(i).getPosition().longitude, distance);

					if (distance[0] <= 1000) //半徑1KM
					{
						markers.add(allMarkers.get(i));
						snos.add(line.split(",")[0]);
						sareas.add(line.split(",")[4]);
					}
					i++;

					line = br.readLine();
				}
			else
				//全部				
				while (line != null)
				{
					snos.add(line.split(",")[0]);
					sareas.add(line.split(",")[4]);

					line = br.readLine();
				}

			fr.close();
			br.close();

			String myLocationString = locationClient.getLastLocation().getLatitude() + "," + locationClient.getLastLocation().getLongitude();
			if (!isAll && markers.size() > 0)
			{
				if (isShowList)
					new RealTimeBikeTask(MainActivity.this, map, markers, directionPolyline, myLocationString).execute(snos, sareas);
				else
					new RealTimeBikeTask(MainActivity.this, map, markers).execute(snos, sareas);
			}
			if (isAll && allMarkers.size() > 0)
			{
				if (isShowList)
					new RealTimeBikeTask(MainActivity.this, map, allMarkers, directionPolyline, myLocationString).execute(snos, sareas);
				else
					new RealTimeBikeTask(MainActivity.this, map, allMarkers).execute(snos, sareas);
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
						startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
						break;
					case 1: //更新附近(1KM)
						UpdateBike(false, true);
						break;
					case 2: //更新全部
						UpdateBike(true, true);
						break;
					case 3: //運動計時
						SharedPreferences settings = getSharedPreferences("Preference", 0);
						if (!settings.getBoolean("hasWeight", false))
						{
							new AlertDialog.Builder(MainActivity.this).setTitle("運動計時").setMessage("計算消耗熱量需要您的體重, 前往設定?").setPositiveButton("設定", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
								}
							}).setNegativeButton("取消", null).show();
						}
						else
						{
							if (!isStartExe)
								InitExercise();
							FinishExercise();
						}
						break;
					case 4: //設定
						startActivity(new Intent(MainActivity.this, SettingActivity.class));
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
		SharedPreferences settings = getSharedPreferences("Preference", 0);
		weight = settings.getFloat("myWeight", 60f); //體重(pb)

		//TODO
		exerciseTimerView = new ExerciseTimerView(MainActivity.this);
		exerciseTimerView.Start();
		//view1.SetTimeText("23");

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.exercise_start_view, null);
		LinearLayout linearLayout = (LinearLayout) layout.findViewById(R.id.lilayout_exercise_start);
		linearLayout.addView(exerciseTimerView);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout).setCancelable(false).setPositiveButton("OK", null);

		AlertDialog dialog = builder.create();
		dialog.show();

		DisplayMetrics dm = new DisplayMetrics(); // 建立一個DisplayMetrics物件
		getWindowManager().getDefaultDisplay().getMetrics(dm); // 取得裝置的資訊
		int Width = dm.widthPixels;
		int Height = dm.heightPixels;

		LayoutParams lp = (LayoutParams) linearLayout.getLayoutParams();
		lp.width = (int) (Width * 0.8);
		lp.height = (int) (Width * 0.9);
		linearLayout.setLayoutParams(lp);
	}

	public void DoExercise() //於locationChange
	{
		if (isStartExe && locationClient.getLastLocation() != null && locationClient.isConnected())
		{
			if (locationClient.getLastLocation().hasSpeed())
			{
				//speed = locationClient.getLastLocation().getSpeed() * 3.6f;// (currentDistance / currentTimeSec) * 3.6f; //時速
				//大於30公分才紀錄
				myTracesPoints.add(new LatLng(locationClient.getLastLocation().getLatitude(), locationClient.getLastLocation().getLongitude()));
			}

			myTracesDistance = 0;
			float[] totalDistance = new float[1];
			myTracesPolyline.setPoints(myTracesPoints);
			for (int i = 0; i < myTracesPoints.size() - 1; i++)
			{
				Location.distanceBetween(myTracesPoints.get(i).latitude, myTracesPoints.get(i).longitude, myTracesPoints.get(i + 1).latitude, myTracesPoints.get(i + 1).longitude, totalDistance);
				myTracesDistance += totalDistance[0] / 1000;
			}
		}
	}

	private void FinishExercise()
	{
		//http://www.infinitnutrition.us/library/Calculating%20Cycling%20Calories.pdf
		float aveSpeed = (myTracesDistance / exerciseTimerView.GetTotalTimeSecond()) * 1.609344f; //(myTracesDistance / timerSec) * 0.00044704f; //mph

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

		caloric = (float) ((coefficient * (weight * 2.204623f) * (exerciseTimerView.GetTotalTimeSecond() / 60) + locationClient.getLastLocation().getAltitude() / (100 * 0.3048f)) * 0.4f); //min*0.3, max*0.5

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.exercise_finish_view, null);

		TextView txt_exercise_distance = (TextView) layout.findViewById(R.id.txt_exercise_distance);
		TextView txt_exercise_time = (TextView) layout.findViewById(R.id.txt_exercise_time);
		TextView txt_exercise_speed = (TextView) layout.findViewById(R.id.txt_exercise_speed);
		TextView txt_exercise_caloric = (TextView) layout.findViewById(R.id.txt_exercise_caloric);

		txt_exercise_distance.setText(myTracesDistance + " Km");
		txt_exercise_time.setText(String.format("%02d:%02d:%02d", (int) exerciseTimerView.GetTotalTimeSecond() / 3600, (int) exerciseTimerView.GetTotalTimeSecond() / 60 % 60, (int) exerciseTimerView.GetTotalTimeSecond() % 60 % 60));
		txt_exercise_speed.setText(String.format("%02.2f km/hr", (myTracesDistance / exerciseTimerView.GetTotalTimeSecond()) * 3.6f));
		txt_exercise_caloric.setText(caloric + " caloric");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout).setCancelable(false).setPositiveButton("OK", null);

		AlertDialog dialog = builder.create();
		dialog.show();
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
							if (isDrection) //更新位置後要以自己位置重畫線
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
	protected void onResume()
	{
		super.onResume();
		setUpMapIfNeeded();
		setUpLocationClientIfNeeded();
		locationClient.connect();
		AutoUpdate();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (locationClient != null && !isStartExe)
		{
			locationClient.disconnect();
		}
		udateHandler.removeCallbacks(udateRunnable);
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
