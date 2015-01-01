package kr.KENNYSOFT.Student;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

@SuppressWarnings("deprecation")
public class Account extends PreferenceActivity
{
	boolean mUserIdChk,mPwdChk,mIsLogined;
	SharedPreferences mPref;
	Toolbar mToolbar;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.account);
		
		findPreference("userId").setOnPreferenceClickListener(mUserIdClick);
		findPreference("userId").setOnPreferenceChangeListener(mUserIdChange);
		findPreference("pwd").setOnPreferenceClickListener(mPwdClick);
		findPreference("pwd").setOnPreferenceChangeListener(mPwdChange);
		findPreference("login").setOnPreferenceClickListener(mLoginClick);
		
		mPref=getSharedPreferences("kr.KENNYSOFT.Student_preferences",MODE_PRIVATE);
		mUserIdChk=mPref.getString("userId","").length()>0;
		mPwdChk=mPref.getString("pwd","").length()>0;
		mIsLogined=mPref.getBoolean("isLogined",false);
		findPreference("pwd").setEnabled(mUserIdChk);
		findPreference("login").setEnabled(mPwdChk);
		findPreference("useAutoLogin").setEnabled(mIsLogined);
		if(mIsLogined)
		{
			findPreference("userId").setEnabled(false);
			findPreference("pwd").setEnabled(false);
			findPreference("login").setTitle(R.string.account_logout);
			findPreference("login").setOnPreferenceClickListener(mLogoutClick);
			findPreference("useAutoLogin").setSummary(String.format(getString(R.string.account_useAutoLogin_summary),mPref.getString("userId","")));
		}
		else ((CheckBoxPreference)findPreference("useAutoLogin")).setChecked(false);
	}
	
	public void setContentView(int layoutResID)
	{
		ViewGroup contentView=(ViewGroup)LayoutInflater.from(this).inflate(R.layout.account,new LinearLayout(this),false);
		mToolbar=(Toolbar)contentView.findViewById(R.id.account_toolbar);
		mToolbar.setTitle(getTitle());
		mToolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				finish();
			}
		});
		ViewGroup contentWrapper=(ViewGroup)contentView.findViewById(R.id.account_preference);
		LayoutInflater.from(this).inflate(layoutResID,contentWrapper,true);
		getWindow().setContentView(contentView);
	}
	
	public void login()
	{
		mIsLogined=true;
		SharedPreferences.Editor edit=mPref.edit();
		edit.putBoolean("isLogined",true);
		edit.putBoolean("useAutoLogin",true);
		edit.commit();
		findPreference("userId").setEnabled(false);
		findPreference("pwd").setEnabled(false);
		findPreference("login").setTitle(R.string.account_logout);
		findPreference("login").setOnPreferenceClickListener(mLogoutClick);
		findPreference("useAutoLogin").setEnabled(true);
		findPreference("useAutoLogin").setSummary(String.format(getString(R.string.account_useAutoLogin_summary),mPref.getString("userId","")));
		((CheckBoxPreference)findPreference("useAutoLogin")).setChecked(true);
	}
	
	public void logout()
	{
		mUserIdChk=false;
		mPwdChk=false;
		mIsLogined=false;
		SharedPreferences.Editor edit=mPref.edit();
		edit.putString("userId","");
		edit.putString("pwd","");
		edit.putBoolean("isLogined",false);
		edit.putBoolean("useAutoLogin",false);
		edit.commit();
		findPreference("userId").setEnabled(true);
		findPreference("pwd").setEnabled(false);
		findPreference("login").setTitle(R.string.account_login);
		findPreference("login").setOnPreferenceClickListener(mLoginClick);
		findPreference("login").setEnabled(false);
		((CheckBoxPreference)findPreference("useAutoLogin")).setChecked(false);
		findPreference("useAutoLogin").setEnabled(false);
	}
	
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
			findPreference("pwd").setEnabled(mUserIdChk);
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
			mPwdChk=newValue.toString().length()>0;
			SharedPreferences.Editor edit=mPref.edit();
			if(mPwdChk)edit.putString("pwd",encryptedNewValue);
			else edit.putString("pwd","");
			edit.commit();
			findPreference("login").setEnabled(mPwdChk);
			return false;
		}
	};
	
	OnPreferenceClickListener mLoginClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(final Preference preference)
		{
			new AccountHttpClient(Account.this).execute("http://student.gs.hs.kr/student/api/login.do?key=d56b699830e7&userId="+mPref.getString("userId","")+"&pwd="+mPref.getString("pwd","")+"&type=STUD");
			return true;
		}
	};
	
	OnPreferenceClickListener mLogoutClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(final Preference preference)
		{
			logout();
			return true;
		}
	};
}

class AccountHttpClient extends AsyncTask<String,Void,HttpResponse>
{
	Context context;
	
	AccountHttpClient(Context context)
	{
		this.context=context;
	}
	
	protected HttpResponse doInBackground(String... urls)
	{
		try
		{
			return new DefaultHttpClient().execute(new HttpGet(urls[0]));
		}
		catch(Exception e)
		{
		}
		return null;
	}
	
	protected void onPostExecute(HttpResponse response)
	{
		try
		{
			String html=EntityUtils.toString(response.getEntity());
			if(html.contains("OK"))((Account)context).login();
			else if(html.contains("error"))
			{
				new AlertDialog.Builder(context).setTitle(context.getString(R.string.login_error)).setMessage(html.substring(html.indexOf("message\":")+10,html.length()-9)).setPositiveButton(android.R.string.ok,new OnClickListener()
				{
					public void onClick(DialogInterface dialog,int whichButton)
					{
						((Account)context).logout();
					}
				}).setCancelable(false).create().show();
			}
			else
			{
				new AlertDialog.Builder(context).setTitle(context.getString(R.string.login_failed)).setMessage(context.getString(R.string.login_failed_message)).setPositiveButton(android.R.string.ok,new OnClickListener()
				{
					public void onClick(DialogInterface dialog,int whichButton)
					{
						context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
					}
				}).setCancelable(false).create().show();
			}
		}
		catch(Exception e)
		{
		}
	}
}