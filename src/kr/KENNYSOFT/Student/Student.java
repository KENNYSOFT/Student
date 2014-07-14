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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class Student extends Activity
{
	ActionBarDrawerToggle mDrawerToggle;
	boolean mUseZoom,mUseFilter,mUseAutoLogin,mFirstRun,isShowingActionBar,isShowingDrawer,isRegistered,isLogined,isExiting;
	DrawerLayout mDrawerLayout;
	int mVersionCode;
	ListView mDrawerList;
	ProgressBar progressbar;
	String userId,pwd,phoneNumber;
	WebView webview;
	
	String[] mNavTitles={"송죽 학사 메인","선생님 검색","학생 검색","학교 홈페이지"};
	String[] mNavLinks={"http://student.gs.hs.kr/student/index.do","http://student.gs.hs.kr/student/searchTeacher.do","http://student.gs.hs.kr/student/searchStudent.do","http://gs.hs.kr"};
	boolean[] mNavNeeds={true,true,true,false};
	
	int[][] mBannerIds={{R.drawable.banner_winter_0,R.drawable.banner_winter_1,R.drawable.banner_winter_2,R.drawable.banner_winter_3,R.drawable.banner_winter_4},{R.drawable.banner_spring_0,R.drawable.banner_spring_1,R.drawable.banner_spring_2,R.drawable.banner_spring_3,R.drawable.banner_spring_4},{R.drawable.banner_summer_0,R.drawable.banner_summer_1,R.drawable.banner_summer_2,R.drawable.banner_summer_3,R.drawable.banner_summer_4},{R.drawable.banner_fall_0,R.drawable.banner_fall_1,R.drawable.banner_fall_2,R.drawable.banner_fall_3,R.drawable.banner_fall_4}};
	int mBannerPos;
	
	public void setAutoLogined()
	{
		webview.clearHistory();
		isLogined=true;
		((TextView)findViewById(R.id.left_text)).setText(String.format(getString(R.string.drawer_status_2),userId));
		if(isRegistered)
		{
			unregisterReceiver(broadcastreceiver);
			isRegistered=false;
		}
	}
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.student);
		
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
		{
			getActionBar().setHomeButtonEnabled(true);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			
			mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
			mDrawerList=(ListView)findViewById(R.id.left_list);
			mDrawerToggle=new ActionBarDrawerToggle(this,mDrawerLayout,R.drawable.ic_drawer,R.string.drawer_open,R.string.drawer_close)
			{
				public void onDrawerOpened(View drawerView)
				{
					isShowingDrawer=true;
				}
				public void onDrawerClosed(View view)
				{
					isShowingDrawer=false;
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
						if(!webview.getUrl().equals("http://student.gs.hs.kr/student/login.do"))webview.loadUrl("http://student.gs.hs.kr/student/login.do");
					}
					mDrawerLayout.closeDrawer(GravityCompat.START);
				}
			});
		}
		
		findViewById(R.id.left_banner).setBackgroundResource(mBannerIds[((Calendar.getInstance().get(Calendar.MONTH)+1)%12)/3][mBannerPos]);
		findViewById(R.id.left_banner).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				mBannerPos=(mBannerPos+1)%5;
				findViewById(R.id.left_banner).setBackgroundResource(mBannerIds[((Calendar.getInstance().get(Calendar.MONTH)+1)%12)/3][mBannerPos]);
			}
		});
		
		SharedPreferences mPref=getSharedPreferences("kr.KENNYSOFT.Student_preferences",MODE_PRIVATE);
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
			SharedPreferences.Editor edit=getSharedPreferences("kr.KENNYSOFT.Student_preferences",MODE_PRIVATE).edit();
			edit.putBoolean("pwdChk",pwd.length()>0);
			edit.putString("pwd",encryptedNewValue);
			edit.commit();
		}
		
		progressbar=(ProgressBar)findViewById(R.id.progress);
		
		webview=(WebView)findViewById(R.id.webview);
		webview.getSettings().setSavePassword(false);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setUserAgentString(webview.getSettings().getUserAgentString()+" Student/1.0.0");
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.ECLAIR_MR1)
		{
			webview.getSettings().setDomStorageEnabled(true);
			webview.getSettings().setDatabaseEnabled(true);
			webview.getSettings().setDatabasePath(this.getApplicationContext().getDir("database",Context.MODE_PRIVATE).getPath());
		}
		webview.setWebChromeClient(new WebChromeClient()
		{
			public void onProgressChanged(WebView view,int progress)
			{
				progressbar.setProgress(progress);
				if(progress<100)progressbar.setVisibility(View.VISIBLE);
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
				for(int i=0;i<mDrawerList.getCount();++i)
				{
					if(mDrawerList.getChildAt(i)==null)continue;
					if(webview.getUrl().startsWith(mNavLinks[i]))((TextView)mDrawerList.getChildAt(i)).setTypeface(Typeface.DEFAULT_BOLD);
					else ((TextView)mDrawerList.getChildAt(i)).setTypeface(Typeface.DEFAULT);
				}
				if(mDrawerList.getChildAt(0)!=null&&webview.getUrl().equals("http://student.gs.hs.kr/student/"))((TextView)mDrawerList.getChildAt(0)).setTypeface(Typeface.DEFAULT_BOLD);
				if(!isLogined&&(url.equals("http://student.gs.hs.kr/student/")||url.equals("http://student.gs.hs.kr/student/index.do")))
				{
					webview.clearHistory();
					isLogined=true;
					((TextView)findViewById(R.id.left_text)).setText(getString(R.string.drawer_status_1));
					if(isRegistered)
					{
						unregisterReceiver(broadcastreceiver);
						isRegistered=false;
					}
				}
				if(url.equals("http://student.gs.hs.kr/student/logout.do"))finish();
				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
				{
					getActionBar().setTitle(webview.getTitle());
					getActionBar().setSubtitle(webview.getUrl());
				}
			}
		});
		if(mUseAutoLogin)new StudentHttpClient(Student.this,webview).execute("http://student.gs.hs.kr/student/api/login.do?key=d56b699830e7&userId="+userId+"&pwd="+pwd+"&mdn="+phoneNumber+"&type=STUD");
		else webview.loadUrl("http://student.gs.hs.kr/student/login.do");
		
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
		{
			isShowingActionBar=true;
			webview.postDelayed(new Runnable()
			{
				public void run()
				{
					if(isShowingActionBar)
					{
						getActionBar().hide();
						isShowingActionBar=false;
					}
				}
			},3000);
			
			((Button)findViewById(R.id.button)).setOnClickListener(new Button.OnClickListener()
			{
				public void onClick(View view)
				{
					if(!isShowingActionBar)
					{
						getActionBar().show();
						isShowingActionBar=true;
						webview.postDelayed(new Runnable()
						{
							public void run()
							{
								if(isShowingActionBar)
								{
									getActionBar().hide();
									isShowingActionBar=false;
								}
							}
						},3000);
					}
					else
					{
						if(!isShowingDrawer)
						{
							mDrawerLayout.openDrawer(GravityCompat.START);
							isShowingDrawer=true;
							webview.postDelayed(new Runnable()
							{
								public void run()
								{
									if(isShowingDrawer)
									{
										mDrawerLayout.closeDrawer(GravityCompat.START);
										isShowingDrawer=false;
									}
								}
							},3000);
						}
					}
				}
			});
		}
		
		try
		{
			final int VersionCode=this.getPackageManager().getPackageInfo(this.getPackageName(),PackageManager.GET_META_DATA).versionCode;
			if(mVersionCode<VersionCode)
			{
				new AlertDialog.Builder(this).setTitle(R.string.preference_update).setMessage(R.string.update).setPositiveButton(R.string.ok,new OnClickListener()
				{
					public void onClick(DialogInterface dialog,int whichButton)
					{
						mVersionCode=VersionCode;
						SharedPreferences.Editor edit=getSharedPreferences("kr.KENNYSOFT.Student_preferences",MODE_PRIVATE).edit();
						edit.putInt("VersionCode",mVersionCode);
						edit.commit();
					}
				}).show();
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
							startActivity(new Intent(Student.this,Setting.class));
						}
					}).show();
					getSharedPreferences("kr.KENNYSOFT.Student_preferences",MODE_PRIVATE).edit().putBoolean("FirstRun",true).commit();
				}
			}).setNegativeButton(R.string.first_disagree,new OnClickListener()
			{
				public void onClick(DialogInterface dialog,int whichButton)
				{
					finish();
				}
			}).show();
		}
		
		IntentFilter intentfilter=new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		intentfilter.setPriority(2147483647);
		registerReceiver(broadcastreceiver,intentfilter);
		isRegistered=true;
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
	
	public void onResume()
	{
		boolean mUseAutoLoginBACKUP=mUseAutoLogin;
		String userIdBACKUP=userId,pwdBACKUP=pwd;
		super.onResume();
		SharedPreferences mPref=getSharedPreferences("kr.KENNYSOFT.Student_preferences",MODE_PRIVATE);
		mUseZoom=mPref.getBoolean("useZoom",false);
		webview.getSettings().setBuiltInZoomControls(mUseZoom);
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)webview.getSettings().setDisplayZoomControls(false);
		mUseFilter=mPref.getBoolean("useFilter",true);
		userId=mPref.getString("userId","");
		pwd=mPref.getString("pwd","");
		mUseAutoLogin=mPref.getBoolean("useAutoLogin",false);
		if(!isLogined&&((!mUseAutoLoginBACKUP&&mUseAutoLogin)||(mUseAutoLogin&&!userIdBACKUP.equals(userId))||(mUseAutoLogin&&!pwdBACKUP.equals(pwd))))new StudentHttpClient(Student.this,webview).execute("http://student.gs.hs.kr/student/api/login.do?key=d56b699830e7&userId="+userId+"&pwd="+pwd+"&mdn="+phoneNumber+"&type=STUD");
	}
	
	public void onDestroy()
	{
		super.onDestroy();
		if(isRegistered)
		{
			unregisterReceiver(broadcastreceiver);
			isRegistered=false;
		}
		Toast.makeText(Student.this,R.string.quit_end,Toast.LENGTH_SHORT).show();
	}

	public boolean onKeyDown(int keyCode,KeyEvent event)
	{
		if(keyCode==KeyEvent.KEYCODE_BACK)
		{
			if(isShowingDrawer||isShowingActionBar||webview.canGoBack())return true;
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
		}
		else return super.onKeyDown(keyCode,event);
	}
	
	public boolean onKeyUp(int keyCode,KeyEvent event)
	{
		if(keyCode==KeyEvent.KEYCODE_MENU)
		{
			if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
			{
				if(!isShowingActionBar)
				{
					getActionBar().show();
					isShowingActionBar=true;
					webview.postDelayed(new Runnable()
					{
						public void run()
						{
							if(isShowingActionBar)
							{
								getActionBar().hide();
								isShowingActionBar=false;
							}
						}
					},3000);
				}
				else
				{
					if(!isShowingDrawer)
					{
						mDrawerLayout.openDrawer(GravityCompat.START);
						isShowingDrawer=true;
						webview.postDelayed(new Runnable()
						{
							public void run()
							{
								if(isShowingDrawer)
								{
									mDrawerLayout.closeDrawer(GravityCompat.START);
									isShowingDrawer=false;
								}
							}
						},3000);
					}
				}
				return true;
			}
			else return super.onKeyDown(keyCode,event);
		}
		else if(keyCode==KeyEvent.KEYCODE_BACK)
		{
			if(isShowingDrawer)
			{
				mDrawerLayout.closeDrawer(GravityCompat.START);
				isShowingDrawer=false;
				return true;
			}
			else if(isShowingActionBar)
			{
				getActionBar().hide();
				isShowingActionBar=false;
				return true;
			}
			else if(webview.canGoBack())
			{
				webview.goBack();
				return true;
			}
			else return super.onKeyDown(keyCode,event);
		}
		else return super.onKeyDown(keyCode,event);
	}
	
	private void setRead(String address,String body)
	{
		int cnt=0;
		try
		{
			Cursor cursor=getContentResolver().query(Uri.parse("content://sms/inbox"),null,null,null,null);
			while(cursor.moveToNext())
			{
				if(cnt++>10)break;
				if(cursor.getString(cursor.getColumnIndex("address")).equals(address)&&cursor.getString(cursor.getColumnIndex("body")).equals(body)&&cursor.getInt(cursor.getColumnIndex("read"))==0)
				{
					String SmsMessageId=cursor.getString(cursor.getColumnIndex("_id"));
					ContentValues values=new ContentValues();
					values.put("read",true);
					getContentResolver().update(Uri.parse("content://sms/inbox"),values,"_id="+SmsMessageId,null);
					break;
				}
			}
			cursor.close();
		}
		catch(Exception e)
		{
		}
	}
	
	BroadcastReceiver broadcastreceiver=new BroadcastReceiver()
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
				
				String address=msgs[i].getOriginatingAddress();
				if(mUseFilter&&!address.equals("0312590420"))continue;
				
				String body=msgs[i].getDisplayMessageBody();
				if(body.length()!=41)continue;
				if(!body.substring(0,12).startsWith("[송죽][외부접근코드]"))continue;
				
				setRead(address,body);
				abortBroadcast();
				
				if(Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT)
				{
					webview.loadUrl("javascript:document.getElementById('accessCode').value='"+body.substring(27,32)+"';");
					webview.loadUrl("javascript:document.getElementsByClassName('btn btn-large btn-primary')[0].click();");
				}
				else
				{
					webview.evaluateJavascript("document.getElementById('accessCode').value='"+body.substring(27,32)+"';",null);
					webview.evaluateJavascript("document.getElementsByClassName('btn btn-large btn-primary')[0].click();",null);
				}
				
				unregisterReceiver(broadcastreceiver);
				isRegistered=false;
				
				return;
			}
		}
	};
	
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
		case R.id.action_information:
			new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setTitle(R.string.full_name).setMessage(R.string.information).setPositiveButton(R.string.ok,null).show();
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
}

