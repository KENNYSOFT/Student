package kr.KENNYSOFT.Student;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.crypto.Cipher;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class Student extends ActionBarActivity
{
	ActionBarDrawerToggle mDrawerToggle;
	boolean mUseZoom,mUseFilter,mUseAutoLogin,mFirstRun,isDrawerShowing,isRegistered,isLogined,isCleared,isExiting;
	DrawerLayout mDrawerLayout;
	int mVersionCode;
	ListView mDrawerList;
	ProgressBar progressbar;
	SharedPreferences mPref;
	String userId,pwd,phoneNumber;
	VelocityTracker mVelocityTracker;
	WebView webview;
	
	String[] mNavTitles={"송죽 학사 메인","선생님 검색","학생 검색","학교 홈페이지"};
	String[] mNavLinks={"http://student.gs.hs.kr/student/index.do","http://student.gs.hs.kr/student/searchTeacher.do","http://student.gs.hs.kr/student/searchStudent.do","http://gs.hs.kr"};
	boolean[] mNavNeeds={true,true,true,false};
	
	int[][] mBannerIds={{R.drawable.banner_winter_0,R.drawable.banner_winter_1,R.drawable.banner_winter_2,R.drawable.banner_winter_3,R.drawable.banner_winter_4},{R.drawable.banner_spring_0,R.drawable.banner_spring_1,R.drawable.banner_spring_2,R.drawable.banner_spring_3,R.drawable.banner_spring_4},{R.drawable.banner_summer_0,R.drawable.banner_summer_1,R.drawable.banner_summer_2,R.drawable.banner_summer_3,R.drawable.banner_summer_4},{R.drawable.banner_fall_0,R.drawable.banner_fall_1,R.drawable.banner_fall_2,R.drawable.banner_fall_3,R.drawable.banner_fall_4}};
	int mBannerPos;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.student);
		
		getSupportActionBar().setElevation(0);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			
		mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
		mDrawerList=(ListView)findViewById(R.id.left_list);
		mDrawerToggle=new ActionBarDrawerToggle(this,mDrawerLayout,R.string.drawer_open,R.string.drawer_close)
		{
			public void onDrawerOpened(View drawerView)
			{
				isDrawerShowing=true;
				getSupportActionBar().setTitle(getString(R.string.app_name));
				getSupportActionBar().setSubtitle(null);
			}
			public void onDrawerClosed(View view)
			{
				isDrawerShowing=false;
				getSupportActionBar().setTitle(webview.getTitle());
				getSupportActionBar().setSubtitle(webview.getUrl());
				mBannerPos=(mBannerPos+1)%5;
				findViewById(R.id.left_banner).setBackgroundResource(mBannerIds[((Calendar.getInstance().get(Calendar.MONTH)+1)%12)/3][mBannerPos]);
			}
		};
		
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,GravityCompat.START);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.listitem,mNavTitles));
		mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent,View view,int position,long id)
			{
				if(isLogined||!mNavNeeds[(int)id])webview.loadUrl(mNavLinks[position]);
				else
				{
					Toast.makeText(Student.this,getString(R.string.drawer_toast_needs_login),Toast.LENGTH_SHORT).show();
					if(webview.getUrl()!=null&&!webview.getUrl().equals("http://student.gs.hs.kr/student/login.do"))webview.loadUrl("http://student.gs.hs.kr/student/login.do");
				}
				mDrawerLayout.closeDrawer(GravityCompat.START);
			}
		});
		
		findViewById(R.id.left_banner).setBackgroundResource(mBannerIds[((Calendar.getInstance().get(Calendar.MONTH)+1)%12)/3][mBannerPos]);
		findViewById(R.id.left_banner).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				mBannerPos=(mBannerPos+1)%5;
				findViewById(R.id.left_banner).setBackgroundResource(mBannerIds[((Calendar.getInstance().get(Calendar.MONTH)+1)%12)/3][mBannerPos]);
			}
		});
		
		mPref=getSharedPreferences("kr.KENNYSOFT.Student_preferences",MODE_PRIVATE);
		userId=mPref.getString("userId","");
		pwd=mPref.getString("pwd","");
		mUseAutoLogin=mPref.getBoolean("useAutoLogin",false);
		mFirstRun=mPref.getBoolean("FirstRun",false);
		mVersionCode=mPref.getInt("VersionCode",0);
		
		try
		{
			phoneNumber="0"+((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number().substring(((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number().length()-10,((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number().length());
		}
		catch(Exception e)
		{
			phoneNumber="01000000000";
		}
		
		if(mVersionCode<=12)
		{
			String encryptedNewValue=null;
			try
			{
				Cipher cipher=Cipher.getInstance("RSA/None/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE,KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger("a84f535e7f4531973c4f966e2f0cac1594057ce2618bb7031ec46933638e03f6b8d08e2ef0a18d61605b13347f8648c2729596683dec96efb17c74635a4a3a0a71410ba42bdfcba51051e6213f7110926c16d4f40d570e4cc9d96a380a48d84a4aba9feffcf86f26434e20b55076eed4a8fcd67af3266f4453ec7cd72e47d127",16),new BigInteger("10001",16))));
				do
				{
					encryptedNewValue=new BigInteger(cipher.doFinal(pwd.getBytes())).toString(16);
				}while(encryptedNewValue.getBytes()[0]=='-');
			}
			catch(Exception e)
			{
			}
			pwd=encryptedNewValue;
			SharedPreferences.Editor edit=mPref.edit();
			edit.putString("pwd",encryptedNewValue);
			edit.commit();
		}
		
		if(mVersionCode<=16)
		{
			if(userId.length()>0&&pwd.length()>0)
			{
				SharedPreferences.Editor edit=mPref.edit();
				edit.putBoolean("isLogined",true);
				edit.commit();
			}
		}
		
		progressbar=(ProgressBar)findViewById(R.id.progress);
		
		webview=(WebView)findViewById(R.id.webview);
		webview.getSettings().setSavePassword(false);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setDomStorageEnabled(true);
		webview.getSettings().setDatabaseEnabled(true);
		webview.getSettings().setDatabasePath(this.getApplicationContext().getDir("database",Context.MODE_PRIVATE).getPath());
		try
		{
			webview.getSettings().setUserAgentString(webview.getSettings().getUserAgentString()+" Student/"+this.getPackageManager().getPackageInfo(this.getPackageName(),PackageManager.GET_META_DATA).versionName);
		}
		catch(Exception e)
		{
		}
		webview.setWebChromeClient(new WebChromeClient()
		{
			public void onProgressChanged(WebView webview,int progress)
			{
				progressbar.setProgress(progress);
				if(progress<progressbar.getMax())progressbar.setVisibility(View.VISIBLE);
				else progressbar.setVisibility(View.INVISIBLE);
			}
		});
		webview.setDownloadListener(new DownloadListener()
		{
			public void onDownloadStart(String url,String userAgent,String contentDisposition,String mimetype,long contentLength)
			{
				if(url.equals("http://student.gs.hs.kr/student/css/fonts/NanumGothic.ttf"))Toast.makeText(Student.this,getString(R.string.download_pc_only),Toast.LENGTH_SHORT).show();
				else
				{
					if(Build.VERSION.SDK_INT<Build.VERSION_CODES.GINGERBREAD)Toast.makeText(Student.this,getString(R.string.download_unavailable),Toast.LENGTH_SHORT).show();
					else
					{
						try
						{
							String fileName=URLDecoder.decode(contentDisposition.substring(20,contentDisposition.length()),"UTF-8").replace("+"," "),mimeType=null;
							Toast.makeText(Student.this,getString(R.string.download_starting),Toast.LENGTH_SHORT).show();
							if(fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase(Locale.getDefault()).equals("hwp"))mimeType="application/x-hwp";
							else mimeType=MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase(Locale.getDefault()));
							if(Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB)((DownloadManager)Student.this.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(new DownloadManager.Request(Uri.parse(url)).addRequestHeader("Cookie",CookieManager.getInstance().getCookie("http://student.gs.hs.kr/student/index.do")).setDescription(getString(R.string.app_name)).setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName).setMimeType(mimeType));
							else ((DownloadManager)Student.this.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(new DownloadManager.Request(Uri.parse(url)).addRequestHeader("Cookie",CookieManager.getInstance().getCookie("http://student.gs.hs.kr/student/index.do")).setDescription(getString(R.string.app_name)).setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName).setMimeType(mimeType).setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED));
						}
						catch(Exception e)
						{
						}
					}
				}
			}
		});
		webview.setWebViewClient(new WebViewClient()
		{
			public void onPageFinished(WebView view,String url)
			{
				if(isLogined&&!isCleared)
				{
					webview.clearHistory();
					isCleared=true;
				}
				if(!isDrawerShowing)
				{
					getSupportActionBar().setTitle(webview.getTitle());
					getSupportActionBar().setSubtitle(webview.getUrl());
				}
				for(int i=0;i<mDrawerList.getCount();++i)
				{
					if(mDrawerList.getChildAt(i)==null)continue;
					if(url.startsWith(mNavLinks[i]))((TextView)mDrawerList.getChildAt(i)).setTypeface(Typeface.DEFAULT_BOLD);
					else ((TextView)mDrawerList.getChildAt(i)).setTypeface(Typeface.DEFAULT);
				}
				if(url.equals("http://student.gs.hs.kr/student/")&&mDrawerList.getChildAt(0)!=null)((TextView)mDrawerList.getChildAt(0)).setTypeface(Typeface.DEFAULT_BOLD);
				if(!isLogined&&(url.equals("http://student.gs.hs.kr/student/")||url.equals("http://student.gs.hs.kr/student/index.do")))
				{
					isLogined=true;
					if(isRegistered)
					{
						unregisterReceiver(broadcastReceiver);
						isRegistered=false;
					}
					if(getIntent().getAction()=="android.intent.action.VIEW"&&getIntent().getDataString()!=null)webview.loadUrl(getIntent().getDataString());
				}
				if(url.equals("http://student.gs.hs.kr/student/logout.do"))finish();
			}
		});
		if(mUseAutoLogin)
		{
			((TextView)findViewById(R.id.left_text)).setText(getString(R.string.drawer_status_1));
			if(getIntent().getAction()=="android.intent.action.VIEW")new StudentHttpClient(Student.this,webview,getIntent().getDataString()).execute("http://student.gs.hs.kr/student/api/login.do?key=d56b699830e7&userId="+userId+"&pwd="+pwd+"&mdn="+phoneNumber+"&type=STUD");
			else new StudentHttpClient(Student.this,webview,null).execute("http://student.gs.hs.kr/student/api/login.do?key=d56b699830e7&userId="+userId+"&pwd="+pwd+"&mdn="+phoneNumber+"&type=STUD");
		}
		else webview.loadUrl("http://student.gs.hs.kr/student/login.do");
		
		findViewById(R.id.cover).setOnTouchListener(new OnTouchListener()
		{
			public boolean onTouch(View v,MotionEvent event)
			{
				switch(event.getAction()&MotionEvent.ACTION_MASK)
				{
				case MotionEvent.ACTION_DOWN:
					if(mVelocityTracker==null)mVelocityTracker=VelocityTracker.obtain();
					else mVelocityTracker.clear();
					mVelocityTracker.addMovement(event);
					break;
				case MotionEvent.ACTION_MOVE:
					mVelocityTracker.addMovement(event);
					mVelocityTracker.computeCurrentVelocity(1000);
					if(VelocityTrackerCompat.getYVelocity(mVelocityTracker,(event.getAction()&MotionEvent.ACTION_POINTER_ID_MASK)>>MotionEvent.ACTION_POINTER_ID_SHIFT)<-1000)getSupportActionBar().hide();
					if(VelocityTrackerCompat.getYVelocity(mVelocityTracker,(event.getAction()&MotionEvent.ACTION_POINTER_ID_MASK)>>MotionEvent.ACTION_POINTER_ID_SHIFT)>1000)getSupportActionBar().show();
					break;
				case MotionEvent.ACTION_UP:
					mVelocityTracker.recycle();
					break;
				}
				webview.onTouchEvent(event);
				return true;
			}	
		});
		
		try
		{
			final int VersionCode=this.getPackageManager().getPackageInfo(this.getPackageName(),PackageManager.GET_META_DATA).versionCode;
			if(mVersionCode<VersionCode)
			{
				new AlertDialog.Builder(this).setTitle(R.string.preference_update).setMessage(R.string.update).setPositiveButton(R.string.ok,null).setCancelable(false).show();
				mVersionCode=VersionCode;
				SharedPreferences.Editor edit=mPref.edit();
				edit.putInt("VersionCode",mVersionCode);
				edit.commit();
			}
		}
		catch(NameNotFoundException e)
		{
		}
		
		if(!mFirstRun)
		{
			new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setTitle(R.string.first_title).setMessage(R.string.first_message).setPositiveButton(R.string.first_agree,new OnClickListener()
			{
				public void onClick(DialogInterface dialog,int whichButton)
				{
					new AlertDialog.Builder(Student.this).setIcon(R.drawable.ic_launcher).setTitle(R.string.second_title).setMessage(R.string.second_message).setPositiveButton(R.string.ok,new OnClickListener()
					{
						public void onClick(DialogInterface dialog,int whichButton)
						{
							startActivity(new Intent(Student.this,Account.class));
						}
					}).setCancelable(false).show();
					SharedPreferences.Editor edit=mPref.edit();
					edit.putBoolean("FirstRun",true);
					edit.commit();
				}
			}).setNegativeButton(R.string.first_disagree,new OnClickListener()
			{
				public void onClick(DialogInterface dialog,int whichButton)
				{
					finish();
				}
			}).setCancelable(false).show();
		}
		
		IntentFilter intentFilter=new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		intentFilter.setPriority(2147483647);
		registerReceiver(broadcastReceiver,intentFilter);
		isRegistered=true;
	}
	
	public void onResume()
	{
		boolean mUseAutoLoginBACKUP=mUseAutoLogin;
		String userIdBACKUP=userId,pwdBACKUP=pwd;
		super.onResume();
		mUseZoom=mPref.getBoolean("useZoom",false);
		webview.getSettings().setBuiltInZoomControls(mUseZoom);
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)webview.getSettings().setDisplayZoomControls(false);
		mUseFilter=mPref.getBoolean("useFilter",true);
		userId=mPref.getString("userId","");
		pwd=mPref.getString("pwd","");
		mUseAutoLogin=mPref.getBoolean("useAutoLogin",false);
		if(!isLogined&&((!mUseAutoLoginBACKUP&&mUseAutoLogin)||(mUseAutoLogin&&!userIdBACKUP.equals(userId))||(mUseAutoLogin&&!pwdBACKUP.equals(pwd))))
		{
			((TextView)findViewById(R.id.left_text)).setText(getString(R.string.drawer_status_1));
			new StudentHttpClient(Student.this,webview,null).execute("http://student.gs.hs.kr/student/api/login.do?key=d56b699830e7&userId="+userId+"&pwd="+pwd+"&mdn="+phoneNumber+"&type=STUD");
		}
	}
	
	public void onDestroy()
	{
		super.onDestroy();
		if(isRegistered)
		{
			unregisterReceiver(broadcastReceiver);
			isRegistered=false;
		}
		Toast.makeText(Student.this,R.string.quit_end,Toast.LENGTH_SHORT).show();
	}

	public boolean onKeyDown(int keyCode,KeyEvent event)
	{
		switch(keyCode)
		{
		case KeyEvent.KEYCODE_BACK:
			if(isDrawerShowing||webview.canGoBack())return true;
			else
			{
				if(!isExiting)
				{
					Toast.makeText(Student.this,R.string.quit_ing,Toast.LENGTH_SHORT).show();
					isExiting=true;
					webview.postDelayed(new Runnable()
					{
						public void run()
						{
							isExiting=false;
						}
					},3000);
				}
				else finish();
				return true;
			}
		default:
			return super.onKeyDown(keyCode,event);
		}
	}
	
	public boolean onKeyUp(int keyCode,KeyEvent event)
	{
		switch(keyCode)
		{
		case KeyEvent.KEYCODE_BACK:
			if(isDrawerShowing)
			{
				mDrawerLayout.closeDrawer(GravityCompat.START);
				isDrawerShowing=false;
				return true;
			}
			else if(webview.canGoBack())
			{
				webview.goBack();
				return true;
			}
			else return super.onKeyDown(keyCode,event);
		default:
			return super.onKeyDown(keyCode,event);
		}
	}
	
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}
	
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		invalidateOptionsMenu();
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.student,menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(mDrawerToggle.onOptionsItemSelected(item))return true;
		switch(item.getItemId())
		{
		case R.id.action_refresh:
			webview.reload();
			return true;
		case R.id.action_setting:
			startActivity(new Intent(this,Setting.class));
			return true;
		case R.id.action_exit:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void setAutoLogined()
	{
		isLogined=true;
		webview.clearHistory();
		((TextView)findViewById(R.id.left_text)).setText(String.format(getString(R.string.drawer_status_2),userId));
		if(isRegistered)
		{
			unregisterReceiver(broadcastReceiver);
			isRegistered=false;
		}
	}
	
	BroadcastReceiver broadcastReceiver=new BroadcastReceiver()
	{
		public void onReceive(Context context,Intent intent)
		{
			Bundle extras=intent.getExtras();
			if(extras==null)return;
			
			Object[] pdus=(Object[])extras.get("pdus");
			if(pdus==null)return;
			
			SmsMessage[] msgs=new SmsMessage[pdus.length];
			
			for(int i=0;i<pdus.length;i++)
			{
				msgs[i]=SmsMessage.createFromPdu((byte[])pdus[i]);
				
				final String address=msgs[i].getOriginatingAddress();
				if(mUseFilter&&!address.equals("0312590420"))continue;
				
				final String body=msgs[i].getDisplayMessageBody();
				String accessCode=null;
				switch(body.length())
				{
				case 32:
					if(!body.substring(0,5).equals("[송죽]["))continue;
					accessCode=body.substring(5,10);
					break;
				case 40:
					if(!body.substring(0,13).equals("[Web발신]\n[송죽]["))continue;
					accessCode=body.substring(13,18);
					break;
				default:
					continue;
				}
				
				if(Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT)
				{
					webview.loadUrl("javascript:document.getElementById('accessCode').value='"+accessCode+"';");
					webview.loadUrl("javascript:document.getElementsByClassName('btn btn-large btn-primary')[0].click();");
				}
				else
				{
					webview.evaluateJavascript("document.getElementById('accessCode').value='"+accessCode+"';",null);
					webview.evaluateJavascript("document.getElementsByClassName('btn btn-large btn-primary')[0].click();",null);
				}
				
				unregisterReceiver(broadcastReceiver);
				isRegistered=false;
				
				return;
			}
		}
	};
}

@SuppressWarnings("deprecation")
class StudentHttpClient extends AsyncTask<String,Void,HttpResponse>
{
	Context context;
	String toContinue;
	WebView webview;
	
	StudentHttpClient(Context context,WebView webview,String toContinue)
	{
		this.context=context;
		this.webview=webview;
		this.toContinue=toContinue;
	}
	
	protected HttpResponse doInBackground(String... urls)
	{
		try
		{
			HttpClient httpClient=new DefaultHttpClient();
			HttpResponse httpResponse=httpClient.execute(new HttpGet(urls[0]));
			List<Cookie> cookies=((DefaultHttpClient)httpClient).getCookieStore().getCookies();
			CookieManager cookieManager=CookieManager.getInstance();
			for(Cookie cookie:cookies)cookieManager.setCookie(cookie.getDomain(),cookie.getName()+"="+cookie.getValue()+";path="+cookie.getPath());
			CookieSyncManager.createInstance(context).sync();
			return httpResponse;
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
			if(html.contains("OK"))
			{
				((Student)context).setAutoLogined();
				if(toContinue!=null)webview.loadUrl(toContinue);
				else webview.loadUrl("http://student.gs.hs.kr/student/index.do");
			}
			else if(html.contains("error"))
			{
				SharedPreferences.Editor edit=((Student)context).mPref.edit();
				edit.putString("userId","");
				edit.putString("pwd","");
				edit.putBoolean("useAutoLogin",false);
				edit.putBoolean("isLogined",false);
				edit.commit();
				new AlertDialog.Builder(context).setTitle(context.getString(R.string.login_error)).setMessage(html.substring(html.indexOf("message\":")+10,html.length()-9)).setPositiveButton(android.R.string.ok,new OnClickListener()
				{
					public void onClick(DialogInterface dialog,int whichButton)
					{
						context.startActivity(new Intent(context,Account.class));
					}
				}).setCancelable(false).show();
				webview.loadUrl("http://student.gs.hs.kr/student/login.do");
			}
			else
			{
				new AlertDialog.Builder(context).setTitle(context.getString(R.string.login_failed)).setMessage(context.getString(R.string.login_failed_message)).setPositiveButton(android.R.string.ok,new OnClickListener()
				{
					public void onClick(DialogInterface dialog,int whichButton)
					{
						context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
					}
				}).setCancelable(false).show();
			}
		}
		catch(Exception e)
		{
		}
	}
}