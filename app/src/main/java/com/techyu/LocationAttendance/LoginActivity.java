package com.techyu.LocationAttendance;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;

public class LoginActivity extends Activity implements View.OnClickListener {

    private DBOpenHelper mDBOpenHelper;
    private String userName;
    private String userCategories;
    private SharedPreferences.Editor editor;

    //双击返回记录时间
    private long firstTime = 0;
    private String userResult;

    private EditText et_ID;
    private EditText et_password;
    private Button btn_login;
    private TextView tv_regist;
    private TextView tv_forget;
    private CheckBox cb_rmbPsw;
    ProgressDialog progress;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);//禁止横屏
        setContentView(R.layout.activity_login);

        initView();//初始化界面
        mDBOpenHelper = new DBOpenHelper(this);

        SharedPreferences sp = getSharedPreferences("user_mes", MODE_PRIVATE);
        editor = sp.edit();
        if(sp.getBoolean("flag",false)){
            String user_read = sp.getString("user","");
            String psw_read = sp.getString("psw","");
            et_ID.setText(user_read);
            et_password.setText(psw_read);
        }
    }

    private void initView() {
        //初始化控件
        et_ID = findViewById(R.id.et_login_name);
        et_password = findViewById(R.id.et_login_password);
        btn_login = findViewById(R.id.btn_login);
        tv_regist = findViewById(R.id.tv_login_regist);
        cb_rmbPsw = findViewById(R.id.cb_rmbPsw);
        //设置点击事件监听器
        btn_login.setOnClickListener(this);
        tv_regist.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_login_regist: //注册
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);//跳转到注册界面
                startActivity(intent);
                finish();
                break;
            /**
             * 登录验证：
             *
             * 从EditText的对象上获取文本编辑框输入的数据，并把左右两边的空格去掉
             *  String name = mEtLoginactivityUsername.getText().toString().trim();
             *  String password = mEtLoginactivityPassword.getText().toString().trim();
             *  进行匹配验证,先判断一下用户名密码是否为空，
             *  if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password))
             *  再进而for循环判断是否与数据库中的数据相匹配
             *  if (name.equals(user.getName()) && password.equals(user.getPassword()))
             *  一旦匹配，立即将match = true；break；
             *  否则 一直匹配到结束 match = false；
             *
             *  登录成功之后，进行页面跳转：
             *
             *  Intent intent = new Intent(this, MainActivity.class);
             *  startActivity(intent);
             *  finish();//销毁此Activity
             */
            case R.id.btn_login:
                String user_ID = et_ID.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                if (!TextUtils.isEmpty(user_ID) && !TextUtils.isEmpty(password)) {
                    ArrayList<User> data = mDBOpenHelper.getAllData();
                    boolean match = false;
                    boolean match2 = false;
                    for (int i = 0; i < data.size(); i++) {
                        User user = data.get(i);
                        if ((user_ID.equals(user.getUserID()) && password.equals(user.getPassword()))) {
                            userName = user.getUserName();
                            userCategories = user.getCategories();
                            match = true;
                            if(cb_rmbPsw.isChecked()){
                                editor.putBoolean("flag",true);
                                editor.putString("user",user.getUserID());
                                editor.putString("psw",user.getPassword());
                                editor.apply();
                                match2 = true;
                            }else {
                                editor.putString("user",user.getUserID());
                                editor.putString("psw","");
//                                editor.clear();
                                editor.apply();
                                match2 = false;
                            }
                            break;
                        } else {
                            match = false;
                        }
                    }
                    if (match) {
                        if(match2){
                            Toast.makeText(this, "成功记住密码", Toast.LENGTH_SHORT).show();
                            cb_rmbPsw.setChecked(true);
                        }
                        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                        Runnable target;
                        //用线程启动
                        Thread thread = new Thread(){
                            @Override
                            public void run(){
                                try {
                                    sleep(2000);//2秒 模拟登录时间
                                    String user_name = userName;
                                    Intent intent1 = new Intent(LoginActivity.this, MainActivityNew.class);//设置自己跳转到成功的界面
                                    intent1.putExtra("userName",userName);
                                    intent1.putExtra("userCategories",userCategories);
                                    startActivity(intent1);
                                    finish();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();//打开线程
                    } else {
                        Toast.makeText(this, "用户名或密码不正确，请重新输入", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "请输入你的用户名或密码", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}