class StudentHttpClient extends AsyncTask<String,Void,HttpResponse>
{
	Context context;
	WebView webview;
	
	StudentHttpClient(Context context,WebView webview)
	{
		this.context=context;
		this.webview=webview;
	}
	
	protected HttpResponse doInBackground(String... urls)
	{
		try
		{
			HttpClient httpclient=new DefaultHttpClient();
			HttpResponse httpresponse=httpclient.execute(new HttpGet(urls[0]));
			List<Cookie> cookies=((DefaultHttpClient)httpclient).getCookieStore().getCookies();
			CookieSyncManager.createInstance(context);
			CookieManager cookieManager=CookieManager.getInstance();
			for(Cookie cookie:cookies)
			{
				String cookieString=cookie.getName()+"="+cookie.getValue()+";path="+cookie.getPath();
				cookieManager.setCookie(cookie.getDomain(),cookieString);
			}
			CookieSyncManager.getInstance().sync();
			return httpresponse;
		}
		catch(Exception e)
		{
		}
		return null;
	}
	
	protected void onPostExecute(HttpResponse result)
	{
		try
		{
			String html=EntityUtils.toString(result.getEntity());
			if(html.contains("OK"))
			{
				((Student)context).setAutoLogined();
				webview.loadUrl("http://student.gs.hs.kr/student/index.do");
			}
			else
			{
				new AlertDialog.Builder(context).setTitle(context.getString(R.string.login_failed)).setMessage(html.substring(html.indexOf("message\":")+10,html.length()-9)).setPositiveButton(android.R.string.ok,new OnClickListener()
				{
					public void onClick(DialogInterface dialog,int whichButton)
					{
						context.startActivity(new Intent(context,Setting.class));
					}
				}).setCancelable(false).create().show();
				webview.loadUrl("http://student.gs.hs.kr/student/login.do");
			}
		}
		catch(Exception e)
		{
		}
	}
}