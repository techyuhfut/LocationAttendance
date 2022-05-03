package com.techyu.LocationAttendance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.techyu.LocationAttendance.baidu.location.BaiDuLocationActivity;
import com.techyu.LocationAttendance.baidu.location.GeoFenceMultipleActivity;
import com.techyu.LocationAttendance.baidu.map.BaiDuMapActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btBaiduLocation = findViewById(R.id.bt_baidu_location);
        btBaiduLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BaiDuLocationActivity.class);
                startActivity(intent);
            }
        });

        Button btBaiduMap = findViewById(R.id.bt_baidu_map);
        btBaiduMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BaiDuMapActivity.class);
                startActivity(intent);
            }
        });

        Button btBaiduGeoFence = findViewById(R.id.bt_baidu_geofence);
        btBaiduGeoFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GeoFenceMultipleActivity.class);
                startActivity(intent);
            }
        });
        Button btAttendanceSetting=findViewById(R.id.bt_attendance_setting);
        btAttendanceSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AttendanceSettingActivity.class);
                startActivity(intent);
            }
        });

        Button btCheckIn=findViewById(R.id.bt_check_in);
        btCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CheckInActivity.class);
                startActivity(intent);
            }
        });
    }
}