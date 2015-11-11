package kr.KENNYSOFT.Student;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.hudomju.swipe.OnItemClickListener;
import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.SwipeableItemClickListener;
import com.hudomju.swipe.adapter.RecyclerViewAdapter;

import android.accounts.AccountManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class Student extends AppCompatActivity
{
	static final int ACTION_SEARCH_STUDENT=1;
	static final int ACTION_SEARCH_TEACHER=2;
	
	boolean mUseZoom,mUseFilter,mUseAutoLogin,mFirstRun,isNestedScrollEnding,isDrawerShowing,isDrawerAdded,isRegistered,isLogined,isQuiting;
	int mVersionCode,mNowFolder;
	NestedScrollView mNestedScrollView;
	ProgressBar mProgressBar;
	SharedPreferences mPref;
	String mUserId,mPwd,mPhoneNumber;
	TextView mToolbarTitle,mToolbarSubtitle;
	Toolbar mToolbar;
	WebView mWebView;
	
	ActionBarDrawerToggle mDrawerToggle;
	ArrayList<StudentMenuItemList> mDrawerFolder;
	DrawerLayout mDrawerLayout;
	StudentMenuItemAdapter mDrawerAdapter;
	SwipeToDismissTouchListener<RecyclerViewAdapter> mSwipeToDismissTouchListener;
	RecyclerView mDrawerView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.student);
		
		mToolbar=(Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mToolbarTitle=(TextView)findViewById(R.id.toolbar_title);
		mToolbarSubtitle=(TextView)findViewById(R.id.toolbar_subtitle);
		
		findViewById(R.id.toolbar_information).setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if(mToolbarSubtitle.getVisibility()==View.VISIBLE)
				{
					new AlertDialog.Builder(Student.this).setTitle(mToolbarTitle.getText()).setMessage(mToolbarSubtitle.getText()).setNeutralButton(getString(R.string.action_copy),new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog,int which)
						{
							if(VERSION.SDK_INT<VERSION_CODES.HONEYCOMB)((android.text.ClipboardManager)Student.this.getSystemService(Context.CLIPBOARD_SERVICE)).setText(mWebView.getUrl());
							else ((ClipboardManager)Student.this.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Student",mWebView.getUrl()));
						}
					}).setPositiveButton(R.string.ok,null).show();
				}
			}
		});
		
		mDrawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
		mDrawerToggle=new ActionBarDrawerToggle(this,mDrawerLayout,R.string.drawer_open,R.string.drawer_close)
		{
			@Override
			public void onDrawerOpened(View drawerView)
			{
				mDrawerAdapter.notifyDataSetChanged();
				isDrawerShowing=true;
			}
			
			@Override
			public void onDrawerClosed(View view)
			{
				mSwipeToDismissTouchListener.processPendingDismisses();
				mDrawerAdapter.notifyDataSetChanged();
				mDrawerAdapter.setHeaderNext();
				isDrawerShowing=false;
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,GravityCompat.START);
		mDrawerAdapter=new StudentMenuItemAdapter(this);
		mDrawerAdapter.add(new StudentMenuItem(true,false,0,"송죽 학사 메인","home","http://student.gs.hs.kr/student/index.do",""));
		mDrawerAdapter.add(new StudentMenuItem(true,false,0,"교사 검색","search","http://student.gs.hs.kr/student/searchTeacher.do",""));
		mDrawerAdapter.add(new StudentMenuItem(true,false,0,"학생 검색","search","http://student.gs.hs.kr/student/searchStudent.do",""));
		mDrawerAdapter.add(new StudentMenuItem(true,false,0,"학교 홈페이지","globe","https://www.gs.hs.kr/",""));
		mDrawerFolder=new ArrayList<StudentMenuItemList>();
		mDrawerView=(RecyclerView)findViewById(R.id.drawer);
		mDrawerView.setLayoutManager(new LinearLayoutManager(this));
		mDrawerView.setAdapter(mDrawerAdapter);
		mDrawerView.addOnItemTouchListener(new SwipeableItemClickListener(this,new OnItemClickListener()
		{
			public void onItemClick(View view,int position)
			{
				switch(mDrawerAdapter.getItemViewType(position))
				{
				case StudentMenuItemAdapter.TYPE_HEADER:
					mDrawerAdapter.setHeaderNext();
					break;
				case StudentMenuItemAdapter.TYPE_ITEM:
					position--;
					switch(view.getId())
					{
					case R.id.toast_undo:
						mSwipeToDismissTouchListener.undoPendingDismiss();
						break;
					case R.id.list_item:
					case R.id.list_item_icon:
					case R.id.list_item_title:
						if(mSwipeToDismissTouchListener.processPendingDismisses())break;
						if(mDrawerAdapter.get(position).Link.length()>0)
						{
							if(isLogined||!mDrawerAdapter.get(position).isLoginNeeded)mWebView.loadUrl(mDrawerAdapter.get(position).Link);
							else
							{
								Toast.makeText(Student.this,getString(R.string.drawer_login_needed),Toast.LENGTH_SHORT).show();
								if(mWebView.getUrl()!=null&&!mWebView.getUrl().equals("http://student.gs.hs.kr/student/login.do"))mWebView.loadUrl("http://student.gs.hs.kr/student/login.do");
							}
							mDrawerAdapter.notifyItemChanged(position);
							mDrawerLayout.closeDrawer(GravityCompat.START);
						}
						else
						{
							if(!mDrawerAdapter.get(position).isExpanded)
							{
								int added=0;
								for(int i=0;i<mDrawerFolder.get(mDrawerAdapter.get(position).FolderId-1).size();++i)if(mDrawerAdapter.addToIndex(position+1+added,mDrawerFolder.get(mDrawerAdapter.get(position).FolderId-1).get(i)))added++;
								mDrawerAdapter.setExpanded(position);
							}
							else
							{
								while(mDrawerAdapter.getItemCount()-2>position+1&&mDrawerAdapter.get(position+1).FolderId==mDrawerAdapter.get(position).FolderId)mDrawerAdapter.remove(position+1);
								mDrawerAdapter.setCollapsed(position);
							}
						}
						break;
					}
					break;
				}
			}
		}));
		mDrawerView.setOnTouchListener(mSwipeToDismissTouchListener=new SwipeToDismissTouchListener<RecyclerViewAdapter>(new RecyclerViewAdapter(mDrawerView),new SwipeToDismissTouchListener.DismissCallbacks<RecyclerViewAdapter>()
		{
			public boolean canDismiss(int position)
			{
				if(position==0||position==mDrawerAdapter.getItemCount()-1)return false;
				else return !mDrawerAdapter.get(position-1).isExpanded;
			}
			
			public void onDismiss(RecyclerViewAdapter recyclerView,int position)
			{
				int now=mPref.getInt("deletedMenuCnt",0);
				SharedPreferences.Editor edit=mPref.edit();
				edit.putString("deletedMenu"+now,mDrawerAdapter.remove(position-1).toString());
				edit.putInt("deletedMenuCnt",now+1);
				edit.commit();
			}
		}));
		mDrawerView.setOnScrollListener((RecyclerView.OnScrollListener)mSwipeToDismissTouchListener.makeScrollListener());
		
		mProgressBar=(ProgressBar)findViewById(R.id.progressbar);
		
		mNestedScrollView=(NestedScrollView)findViewById(R.id.nestedscrollview);
		
		mWebView=(WebView)findViewById(R.id.webview);
		if(VERSION.SDK_INT>=VERSION_CODES.HONEYCOMB)mWebView.getSettings().setDisplayZoomControls(false);
		mWebView.getSettings().setSavePassword(false);
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.getSettings().setDatabaseEnabled(true);
		mWebView.getSettings().setDatabasePath(this.getApplicationContext().getDir("database",Context.MODE_PRIVATE).getPath());
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.addJavascriptInterface(this,"student");
		try
		{
			mWebView.getSettings().setUserAgentString(mWebView.getSettings().getUserAgentString()+" Student/"+this.getPackageManager().getPackageInfo(this.getPackageName(),PackageManager.GET_META_DATA).versionName);
		}
		catch(Exception e)
		{
		}
		mWebView.setWebChromeClient(new WebChromeClient()
		{
			@Override
			public void onProgressChanged(WebView webview,int progress)
			{
				if(progress<mProgressBar.getMax())
				{
					mProgressBar.setVisibility(View.VISIBLE);
					mProgressBar.setProgress(progress);
				}
				else mProgressBar.setVisibility(View.INVISIBLE);
			}
		});
		mWebView.setDownloadListener(new DownloadListener()
		{
			public void onDownloadStart(String url,String userAgent,String contentDisposition,String mimetype,long contentLength)
			{
				if(url.equals("http://student.gs.hs.kr/student/css/fonts/NanumGothic.ttf"))Toast.makeText(Student.this,getString(R.string.download_pc_only),Toast.LENGTH_SHORT).show();
				else
				{
					if(VERSION.SDK_INT<VERSION_CODES.GINGERBREAD)Toast.makeText(Student.this,getString(R.string.download_unavailable),Toast.LENGTH_SHORT).show();
					else
					{
						try
						{
							String fileName=URLDecoder.decode(contentDisposition.substring(20,contentDisposition.length()),"UTF-8").replace("+"," "),mimeType=null;
							Toast.makeText(Student.this,getString(R.string.download_starting),Toast.LENGTH_SHORT).show();
							if(fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase(Locale.getDefault()).equals("hwp"))mimeType="application/x-hwp";
							else mimeType=MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase(Locale.getDefault()));
							((DownloadManager)Student.this.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(new DownloadManager.Request(Uri.parse(url)).addRequestHeader("Cookie",CookieManager.getInstance().getCookie("http://student.gs.hs.kr/student/index.do")).setDescription(getString(R.string.app_name)).setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName).setMimeType(mimeType).setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED));
						}
						catch(Exception e)
						{
						}
					}
				}
			}
		});
		mWebView.setWebViewClient(new WebViewClient()
		{
			@Override
			public boolean shouldOverrideUrlLoading(WebView view,String url)
			{
				if(url.startsWith("http://student.gs.hs.kr"))return false;
				else
				{
					startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));
					return true;
				}
			}
			
			@Override
			public void onReceivedSslError(WebView view,SslErrorHandler handler,SslError error)
			{
				if(error.getUrl().startsWith("http://student.gs.hs.kr"))handler.proceed();
				else
				{
					startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(error.getUrl())));
					handler.cancel();
				}
			}
			
			@Override
			public void onPageStarted(WebView view,String url,Bitmap favicon)
			{
				mToolbarTitle.setText(R.string.action_loading);
				mToolbarSubtitle.setVisibility(View.GONE);
				mDrawerAdapter.notifyDataSetChanged();
				if(url.equals("http://student.gs.hs.kr/student/"))view.loadUrl("http://student.gs.hs.kr/student/index.do");
				if(url.equals("http://student.gs.hs.kr/student/logout.do"))finish();
			}
			
			@Override
			public void onPageFinished(WebView view,String url)
			{
				if(url.equals("http://student.gs.hs.kr/student/login.do"))mWebView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
				else mWebView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
				mToolbarTitle.setText(mWebView.getTitle());
				mToolbarSubtitle.setVisibility(View.VISIBLE);
				mToolbarSubtitle.setText(mWebView.getUrl());
				if(!isLogined&&(url.equals("http://student.gs.hs.kr/student/")||url.equals("http://student.gs.hs.kr/student/index.do")))
				{
					isLogined=true;
					mWebView.clearHistory();
					if(isRegistered)
					{
						unregisterReceiver(mBroadcastReceiver);
						isRegistered=false;
					}
					new StudentURLConnection(Student.this,StudentURLConnection.TYPE_LEFTMENU).execute("http://student.gs.hs.kr/student/index.do");
					if(getIntent().getAction()=="android.intent.action.VIEW"&&getIntent().getDataString()!=null)mWebView.loadUrl(getIntent().getDataString());
				}
				if(url.startsWith("http://student.gs.hs.kr/student/well/myGoodsList.do")||url.startsWith("http://student.gs.hs.kr/student/well/goodsInfo.do")||url.startsWith("http://student.gs.hs.kr/student/well/goodsUse.do"))
				{
					if(VERSION.SDK_INT<VERSION_CODES.KITKAT)mWebView.loadUrl("javascript:layerPopup=function(url,title){window.location.href=url;}");
					else mWebView.evaluateJavascript("layerPopup=function(url,title){window.location.href=url;}",null);
				}
				if(url.startsWith("http://student.gs.hs.kr/student/sms/sendList.do"))
				{
					if(VERSION.SDK_INT<VERSION_CODES.KITKAT)mWebView.loadUrl("javascript:for(var i=1;i<document.getElementsByClassName('item5').length;++i)document.getElementsByClassName('item5')[i].getElementsByTagName('xmp')[0].innerHTML=document.getElementsByClassName('item5')[i].title");
					else mWebView.evaluateJavascript("for(var i=1;i<document.getElementsByClassName('item5').length;++i)document.getElementsByClassName('item5')[i].getElementsByTagName('xmp')[0].innerHTML=document.getElementsByClassName('item5')[i].title",null);
				}
				if(Student.this.getPackageManager().queryIntentActivities(new Intent("kr.KENNYSOFT.Student.Action.SEARCH_STUDENT"),PackageManager.MATCH_DEFAULT_ONLY).size()>0)
				{
					if(VERSION.SDK_INT<VERSION_CODES.KITKAT)mWebView.loadUrl("javascript:studentPopup=function(id,grade,callback){var isGrade='Y';if(isNull(grade)){isGrade='N';}var url=contextPath+'/searchStudent.do?target='+id+'&grade='+grade+'&isGrade='+isGrade+'&callback='+callback;window.student.searchStudent(url,'학생검색');return false;}");
					else mWebView.evaluateJavascript("studentPopup=function(id,grade,callback){var isGrade='Y';if(isNull(grade)){isGrade='N';}var url=contextPath+'/searchStudent.do?target='+id+'&grade='+grade+'&isGrade='+isGrade+'&callback='+callback;window.student.searchStudent(url,'학생검색');return false;}",null);
				}
				if(Student.this.getPackageManager().queryIntentActivities(new Intent("kr.KENNYSOFT.Student.Action.SEARCH_TEACHER"),PackageManager.MATCH_DEFAULT_ONLY).size()>0)
				{
					if(VERSION.SDK_INT<VERSION_CODES.KITKAT)mWebView.loadUrl("javascript:teacherPopup=function(id){var url=contextPath+'/searchTeacher.do?target='+id;window.student.searchTeacher(url,'교사검색');return false;}");
					else mWebView.evaluateJavascript("teacherPopup=function(id){var url=contextPath+'/searchTeacher.do?target='+id;window.student.searchTeacher(url,'교사검색');return false;}",null);
				}
				if(VERSION.SDK_INT<VERSION_CODES.KITKAT)
				{
					mWebView.loadUrl("javascript:$('body').stop();");
					mWebView.loadUrl("javascript:$('ol').stop();");
					mWebView.loadUrl("javascript:document.getElementById('newsToggle').remove();");
					mWebView.loadUrl("javascript:document.getElementsByClassName('navbar navbar-inverse navbar-fixed-top')[0].parentNode.removeChild(document.getElementsByClassName('navbar navbar-inverse navbar-fixed-top')[0]);");
					mWebView.loadUrl("javascript:document.body.style.paddingTop='0px';");
					mWebView.loadUrl("javascript:document.getElementsByClassName('span3 bs-docs-sidebar')[0].parentNode.removeChild(document.getElementsByClassName('span3 bs-docs-sidebar')[0]);");
				}
				else
				{
					mWebView.evaluateJavascript("$('body').stop();",null);
					mWebView.evaluateJavascript("$('ol').stop();",null);
					mWebView.evaluateJavascript("document.getElementById('newsToggle').remove();",null);
					mWebView.evaluateJavascript("document.getElementsByClassName('navbar navbar-inverse navbar-fixed-top')[0].parentNode.removeChild(document.getElementsByClassName('navbar navbar-inverse navbar-fixed-top')[0]);",null);
					mWebView.evaluateJavascript("document.body.style.paddingTop='0px';",null);
					mWebView.evaluateJavascript("document.getElementsByClassName('span3 bs-docs-sidebar')[0].parentNode.removeChild(document.getElementsByClassName('span3 bs-docs-sidebar')[0]);",null);
				}
			}
		});
		
		mPref=PreferenceManager.getDefaultSharedPreferences(this);
		mUserId=mPref.getString("userId","");
		mPwd=mPref.getString("pwd","");
		mUseAutoLogin=mPref.getBoolean("useAutoLogin",false);
		mFirstRun=mPref.getBoolean("FirstRun",false);
		mVersionCode=mPref.getInt("VersionCode",0);
		
		try
		{
			mPhoneNumber="0"+((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number().substring(((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number().length()-10,((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number().length());
		}
		catch(Exception e)
		{
			mPhoneNumber="01000000000";
		}
		
		if(mVersionCode<13&&mPwd.length()>0)
		{
			String encryptedNewValue=null;
			try
			{
				Cipher cipher=Cipher.getInstance("RSA/None/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE,KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger("a84f535e7f4531973c4f966e2f0cac1594057ce2618bb7031ec46933638e03f6b8d08e2ef0a18d61605b13347f8648c2729596683dec96efb17c74635a4a3a0a71410ba42bdfcba51051e6213f7110926c16d4f40d570e4cc9d96a380a48d84a4aba9feffcf86f26434e20b55076eed4a8fcd67af3266f4453ec7cd72e47d127",16),new BigInteger("10001",16))));
				do
				{
					encryptedNewValue=new BigInteger(cipher.doFinal(mPwd.getBytes())).toString(16);
				}while(encryptedNewValue.getBytes()[0]=='-');
			}
			catch(Exception e)
			{
			}
			mPwd=encryptedNewValue;
			SharedPreferences.Editor edit=mPref.edit();
			edit.putString("pwd",encryptedNewValue);
			edit.commit();
		}
		
		if(mVersionCode<17&&mUserId.length()>0&&mPwd.length()>0)
		{
			SharedPreferences.Editor edit=mPref.edit();
			edit.putBoolean("isLogined",true);
			edit.commit();
		}
		
		if(mVersionCode<20&&mPref.getBoolean("isLogined",false))AccountManager.get(this).addAccountExplicitly(new android.accounts.Account(mUserId,"kr.KENNYSOFT.Student.ACCOUNT"),null,null);
		
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
			new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setTitle(R.string.first_title).setMessage(R.string.first_message).setPositiveButton(R.string.first_agree,new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog,int which)
				{
					new AlertDialog.Builder(Student.this).setIcon(R.drawable.ic_launcher).setTitle(R.string.second_title).setMessage(R.string.second_message).setPositiveButton(R.string.ok,new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog,int which)
						{
							startActivity(new Intent("kr.KENNYSOFT.Student.Action.ACCOUNT"));
						}
					}).setCancelable(false).show();
					SharedPreferences.Editor edit=mPref.edit();
					edit.putBoolean("FirstRun",true);
					edit.commit();
				}
			}).setNegativeButton(R.string.first_disagree,new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog,int which)
				{
					finish();
				}
			}).setCancelable(false).show();
		}
		
		if(mUseAutoLogin)
		{
			mDrawerAdapter.setFooterText(getString(R.string.drawer_status_1));
			new StudentURLConnection(Student.this,StudentURLConnection.TYPE_LOGIN).execute("http://student.gs.hs.kr/student/api/login.do?key=d56b699830e7&userId="+mUserId+"&pwd="+mPwd+"&mdn="+mPhoneNumber+"&type=STUD");
		}
		else mWebView.loadUrl("http://student.gs.hs.kr/student/login.do");
		
		IntentFilter intentFilter=new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		intentFilter.setPriority(2147483647);
		registerReceiver(mBroadcastReceiver,intentFilter);
		isRegistered=true;
	}
	
	@Override
	protected void onResume()
	{
		boolean mUseAutoLoginBACKUP=mUseAutoLogin;
		String userIdBACKUP=mUserId,pwdBACKUP=mPwd;
		super.onResume();
		mUseZoom=mPref.getBoolean("useZoom",false);
		mWebView.getSettings().setBuiltInZoomControls(mUseZoom);
		mUseFilter=mPref.getBoolean("useFilter",true);
		mUserId=mPref.getString("userId","");
		mPwd=mPref.getString("pwd","");
		mUseAutoLogin=mPref.getBoolean("useAutoLogin",false);
		if(!isLogined&&((!mUseAutoLoginBACKUP&&mUseAutoLogin)||(mUseAutoLogin&&!userIdBACKUP.equals(mUserId))||(mUseAutoLogin&&!pwdBACKUP.equals(mPwd))))
		{
			mDrawerAdapter.setFooterText(getString(R.string.drawer_status_1));
			new StudentURLConnection(Student.this,StudentURLConnection.TYPE_LOGIN).execute("http://student.gs.hs.kr/student/api/login.do?key=d56b699830e7&userId="+mUserId+"&pwd="+mPwd+"&mdn="+mPhoneNumber+"&type=STUD");
		}
		if(mPref.getBoolean("isMenuRestored",false))
		{
			mNowFolder=0;
			mDrawerAdapter.clear();
			mDrawerAdapter.add(new StudentMenuItem(true,false,0,"송죽 학사 메인","home","http://student.gs.hs.kr/student/index.do",""));
			mDrawerAdapter.add(new StudentMenuItem(true,false,0,"교사 검색","search","http://student.gs.hs.kr/student/searchTeacher.do",""));
			mDrawerAdapter.add(new StudentMenuItem(true,false,0,"학생 검색","search","http://student.gs.hs.kr/student/searchStudent.do",""));
			mDrawerAdapter.add(new StudentMenuItem(false,false,0,"학교 홈페이지","globe","https://www.gs.hs.kr/",""));
			mDrawerFolder.clear();
			new StudentURLConnection(this,StudentURLConnection.TYPE_LEFTMENU).execute("http://student.gs.hs.kr/student/index.do");
			SharedPreferences.Editor edit=mPref.edit();
			edit.putBoolean("isMenuRestored",false);
			edit.commit();
		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if(isRegistered)
		{
			unregisterReceiver(mBroadcastReceiver);
			isRegistered=false;
		}
	}
	
	@Override
	public boolean onKeyUp(int keyCode,KeyEvent event)
	{
		switch(keyCode)
		{
		case KeyEvent.KEYCODE_BACK:
			if((event.getFlags()&KeyEvent.FLAG_CANCELED)==0)
			{
				if(isDrawerShowing)mDrawerLayout.closeDrawer(GravityCompat.START);
				else if(mWebView.canGoBack())mWebView.goBack();
				else if(!isQuiting)
				{
					Toast.makeText(Student.this,R.string.quit_ing,Toast.LENGTH_SHORT).show();
					isQuiting=true;
					mWebView.postDelayed(new Runnable()
					{
						public void run()
						{
							isQuiting=false;
						}
					},3000);
				}
				else finish();
				return true;
			}
			else return super.onKeyDown(keyCode,event);
		default:
			return super.onKeyDown(keyCode,event);
		}
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
		mToolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.abc_text_size_title_material_toolbar));
		mToolbarSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.abc_text_size_subtitle_material_toolbar));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.student,menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(mDrawerToggle.onOptionsItemSelected(item))return true;
		switch(item.getItemId())
		{
		case R.id.action_refresh:
			mWebView.reload();
			return true;
		case R.id.action_setting:
			startActivity(new Intent("kr.KENNYSOFT.Student.Action.SETTING"));
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
		if(isRegistered)
		{
			unregisterReceiver(mBroadcastReceiver);
			isRegistered=false;
		}
		mDrawerAdapter.setFooterText(String.format(getString(R.string.drawer_status_2),mUserId));
		new StudentURLConnection(this,StudentURLConnection.TYPE_LEFTMENU).execute("http://student.gs.hs.kr/student/index.do");
		if(getIntent().getAction()=="android.intent.action.VIEW")mWebView.loadUrl(getIntent().getDataString());
		else mWebView.loadUrl("http://student.gs.hs.kr/student/index.do");
	}
	
	public void addLeftMenu(String menu)
	{
		String[] lines=menu.split(Pattern.quote("\n"));
		for(String line:lines)
		{
			String[] values=line.split(Pattern.quote("+"));
			if(values[1].startsWith("-"))
			{
				String title;
				if(values[1].startsWith("--"))title=values[1].replace("--","	");
				else title=values[1].replace("-","");
				mDrawerFolder.get(mNowFolder-1).add(new StudentMenuItem(true,false,mNowFolder,title,"",values[0],""));
			}
			else
			{
				String icon="",color="";
				if(values.length>=3)icon=values[2];
				if(values.length>=4)color=values[3];
				mDrawerAdapter.add(new StudentMenuItem(false,false,++mNowFolder,values[1],icon,values[0],color));
				mDrawerFolder.add(new StudentMenuItemList(this));
			}
		}
		isDrawerAdded=true;
	}
	
	@JavascriptInterface
	public void searchStudent(String url,String title)
	{
		startActivityForResult(new Intent("kr.KENNYSOFT.Student.Action.SEARCH_STUDENT").putExtra("title",title).putExtra("cookie",CookieManager.getInstance().getCookie("http://student.gs.hs.kr/student/index.do")).putExtra("url",url),ACTION_SEARCH_STUDENT);
	}
	
	@JavascriptInterface
	public void searchTeacher(String url,String title)
	{
		startActivityForResult(new Intent("kr.KENNYSOFT.Student.Action.SEARCH_TEACHER").putExtra("title",title).putExtra("cookie",CookieManager.getInstance().getCookie("http://student.gs.hs.kr/student/index.do")).putExtra("url",url),ACTION_SEARCH_TEACHER);
	}
	
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent intent)
	{
		super.onActivityResult(requestCode,resultCode,intent);
		if(resultCode!=RESULT_OK)return;
		switch(requestCode)
		{
		case ACTION_SEARCH_STUDENT:
			for(String line:intent.getStringArrayExtra("data"))
			{
				if(VERSION.SDK_INT<VERSION_CODES.KITKAT)mWebView.loadUrl("javascript:studentList.studentSelect('',"+line+");");
				else mWebView.evaluateJavascript("studentList.studentSelect('',"+line+");",null);
			}
			break;
		case ACTION_SEARCH_TEACHER:
			if(VERSION.SDK_INT<VERSION_CODES.KITKAT)mWebView.loadUrl("javascript:teacherSelect('',"+intent.getStringExtra("data")+");");
			else mWebView.evaluateJavascript("teacherSelect('',"+intent.getStringExtra("data")+");",null);
			break;
		}
	}
	
	BroadcastReceiver mBroadcastReceiver=new BroadcastReceiver()
	{
		public void onReceive(Context context,Intent intent)
		{
			try
			{
				Object[] pdus=(Object[])intent.getExtras().get("pdus");
				for(int i=0;i<pdus.length;i++)
				{
					SmsMessage msg=SmsMessage.createFromPdu((byte[])pdus[i]);
					String address=msg.getOriginatingAddress();
					if(mUseFilter&&!address.equals("0312590420"))continue;
					String body=msg.getDisplayMessageBody(),accessCode=null;
					if(body.length()==32&&body.startsWith("[송죽]["))accessCode=body.substring(5,10);
					else if(body.length()==40&&body.startsWith("[Web발신]\n[송죽]["))accessCode=body.substring(13,18);
					else continue;
					if(VERSION.SDK_INT<VERSION_CODES.KITKAT)
					{
						mWebView.loadUrl("javascript:document.getElementById('accessCode').value='"+accessCode+"';");
						mWebView.loadUrl("javascript:document.getElementsByClassName('btn btn-large btn-primary')[0].click();");
					}
					else
					{
						mWebView.evaluateJavascript("document.getElementById('accessCode').value='"+accessCode+"';",null);
						mWebView.evaluateJavascript("document.getElementsByClassName('btn btn-large btn-primary')[0].click();",null);
					}
					unregisterReceiver(mBroadcastReceiver);
					isRegistered=false;
					break;
				}
			}
			catch(Exception e)
			{
			}
		}
	};
}

