package com.zd.launcher.mylauncher;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {
    private static String TAG = "AddAppWidget";

    private Button btAddShortCut;
    private Button btAddShortCut1;
    private LinearLayout linearLayout;  // 装载Appwidget的父视图

    private static final int MY_REQUEST_APPWIDGET = 1;
    private static final int MY_CREATE_APPWIDGET = 2;

    private static final int HOST_ID = 1024;

    private AppWidgetHost mAppWidgetHost = null;
    AppWidgetManager appWidgetManager = null;
    private List<ResolveInfo> mApps;
    private GridView mGrid;

    List<AppWidgetProviderInfo> list;
    private static final int APPWIDGET_HOST_ID = 0x200;

    private static final int REQUEST_ADD_WIDGET = 11;
    private static final int REQUEST_CREATE_WIDGET = 21;

    AppWidgetManager manager;
    MyWidgetHost myWidgetHostView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        loadApps();
        setContentView(R.layout.activity_main);
        mGrid = (GridView) findViewById(R.id.apps_list);
        mGrid.setAdapter(new AppsAdapter());

        mGrid.setOnItemClickListener(listener);
        btAddShortCut = (Button) findViewById(R.id.bt_addShortcut);
        btAddShortCut1 = (Button) findViewById(R.id.bt_addShortcut1);

        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);


        //其参数hostid大意是指定该AppWidgetHost 即本Activity的标记Id， 直接设置为一个整数值吧 。
        mAppWidgetHost = new AppWidgetHost(MainActivity.this, HOST_ID);

        //为了保证AppWidget的及时更新 ， 必须在Activity的onCreate/onStar方法调用该方法
        // 当然可以在onStop方法中，调用mAppWidgetHost.stopListenering() 停止AppWidget更新
        mAppWidgetHost.startListening();

        //获得AppWidgetManager对象
        appWidgetManager = AppWidgetManager.getInstance(MainActivity.this);
        manager = AppWidgetManager.getInstance(MainActivity.this);

        btAddShortCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示所有能创建AppWidget的列表 发送此 ACTION_APPWIDGET_PICK 的Action
                Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);

                //向系统申请一个新的appWidgetId ，该appWidgetId与我们发送Action为ACTION_APPWIDGET_PICK
                //  后所选择的AppWidget绑定 。 因此，我们可以通过这个appWidgetId获取该AppWidget的信息了

                //为当前所在进程申请一个新的appWidgetId
                int newAppWidgetId = mAppWidgetHost.allocateAppWidgetId();
                Log.i(TAG, "The new allocate appWidgetId is ----> " + newAppWidgetId);

                //作为Intent附加值 ， 该appWidgetId将会与选定的AppWidget绑定
                pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newAppWidgetId);

                //选择某项AppWidget后，立即返回，即回调onActivityResult()方法
                startActivityForResult(pickIntent, MY_REQUEST_APPWIDGET);
            }
        });

        myWidgetHostView =  (MyWidgetHost) findViewById(R.id.myhost);

        myWidgetHostView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectWidgets();
            }
        });

        btAddShortCut1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectWidgets();
            }
        });

    }

    protected void selectWidgets() {
        int widgetId = mAppWidgetHost.allocateAppWidgetId();

        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        startActivityForResult(pickIntent, REQUEST_ADD_WIDGET);
    }

    private void createWidget(Intent data) {
        // 获取选择的widget的id
        int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        // 获取所选的Widget的AppWidgetProviderInfo信息
        AppWidgetProviderInfo appWidget = manager.getAppWidgetInfo(appWidgetId);

        // 根据AppWidgetProviderInfo信息，创建HostView

        View hostView = mAppWidgetHost.createView(this, appWidgetId, appWidget);
        // View view = hostView.findViewById(appWidget.autoAdvanceViewId);
        // ((Advanceable)view).fyiWillBeAdvancedByHostKThx();
        // 将HostView添加到桌面
        myWidgetHostView.addInScreen(hostView, appWidget.minWidth + 100,
                appWidget.minHeight + 200);

        myWidgetHostView.requestLayout();
    }

    // 添加选择的widget。需要判断其是否含有配置，如果有，需要首先进入配置

    private void addWidget(Intent data) {
        int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                -1);
        AppWidgetProviderInfo appWidget = manager.getAppWidgetInfo(appWidgetId);

        Log.d("AppWidget", "configure:" + appWidget.configure);

        if (appWidget.configure != null) {
            // 有配置，弹出配置
            Intent intent = new Intent(
                    AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidget.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            startActivityForResult(intent, REQUEST_CREATE_WIDGET);

        } else {
            // 没有配置，直接添加
            onActivityResult(REQUEST_CREATE_WIDGET, RESULT_OK, data);
        }

    }

    // 如果
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //直接返回，没有选择任何一项 ，例如按Back键
        if (resultCode == RESULT_CANCELED)
            return;

        switch (requestCode) {
            case MY_REQUEST_APPWIDGET:
                Log.i(TAG, "MY_REQUEST_APPWIDGET intent info is -----> " + data);
                int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

                Log.i(TAG, "MY_REQUEST_APPWIDGET : appWidgetId is ----> " + appWidgetId);

                //得到的为有效的id
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    //查询指定appWidgetId的 AppWidgetProviderInfo对象 ， 即在xml文件配置的<appwidget-provider />节点信息
                    AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);

                    //如果配置了configure属性 ， 即android:configure = "" ，需要再次启动该configure指定的类文件,通常为一个Activity
                    if (appWidgetProviderInfo.configure != null) {

                        Log.i(TAG, "The AppWidgetProviderInfo configure info -----> " + appWidgetProviderInfo.configure);

                        //配置此Action
                        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
                        intent.setComponent(appWidgetProviderInfo.configure);
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);


                        startActivityForResult(intent, MY_CREATE_APPWIDGET);
                    } else  //直接创建一个AppWidget
                        onActivityResult(MY_CREATE_APPWIDGET, RESULT_OK, data);  //参数不同，简单回调而已
                }
                break;
            case MY_CREATE_APPWIDGET:
                completeAddAppWidget(data);
                break;

            case REQUEST_ADD_WIDGET:
                addWidget(data);
                break;
            case REQUEST_CREATE_WIDGET:
                createWidget(data);
                break;
            default:
                break;
        }
        if (requestCode == REQUEST_CREATE_WIDGET
                && resultCode == RESULT_CANCELED && data != null) {
            int appWidgetId = data.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }

    //向当前视图添加一个用户选择的
    private void completeAddAppWidget(Intent data) {
        Bundle extra = data.getExtras();
        int appWidgetId = extra.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        //等同于上面的获取方式
        //int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID , AppWidgetManager.INVALID_APPWIDGET_ID) ;

        Log.i(TAG, "completeAddAppWidget : appWidgetId is ----> " + appWidgetId);

        if (appWidgetId == -1) {
            Toast.makeText(MainActivity.this, "添加窗口小部件有误", Toast.LENGTH_SHORT);
            return;
        }

        AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
        AppWidgetHostView hostView = mAppWidgetHost.createView(MainActivity.this, appWidgetId, appWidgetProviderInfo);
        hostView.setClickable(false);
//        linearLayout.addView(hostView) ;

        int widget_minWidht = appWidgetProviderInfo.minWidth;
        int widget_minHeight = appWidgetProviderInfo.minHeight;
        Log.i("Widget:", widget_minWidht + ":" + widget_minHeight);
        //设置长宽  appWidgetProviderInfo 对象的 minWidth 和  minHeight 属性
        FrameLayout.LayoutParams linearLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, widget_minHeight * 2);
        //添加至LinearLayout父视图中
        linearLayout.addView(hostView, linearLayoutParams);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    public void onSetWallpaper() {
        //生成一个设置壁纸的请求
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper, "chooser_wallpaper");
        //发送设置壁纸的请求
        startActivity(chooser);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                onSetWallpaper();
                return true;
        }
        return false;
    }

    private AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ResolveInfo info = mApps.get(position);

            //该应用的包名
            String pkg = info.activityInfo.packageName;
            //应用的主activity类
            String cls = info.activityInfo.name;

            ComponentName componet = new ComponentName(pkg, cls);

            Intent i = new Intent();
            i.setComponent(componet);
            startActivity(i);
        }

    };

    private void loadApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    public class AppsAdapter extends BaseAdapter {
        public AppsAdapter() {
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i;


            if (convertView == null) {
                i = new ImageView(MainActivity.this);
                i.setScaleType(ImageView.ScaleType.FIT_CENTER);
                i.setLayoutParams(new GridView.LayoutParams(50, 50));
            } else {
                i = (ImageView) convertView;
            }

            ResolveInfo info = mApps.get(position);
            i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));

            return i;
        }

        public final int getCount() {
            return mApps.size();
        }

        public final Object getItem(int position) {
            return mApps.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }
    }
}
