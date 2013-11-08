package huadi.ubikeplus;

import android.R.bool;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

public class ExerciseTimerView extends View
{
	private Paint paint;
	private RectF oval;

	private float timeStrokeWidth = 30;
	private int textSize = 30;

	private float radius_hour, radius_minu, radius_sec;
	private float radius_alarmTimer;
	private float radius_stop;

	private Handler timeHandler = new Handler();
	private int timePercentSecond = 0;
	private int alarmPercentSecond = 30 * 60 * 100; //30分鐘
	private int stopPercentSecond = 3 * 100; //3秒

	private String speedString = "";

	private boolean isTimerStart = false;
	private boolean isAlarmStart = false;
	private boolean isPause = false;
	private boolean isStop = false;
	private boolean isCount = false;

	private float degree_hour = 0;
	private float degree_minu = 0;
	private float degree_sec = 0;
	private float degree_alarm = 360;
	private float degree_stop = 0;

	private float timerCenter_x, timerCenter_y;
	private float alarmTimerCenter_x, alarmTimerCenter_y;
	private float stopCenter_x, stopCenter_y;

	private Bitmap timerBitmap;
	private Bitmap alarmBitmap;
	private Bitmap stopBitmap;

	private int bmpHalfWidth;
	private int bmpHalfHigh;

	private MediaPlayer mediaPlayer;

