package com.honliv.z.common;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.honliv.z.R;
import com.honliv.z.common.CalendarCard.OnCellClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;




/**
 * 自定义的日历PopWindow
 *
 * @author tzb
 *
 */
public class PopCalendar extends PopupWindow {

	/**
	 * popwindow的显示位置的对齐方式
	 */
	private static int GRAVITY_FOR_POP = Gravity.BOTTOM;
	/**
	 * 起始横坐标
	 */
	private static int X_LOCATION = 0;

	/**
	 * 起始Y坐标
	 */
	private static int Y_LOCATION = 0;

	/**
	 * 当前对象的实例
	 */
	public static PopCalendar instance;

	/**
	 * 用来装日历的ViewPager
	 */
	private static ViewPager mViewPager;

	/**
	 * 日历组件适配器
	 * */
	private static CalendarViewAdapter<CalendarCard> adapter;
	/**
	 * 上一个月选择按钮
	 */
	private static ImageButton preImgBtn;
	/**
	 * 下一个月选择按钮
	 */
	private static ImageButton nextImgBtn;
	/**
	 * 头部显示的年月
	 */
	private static TextView slide_time;
	/**
	 * 默认的ViewPager下标
	 */
	private static int mCurrentIndex = 498;
	/**
	 * 日历卡数据
	 */
	private static CalendarCard[] mShowViews;

	/**
	 * 默认的方向
	 */
	private static SildeDirection mDirection = SildeDirection.NO_SILDE;

	enum SildeDirection {
		RIGHT, LEFT, NO_SILDE;
	}

	/**
	 * 日历上标注的日期(格式"2015-12-25")
	 */
	public static List<Date> liang = new ArrayList<Date>();
	/**
	 * 上下文对象
	 * */
	private Context context;

	private static View parent;

	public PopCalendar(Context context) {
		super(context);
		this.context = context;
	}

	public PopCalendar(View view, int matchParent, int wrapContent, View parent) {
		super(view, matchParent, wrapContent);
		this.parent = parent;
	}

	/**
	 *
	 * @param context
	 *            上下文
	 * @param parent
	 *            要参考的组件
	 * @param gravity
	 *            表示相对位置
	 * @param onClickdate
	 *            点击日期回调方法
	 * @param onConfirm
	 *            点击确定回调方法
	 * @return
	 */
	public static synchronized PopCalendar getInstance(Context context, View parent, int gravity, OnCellClickListener onClickdate,
													   OnClickListener onConfirm, OnDismissListener onDismiss) {
		if (gravity != -1) {
			GRAVITY_FOR_POP = gravity;
		}
		if (context == null) {
			return null;
		}
		if (parent == null) {
			return null;
		}

		if (null == instance) {
			View view = LayoutInflater.from(context).inflate(R.layout.layout_calendar, null);
			mViewPager = (ViewPager) view.findViewById(R.id.activity_user_calendar_Viewpager);
			preImgBtn = (ImageButton) view.findViewById(R.id.activity_user_calendar_PreMonthBtn);
			nextImgBtn = (ImageButton) view.findViewById(R.id.activity_user_calendar_NextMonthBtn);
			slide_time = (TextView) view.findViewById(R.id.slide_time);
			TextView btn_confirm = (TextView) view.findViewById(R.id.tv_confirm);
			// 下面是两种方法得到宽度和高度 getWindow().getDecorView().getWidth()
			instance = new PopCalendar(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, parent);

			final OnCellClickListener listener = onClickdate;

			preImgBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
				}
			});
			nextImgBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
				}
			});

			Calendar c = Calendar.getInstance();

			CalendarCard[] views = new CalendarCard[3];
			for (int i = 0; i < 3; i++) {
				views[i] = new CalendarCard(context, new OnCellClickListener() {

					@Override
					public void clickDate(CustomDate date) {
						if (listener != null) {
							listener.clickDate(date);
						}
						// window.dismiss();
					}

					@Override
					public void changeDate(CustomDate date) {
						slide_time.setText(date.year + "年" + date.month + "月");
					}
				}, liang);
			}

			adapter = new CalendarViewAdapter<CalendarCard>(views);
			setViewPager();

			// 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
			instance.setFocusable(true);

			// 实例化一个ColorDrawable颜色为半透明
			ColorDrawable dw = new ColorDrawable(0xffffffff);
			instance.setBackgroundDrawable(dw);
			final OnClickListener onclick = onConfirm;
			btn_confirm.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (onclick != null) {
						onclick.onClick(v);
					}
					instance.dismiss();
				}
			});

			// 设置popWindow的显示和消失动画
			instance.setAnimationStyle(R.style.mypopwindow_anim_style);
			// 在底部显示
			// View parent = activity.findViewById(R.id.contact_main);
			// window.showAtLocation(parent, GRAVITY_FOR_POP, X_LOCATION,
			// Y_LOCATION);
			final OnDismissListener dListener = onDismiss;
			// popWindow消失监听方法
			instance.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					if (dListener != null) {
						dListener.onDismiss();
					}
					System.out.println("popWindow消失");
				}
			});
		}
		return instance;
	}

	/**
	 * 显示popWindow
	 */
	public static void show() {
		instance.showAsDropDown(parent, X_LOCATION, Y_LOCATION);
	}

	private static void setViewPager() {
		mViewPager.setAdapter(adapter);
		mViewPager.setCurrentItem(498);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {

				measureDirection(position);
				updateCalendarView(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	/**
	 * 计算方向
	 *
	 * @param position
	 *            viewpager的位置
	 */
	private static void measureDirection(int position) {

		if (position > mCurrentIndex) {
			mDirection = SildeDirection.RIGHT;

		} else if (position < mCurrentIndex) {
			mDirection = SildeDirection.LEFT;
		}
		mCurrentIndex = position;
	}

	/**
	 * 更新日历视图
	 *
	 * @param position
	 *            viewpager的位置
	 */
	private static void updateCalendarView(int position) {
		mShowViews = adapter.getAllItems();
		if (mDirection == SildeDirection.RIGHT) {
			mShowViews[position % mShowViews.length].rightSlide();
		} else if (mDirection == SildeDirection.LEFT) {
			mShowViews[position % mShowViews.length].leftSlide();
		}
		mDirection = SildeDirection.NO_SILDE;
	}

	public void destroy() {
		instance = null;
	}
}