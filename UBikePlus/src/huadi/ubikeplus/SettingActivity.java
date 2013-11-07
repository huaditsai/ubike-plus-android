package huadi.ubikeplus;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class SettingActivity extends PreferenceActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_setting);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));	
		
		getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
	}

	public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
        
        //§ï­I´º¦â
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = super.onCreateView(inflater, container, savedInstanceState);
            if(v != null) {
            	v.setBackgroundColor(Color.rgb(221, 225, 232));
            }
            return v;
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