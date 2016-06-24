package com.honliv.z.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.view.View.OnClickListener;

import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.honliv.z.R;
import com.honliv.z.common.Alarm;
import com.honliv.z.common.AlarmClockManager;
import com.honliv.z.common.AlarmHandle;

import java.util.List;
public class MainActivity extends Activity implements OnClickListener {



	private final static int DELETE = 1;
	private final static int ABOUT = 2;
	final int RIGHT = 0;
	final int LEFT = 1;


	private int FLING_MIN_DISTANCE = 110;


	private Context context;
	private final static String TAG = "MainActivity";
	private ListView lv_clocks;
	private List<Alarm> alarms;



	private AlarmAdapter adapter;
	//记录按返回键的时间
	private long downTime = 0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//设置无标题栏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		context = this;
		AlarmClockManager.setNextAlarm(context);
		init();

	}

	//初始化
	private void init() {
		//初始化按钮并添加onClick时间
		findViewById(R.id.ib_add).setOnClickListener(this);
		findViewById(R.id.ib_setting).setOnClickListener(this);
		findViewById(R.id.ib_calender).setOnClickListener(this);
		findViewById(R.id.ib_timecount).setOnClickListener(this);
		findViewById(R.id.ib_time).setOnClickListener(this);

		lv_clocks = (ListView) findViewById(R.id.lv_clocks);
		lv_clocks.setDivider(new ColorDrawable(Color.WHITE));
		lv_clocks.setDividerHeight(1);
		//得到当前所有闹钟
		getAlarms(this);
		adapter = new AlarmAdapter();
		lv_clocks.setAdapter(adapter);



	}

	//获得当前所有闹钟
	private void getAlarms(Context context) {
		Log.v(TAG, "获得闹钟列表");
		alarms = AlarmHandle.getAlarms(context);
	}



	//自定义ListView的适配器
	class AlarmAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if(alarms != null){
				return alarms.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return alarms.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if(convertView == null){
				convertView = LayoutInflater.from(context).inflate(R.layout.list_item, null);
				holder = new Holder();
				convertView.setTag(holder);

				//显示闹钟信息的LinearLayout
				holder.ll_info = (LinearLayout) convertView.findViewById(R.id.ll_info);
				//时间
				holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
				//重复
				holder.tv_repeat = (TextView) convertView.findViewById(R.id.tv_repeat);
				//开关
				holder.cb_switch = (CheckBox) convertView.findViewById(R.id.cb_switch);

			}else{
				holder = (Holder) convertView.getTag();
			}

			final Alarm alarm = alarms.get(position);
			holder.ll_info.setTag(alarm.id);
			//单击进入编辑界面
			holder.ll_info.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					Intent intent = new Intent(context, NewClockActivity.class);
					intent.putExtra("alarm",alarms.get(position));
					startActivityForResult(intent, 10);
				}
			});
			String hourStr = (alarm.hour+"").length() == 1 ? "0" + alarm.hour : alarm.hour + "";
			String minutesStr = (alarm.minutes+"").length() == 1 ? "0" + alarm.minutes : alarm.minutes + "";
			holder.tv_time.setText(hourStr + ":" + minutesStr);
			holder.tv_repeat.setText(alarm.repeat);

			holder.cb_switch.setChecked(alarm.enabled == 1 ? true : false);

			//开关控制
			holder.cb_switch.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ContentValues values = new ContentValues();
					boolean isChecked = true;
					if(((CheckBox)v).isChecked()){
						isChecked = true;
					}else{
						isChecked = false;
					}
					//更新数据库里中的数据
					values.put(Alarm.Columns.ENABLED, isChecked ? 1 : 0);
					AlarmHandle.updateAlarm(context, values, alarm.id);
					alarms.get(position).enabled = isChecked ? 1 : 0;
					if(isChecked){
						//打开闹钟
						AlarmClockManager.setAlarm(context, alarm);
					}else{
						//关闭闹钟
						AlarmClockManager.cancelAlarm(context, alarm.id);
					}

				}
			});
			return convertView;
		}

		class Holder{
			LinearLayout ll_info;
			TextView tv_time;
			TextView tv_repeat;
			CheckBox cb_switch;
		}
	}

	/*
	 * 上层页面返回后做相应操作 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.v(TAG, "更新adpater");
		switch(resultCode){
			case Alarm.UPDATE_ALARM :
				if(adapter != null){
					getAlarms(context);
					adapter.notifyDataSetChanged();
				}
				Alarm alarm = (Alarm) data.getSerializableExtra("alarm");
				if(alarm != null){
					//打开闹钟
					AlarmClockManager.setAlarm(context, alarm);
				}
				break;
			case Alarm.DELETE_ALARM:
				if(adapter != null){
					getAlarms(context);
					adapter.notifyDataSetChanged();
				}
				break;
		}
	}

	/*
	 * 主界面两个按钮的点击事件 
	 */
	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch(v.getId()){
			case R.id.ib_add:
				intent = new Intent(this,NewClockActivity.class);
				startActivityForResult(intent, 10);
				break;
			case R.id.ib_calender:
				intent =new Intent(this,CalendarActivity.class);
				startActivity(intent);
				break;
			case R.id.ib_time:
				intent =new Intent(this,ClockActivity.class);
				startActivity(intent);
				break;
			case R.id.ib_timecount:
				intent=new Intent(this,TimeCountActivity.class);
				startActivity(intent);
				break;
			case R.id.ib_setting:
				intent = new Intent(this,SettingActivity.class);
				startActivity(intent);
				break;
		}

	}

	/*
	 * 界面再次获得焦点 更新数据 
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(adapter != null){
			Log.v(TAG, "onResume中更新闹钟列表");
			getAlarms(context);
			adapter.notifyDataSetChanged();
		}
	}

	/*
	 * 创建菜单 
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*
	 * 菜单项点击事件
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id){
			case R.id.action_add:
				Intent intent = new Intent(this,NewClockActivity.class);
				startActivityForResult(intent, 10);
				break;
			case R.id.action_del:
				showDialog(DELETE);
				break;
			case R.id.action_about:
				showDialog(ABOUT);
				break;

		}
		return super.onOptionsItemSelected(item);
	}


	/*
	 * 创建对话框。 
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch(id){
			case DELETE:
				dialog = new AlertDialog.Builder(context)
						.setTitle(R.string.action_del)
						.setMessage("确定要删除所有闹钟吗？")
						.setNegativeButton("取消", null)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								AlarmHandle.deleteAllAlarm(context);
								//更新adpater
								if(adapter != null){
									getAlarms(context);
									adapter.notifyDataSetChanged();
								}
								dialog.dismiss();
							}
						}).create();
				break;
			case ABOUT:
				dialog = new AlertDialog.Builder(context)
						.setTitle(R.string.action_about)
						.setMessage(R.string.about_content)
						.setNegativeButton("关闭", null)
						.create();
				break;
		}
		return dialog;
	}









	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(System.currentTimeMillis() - downTime > 2000){
				Toast.makeText(context, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				downTime = System.currentTimeMillis();
			}else{
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
