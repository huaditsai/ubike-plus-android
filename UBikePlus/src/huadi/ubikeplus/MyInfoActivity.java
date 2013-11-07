package huadi.ubikeplus;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MyInfoActivity extends Activity
{
	SharedPreferences sharedPreferences;
	ImageView img_myPic;
	TextView txt_myName;

	TextView txt_myHeight;
	TextView txt_myWeight;
	TextView txt_myTotalDistance;
	TextView txt_myAvgSpeed;
	TextView txt_myMonthDistance;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_myinfo);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));

		sharedPreferences = getSharedPreferences("Preference", 0);

		img_myPic = (ImageView) findViewById(R.id.img_myPic);
		img_myPic.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AlertDialog.Builder builderSingle = new AlertDialog.Builder(MyInfoActivity.this);
				//builderSingle.setIcon(R.drawable.ic_launcher);
				//builderSingle.setTitle("Select One Name:-");

				final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MyInfoActivity.this, android.R.layout.select_dialog_item);
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
								break;

							default:
								break;
						}
						//						String strName = arrayAdapter.getItem(position);
						//						AlertDialog.Builder builderInner = new AlertDialog.Builder(MyInfoActivity.this);						
						//						builderInner.setTitle("Your Selected Item is");
						//						builderInner.setMessage(strName);
						//						builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener()
						//						{
						//							@Override
						//							public void onClick(DialogInterface dialog, int which)
						//							{
						//								dialog.dismiss();
						//							}
						//						});
						//						builderInner.show();
					}
				});
				builderSingle.show();
			}
		});

		Uri imgUri = Uri.parse(sharedPreferences.getString("ImgUri", "android.resource://huadi.ubikeplus/drawable/facebook_profile_image"));
		img_myPic.setImageURI(imgUri);

		ScaleImg(imgUri);

		txt_myName = (TextView) findViewById(R.id.txt_myName);
		txt_myName.setText(sharedPreferences.getString("myName", "My Name"));
		txt_myName.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final EditText inputEditText = new EditText(MyInfoActivity.this);
				inputEditText.setSingleLine();
				
				new AlertDialog.Builder(MyInfoActivity.this)
				.setTitle("輸入名稱")
				.setView(inputEditText)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						txt_myName.setText(inputEditText.getText());
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						// Canceled.
					}
				}).show();
			}
		});

		txt_myHeight = (TextView) findViewById(R.id.txt_myHeight);
		txt_myHeight.setText(sharedPreferences.getString("myHeight", "190") + " cm");

		txt_myWeight = (TextView) findViewById(R.id.txt_myWeight);
		txt_myWeight.setText(sharedPreferences.getString("myWeight", "60") + " kg");

		txt_myTotalDistance = (TextView) findViewById(R.id.txt_myTotalDistance);
		txt_myTotalDistance.setText(sharedPreferences.getString("myTotalDistance", "0") + " km");

		txt_myAvgSpeed = (TextView) findViewById(R.id.txt_myAvgSpeed);
		txt_myAvgSpeed.setText(sharedPreferences.getString("myAvgSpeed", "0") + " km/hr");

		txt_myMonthDistance = (TextView) findViewById(R.id.txt_myMonthDistance);
		txt_myMonthDistance.setText(sharedPreferences.getString("myMonthDistance", "0") + " km");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) // 有選擇檔案
		{
			Uri uri = data.getData(); // 取得檔案的 Uri
			if (uri != null)
			{
				img_myPic.setImageURI(uri); // 利用 Uri 顯示 ImageView 圖片
				ScaleImg(uri);
				sharedPreferences.edit().putString("ImgUri", uri.toString()).commit();
			}
			else
				Toast.makeText(this, "無效的檔案路徑", Toast.LENGTH_LONG).show();
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
			// TODO 自動產生的 catch 區塊
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO 自動產生的 catch 區塊
			e.printStackTrace();
		}
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

}