package com.techyu.LocationAttendance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by qunqun on 2018/8/30.
 */

public class itemAdapter extends ArrayAdapter<AttendanceRecord> {
    private int resource;
    private List<AttendanceRecord> listItems;
    private Context context;
    private String CurrentUser;


    // 构造方法，传入布局、context和数据源list
    public itemAdapter(Context context, int resource, List<AttendanceRecord> objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.listItems = objects;
        this.context = context;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public AttendanceRecord getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public int getCount() {
        if (null == listItems) {
            return 0;
        }
        return listItems.size();
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(final int position, View convertView,
                        ViewGroup viewgroup) {
        final AttendanceRecord item = listItems.get(position);
        final View view;
        final ViewHolder viewholder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resource, null);
            viewholder = new ViewHolder();
            viewholder.tvDate = (TextView) view.findViewById(R.id.tvDate);
            viewholder.tvTime = (TextView) view.findViewById(R.id.tvtime);
            viewholder.tvState = (TextView) view.findViewById(R.id.tvState);
            viewholder.tvLat = (TextView) view.findViewById(R.id.tvLat);
            viewholder.tvAddr = (TextView) view.findViewById(R.id.tvAddr);
            viewholder.imgstate = (ImageView) view.findViewById(R.id.imgstate);
            view.setTag(viewholder);
        } else {
            view = convertView;
            viewholder = (ViewHolder) view.getTag();
        }
        viewholder.tvDate.setText("日期:"+item.getDate());
        viewholder.tvTime.setText("时间:"+item.getCheckTime());
        viewholder.tvState.setText(item.getCheckState());
        if(!item.getCheckState().equals("考勤成功")){
            viewholder.tvState.setTextColor(Color.parseColor("#B22222"));
            viewholder.imgstate.setImageResource(R.drawable.fail);
        }
        else{
            viewholder.tvState.setTextColor(Color.parseColor("#00FF7F"));
            viewholder.imgstate.setImageResource(R.drawable.success);
        }
        viewholder.tvLat.setText("经度："+item.getChecklatiude()+"纬度："+item.getChecklongtidue());
        viewholder.tvAddr.setText(item.getCheckaddr());
//        if("签到正常".equals(item.getCheckState())||"签退正常".equals(item.getCheckState()))
//            viewholder.imgstate.setImageResource(R.drawable.success);
//        else
//            viewholder.imgstate.setImageResource(R.drawable.fail);
        return view;
    }

    /**
     * 清除所有数据
     */
    public void clear() {
        if (listItems != null) {
            listItems.clear();
        }
        //notifyDataSetChanged();
    }

    /**
     * 重新添加数据
     *
     * @author ubuntu
     */
    public void refresh(List<AttendanceRecord> list) {
        if (listItems != null) {
            listItems = list;
            notifyDataSetChanged();
        }
    }


    class ViewHolder {
        TextView tvDate;
        TextView tvTime;
        TextView tvState;
        TextView tvLat;
        TextView tvAddr;
        ImageView imgstate;
    }
}
