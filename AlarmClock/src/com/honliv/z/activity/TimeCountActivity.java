package com.honliv.z.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Chronometer;

import com.honliv.z.R;

;


public class TimeCountActivity extends Activity implements OnClickListener {
    private Chronometer timer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_timecount);
        // 获得计时器对象
        timer = (Chronometer) this.findViewById(R.id.chronometer);
        //长按计时器时，出现上下文菜单

        findViewById(R.id.bt_startcount).setOnClickListener(this);
        findViewById(R.id.bt_stopcount).setOnClickListener(this);
        findViewById(R.id.bt_reset).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_startcount:
                // 将计时器清零
                timer.setBase(SystemClock.elapsedRealtime());

                //开始计时
                timer.start();
                break;
            case R.id.bt_stopcount:
                //停止计时
                timer.stop();
                break;
            case R.id.bt_reset:
                //将计时器清零
                timer.setBase(SystemClock.elapsedRealtime());
                break;
        }
    }




}