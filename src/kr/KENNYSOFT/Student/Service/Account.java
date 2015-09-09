package kr.KENNYSOFT.Student.Service;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class Account extends Service
{
	AccountAuthenticator mAccountAuthenticator;
	
	public IBinder onBind(Intent intent)
	{
		IBinder iBinder=null;
		if(intent.getAction().equals(AccountManager.ACTION_AUTHENTICATOR_INTENT))
		{
			if(mAccountAuthenticator==null)mAccountAuthenticator=new AccountAuthenticator(this);
			iBinder=mAccountAuthenticator.getIBinder();
		}
		return iBinder;
	}
}

class AccountAuthenticator extends AbstractAccountAuthenticator
{
	AccountAuthenticator(Context context)
	{
		super(context);
	}
	
	public Bundle addAccount(AccountAuthenticatorResponse response,String accountType,String authTokenType,String[] requiredFeatures,Bundle options)
	{
		Bundle bundle=new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT,new Intent("kr.KENNYSOFT.Student.Action.ADD_ACCOUNT").putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,response));
		return bundle;
	}
	
	public Bundle confirmCredentials(AccountAuthenticatorResponse response,android.accounts.Account account,Bundle options)
	{
		return null;
	}
	
	public Bundle editProperties(AccountAuthenticatorResponse response,String accountType)
	{
		return null;
	}
	
	public Bundle getAuthToken(AccountAuthenticatorResponse response,android.accounts.Account account,String authTokenType,Bundle options)
	{
		return null;
	}
	
	public String getAuthTokenLabel(String authTokenType)
	{
		return null;
	}
	
	public Bundle hasFeatures(AccountAuthenticatorResponse response,android.accounts.Account account,String[] features)
	{
		return null;
	}
	
	public Bundle updateCredentials(AccountAuthenticatorResponse response,android.accounts.Account account,String authTokenType,Bundle options)
	{
		return null;
	}
}