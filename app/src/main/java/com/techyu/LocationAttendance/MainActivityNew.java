package com.techyu.LocationAttendance;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/** 登录后进入的主活动，当前版本有 分析 检测和设置  三个fragment
 *
 * 设置APP启动动画
 */
//@RuntimePermissions
public class MainActivityNew extends FragmentActivity implements OnClickListener {
    //双击返回记录时间
    private long firstTime = 0;
    //三个fragment
    private SetLocFragment setLocFragment=null;
    private CheckInFragment checkInFragment=null;
    private RecordFragment recordFragment=null;
    private Bundle LoginBundle;
    private Bundle MainBundle;

    Boolean setLocTransactionAddFlag=false;
    Boolean checkInTransactionAddFlag=false;
    Boolean recordTransactionAddFlag=false;

    private View setLoc_view;
    private View checkIn_view;
    private View record_view;
    private View content_view;

    private ImageView img_setLoc;
    private ImageView img_checkIn;
    private ImageView img_record;

    private TextView tv_setLoc;
    private TextView tv_checkIn;
    private TextView tv_record;

    private Boolean  isPermissionOk=true;


    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private static  String userName;
    private static  String userCategories;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        // 获取定位权限
        getPermissions();
        //控制沉浸式状态栏代码
//        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }


        //初始化界面
        initViews();
//        updateAnalysisData();
        fragmentManager = getSupportFragmentManager();

        Intent intent = getIntent();
        //MainActivity中接受不同的Intent用于开启不同的fragment
        //先判断权限情况
        int id = intent.getIntExtra("flag", 2);
        if (id == 2)
            setTabSelection(2);
        else if(id == 0)
            setTabSelection(0);
        else if(id == 1)
            setTabSelection(1);
    }


    private void initViews() {
        LoginBundle = getIntent().getExtras();
        userName = LoginBundle.getString("userName");
        userCategories = LoginBundle.getString("userCategories");
        System.out.println("测试获取用户数据"+userName+userCategories);
        MainBundle  = new Bundle();
        MainBundle.putString("userName", userName);
        MainBundle.putString("userCategories", userCategories);
        checkIn_view = findViewById(R.id.checkin_layout);
        setLoc_view = findViewById(R.id.setLoc_layout);
        record_view = findViewById(R.id.record_layout);
        content_view=findViewById(R.id.content);

        img_checkIn = findViewById(R.id.checkin_image);
        img_setLoc = findViewById(R.id.setLoc_image);
        img_record =findViewById(R.id.record_image);
        tv_checkIn = findViewById(R.id.checkin_text);
        tv_setLoc =findViewById(R.id.setLoc_text);
        tv_record = findViewById(R.id.record_text);

        checkIn_view.setOnClickListener(this);
        record_view.setOnClickListener(this);
        setLoc_view.setOnClickListener(this);
        content_view.setOnClickListener(this);


    }

    /**
     * Android 从6.0 开始要求一些权限在动态运行时申请
     * 单纯的在 AndroidManifest.xml 注册不起作用
     */
    @TargetApi(23)
    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Dexter.withActivity(this)
                    .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    .withListener(new CompositeMultiplePermissionsListener())
                    .check();
        }
    }

    @Override
    public void onClick(View v) {
        if(isPermissionOk){
            switch (v.getId()) {
                case R.id.checkin_layout:
                    setTabSelection(0);
                    break;
                case R.id.setLoc_layout:
                    setTabSelection(2);
                    break;
                case R.id.record_layout:e:
                    setTabSelection(1);
                    break;
                default:
                    break;
            }
        }else{
            getPermissions();
        }

    }


    public void setTabSelection(int i) {
        // 每次选中之前先清除掉上次的选中状态
        clearSelection();
        // 开启一个Fragment事务
        transaction = fragmentManager.beginTransaction();
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);
        switch (i) {
            case 0://主界面
                // 当点击了消息tab时，改变控件的图片和文字颜色
                img_checkIn.setImageResource(R.drawable.checkin_select);
                tv_checkIn.setTextColor(Color.parseColor("#2EA58C"));
                if (checkInTransactionAddFlag==false) {
                    checkInFragment = new CheckInFragment();
                    checkInFragment.setArguments(MainBundle);
                    transaction.add(R.id.content, checkInFragment);
                    checkInTransactionAddFlag=true;
                } else {
                    transaction.show(checkInFragment);
                }
                break;

            case 1://设置界面
                // 当点击了设置tab时，改变控件的图片和文字颜色
                img_record.setImageResource(R.drawable.record_sele);
                tv_record.setTextColor(Color.parseColor("#2EA58C"));
                if (recordTransactionAddFlag==false) {
                    recordFragment = new RecordFragment();
                    recordFragment.setArguments(MainBundle);
                    transaction.add(R.id.content, recordFragment);
                    recordTransactionAddFlag=true;
                } else {
                     transaction.show(recordFragment);
                }
                break;
            case 2://数据界面页
                // 当点击了分析tab时，改变控件的图片和文字颜色
                img_setLoc.setImageResource(R.drawable.setloc_sele);
                tv_setLoc.setTextColor(Color.parseColor("#2EA58C"));
                if (setLocTransactionAddFlag==false) {
                    setLocFragment = new SetLocFragment();
                    transaction.add(R.id.content, setLocFragment);
                    setLocTransactionAddFlag=true;
                } else {
                    transaction.show(setLocFragment);
                }
                break;
            default:
                break;
        }
        transaction.commit();
    }


    private void clearSelection() {
        img_checkIn.setImageResource(R.drawable.checkin_un);
        img_record.setImageResource(R.drawable.record_un);
        img_setLoc.setImageResource(R.drawable.setloc_un);
        tv_checkIn.setTextColor(Color.parseColor("#B0B5BC"));
        tv_record.setTextColor(Color.parseColor("#B0B5BC"));
        tv_setLoc.setTextColor(Color.parseColor("#B0B5BC"));
    }

    /* *
     * 将所有的Fragment都置为隐藏状态。
     *
     * @param transaction
     *            用于对Fragment执行操作的事务*/

    private void hideFragments(FragmentTransaction transaction) {
        if (checkInTransactionAddFlag==true && checkInFragment!=null) {
            transaction.hide(checkInFragment);
        }
        if (recordTransactionAddFlag==true && recordFragment!=null) {
            transaction.hide(recordFragment);
        }
        if (setLocTransactionAddFlag==true && setLocFragment!=null) {
            transaction.hide(setLocFragment);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }


    //重写返回按键 两次退出
    @Override
    public void onBackPressed() {
        long secondTime = System.currentTimeMillis();
        if (secondTime - firstTime > 2000) {
            Toast.makeText(MainActivityNew.this, "再按一次考勤打卡", Toast.LENGTH_SHORT).show();
            firstTime = secondTime;
        } else{
            finish();
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }




}