class StudentURLConnection extends AsyncTask<String,Void,String>
{
	static final int TYPE_LOGIN=1;
	static final int TYPE_LEFTMENU=2;
	
	Context mContext;
	int mType;
	
	StudentURLConnection(Context context,int type)
	{
		mContext=context;
		mType=type;
	}
	
	protected String doInBackground(String... urls)
	{
		String html="",line;
		try
		{
			CookieManager cookieManager=CookieManager.getInstance();
			URLConnection connection=new URL(urls[0]).openConnection();
			connection.setRequestProperty("Cookie",cookieManager.getCookie(urls[0]));
			BufferedReader in=new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((line=in.readLine())!=null)html=html+line+"\n";
			List<String> cookiesHeader=connection.getHeaderFields().get("Set-Cookie");
			if(cookiesHeader!=null)for(String cookie:cookiesHeader)cookieManager.setCookie("student.gs.hs.kr",cookie);
		}
		catch(Exception e)
		{
		}
		return html;
	}
	
	@Override
	protected void onPostExecute(String html)
	{
		switch(mType)
		{
		case TYPE_LOGIN:
			if(html.contains("OK"))((Student)mContext).setAutoLogined();
			else if(html.contains("error"))
			{
				new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.login_error)).setMessage(html.substring(html.indexOf("message\":")+10,html.length()-6)).setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog,int which)
					{
						mContext.startActivity(new Intent("kr.KENNYSOFT.Student.Action.LOGOUT"));
					}
				}).setCancelable(false).show();
			}
			else
			{
				new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.login_failed)).setMessage(mContext.getString(R.string.login_failed_message)).setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog,int which)
					{
						mContext.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
					}
				}).setCancelable(false).show();
			}
			break;
		case TYPE_LEFTMENU:
			String menu="";
			Document document=Jsoup.parse(html);
			for(Element element:document.select("div.navbar.navbar-inverse.navbar-fixed-top").first().getElementsByTag("a"))
			{
				if(element.className().equals("brand"))menu=menu+"+"+element.text()+"++green\n";
				else menu=menu+"http://student.gs.hs.kr"+element.attr("href")+"+-"+element.text()+"\n";
			}
			for(Element element:document.select("div.accordion.leftMenu div.accordion-group"))
			{
				Element heading=element.getElementsByClass("accordion-heading").first();
				if(heading.getElementsByTag("a").size()==0)continue;
				if(heading.getElementsByTag("a").first().attr("href").indexOf('#')==-1)menu=menu+"http://student.gs.hs.kr"+heading.getElementsByTag("a").first().attr("href");
				menu=menu+"+"+heading.getElementsByTag("a").first().text()+"+"+heading.getElementsByTag("i").first().className().substring(5).replace("-","_");
				if(heading.getElementsByTag("a").first().className().length()>23)menu=menu+"+"+heading.getElementsByTag("a").first().className().substring(23);
				menu=menu+"\n";
				if(element.getElementsByClass("accordion-body").size()>0)
				{
					for(Element item:element.getElementsByClass("accordion-body").first().getElementsByTag("a"))
					{
						if(item.parent().parent().className().equals("sub2Menu"))menu=menu+"http://student.gs.hs.kr"+item.attr("href")+"+--"+item.text()+"\n";
						else menu=menu+"http://student.gs.hs.kr"+item.attr("href")+"+-"+item.text()+"\n";
					}
				}
			}
			((Student)mContext).addLeftMenu(menu);
			break;
		}
	}
}

