
package com.techyu.LocationAttendance;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.baidu.geofence.GeoFence;
import com.baidu.geofence.GeoFenceClient;
import com.baidu.geofence.GeoFenceListener;
import com.baidu.geofence.model.DPoint;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.techyu.LocationAttendance.baidu.location.CheckPermissionsActivity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地理围栏
 */
public class AttendanceSettingActivity extends CheckPermissionsActivity
        implements
        OnClickListener,
        GeoFenceListener,
        BaiduMap.OnMapClickListener {

    private EditText et_attendanceId;
    private EditText et_attendanceRadius;
    private EditText etKeyword;

    private Button bt_AddFence;
    private Button bt_RemoveFence;

    /**
     * 用于显示当前的位置
     * 示例中是为了显示当前的位置，在实际使用中，单独的地理围栏可以不使用定位接口
     */
    private LocationClient mlocationClient;
    private MapView mMapView;
    private BaiduMap mBdMap;
    // 中心点坐标
    private LatLng centerLatLng = null;
    // 多边形围栏的边界点
    private List<Marker> markerList = new ArrayList<>();
    // 当前的坐标点集合，主要用于进行地图的可视区域的缩放
    private LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
    // 中心点marker
    private Marker centerMarker;
    private MarkerOptions markerOption = null;
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

    private boolean flag1 = true;
    private boolean flag2 = false;
    private boolean flag3 = false;

    //本地保存数据
    Map<String, String> LocMap = new HashMap<String, String>();
    //打卡数据
    private String  customId;
    private String  radiusStr;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_setting);
        setTitle("地理围栏");
        // 初始化地理围栏
        fenceClient = new GeoFenceClient(getApplicationContext());

        bt_AddFence = findViewById(R.id.bt_addFence);
        bt_RemoveFence = findViewById(R.id.bt_removeFence);
        et_attendanceId = findViewById(R.id.et_attendanceId);
        et_attendanceRadius = findViewById(R.id.et_attendanceradisu);
        etKeyword = findViewById(R.id.et_keyword);
        mMapView = findViewById(R.id.map);
