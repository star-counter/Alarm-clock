package com.honliv.z.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.honliv.z.R;
import com.honliv.z.common.CustomDate;
import com.honliv.z.common.PopCalendar;
import com.honliv.z.common.CalendarCard.OnCellClickListener;
public class CalendarActivity extends Activity {

    private Button btn_search;
    private TextView tv_date;
    private PopCalendar mCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        btn_search = (Button) findViewById(R.id.btn_search);
        tv_date = (TextView) findViewById(R.id.tv_date);
        mCalendar = PopCalendar.getInstance(this, btn_search, Gravity.BOTTOM, new OnCellClickListener() {

            @Override
            public void clickDate(CustomDate date) {
                // TODO Auto-generated method stub
                tv_date.setText(date.toString());
            }

            @Override
            public void changeDate(CustomDate date) {
                // TODO Auto-generated method stub
                Log.d("CalenderActivity", "changeDate:" + date.toString());
            }
        }, new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Toast.makeText(CalendarActivity.this, "点击了确定",Toast.LENGTH_LONG).show();

            }
        }, new OnDismissListener() {

            @Override
            public void onDismiss() {
                // TODO Auto-generated method stub
                Log.d("CalendarActivity", "onDismiss");
            }
        });
        btn_search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mCalendar.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mCalendar.destroy();
    }
}