	public ExerciseTimerView(Context context)
	{
		super(context);
		paint = new Paint();
		oval = new RectF();

		timerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exercise_timer_start);
		alarmBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exercise_alarm);
		stopBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exercise_alarm);

		bmpHalfWidth = timerBitmap.getWidth() / 2;
		bmpHalfHigh = timerBitmap.getHeight() / 2;

		if (mediaPlayer != null)
			mediaPlayer.release();
		mediaPlayer = MediaPlayer.create(context, R.raw.bell);
	}

	private final Runnable timerRun = new Runnable() //運動計時
	{
		public void run()
		{
			if (isTimerStart) //碼表
			{
				if (!isPause)
				{
					degree_sec = (timePercentSecond * (360f / 60f / 100f)) % 360f;
					degree_minu = (timePercentSecond * (360f / 3600f / 100f)) % 360f;
					degree_hour = (timePercentSecond * (360f / 86400f / 100f)) % 360f;

					invalidate();
					timePercentSecond += 1;
				}
			}
			if (isAlarmStart)
			{
				degree_alarm = (alarmPercentSecond * (360f / 1800f / 100f)) % 360f;

				invalidate();

				if (alarmPercentSecond > 0)
					alarmPercentSecond -= 1;
				else
				{
					mediaPlayer.start();
					isAlarmStart = false;

					alarmPercentSecond = 30 * 60 * 100; //30分鐘
					degree_alarm = 360;

					alarmBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exercise_alarm);
				}
			}

			if (isCount)
			{
				degree_stop = (stopPercentSecond * (360f / 1f / 100f)) % 360f;
				invalidate();

				if (stopPercentSecond < 100)
					stopPercentSecond += 1;
				else
					isStop = true;
			}
			else if (!isStop)
			{
				degree_stop = (stopPercentSecond * (360f / 3f / 100f)) % 360f;
				invalidate();

				if (stopPercentSecond > 0)
					stopPercentSecond -= 5;
			}

			timeHandler.postDelayed(this, 10);
		}
	};

	public void Start()
	{
		timeHandler.postDelayed(timerRun, 10); //Timer
	}

	public void Stop()
	{
		timeHandler.removeCallbacks(timerRun); //Timer
	}

	public void SetSpeedText(String text)
	{
		speedString = text;
	}

	public float GetTotalTimeSecond()
	{
		return timePercentSecond / 100f;
	}
	
	public boolean IsTimerStart()
	{
		return isTimerStart;
	}

	public boolean IsStop()
	{
		return isStop;
	}

	private void DrawTimer(Canvas canvas) //計時器
	{
		//底色 秒
		paint.setColor(Color.parseColor("#eeeeee"));
		paint.setStrokeWidth(timeStrokeWidth);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);

		oval.set(timerCenter_x - radius_sec, timerCenter_y - radius_sec, timerCenter_x + radius_sec, timerCenter_y + radius_sec);
		canvas.drawArc(oval, 0, 360, false, paint);

		//分
		paint.setColor(Color.parseColor("#cccccc"));
		paint.setStrokeWidth(timeStrokeWidth);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);

		oval.set(timerCenter_x - radius_minu, timerCenter_y - radius_minu, timerCenter_x + radius_minu, timerCenter_y + radius_minu);
		canvas.drawArc(oval, 0, 360, false, paint);

		//時
		paint.setColor(Color.parseColor("#aaaaaa"));
		paint.setStrokeWidth(timeStrokeWidth);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);

		oval.set(timerCenter_x - radius_hour, timerCenter_y - radius_hour, timerCenter_x + radius_hour, timerCenter_y + radius_hour);
		canvas.drawArc(oval, 0, 360, false, paint); //順時針,3點方向為0

		//秒
		paint.setColor(Color.parseColor("#3387e5"));
		paint.setStrokeWidth(timeStrokeWidth);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);

		oval.set(timerCenter_x - radius_sec, timerCenter_y - radius_sec, timerCenter_x + radius_sec, timerCenter_y + radius_sec);
		canvas.drawArc(oval, -90, degree_sec, false, paint);

		//分
		paint.setColor(Color.parseColor("#3359e5"));
		paint.setStrokeWidth(timeStrokeWidth);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);

		oval.set(timerCenter_x - radius_minu, timerCenter_y - radius_minu, timerCenter_x + radius_minu, timerCenter_y + radius_minu);
		canvas.drawArc(oval, -90, degree_minu, false, paint);

		//時
		paint.setColor(Color.parseColor("#2b2bb0"));
		paint.setStrokeWidth(timeStrokeWidth);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);

		oval.set(timerCenter_x - radius_hour, timerCenter_y - radius_hour, timerCenter_x + radius_hour, timerCenter_y + radius_hour);
		canvas.drawArc(oval, -90, degree_hour, false, paint); //順時針,3點方向為0

		//中間按鈕
		canvas.drawBitmap(timerBitmap, timerCenter_x - bmpHalfWidth, timerCenter_y - bmpHalfWidth, paint);
		//canvas.drawCircle(timerCenter_x, timerCenter_y, btnRadius, paint);

		//字
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setStrokeWidth(0);
		paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
		paint.setColor(Color.BLACK);
		paint.setTextSize(textSize);

		String timeString = String.format("%02d:%02d:%02d", (int) (degree_hour / 15) % 24, (int) (degree_minu / 6) % 60, (int) (degree_sec / 6) % 60);
		canvas.drawText(timeString, timerCenter_x, timerCenter_y + radius_sec + timeStrokeWidth + textSize / 2, paint);

		//		//小數點
		//		paint.setTextSize(textSize / 2);
		//		String timeMilliString = String.format(".%02d", timePercentSecond % 100);
		//		canvas.drawText(timeMilliString, timerCenter_x + paint.measureText(timeString) + paint.measureText(timeMilliString) / 2, timerCenter_y, paint);
	}

	private void DrawAlertTimer(Canvas canvas)
	{
		//背景
		paint.setColor(Color.parseColor("#eeeeee"));
		paint.setStrokeWidth(timeStrokeWidth);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);

		oval.set(alarmTimerCenter_x - radius_alarmTimer, alarmTimerCenter_y - radius_alarmTimer, alarmTimerCenter_x + radius_alarmTimer, alarmTimerCenter_y + radius_alarmTimer);
		canvas.drawArc(oval, 0, 360, false, paint);

		//提醒
		paint.setColor(Color.parseColor("#f5a257"));

		canvas.drawArc(oval, -90, degree_alarm, false, paint); //順時針,3點方向為0

		//中間按鈕
		canvas.drawBitmap(alarmBitmap, alarmTimerCenter_x - bmpHalfWidth, alarmTimerCenter_y - bmpHalfWidth, paint);

		//字
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setStrokeWidth(0);
		paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
		paint.setColor(Color.BLACK);
		paint.setTextSize(textSize);

		//時間
		String timeString = String.format("%02d:%02d", (int) (alarmPercentSecond / 100) / 60, (int) (alarmPercentSecond / 100) % 60);
		canvas.drawText(timeString, alarmTimerCenter_x, alarmTimerCenter_y + radius_alarmTimer + timeStrokeWidth + textSize / 2, paint);

		canvas.drawText("租車提醒", alarmTimerCenter_x, alarmTimerCenter_y - radius_alarmTimer - textSize / 2, paint);
	}

	private void DrawStopTimer(Canvas canvas)
	{
		//背景
		paint.setColor(Color.parseColor("#eeeeee"));
		paint.setStrokeWidth(timeStrokeWidth);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);

		oval.set(stopCenter_x - radius_stop, stopCenter_y - radius_stop, stopCenter_x + radius_stop, stopCenter_y + radius_stop);
		canvas.drawArc(oval, 0, 360, false, paint);

		//停止
		paint.setColor(Color.parseColor("#e55151"));
		canvas.drawArc(oval, -90, degree_stop, false, paint); //順時針,3點方向為0

		//中間按鈕
		canvas.drawBitmap(alarmBitmap, stopCenter_x - bmpHalfWidth, stopCenter_y - bmpHalfWidth, paint);

		//字
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setStrokeWidth(0);
		paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
		paint.setColor(Color.BLACK);
		paint.setTextSize(textSize / 2);

		canvas.drawText("長按以停止", stopCenter_x, stopCenter_y - radius_alarmTimer - textSize / 2, paint);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		//canvas.drawColor(Color.parseColor("#CF000000"));

		float width = (float) getWidth();
		float height = (float) getHeight();

		timerCenter_x = width / 3;
		timerCenter_y = height / 3;

		alarmTimerCenter_x = width * 0.75f;
		alarmTimerCenter_y = height * 0.7f;

		stopCenter_x = width * 0.25f;
		stopCenter_y = height * 0.8f;

		//timeStrokeWidth = timerCenter_x / 8;
		radius_stop = bmpHalfWidth + timeStrokeWidth / 2;
		radius_alarmTimer = bmpHalfWidth + timeStrokeWidth;
		radius_hour = bmpHalfWidth + timeStrokeWidth;
		radius_minu = radius_hour + timeStrokeWidth;
		radius_sec = radius_minu + timeStrokeWidth;

		DrawTimer(canvas);

		DrawAlertTimer(canvas);

		if (isPause)
			DrawStopTimer(canvas);
	}

	private int touchX1, touchY1;
	boolean isTimerBtnClick = false;
	boolean isAlarmBtnClick = false;
	boolean isStopBtnClick = false;

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		touchX1 = (int) event.getX();
		touchY1 = (int) event.getY();

		//int pointerCount = event.getPointerCount(); // 幾個觸控點

		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN: // 按下
				if (Math.abs(touchX1 - timerCenter_x) <= bmpHalfWidth && Math.abs(touchY1 - timerCenter_y) <= bmpHalfHigh)
				{
					isTimerBtnClick = true;

					if (!isTimerStart)
					{
						isPause = false;
						timerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exercise_timer_start_press);
					}
					else
					{
						isPause = true;
						timerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exercise_timer_pause_press);
					}
				}
				if (Math.abs(touchX1 - alarmTimerCenter_x) <= bmpHalfWidth && Math.abs(touchY1 - alarmTimerCenter_y) <= bmpHalfHigh)
				{
					isAlarmBtnClick = true;

					if (!isAlarmStart)
						alarmBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exercise_alarm_press);
					else
						alarmBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exercise_alarm_disable_press);
				}

				if (isPause && Math.abs(touchX1 - stopCenter_x) <= bmpHalfWidth && Math.abs(touchY1 - stopCenter_y) <= bmpHalfHigh)
				{
					isStopBtnClick = true;
					isCount = true;
					stopBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exercise_alarm_disable_press);
				}

				invalidate();

				//Log.e("isTouch_Minu", "" + Math.abs(touchX1 - minu_x) + ", " + Math.abs(touchY1 - minu_y));
				break;

			case MotionEvent.ACTION_MOVE: // 拖曳移動				
				break;

			case MotionEvent.ACTION_UP: // 放開
				if (isTimerBtnClick) //開始計時
				{
					isTimerBtnClick = false;

					isTimerStart = !isTimerStart;
					timePercentSecond = 0;
					degree_hour = 0;
					degree_minu = 0;
					degree_sec = 0;

					if (!isTimerStart)
						timerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exercise_timer_start);
					else
						timerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exercise_timer_pause);
				}

				if (isAlarmBtnClick) //開始倒數
				{
					isAlarmBtnClick = false;

					isAlarmStart = !isAlarmStart;
					alarmPercentSecond = 30 * 60 * 100; //30分鐘
					degree_alarm = 360;

					if (!isAlarmStart)
						alarmBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exercise_alarm);
					else
						alarmBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exercise_alarm_disable);
				}

				if (isStopBtnClick)
				{
					isStopBtnClick = false;
					isCount = false;

					stopBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exercise_alarm_disable);
				}

				invalidate();
				break;
		}
		return true;
	}

}