class StudentMenuItem
{
	boolean isLoginNeeded,isExpanded;
	int FolderId;
	String Title,Icon,Link,Color;
	
	StudentMenuItem(boolean isLoginNeeded,boolean isExpanded,int FolderId,String Title,String Icon,String Link,String Color)
	{
		this.isLoginNeeded=isLoginNeeded;
		this.isExpanded=isExpanded;
		this.FolderId=FolderId;
		this.Title=Title;
		this.Icon=Icon;
		this.Link=Link;
		this.Color=Color;
	}
	
	@Override
	public String toString()
	{
		return Title+Icon+Link+Color;
	}
}

@SuppressWarnings("serial")
class StudentMenuItemList extends ArrayList<StudentMenuItem>
{
	Context mContext;
	SharedPreferences mPref;
	
	StudentMenuItemList(Context context)
	{
		mContext=context;
		mPref=PreferenceManager.getDefaultSharedPreferences(mContext);
	}
	
	@Override
	public boolean add(StudentMenuItem object)
	{
		int i;
		for(i=0;i<mPref.getInt("deletedMenuCnt",0);++i)if(object.toString().equals(mPref.getString("deletedMenu"+i,"")))break;
		if(i>=mPref.getInt("deletedMenuCnt",0))return super.add(object);
		else return false;
	}
	
