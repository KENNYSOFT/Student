package kr.KENNYSOFT.Student;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.Selection;
import android.view.MenuItem;

@SuppressWarnings("deprecation")
public class Setting extends PreferenceActivity
{
	boolean mUserIdChk,mPwdChk;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setting);
		
		findPreference("about").setOnPreferenceClickListener(mAboutClick);
		findPreference("update").setOnPreferenceClickListener(mUpdateClick);
		findPreference("link").setOnPreferenceClickListener(mLinkClick);
		findPreference("userId").setOnPreferenceClickListener(mUserIdClick);
		findPreference("userId").setOnPreferenceChangeListener(mUserIdChange);
		findPreference("pwd").setOnPreferenceClickListener(mPwdClick);
		findPreference("pwd").setOnPreferenceChangeListener(mPwdChange);
		
		SharedPreferences mPref=getSharedPreferences("kr.KENNYSOFT.Student_preferences",MODE_PRIVATE);
		mUserIdChk=mPref.getString("userId","").length()>0;
		mPwdChk=mPref.getBoolean("pwdChk",false);
		if(mUserIdChk)
		{
			findPreference("pwd").setEnabled(true);
			findPreference("useAutoLogin").setSummary(String.format(getString(R.string.preference_useAutoLogin_summary),mPref.getString("userId","")));
			findPreference("useAutoLogin").setEnabled(true);
		}
	}
	
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
	
	OnPreferenceClickListener mAboutClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(Preference preference)
		{
			new AlertDialog.Builder(Setting.this).setIcon(R.drawable.ic_launcher).setTitle(R.string.full_name).setMessage(R.string.information).setPositiveButton(R.string.ok,null).show();
			return true;
		}
	};
	
	OnPreferenceClickListener mUpdateClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(Preference preference)
		{
			new AlertDialog.Builder(Setting.this).setTitle(R.string.preference_update).setMessage(R.string.update).setPositiveButton(R.string.ok,null).show();
			return true;
		}
	};
	
	OnPreferenceClickListener mLinkClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(Preference preference)
		{
			startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=kr.KENNYSOFT.Student")));
			return true;
		}
	};
	
	OnPreferenceClickListener mUserIdClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(final Preference preference)
		{
			Selection.setSelection(((EditTextPreference)preference).getEditText().getText(),((EditTextPreference)preference).getEditText().getText().length());
			return true;
		}
	};
	
	OnPreferenceChangeListener mUserIdChange=new OnPreferenceChangeListener()
	{
		public boolean onPreferenceChange(Preference preference,Object newValue)
		{
			mUserIdChk=newValue.toString().length()>0;
			if(mUserIdChk)
			{
				findPreference("pwd").setEnabled(true);
				findPreference("useAutoLogin").setSummary(String.format(getString(R.string.preference_useAutoLogin_summary),newValue.toString()));
			}
			else
			{
				mPwdChk=false;
				SharedPreferences.Editor edit=getSharedPreferences("kr.KENNYSOFT.Student_preferences",MODE_PRIVATE).edit();
				edit.putBoolean("pwdChk",false);
				edit.commit();
				findPreference("pwd").setEnabled(false);
				((CheckBoxPreference)findPreference("useAutoLogin")).setChecked(false);
				findPreference("useAutoLogin").setSummary(null);
				findPreference("useAutoLogin").setEnabled(false);
			}
			return true;
		}
	};
	
	OnPreferenceClickListener mPwdClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(final Preference preference)
		{
			((EditTextPreference)preference).getEditText().setText("");
			return true;
		}
	};
	
	OnPreferenceChangeListener mPwdChange=new OnPreferenceChangeListener()
	{
		public boolean onPreferenceChange(Preference preference,Object newValue)
		{
			String encryptedNewValue=null;
			try
			{
				Cipher cipher=Cipher.getInstance("RSA/None/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE,KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger("a84f535e7f4531973c4f966e2f0cac1594057ce2618bb7031ec46933638e03f6b8d08e2ef0a18d61605b13347f8648c2729596683dec96efb17c74635a4a3a0a71410ba42bdfcba51051e6213f7110926c16d4f40d570e4cc9d96a380a48d84a4aba9feffcf86f26434e20b55076eed4a8fcd67af3266f4453ec7cd72e47d127",16),new BigInteger("10001",16))));
				do
				{
					encryptedNewValue=new BigInteger(cipher.doFinal(newValue.toString().getBytes())).toString(16);
				}while(encryptedNewValue.getBytes()[0]=='-');
			}
			catch(Exception e)
			{
			}
			SharedPreferences.Editor edit=getSharedPreferences("kr.KENNYSOFT.Student_preferences",MODE_PRIVATE).edit();
			edit.putBoolean("pwdChk",newValue.toString().length()>0);
			edit.putString("pwd",encryptedNewValue);
			edit.commit();
			mPwdChk=newValue.toString().length()>0;
			((CheckBoxPreference)findPreference("useAutoLogin")).setChecked(mPwdChk);
			findPreference("useAutoLogin").setEnabled(mPwdChk);
			return false;
		}
	};
}