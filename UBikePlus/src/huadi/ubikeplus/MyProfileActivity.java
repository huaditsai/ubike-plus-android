package huadi.ubikeplus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MyProfileActivity extends Activity
{
	Facebook facebook;
	SharedPreferences fbSpf;
	static String filefolderName;
	static String filePathRoot;

	boolean isFB = false;

	SharedPreferences sharedPreferences;
	ImageView img_myPic;
	TextView txt_myName;

	TextView txt_myHeight;
	TextView txt_myWeight;
	TextView txt_myTotalDistance;
	TextView txt_myAvgSpeed;
	TextView txt_myMonthDistance;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_myprofile);
        
	    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads()
	    		.detectDiskWrites().detectNetwork().penaltyLog().build());
	    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
	    		.detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
	    
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));

		fbSpf = getSharedPreferences("FaceBook", MODE_PRIVATE); //偏好設定 
		facebook = new Facebook(fbSpf.getString("fbAppID", getResources().getString(R.string.app_id)));
		
		filefolderName = "UBikePlus";
		filePathRoot = Environment.getExternalStorageDirectory() + "/" + filefolderName + "/";

		sharedPreferences = getSharedPreferences("Preference", 0);

		img_myPic = (ImageView) findViewById(R.id.img_myPic);
		img_myPic.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AlertDialog.Builder builderSingle = new AlertDialog.Builder(MyProfileActivity.this);
				//builderSingle.setIcon(R.drawable.ic_launcher);
				builderSingle.setTitle("選擇圖片");

				final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MyProfileActivity.this, android.R.layout.select_dialog_item);
				//arrayAdapter.add("拍照");
				arrayAdapter.add("從檔案選擇圖片");
				arrayAdapter.add("由Facebook匯入");

				builderSingle.setNegativeButton("取消", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				});

				builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int position)
					{
						switch (position)
						{
							case 0:
								Intent intent = new Intent(Intent.ACTION_PICK); // 建立 "選擇檔案 Action" 的 Intent								
								intent.setType("image/*");// 過濾檔案格式
								Intent destIntent = Intent.createChooser(intent, "選擇檔案"); // 建立 "檔案選擇器" 的 Intent(第二個參數: 選擇器的標題)
								startActivityForResult(destIntent, 0); // 切換到檔案選擇器 (它的處理結果, 觸發 onActivityResult 事件)
								break;
							case 1:
								isFB = true;
								FbLogin();
								break;

							default:
								break;
						}
					}
				});
				builderSingle.show();
			}
		});

		Uri imgUri = Uri.parse(sharedPreferences.getString("ImgUri", "android.resource://huadi.ubikeplus/drawable/facebook_profile_image"));
		//img_myPic.setImageURI(imgUri);

		ScaleImg(imgUri);

		txt_myName = (TextView) findViewById(R.id.txt_myName);
		txt_myName.setText(sharedPreferences.getString("myName", "My Name"));
		txt_myName.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final EditText inputEditText = new EditText(MyProfileActivity.this);
				inputEditText.setSingleLine();

				new AlertDialog.Builder(MyProfileActivity.this).setTitle("輸入名稱").setView(inputEditText).setPositiveButton("Ok", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						txt_myName.setText(inputEditText.getText());
						sharedPreferences.edit().putString("myName", inputEditText.getText().toString()).commit();
					}
				}).setNegativeButton("Cancel", null).show();
			}
		});

		txt_myWeight = (TextView) findViewById(R.id.txt_myWeight);
		if (sharedPreferences.getBoolean("hasWeight", false))
			txt_myWeight.setText(sharedPreferences.getFloat("myWeight", 60f) + " kg");
		txt_myWeight.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final EditText inputEditText = new EditText(MyProfileActivity.this);
				inputEditText.setSingleLine();
				inputEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

				new AlertDialog.Builder(MyProfileActivity.this).setTitle("請輸入體重 (公斤)").setView(inputEditText).setPositiveButton("Ok", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						txt_myWeight.setText(inputEditText.getText() + " kg");
						sharedPreferences.edit().putFloat("myWeight", Float.parseFloat(inputEditText.getText().toString())).commit();
						sharedPreferences.edit().putBoolean("hasWeight", true).commit();
					}
				}).setNegativeButton("Cancel", null).show();

			}
		});

		txt_myTotalDistance = (TextView) findViewById(R.id.txt_myTotalDistance);
		txt_myTotalDistance.setText(sharedPreferences.getString("myTotalDistance", "0") + " km");

		txt_myAvgSpeed = (TextView) findViewById(R.id.txt_myAvgSpeed);
		txt_myAvgSpeed.setText(sharedPreferences.getString("myAvgSpeed", "0") + " km/hr");

		txt_myMonthDistance = (TextView) findViewById(R.id.txt_myMonthDistance);
		txt_myMonthDistance.setText(sharedPreferences.getString("myMonthDistance", "0") + " km");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) // 有選擇檔案
		{
			if (isFB)
			{
				isFB = false;
				facebook.authorizeCallback(requestCode, resultCode, data);
			}
			else
			{
				Uri uri = data.getData(); // 取得檔案的 Uri
				if (uri != null)
				{
					//img_myPic.setImageURI(uri); // 利用 Uri 顯示 ImageView 圖片
					ScaleImg(uri);
					sharedPreferences.edit().putString("ImgUri", uri.toString()).commit();
				}
				else
					Toast.makeText(this, "無效的檔案路徑", Toast.LENGTH_LONG).show();
			}
		}
	}

	private void ScaleImg(Uri imgUri)
	{
		try
		{
			Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imgUri);

			int width = bmp.getWidth();
			int height = bmp.getHeight();

			DisplayMetrics dm = new DisplayMetrics(); // 建立一個DisplayMetrics物件
			this.getWindowManager().getDefaultDisplay().getMetrics(dm); // 取得裝置的資訊
			int screenWidth = dm.widthPixels;
			int screenHeight = dm.heightPixels;

			float scale = 1;
			if (screenWidth > screenHeight)
				scale = (screenHeight * 0.5f) / (float) height;
			else
			{
				scale = (screenWidth * 0.5f) / (float) width;
			}

			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			Bitmap newbm = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);

			//畫圓形
			Bitmap output = Bitmap.createBitmap(newbm.getWidth(), newbm.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(output);

			final int color = 0xff424242;
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, newbm.getWidth(), newbm.getHeight());

			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawCircle(newbm.getWidth() / 2, newbm.getHeight() / 2, newbm.getWidth() / 2, paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(newbm, rect, rect, paint);

			img_myPic.setImageBitmap(output);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	private void FbLogin()
	{
		//Log.e("fbLogin", "0");
		facebook.authorize(this, new String[] { "user_about_me","publish_stream", "read_stream","user_photos" }, Facebook.FORCE_DIALOG_AUTH, new DialogListener()
		{
			public void onComplete(Bundle values)
			{
				try
				{
					String token = facebook.getAccessToken();
					long token_expires = facebook.getAccessExpires();
					String about_me = facebook.request("me"); //json string

					JSONObject jb1 = new JSONObject(about_me);
					String id = jb1.getString("id");
					String name = jb1.getString("name");
					String profile_picture = "https://graph.facebook.com/" + id + "/picture?type=large";
					txt_myName.setText(name);
					sharedPreferences.edit().putString("myName", name).commit();

					Uri imgUri = GetFbPic(profile_picture, id);
					ScaleImg(imgUri);

					SharedPreferences.Editor editor = fbSpf.edit();
					editor.putLong("access_expires", token_expires);
					editor.putString("access_token", token);
					editor.putString("fbid", id);
					editor.putString("fbname", name);
					if (fbSpf.getString("join", "").length() < 0)
						editor.putString("join", "true");
					editor.commit();
					setFb();
					
					Toast.makeText(MyProfileActivity.this, name + "已登入Facebook", Toast.LENGTH_SHORT).show();
				}
				catch (MalformedURLException e)
				{
					Log.e("fbLogin1", "MalformedURLException:" + e);
				}
				catch (IOException e)
				{
					Log.e("fbLogin2", "IOException:" + e);
				}
				catch (JSONException e)
				{
					Log.e("fbLogin3", "JSONException:" + e);
				}
			}

			public void onFacebookError(FacebookError e)
			{
				Log.e("fbLogin4", "FacebookError:" + e);
			}

			public void onError(DialogError e)
			{
				Log.e("fbLogin5", "DialogError:" + e);
			}

			public void onCancel()
			{
			}
		});
	}

	@SuppressWarnings("deprecation")
	public void setFb()
	{
		fbSpf = getSharedPreferences("FaceBook", MODE_PRIVATE); //偏好設定 
		final String access_token = fbSpf.getString("access_token", null);
		final Long expires = fbSpf.getLong("access_expires", -1);

		if (access_token != null && expires != -1)
		{
			facebook.setAccessToken(access_token);
			facebook.setAccessExpires(expires);

			String name = fbSpf.getString("fbname", "");
			String id = fbSpf.getString("fbid", "");
			String profile_picture = "https://graph.facebook.com/" + id + "/picture?type=square";
			txt_myName.setText(name);
			sharedPreferences.edit().putString("myName", name).commit();

			Uri imgUri = GetFbPic(profile_picture, id);
			//img_myPic.setImageURI(imgUri); // 利用 Uri 顯示 ImageView 圖片
			ScaleImg(imgUri);

		}

		//					//logged
		//					fb.edit().remove("access_expires").commit();
		//					fb.edit().remove("access_token").commit();
		//					fb.edit().remove("fbid").commit();
		//					fb.edit().remove("fbname").commit();
		//					Toast.makeText(SettingsActivity.this, "已登出Facebook", Toast.LENGTH_SHORT).show();
		//					setFb();

	}

	public Uri GetFbPic(String profile_picture, String id)
	{ //儲存FB User大頭照
		Bitmap bitmap = null;
		try
		{
			URL url = new URL(profile_picture);
			URLConnection conn = url.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BitmapFactory.Options options = new BitmapFactory.Options();
			bitmap = BitmapFactory.decodeStream(is, null, options);

			File folder = new File(Environment.getExternalStorageDirectory(), filefolderName);
			if (!folder.exists())
				folder.mkdir();
			File file = new File(filePathRoot, id + ".png");
			FileOutputStream fos = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);

			//Log.e("fbLogin", "" + filePathRoot + id + ".png");
			sharedPreferences.edit().putString("ImgUri", filePathRoot + id + ".png").commit();
			return Uri.parse(filePathRoot + id + ".png");
		}
		catch (MalformedURLException e)
		{
			Log.e("GetFbPic1", "MalformedURLException:" + e);
		}
		catch (IOException e)
		{
			Log.e("GetFbPic2", "IOException:" + e);
		}

		return null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		// Respond to the action bar's Up/Home button
			case android.R.id.home:
				Intent upIntent = NavUtils.getParentActivityIntent(this);
				if (NavUtils.shouldUpRecreateTask(this, upIntent))
				{
					// This activity is NOT part of this app's task, so create a new task
					// when navigating up, with a synthesized back stack.
					TaskStackBuilder.create(this)
					// Add all of this activity's parents to the back stack
					.addNextIntentWithParentStack(upIntent)
					// Navigate up to the closest parent
					.startActivities();
				}
				else
				{
					// This activity is part of this app's task, so simply
					// navigate up to the logical parent activity.
					NavUtils.navigateUpTo(this, upIntent);
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume()
	{
		super.onResume();
		facebook.extendAccessTokenIfNeeded(this, null);
	}

}