	public boolean addToIndex(int index,StudentMenuItem object)
	{
		int i;
		for(i=0;i<mPref.getInt("deletedMenuCnt",0);++i)if(object.toString().equals(mPref.getString("deletedMenu"+i,"")))break;
		if(i>=mPref.getInt("deletedMenuCnt",0))
		{
			super.add(index,object);
			return true;
		}
		else return false;
	}
}

@SuppressWarnings("deprecation")
class StudentMenuItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	Context mContext;
	int mHeaderPos;
	StudentMenuItemList mDataSet;
	String mHeaderSeason[]={"spring","summer","fall","winter"},mFooterText;
	
	static final int TYPE_HEADER=0;
	static final int TYPE_ITEM=1;
	static final int TYPE_FOOTER=2;
	
	StudentMenuItemAdapter(Context context)
	{
		mContext=context;
		mDataSet=new StudentMenuItemList(mContext);
		mFooterText=mContext.getString(R.string.drawer_status_0);
	}
	
	class StudentMenuHeaderViewHolder extends RecyclerView.ViewHolder
	{
		ImageView mImageView;
		
		StudentMenuHeaderViewHolder(View view)
		{
			super(view);
			mImageView=(ImageView)view.findViewById(R.id.list_header);
		}
	}
	
	class StudentMenuItemViewHolder extends RecyclerView.ViewHolder
	{
		LinearLayout mListItem;
		ImageView mIcon;
		TextView mTitle;
		
		StudentMenuItemViewHolder(View view)
		{
			super(view);
			mListItem=(LinearLayout)view.findViewById(R.id.list_item);
			mIcon=(ImageView)view.findViewById(R.id.list_item_icon);
			mTitle=(TextView)view.findViewById(R.id.list_item_title);
		}
	}
	
	class StudentMenuFooterViewHolder extends RecyclerView.ViewHolder
	{
		TextView mTextView;
		
		StudentMenuFooterViewHolder(View view)
		{
			super(view);
			mTextView=(TextView)view.findViewById(R.id.list_footer);
		}
	}
	
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType)
	{
		switch(viewType)
		{
		case TYPE_HEADER:
			return new StudentMenuHeaderViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_header,parent,false));
		case TYPE_ITEM:
			return new StudentMenuItemViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false));
		case TYPE_FOOTER:
			return new StudentMenuFooterViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_footer,parent,false));
		default:
			return null;
		}
	}
	
	public void onBindViewHolder(RecyclerView.ViewHolder holder,int position)
	{
		switch(getItemViewType(position))
		{
		case TYPE_HEADER:
			onBindHeaderView((StudentMenuHeaderViewHolder)holder);
			break;
		case TYPE_ITEM:
			onBindItemView((StudentMenuItemViewHolder)holder,position-1);
			break;
		case TYPE_FOOTER:
			onBindFooterView((StudentMenuFooterViewHolder)holder);
		}
	}
	
	private void onBindHeaderView(StudentMenuHeaderViewHolder holder)
	{
		holder.mImageView.setImageResource(mContext.getResources().getIdentifier("banner_"+mHeaderSeason[((Calendar.getInstance().get(Calendar.MONTH)+10)%12)/3]+"_"+mHeaderPos,"drawable",mContext.getPackageName()));
	}
	
	private void onBindItemView(StudentMenuItemViewHolder holder,int position)
	{
		ColorMatrix inverter=new ColorMatrix(new float[]{-1,0,0,0,255,0,-1,0,0,255,0,0,-1,0,255,0,0,0,1,0}),multiplier=new ColorMatrix();
		if(mContext.getResources().getIdentifier("glyphicons_"+mDataSet.get(position).Icon,"drawable",mContext.getPackageName())>0)holder.mIcon.setImageResource(mContext.getResources().getIdentifier("glyphicons_"+mDataSet.get(position).Icon,"drawable",mContext.getPackageName()));
		else holder.mIcon.setImageDrawable(null);
		holder.mTitle.setText(mDataSet.get(position).Title);
		if(mDataSet.get(position).Link.length()>0)
		{
			if(((Student)mContext).mWebView.getUrl()!=null&&((Student)mContext).mWebView.getUrl().startsWith(mDataSet.get(position).Link))
			{
				if(mDataSet.get(position).Color.length()==0)holder.mListItem.setBackgroundResource(R.color.menu_bg_active_item);
				else if(mDataSet.get(position).Color.equals("red"))holder.mListItem.setBackgroundResource(R.color.gs_bg_red_active);
				else if(mDataSet.get(position).Color.equals("blue"))holder.mListItem.setBackgroundResource(R.color.gs_bg_blue_active);
				else if(mDataSet.get(position).Color.equals("yellow"))holder.mListItem.setBackgroundResource(R.color.gs_bg_yellow_active);
				else if(mDataSet.get(position).Color.equals("green"))holder.mListItem.setBackgroundResource(R.color.gs_bg_green_active);
				multiplier.setScale(((mContext.getResources().getColor(R.color.color_primary_dark)&0xff0000)>>16)/255f*0.9f,((mContext.getResources().getColor(R.color.color_primary_dark)&0x00ff00)>>8)/255f*0.9f,((mContext.getResources().getColor(R.color.color_primary_dark)&0x0000ff)>>0)/255f*0.9f,1f);
				multiplier.preConcat(inverter);
				holder.mIcon.setColorFilter(new ColorMatrixColorFilter(multiplier.getArray()));
				holder.mTitle.setTextColor(mContext.getResources().getColor(R.color.color_primary_dark));
			}
			else
			{
				if(mDataSet.get(position).Color.length()==0)holder.mListItem.setBackgroundResource(R.color.menu_bg_none);
				else if(mDataSet.get(position).Color.equals("red"))holder.mListItem.setBackgroundResource(R.color.gs_bg_red);
				else if(mDataSet.get(position).Color.equals("blue"))holder.mListItem.setBackgroundResource(R.color.gs_bg_blue);
				else if(mDataSet.get(position).Color.equals("yellow"))holder.mListItem.setBackgroundResource(R.color.gs_bg_yellow);
				else if(mDataSet.get(position).Color.equals("green"))holder.mListItem.setBackgroundResource(R.color.gs_bg_green);
				multiplier.setScale(0.4f,0.4f,0.4f,1f);
				multiplier.preConcat(inverter);
				holder.mIcon.setColorFilter(new ColorMatrixColorFilter(multiplier.getArray()));
				holder.mTitle.setTextColor(mContext.getResources().getColor(R.color.menu_text));
			}
		}
		else
		{
			if(mDataSet.get(position).isExpanded)
			{
				if(mDataSet.get(position).Color.length()==0)holder.mListItem.setBackgroundResource(R.color.menu_bg_active_folder);
				else if(mDataSet.get(position).Color.equals("red"))holder.mListItem.setBackgroundResource(R.color.gs_bg_red_active);
				else if(mDataSet.get(position).Color.equals("blue"))holder.mListItem.setBackgroundResource(R.color.gs_bg_blue_active);
				else if(mDataSet.get(position).Color.equals("yellow"))holder.mListItem.setBackgroundResource(R.color.gs_bg_yellow_active);
				else if(mDataSet.get(position).Color.equals("green"))holder.mListItem.setBackgroundResource(R.color.gs_bg_green_active);
				multiplier.setScale(((mContext.getResources().getColor(R.color.color_primary)&0xff0000)>>16)/255f*0.9f,((mContext.getResources().getColor(R.color.color_primary)&0x00ff00)>>8)/255f*0.9f,((mContext.getResources().getColor(R.color.color_primary)&0x0000ff)>>0)/255f*0.9f,1f);
				multiplier.preConcat(inverter);
				holder.mIcon.setColorFilter(new ColorMatrixColorFilter(multiplier.getArray()));
				holder.mTitle.setTextColor(mContext.getResources().getColor(R.color.color_primary));
			}
			else
			{
				if(mDataSet.get(position).Color.length()==0)holder.mListItem.setBackgroundResource(R.color.menu_bg_none);
				else if(mDataSet.get(position).Color.equals("red"))holder.mListItem.setBackgroundResource(R.color.gs_bg_red);
				else if(mDataSet.get(position).Color.equals("blue"))holder.mListItem.setBackgroundResource(R.color.gs_bg_blue);
				else if(mDataSet.get(position).Color.equals("yellow"))holder.mListItem.setBackgroundResource(R.color.gs_bg_yellow);
				else if(mDataSet.get(position).Color.equals("green"))holder.mListItem.setBackgroundResource(R.color.gs_bg_green);
				multiplier.setScale(0.4f,0.4f,0.4f,1f);
				multiplier.preConcat(inverter);
				holder.mIcon.setColorFilter(new ColorMatrixColorFilter(multiplier.getArray()));
				holder.mTitle.setTextColor(mContext.getResources().getColor(R.color.menu_text));
			}
		}
	}
	
	private void onBindFooterView(StudentMenuFooterViewHolder holder)
	{
		holder.mTextView.setText(mFooterText);
	}
	
	public int getItemCount()
	{
		return mDataSet.size()+2;
	}
	
	@Override
	public int getItemViewType(int position)
	{
		if(position==0)return TYPE_HEADER;
		else if(position==getItemCount()-1)return TYPE_FOOTER;
		else return TYPE_ITEM;
	}
	
	public StudentMenuItem get(int index)
	{
		return mDataSet.get(index);
	}
	
	public boolean add(StudentMenuItem object)
	{
		boolean result=mDataSet.add(object);
		if(result)notifyItemInserted(getItemCount()-2);
		return result;
	}
	
	public boolean addToIndex(int index,StudentMenuItem object)
	{
		boolean result=mDataSet.addToIndex(index,object);
		if(result)notifyItemInserted(index+1);
		return result;
	}
	
	public StudentMenuItem remove(int position)
	{
		StudentMenuItem result=mDataSet.remove(position);
		notifyItemRemoved(position+1);
		return result;
	}
	
	public void clear()
	{
		mDataSet.clear();
		notifyDataSetChanged();
	}
	
	public void setExpanded(int index)
	{
		mDataSet.get(index).isExpanded=true;
		notifyItemChanged(index+1);
	}
	
	public void setCollapsed(int index)
	{
		mDataSet.get(index).isExpanded=false;
		notifyItemChanged(index+1);
	}
	
	public void setHeaderNext()
	{
		mHeaderPos=(mHeaderPos+1)%5;
		notifyItemChanged(0);
	}
	
	public void setFooterText(String text)
	{
		mFooterText=text;
		notifyItemChanged(getItemCount()-1);
	}
}