//		mMapView.onCreate(savedInstanceState);
        markerOption = new MarkerOptions().draggable(true);
        init();
        try {
            mlocationClient = new LocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 使用gps定位
        option.setCoorType(GeoFenceClient.BD09LL); // 设置坐标类型
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);

        // 设置locationClientOption
        if (mlocationClient != null) {
            mlocationClient.setLocOption(option);
        }

        // 注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        if (mlocationClient != null){
            mlocationClient.registerLocationListener(myLocationListener);
            mlocationClient.start();
        }
    }//end of oncreat

    /**
     * init the view
     */
    void init() {
        if (mBdMap == null) {
            mBdMap = mMapView.getMap();
            mBdMap.setMyLocationEnabled(true);
            mBdMap.getUiSettings().setRotateGesturesEnabled(false);
            mBdMap.setOnMapClickListener(this);
        }
//        resetView();
        resetViewRound();

        bt_AddFence.setOnClickListener(this);
        bt_RemoveFence.setOnClickListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(GEOFENCE_BROADCAST_ACTION);
        registerReceiver(mGeoFenceReceiver, filter);
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
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * onDestroy
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        try {
            unregisterReceiver(mGeoFenceReceiver);
        } catch (Throwable e) {
        }

        if (null != fenceClient) {
            fenceClient.removeGeoFence();
        }
        if (null != mlocationClient) {
            mlocationClient.stop();
        }
    }

    /**
     * 点击事件重写
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_addFence:
                addFence();
                saveLoc();
                break;
            case R.id.bt_removeFence:
                fenceList.clear();
                fenceClient.removeGeoFence();
                if (mBdMap != null) {
                    mBdMap.clear(); // 批量清除地图图层上所有标记
                }
                fenceMap.clear();
                centerMarker = null;
                break;
            default:
                break;
        }
    }

    private void drawFence(GeoFence fence) {
        drawCircle(fence, false);
        // 设置所有maker显示在当前可视区域地图中
        LatLngBounds bounds = boundsBuilder.build();
        removeMarkers();
    }

    private void drawCircle(GeoFence fence, boolean isPoi) {
        LatLng center;
        int radius;
        if (isPoi) {
            BDLocation bdLocation = new BDLocation();
            bdLocation.setLatitude(fence.getCenter().getLatitude());
            bdLocation.setLongitude(fence.getCenter().getLongitude());
            BDLocation tempLocation = LocationClient
                    .getBDLocationInCoorType(bdLocation, BDLocation.BDLOCATION_GCJ02_TO_BD09LL);
            center = new LatLng(tempLocation.getLatitude(),
                    tempLocation.getLongitude());
        } else {
            center = centerLatLng;
        }
        radius = (int) fence.getRadius();
        // 绘制一个圆形
        if (center == null) {
            return;
        }
        mBdMap.addOverlay(new CircleOptions().center(center)
                .radius(radius)
                .fillColor(0xAA0000FF) // 填充颜色
                .stroke(new Stroke(5, 0xAA00ff00)));
        boundsBuilder.include(center);
        if (!isPoi) {
            centerLatLng = null;
        }
    }


    Object lock = new Object();

    void drawFence2Map() {
        new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (lock) {
                        if (null == fenceList || fenceList.isEmpty()) {
                            return;
                        }
                        for (GeoFence fence : fenceList) {
                            if (fenceMap.containsKey(fence.getFenceId())) {
                                continue;
                            }
                            drawFence(fence);
                            fenceMap.put(fence.getFenceId(), fence);
                        }
                    }
                } catch (Throwable e) {

                }
            }
        }.start();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    StringBuffer sb = new StringBuffer();
                    sb.append("添加打卡范围成功");
                    customId = (String) msg.obj;
                    if (!TextUtils.isEmpty(customId)) {
                        sb.append("考勤业务Id: ").append(customId);
                    }
                    Toast.makeText(getApplicationContext(), sb.toString(),
                            Toast.LENGTH_SHORT).show();
                    drawFence2Map();
                    break;
                case 1:
                    int errorCode = msg.arg1;
                    Toast.makeText(getApplicationContext(),
                            "添加打卡范围失败,errorcode = " + errorCode, Toast.LENGTH_SHORT).show();
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
        handler.sendMessage(msg);
    }

    @Override
    public void onMapPoiClick(MapPoi mapPoi) {

    }

    @Override
    public void onMapClick(LatLng point) {
        markerOption.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.icon_markb));
       centerLatLng = point;
       addCenterMarker(centerLatLng);
       Toast.makeText(this, "选中地图坐标：" + centerLatLng.longitude + ","
               + centerLatLng.latitude, Toast.LENGTH_SHORT).show();//为null
        Log.d("地图选点","经度：" + centerLatLng.longitude + "，纬度：" + centerLatLng.latitude);

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
                    case GeoFence.INIT_STATUS_IN:
                        sb.append("打卡初始状态:在范围内");
                        Toast.makeText(getApplicationContext(), "打卡初始状态:在范围内", Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case GeoFence.INIT_STATUS_OUT:
                        sb.append("打卡初始状态:在范围外");
                        Toast.makeText(getApplicationContext(), "打卡初始状态:在范围外", Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case GeoFence.STATUS_LOCFAIL:
                        sb.append("定位失败,无法判定目标当前位置和打卡范围之间的状态");
                        Toast.makeText(getApplicationContext(), "定位失败,无法判定目标当前位置和打卡范围之间的状态", Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case GeoFence.STATUS_IN:
                        sb.append("进入打卡范围 ");
                        Toast.makeText(getApplicationContext(), "进入打卡范围", Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case GeoFence.STATUS_OUT:
                        sb.append("离开打卡范围 ");
                        Toast.makeText(getApplicationContext(), "离开围栏", Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case GeoFence.STATUS_STAYED:
                        sb.append("在打卡范围内停留超过10分钟 ");
                        Toast.makeText(getApplicationContext(), "在打卡范围内停留超过10分钟", Toast.LENGTH_SHORT)
                                .show();
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
                handler.sendMessage(msg);
            }
        }
    };

    private void addCenterMarker(LatLng latlng) {
        if (null == centerMarker) {
            centerMarker = (Marker) mBdMap.addOverlay(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_markb)));
        }
        centerMarker.setPosition(latlng);
        centerMarker.setVisible(true);
        markerList.add(centerMarker);
    }


    private void removeMarkers() {
        if (null != centerMarker) {
            centerMarker.remove();
            centerMarker = null;
        }
        if (null != markerList && markerList.size() > 0) {
            for (Marker marker : markerList) {
                marker.remove();
            }
            markerList.clear();
        }
    }

    private void resetView() {
//        etCustomId.setVisibility(View.VISIBLE);
//        etCity.setVisibility(View.GONE);
//        etFenceSize.setVisibility(View.GONE);
//        etKeyword.setVisibility(View.GONE);
//        etPoiType.setVisibility(View.GONE);
//        etRadius.setVisibility(View.GONE);
//        tvGuide.setVisibility(View.GONE);
    }

    private void resetViewRound() {
        et_attendanceRadius.setVisibility(View.VISIBLE);
        Toast.makeText(getApplication(),"请点击地图选择打卡地点", Toast.LENGTH_LONG)
                .show();
    }

    private void resetViewDistrict() {
        etKeyword.setVisibility(View.VISIBLE);
    }



    /**
     * 添加圆形围栏
     */
    private void addFence() {
        customId = et_attendanceId.getText().toString();
        radiusStr = et_attendanceRadius.getText().toString();
        if (null == centerLatLng || TextUtils.isEmpty(radiusStr)) {
            Toast.makeText(getApplicationContext(), "考勤参数不全", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        DPoint centerPoint = new DPoint(centerLatLng.latitude,
                centerLatLng.longitude);
        fenceRadius = Float.parseFloat(radiusStr);
        fenceClient.addGeoFence(centerPoint, GeoFenceClient.BD09LL, fenceRadius, customId);
    }




    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBdMap.setMyLocationData(locData);
        }
    }

    /**
     * 保存地图数据
     */
    public void saveLoc(){
        LocMap.put("adminId", "AdminID");
        LocMap.put("customId", customId);
        LocMap.put("radiusStr", radiusStr);
        LocMap.put("longitude",String.format("%.14f",centerLatLng.longitude));
        LocMap.put("latitude",String.format("%.14f",centerLatLng.latitude) );
        LocMap.put("Time", "20:05:31");//显示时间
        LocMap.put("Date","2022:4:26");//保存日期
        CommonUtils.saveSettingNote(AttendanceSettingActivity.this, "LocData", LocMap);
        System.out.println("-------第一次检测完成时hashmap保存成功----------");
    }

}

