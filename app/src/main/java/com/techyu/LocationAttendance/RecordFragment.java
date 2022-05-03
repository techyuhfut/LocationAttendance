
package com.techyu.LocationAttendance;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;



import java.util.ArrayList;
import java.util.List;
import android.view.LayoutInflater;

import org.json.JSONObject;

/**
 * 地理围栏
 */
public class RecordFragment extends Fragment {

    private itemAdapter adapter;
    private List<AttendanceRecord> itemList = null;
    private List<AttendanceRecord> data = null;
    private ListView listview;

    private TextView mtv_name;
    private TextView mtv_categories;

    private static String username;
    private static String usercategories;

    private String DateStr;
    private static  String userName;
    private static  String userCategories;
    private Bundle MainBundle;
    private DBOpenHelper mDBOpenHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View SetLocLayout = inflater.inflate(R.layout.activity_record, container,
                false);
        itemList = new ArrayList<AttendanceRecord>();
        MainBundle = getArguments();
        if (MainBundle != null) {
            userName = MainBundle.getString("userName");
            userCategories = MainBundle.getString("userCategories");
            System.out.println("check测试获取用户数据"+userName+userCategories);
        }
        username="姓名："+userName;
        usercategories="考勤组："+userCategories;
        mtv_name = (TextView) SetLocLayout.findViewById(R.id.tv_name);
        mtv_categories = (TextView) SetLocLayout.findViewById(R.id.tv_categories);

        mtv_name.setText(username);
        mtv_categories.setText(usercategories);
        listview = (ListView) SetLocLayout.findViewById(R.id.recordList);
        mDBOpenHelper = new DBOpenHelper(SetLocLayout.getContext());
        initData();
        return SetLocLayout;
    }//end of oncreat

    /**
     * init the view
     */
    void initData() {
        ArrayList<AttendanceRecord> data = mDBOpenHelper.getAllLocateData();
        for(AttendanceRecord data1:data){
            itemList.add(data1);
        }
        for (int i = 0; i < 10; i++) {
            final AttendanceRecord item = new AttendanceRecord();
            item.setUserName(userName);
            item.setCheckState("考勤成功");
            item.setDate("2022:04:27");
            item.setCheckTime("16:37");
            item.setChecklatiude("118.632704");
            item.setChecklongtidue("35.793217");
            item.setCheckaddr("签到地址：山东省临沂市沂水县三中家属院");
            itemList.add(item);
        }

        adapter = new itemAdapter(getActivity(), R.layout.item, itemList);
        listview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


}
