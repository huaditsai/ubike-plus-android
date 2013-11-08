package huadi.ubikeplus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class StartActivity extends Activity
{
	private Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_start);

		handler.removeCallbacks(updateTimer);
		handler.postDelayed(updateTimer, 1000); // 3sec
	}

	private Runnable updateTimer = new Runnable()
	{
		public void run()
		{
			Intent intent = new Intent(StartActivity.this, MainActivity.class);
			startActivity(intent);			
			StartActivity.this.finish();
			handler.removeCallbacks(updateTimer);//Stop
		}
	};

}