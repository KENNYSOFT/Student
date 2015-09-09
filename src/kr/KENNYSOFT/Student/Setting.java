package kr.KENNYSOFT.Student;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;

public class Setting extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getSupportFragmentManager().beginTransaction().replace(android.R.id.content,new SettingFragment(this)).commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}

class SettingFragment extends PreferenceFragmentCompat
{
	Context mContext;
	SharedPreferences mPref;
	
	SettingFragment(Context context)
	{
		mContext=context;
		mPref=PreferenceManager.getDefaultSharedPreferences(mContext);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);
	}
	
	public void onCreatePreferences(Bundle savedInstanceState,String rootKey)
	{
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		findPreference("about").setOnPreferenceClickListener(mAboutClick);
		findPreference("opensource").setOnPreferenceClickListener(mOpensourceClick);
		findPreference("update").setOnPreferenceClickListener(mUpdateClick);
		findPreference("restore").setOnPreferenceClickListener(mRestoreClick);
	}
	
	OnPreferenceClickListener mAboutClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(Preference preference)
		{
			new AlertDialog.Builder(mContext).setIcon(R.drawable.ic_launcher).setTitle(R.string.full_name).setMessage(R.string.information).setPositiveButton(R.string.ok,null).show();
			return true;
		}
	};
	
	OnPreferenceClickListener mOpensourceClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(Preference preference)
		{
			new AlertDialog.Builder(mContext).setTitle(R.string.preference_opensource).setMessage(R.string.opensource).setPositiveButton(R.string.ok,null).show();
			return true;
		}
	};
	
	OnPreferenceClickListener mUpdateClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(Preference preference)
		{
			new AlertDialog.Builder(mContext).setTitle(R.string.preference_update).setMessage(R.string.update).setPositiveButton(R.string.ok,null).show();
			return true;
		}
	};
	
	OnPreferenceClickListener mRestoreClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(Preference preference)
		{
			new AlertDialog.Builder(mContext).setIcon(R.drawable.ic_action_warning).setTitle(R.string.preference_restore).setMessage(R.string.preference_restore_message).setPositiveButton(R.string.ok,new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog,int whichButton)
				{
					SharedPreferences.Editor edit=mPref.edit();
					edit.putInt("deletedMenuCnt",0);
					edit.putBoolean("isMenuRestored",true);
					edit.commit();
				}
			}).setNegativeButton(R.string.cancel,null).show();
			return true;
		}
	};
}