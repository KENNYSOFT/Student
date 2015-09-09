package kr.KENNYSOFT.Student;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.view.MenuItem;

public class Account extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getSupportFragmentManager().beginTransaction().replace(android.R.id.content,new AccountFragment(this)).commit();
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

@SuppressWarnings("deprecation")
class AccountFragment extends PreferenceFragmentCompat
{
	Context mContext;
	SharedPreferences mPref; 
	
	AccountFragment(Context context)
	{
		mContext=context;
		mPref=PreferenceManager.getDefaultSharedPreferences(mContext);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.account);
		
		if(getActivity().getIntent().getAction()!=null)
		{
			if(getActivity().getIntent().getAction().equals("kr.KENNYSOFT.Student.Action.ADD_ACCOUNT"))
			{
				if(mPref.getBoolean("isLogined",false)&&AccountManager.get(mContext).addAccountExplicitly(new android.accounts.Account(mPref.getString("userId",""),"kr.KENNYSOFT.Student.ACCOUNT"),null,null))
				{
					AccountAuthenticatorResponse response=getActivity().getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
					Bundle result=new Bundle();
					result.putString(AccountManager.KEY_ACCOUNT_NAME,mPref.getString("userId",""));
					result.putString(AccountManager.KEY_ACCOUNT_TYPE,"kr.KENNYSOFT.Student.ACCOUNT");
					response.onResult(result);
					getActivity().finish();
				}
			}
			if(getActivity().getIntent().getAction().equals("kr.KENNYSOFT.Student.Action.LOGOUT"))logout();
		}
	}
	
	public void onCreatePreferences(Bundle savedInstanceState,String rootKey)
	{
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		findPreference("userId").setOnPreferenceClickListener(mUserIdClick);
		findPreference("userId").setOnPreferenceChangeListener(mUserIdChange);
		findPreference("pwd").setOnPreferenceClickListener(mPwdClick);
		findPreference("pwd").setOnPreferenceChangeListener(mPwdChange);
		updatePreference();
	}
	
	OnPreferenceClickListener mUserIdClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(Preference preference)
		{
			((EditTextPreference)preference).setText("");
			return true;
		}
	};
	
	OnPreferenceChangeListener mUserIdChange=new OnPreferenceChangeListener()
	{
		public boolean onPreferenceChange(Preference preference,Object newValue)
		{
			findPreference("pwd").setEnabled(newValue.toString().length()>0);
			return true;
		}
	};
	
	OnPreferenceClickListener mPwdClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(Preference preference)
		{
			((EditTextPreference)preference).setText("");
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
			SharedPreferences.Editor edit=mPref.edit();
			if(newValue.toString().length()>0)edit.putString("pwd",encryptedNewValue);
			else edit.putString("pwd","");
			edit.commit();
			findPreference("login").setEnabled(newValue.toString().length()>0);
			return false;
		}
	};
	
	OnPreferenceClickListener mLoginClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(Preference preference)
		{
			new AccountURLConnection(mContext,AccountFragment.this).execute("http://student.gs.hs.kr/student/api/login.do?key=d56b699830e7&userId="+mPref.getString("userId","")+"&pwd="+mPref.getString("pwd","")+"&type=STUD");
			return true;
		}
	};
	
	OnPreferenceClickListener mLogoutClick=new OnPreferenceClickListener()
	{
		public boolean onPreferenceClick(Preference preference)
		{
			logout();
			return true;
		}
	};
	
	void updatePreference()
	{
		try
		{
			if(!mPref.getBoolean("isLogined",false))
			{
				findPreference("userId").setEnabled(true);
				findPreference("pwd").setEnabled(mPref.getString("userId","").length()>0);
				findPreference("login").setEnabled(mPref.getString("pwd","").length()>0);
				findPreference("login").setTitle(R.string.account_login);
				findPreference("login").setOnPreferenceClickListener(mLoginClick);
				findPreference("useAutoLogin").setEnabled(false);
				findPreference("useAutoLogin").setSummary(null);
				((SwitchPreferenceCompat)findPreference("useAutoLogin")).setChecked(false);
			}
			else
			{
				findPreference("userId").setEnabled(false);
				findPreference("pwd").setEnabled(false);
				findPreference("login").setTitle(R.string.account_logout);
				findPreference("login").setOnPreferenceClickListener(mLogoutClick);
				findPreference("useAutoLogin").setEnabled(true);
				findPreference("useAutoLogin").setSummary(String.format(getString(R.string.account_useAutoLogin_summary),mPref.getString("userId","")));
			}
		}
		catch(Exception e)
		{
		}
	}
	
	public void login()
	{
		AccountManager.get(mContext).addAccountExplicitly(new android.accounts.Account(mPref.getString("userId",""),"kr.KENNYSOFT.Student.ACCOUNT"),null,null);
		((SwitchPreferenceCompat)findPreference("useAutoLogin")).setChecked(true);
		SharedPreferences.Editor edit=mPref.edit();
		edit.putBoolean("isLogined",true);
		edit.putBoolean("useAutoLogin",true);
		edit.commit();
		updatePreference();
	}
	
	public void logout()
	{
		AccountManager.get(mContext).removeAccount(new android.accounts.Account(mPref.getString("userId",""),"kr.KENNYSOFT.Student.ACCOUNT"),null,null);
		SharedPreferences.Editor edit=mPref.edit();
		edit.putString("userId","");
		edit.putString("pwd","");
		edit.putBoolean("isLogined",false);
		edit.putBoolean("useAutoLogin",false);
		edit.commit();
		updatePreference();
	}
}

class AccountURLConnection extends AsyncTask<String,Void,String>
{
	Context mContext;
	Fragment mFragment;
	
	AccountURLConnection(Context context,Fragment fragment)
	{
		mContext=context;
		mFragment=fragment;
	}
	
	protected String doInBackground(String... urls)
	{
		String html="";
		try
		{
			URLConnection connection=new URL(urls[0]).openConnection();
			InputStream is=connection.getInputStream();
			BufferedReader in=new BufferedReader(new InputStreamReader(is));
			String line="";
			while((line=in.readLine())!=null)html=html+line+"\n";
		}
		catch(Exception e)
		{
		}
		return html;
	}
	
	@Override
	protected void onPostExecute(String html)
	{
		if(html.contains("OK"))((AccountFragment)mFragment).login();
		else if(html.contains("error"))
		{
			new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.login_error)).setMessage(html.substring(html.indexOf("message\":")+10,html.length()-6)).setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog,int whichButton)
				{
					((AccountFragment)mFragment).logout();
				}
			}).setCancelable(false).create().show();
		}
		else
		{
			new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.login_failed)).setMessage(mContext.getString(R.string.login_failed_message)).setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog,int whichButton)
				{
					mContext.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
				}
			}).setCancelable(false).create().show();
		}
	}
}