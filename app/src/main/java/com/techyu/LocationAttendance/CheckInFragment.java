package com.techyu.LocationAttendance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.baidu.geofence.GeoFence;
import com.baidu.geofence.GeoFenceClient;
import com.baidu.geofence.GeoFenceListener;
import com.baidu.geofence.model.DPoint;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;
import com.techyu.LocationAttendance.baidu.location.LocationService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckInFragment extends Fragment implements
        GeoFenceListener{
    private LocationService locationService;
    private static final int KEY = 1;
    private static String username;
    private static String usercategories;
    private ImageView miv_refresh;
    private ImageView miv_map_status;
    private ImageView miv_circle;
    private TextView mtv_name;
    private TextView mtv_categories;
    private TextView mtv_time;
    private TextView mtv_morning_checkin;
    private TextView mtv_evening_checkout;
    private TextView mtv_checkin;
    private TextView mtv_loc;
    private TextView mtv_location_desc;
    private TextView mtv_checkin_tips;
    private static String customId = "111";
    private static String radiusStr = "1000";
    private static Double longitude = 117.21181919372374;
    private static Double latitude = 31.77826380616622;
    // 地理围栏客户端
    private GeoFenceClient fenceClient = null;
    // 要创建的围栏半径
    private float fenceRadius = 0.0F;//单位m
    // 触发地理围栏的行为，默认为进入提醒
    private int activatesAction = GeoFenceClient.GEOFENCE_IN;
    // 地理围栏的广播action
    private static final String GEOFENCE_BROADCAST_ACTION = "com.example.geofence";
    // 记录已经添加成功的围栏
    private HashMap<String, GeoFence> fenceMap = new HashMap<>();
    private boolean flagloc = false;
    private boolean flagmorn = false;//未打卡false 已打卡true
    private boolean flageven = false;
    private boolean flagtime = false;
    private Date datemorn;
    private Date dateeven;
    private Date datenow;
    private View CheckInLayout;

    private static  String userName;
    private static  String userCategories;
    private Bundle MainBundle;
    private boolean fencereadyflag=false;
    private boolean fencefirstflag=false;

    //本地保存数据
    Map<String, String> HistoryMap = new HashMap<String, String>();

    private String checkState;
    private String date;
    private String checkTime;
    private String checklatiude;
    private String checklongtidue;
    private String checkaddr;
    private DBOpenHelper mDBOpenHelper;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CheckInLayout = inflater.inflate(R.layout.activity_check_in, container,
                false);
        // 获取定位权限
        getPermissions();
        initview();
        mDBOpenHelper = new DBOpenHelper(CheckInLayout.getContext());
        return CheckInLayout;

    }


    public  void initview() {
        MainBundle = getArguments();
        if (MainBundle != null) {
            userName = MainBundle.getString("userName");
            userCategories = MainBundle.getString("userCategories");
            System.out.println("check测试获取用户数据"+userName+userCategories);
        }

        //控件绑定
        miv_refresh = (ImageView) CheckInLayout.findViewById(R.id.iv_refresh);
        miv_map_status = (ImageView) CheckInLayout.findViewById(R.id.iv_map_status);
        miv_circle = (ImageView) CheckInLayout.findViewById(R.id.iv_circle);
        mtv_name = (TextView) CheckInLayout.findViewById(R.id.tv_name);
        mtv_categories = (TextView) CheckInLayout.findViewById(R.id.tv_categories);
        mtv_time = (TextView) CheckInLayout.findViewById(R.id.tv_time);
        mtv_morning_checkin = (TextView) CheckInLayout.findViewById(R.id.morning_checkin);
        mtv_evening_checkout = (TextView) CheckInLayout.findViewById(R.id.evening_checkout);
        mtv_checkin = (TextView) CheckInLayout.findViewById(R.id.tv_checkin);
        mtv_loc = (TextView) CheckInLayout.findViewById(R.id.tv_loc);
        mtv_location_desc = (TextView) CheckInLayout.findViewById(R.id.tv_location_desc);
        mtv_checkin_tips = (TextView) CheckInLayout.findViewById(R.id.tv_checkin_tips);
        username="姓名："+userName;
        usercategories="考勤组："+userCategories;
        mtv_name.setText(username);
        mtv_categories.setText(usercategories);
        new TimeThread().start();
        mtv_loc.setMovementMethod(ScrollingMovementMethod.getInstance());
        // 初始化地理围栏
        fenceClient = new GeoFenceClient(getActivity());
        //设置过滤器
        IntentFilter filter = new IntentFilter();
        filter.addAction(GEOFENCE_BROADCAST_ACTION);
        getActivity().registerReceiver(mGeoFenceReceiver, filter);
        /**
         * 创建pendingIntent
         */
        fenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
        fenceClient.isHighAccuracyLoc(true); // 在即将触发侦听行为时允许开启高精度定位模式(开启gps定位，gps定位结果优先)
        fenceClient.setGeoFenceListener(this);
        /**
         * 设置地理围栏的触发行为,默认为进入
         */
        fenceClient.setActivateAction(GeoFenceClient.GEOFENCE_IN);
        addFence();

        miv_circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击设定
                checkin();
            }
        });

    }

    /**
     *
     */
    private void readRecord(){
        //读取数据库操作

    }

    /**
     * 打卡方法
     */
    private void checkin(){
        if(!fencereadyflag){
            addFence();//判定打卡范围
        }
        //判断打卡时间
        else {
            long curTime = System.currentTimeMillis();
            CharSequence time = DateFormat.format("HH:mm:ss", curTime);
            String timestr = time.toString();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy:MM:dd");
            Date d1 = new Date();
            date= sdf1.format(d1);
            checkTime=timestr;
            try {
                datemorn = sdf.parse("09:00:00");
                dateeven = sdf.parse("18:00:00");
                datenow = sdf.parse(timestr);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (!flagloc) {
                Toast.makeText(getActivity(),
                        "未进入打卡范围", Toast.LENGTH_SHORT).show();
            } else if (flagloc && !flagmorn && datenow.compareTo(datemorn) <= 0) {
                flagmorn = true;
                mtv_morning_checkin.setText("已打卡");
                mtv_checkin.setText("打卡成功");
                checkState="考勤成功";
                saveHistory();
                Toast.makeText(getActivity(),
                        "上班打卡成功", Toast.LENGTH_SHORT).show();
            } else if (flagloc && !flagmorn && datenow.compareTo(datemorn) > 0) {
                flagmorn = true;
                mtv_morning_checkin.setText("已打卡 迟到");
                mtv_checkin.setText("打卡成功");
                checkState="考勤迟到";
                saveHistory();
                Toast.makeText(getActivity(),
                        "上班打卡迟到", Toast.LENGTH_SHORT).show();
            } else if (flagloc && flagmorn && datenow.compareTo(dateeven) < 0) {
                mtv_checkin.setText("已打卡");
                Toast.makeText(getActivity(),
                        "上班已打卡，请勿重复打卡", Toast.LENGTH_SHORT).show();
            } else if (flagloc && !flageven && datenow.compareTo(dateeven) >= 0) {
                flageven = true;
                mtv_evening_checkout.setText("已打卡");
                mtv_checkin.setText("打卡成功");
                checkState="考勤成功";
                saveHistory();
                Toast.makeText(getActivity(),
                        "下班打卡成功", Toast.LENGTH_SHORT).show();
            } else if (flagloc && flageven && datenow.compareTo(dateeven) >= 0) {
                mtv_checkin.setText("已成功");
                checkState="考勤成功";
                Toast.makeText(getActivity(),
                        "下班已打卡，请勿重复打卡", Toast.LENGTH_SHORT).show();
            }

        }
    }
//    /**
//     * onDestroy
//     * 方法必须重写
//     */
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        try {
//            unregisterReceiver(mGeoFenceReceiver);
//        } catch (Throwable e) {
//        }
//
//        if (null != fenceClient) {
//            fenceClient.removeGeoFence();
//        }
////        if (null != mlocationClient) {
////            mlocationClient.stop();
////        }
//    }


    /**
     * 添加圆形围栏
     */
    private void addFence() {
        String longitudeStr=CommonUtils.getSettingNote(getActivity(), "LocData", "longitude");
        if(null==longitudeStr){
            Toast.makeText(getActivity(),"未设置打卡地点,请设置后再进行打卡",Toast.LENGTH_LONG).show();
            System.out.println("未设置打卡地点,请设置后再进行打卡");
            fencereadyflag=false;
        }
        else{
            fencereadyflag=true;
        }
        if(fencereadyflag){
        String latitudeStr=CommonUtils.getSettingNote(getActivity(), "LocData", "latitude");
        radiusStr=CommonUtils.getSettingNote(getActivity(), "LocData", "radiusStr");
        customId=CommonUtils.getSettingNote(getActivity(), "LocData", "customId");
        longitude=Double.parseDouble(longitudeStr);
        latitude=Double.parseDouble(latitudeStr);
        System.out.println("-------地址记录读取成功----------"+radiusStr);
        DPoint centerPoint = new DPoint(latitude, longitude);
        fenceRadius = Float.parseFloat(radiusStr);
        fenceClient.addGeoFence(centerPoint, GeoFenceClient.BD09LL, fenceRadius, customId);
        }
    }


    @SuppressLint("HandlerLeak")
    Handler handlerfence = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    StringBuffer sb = new StringBuffer();
                    sb.append("获取打卡范围成功");
                    String customId = (String) msg.obj;
                    if (!TextUtils.isEmpty(customId)) {
                        sb.append("签到Id: ").append(customId);
                    }
                    Toast.makeText(getActivity(), sb.toString(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    int errorCode = msg.arg1;
                    Toast.makeText(getActivity(),
                            "获取打卡范围失败,errorcode = " + errorCode, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    String statusStr = (String) msg.obj;
                    break;
                default:
                    break;
            }
        }
    };

    List<GeoFence> fenceList = new ArrayList<>();

    @Override
    public void onGeoFenceCreateFinished(final List<GeoFence> geoFenceList,
                                         int errorCode, String customId) {
        Message msg = Message.obtain();
        if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {
            fenceList.addAll(geoFenceList);
            msg.obj = customId;
            msg.what = 0;
        } else {
            msg.arg1 = errorCode;
            msg.what = 1;
        }
        handlerfence.sendMessage(msg);
    }

    /**
     * 接收触发围栏后的广播,当添加围栏成功之后，会立即对所有围栏状态进行一次侦测，只会发送一次围栏和位置之间的初始状态；
     * 当触发围栏之后也会收到广播,对于同一触发行为只会发送一次广播不会重复发送，除非位置和围栏的关系再次发生了改变。
     */
    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 接收广播
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                Bundle bundle = intent.getExtras();
                String customId = bundle
                        .getString(GeoFence.BUNDLE_KEY_CUSTOMID);
                String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
                GeoFence geoFence = bundle.getParcelable(GeoFence.BUNDLE_KEY_FENCE);
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
                int locType = bundle.getInt(GeoFence.BUNDLE_KEY_LOCERRORCODE);
                StringBuffer sb = new StringBuffer();
                switch (status) {
//                    case GeoFence.INIT_STATUS_IN:
//                        sb.append("打卡初始状态:在范围内");
//                        Toast.makeText(getActivity(), "打卡初始状态:在范围内", Toast.LENGTH_SHORT)
//                                .show();
//                        break;
                    case GeoFence.INIT_STATUS_OUT:
//                        sb.append("打卡初始状态:在范围外");
//                        Toast.makeText(getActivity(), "打卡初始状态:在范围外", Toast.LENGTH_SHORT)
//                                .show();
                        break;
                    case GeoFence.STATUS_LOCFAIL:
//                        sb.append("定位失败,无法判定目标当前位置和打卡范围之间的状态");
//                        Toast.makeText(getActivity(), "定位失败,无法判定目标当前位置和打卡范围之间的状态", Toast.LENGTH_SHORT)
//                                .show();
                        break;
                    case GeoFence.STATUS_IN:
                        flagloc=true;
//                        sb.append("进入打卡范围 ");
                        mtv_location_desc.setText("进入管理员指定打卡范围");
                        mtv_checkin_tips.setText("请点击打卡按钮进行签到");
                        miv_map_status.setImageResource(R.drawable.loc_map_success);
//                        Toast.makeText(getActivity(), "进入打卡范围", Toast.LENGTH_SHORT)
//                                .show();
                        break;
                    case GeoFence.STATUS_OUT:
                        flagloc=false;
//                        sb.append("离开打卡范围 ");
//                        Toast.makeText(getActivity(), "离开围栏", Toast.LENGTH_SHORT)
//                                .show();
                        break;
                    case GeoFence.STATUS_STAYED:
//                        sb.append("在打卡范围内停留超过10分钟 ");
//                        Toast.makeText(getActivity(), "在打卡范围内停留超过10分钟", Toast.LENGTH_SHORT)
//                                .show();
                        break;
                    default:
                        break;
                }
                if (status != GeoFence.STATUS_LOCFAIL) {
                    if (!TextUtils.isEmpty(customId)) {
                        sb.append(" customId: " + customId);
                    }
                    sb.append(" fenceId: " + fenceId);
                }
                String str = sb.toString();
                Message msg = Message.obtain();
                msg.obj = str;
                msg.what = 2;
                handlerfence.sendMessage(msg);
            }
        }
    };


    /**
     * Android 从6.0 开始要求一些权限在动态运行时申请
     * 单纯的在 AndroidManifest.xml 注册不起作用
     */
    @TargetApi(23)
    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Dexter.withActivity(getActivity())
                    .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    .withListener(new CompositeMultiplePermissionsListener())
                    .check();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // -----------location config ------------
        locationService = ((MyApplication) getActivity().getApplication()).locationService;
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //注册监听
        int type = getActivity().getIntent().getIntExtra("from", 0);
        if (type == 0) {
            LocationService.setLocationOption(locationService.getDefaultLocationClientOption());
        } else if (type == 1) {
            locationService.start();
        }

        if (mtv_loc.getText().toString().equals("您当前位置：")) {
            locationService.start();// 定位SDK
            // start之后会默认发起一次定位请求，开发者无须判断 isStart 并主动调用request
            mtv_loc.setText("停止定位");
        } else {
            locationService.stop();
            //mtv_loc.setText("百度地图 · 开始定位");
        }
    }

    /***
     * Stop location service
     */
    @Override
    public void onStop() {
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        super.onStop();
    }

    /**
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     */
    private final BDAbstractLocationListener mListener = new BDAbstractLocationListener() {
        /**
         * 定位请求回调函数
         * @param location 定位结果
         */
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                checklatiude=Double.toString(location.getLatitude());
                checklongtidue=Double.toString(location.getLongitude());
                checkaddr=location.getAddrStr();
                StringBuffer sb = new StringBuffer(256);
                sb.append("您当前位置：");
                sb.append(location.getAddrStr());
                // 切回主线程更新 UI
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mtv_loc.setText(sb.toString());
                    }
                });
            }
        }
    };

    public class TimeThread extends Thread {
        @Override
        public void run () {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = KEY;
                    handler.sendMessage(msg);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while(true);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case KEY :
                    long sysTime = System.currentTimeMillis();
                    CharSequence sysTimeStr = DateFormat.format("HH:mm:ss", sysTime);
                    mtv_time.setText(sysTimeStr);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 保存历史数据
     */
    public void saveHistory(){
        mDBOpenHelper.addRecord("userID",userName,checkState,date,checkTime,checklatiude,checklongtidue,checkaddr);
        System.out.println("-------测试保存历史记录----------");
    }
}