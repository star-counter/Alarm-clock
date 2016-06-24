package com.honliv.z.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * 自定义日历卡
 *
 */
public class CalendarCard extends View {

	private static final int TOTAL_COL = 7; // 7列
	private static final int TOTAL_ROW = 6; // 6行

	private Paint mCirclePaint; // 绘制圆形的画笔
	private Paint mTextPaint; // 绘制文本的画笔
	private int mViewWidth; // 视图的宽度
	private int mViewHeight; // 视图的高度
	private int mCellSpace; // 单元格间距
	private Row rows[] = new Row[TOTAL_ROW]; // 行数组，每个元素代表一行
	private static CustomDate mShowDate; // 自定义的日期，包括year,month,day
	private OnCellClickListener mCellClickListener; // 单元格点击回调事件
	private int touchSlop; //
	private boolean callBackCellSpace;

	private Cell mClickCell;
	private float mDownX;
	private float mDownY;
	private int col, row;
	public static String checkedDay;// 被选中的天

	public List<Date> list;

	Calendar now = Calendar.getInstance();

	/**
	 * 单元格点击的回调接口
	 *
	 * @author huangyi
	 *
	 */
	public interface OnCellClickListener {
		void clickDate(CustomDate date); // 回调点击的日期

		void changeDate(CustomDate date); // 回调滑动ViewPager改变的日期
	}

	public CalendarCard(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public CalendarCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CalendarCard(Context context) {
		super(context);
		init(context);
	}

	public CalendarCard(Context context, OnCellClickListener listener, List<Date> list) {
		super(context);
		this.mCellClickListener = listener;
		this.list = list;
		init(context);
	}

	public CalendarCard(Context context, List<Date> list) {
		super(context);
		this.list = list;
		init(context);
	}

	private void init(Context context) {
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		initDate();
	}

	private void initDate() {
		mShowDate = new CustomDate();
		fillDate();//
	}

	private void fillDate() {

		int monthDay = DateUtil.getCurrentMonthDay(); // 今天
		int lastMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month - 1); // 上个月的天数
		int currentMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month); // 当前月的天数
		int firstDayWeek = DateUtil.getWeekDayFromDate(mShowDate.year, mShowDate.month);// 选中的天在第几列
		boolean isCurrentMonth = false;
		if (DateUtil.isCurrentMonth(mShowDate)) {
			isCurrentMonth = true;
		}
		int day = 0;

		for (int j = 0; j < TOTAL_ROW; j++) {
			rows[j] = new Row(j);
			for (int i = 0; i < TOTAL_COL; i++) {
				int position = i + j * TOTAL_COL; // 单元格位置
				// 这个月的
				if (position >= firstDayWeek && position < firstDayWeek + currentMonthDays) {
					day++;
					rows[j].cells[i] = new Cell(CustomDate.modifiDayForObject(mShowDate, day), State.CURRENT_MONTH_DAY, i, j);
					// 今天
					if (isCurrentMonth && day == monthDay) {
						CustomDate date = CustomDate.modifiDayForObject(mShowDate, day);
						rows[j].cells[i] = new Cell(date, State.TODAY, i, j);
					}
					if (isCurrentMonth && day > monthDay) { // 如果比这个月的今天要大，表示还没到
						rows[j].cells[i] = new Cell(CustomDate.modifiDayForObject(mShowDate, day), State.UNREACH_DAY, i, j);
					}
					if ((position != 0) || (day == 1 && i == 0 && j == 0 && !isCurrentMonth)) {// 选中的日期
						if ((i == col && j == row)) {
							CustomDate date = CustomDate.modifiDayForObject(mShowDate, day);
							rows[j].cells[i] = new Cell(date, State.CHECKED_DAY, i, j);
							checkedDay = day + "";
						}
					}

					for (int k = 0; k < list.size(); k++) {
						now.setTime(list.get(k));
						if (mShowDate.getYear() == now.get(Calendar.YEAR)) {
							if (mShowDate.getMonth() == now.get(Calendar.MARCH) + 1) {
								if (day == now.get(Calendar.DAY_OF_MONTH)) {
									CustomDate date = CustomDate.modifiDayForObject(mShowDate, day);
									rows[j].cells[i] = new Cell(date, State.MARK_DAY, i, j);
								}
							}
						}

					}

					// 过去一个月
				} else if (position < firstDayWeek) {
					rows[j].cells[i] = new Cell(new CustomDate(mShowDate.year, mShowDate.month - 1, lastMonthDays
							- (firstDayWeek - position - 1)), State.PAST_MONTH_DAY, i, j);
					// 下个月
				} else if (position >= firstDayWeek + currentMonthDays) {
					rows[j].cells[i] = new Cell((new CustomDate(mShowDate.year, mShowDate.month + 1, position - firstDayWeek
							- currentMonthDays + 1)), State.NEXT_MONTH_DAY, i, j);
				}
			}
		}
		mCellClickListener.changeDate(mShowDate);
	}

	private void fill() {
		int monthDay = DateUtil.getCurrentMonthDay(); // 今天
		int lastMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month - 1); // 上个月的天数
		int currentMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month); // 当前月的天数
		int firstDayWeek = DateUtil.getWeekDayFromDate(mShowDate.year, mShowDate.month);// 选中的天在第几列
		boolean isCurrentMonth = false;
		if (DateUtil.isCurrentMonth(mShowDate)) {
			isCurrentMonth = true;
		}
		int day = 0;
		for (int j = 0; j < TOTAL_ROW; j++) {
			rows[j] = new Row(j);
			for (int i = 0; i < TOTAL_COL; i++) {
				int position = i + j * TOTAL_COL; // 单元格位置
				// 这个月的
				if (position >= firstDayWeek && position < firstDayWeek + currentMonthDays) {
					day++;
					rows[j].cells[i] = new Cell(CustomDate.modifiDayForObject(mShowDate, day), State.CURRENT_MONTH_DAY, i, j);
					// 今天
					if (isCurrentMonth && day == monthDay) {
						CustomDate date = CustomDate.modifiDayForObject(mShowDate, day);
						rows[j].cells[i] = new Cell(date, State.TODAY, i, j);
					}
					if (isCurrentMonth && day > monthDay) { // 如果比这个月的今天要大，表示还没到
						rows[j].cells[i] = new Cell(CustomDate.modifiDayForObject(mShowDate, day), State.UNREACH_DAY, i, j);
					}

					for (int k = 0; k < list.size(); k++) {
						now.setTime(list.get(k));
						if (mShowDate.getYear() == now.get(Calendar.YEAR)) {
							if (mShowDate.getMonth() == now.get(Calendar.MARCH) + 1) {
								if (day == now.get(Calendar.DAY_OF_MONTH)) {
									CustomDate date = CustomDate.modifiDayForObject(mShowDate, day);
									rows[j].cells[i] = new Cell(date, State.MARK_DAY, i, j);
								}
							}
						}

					}

					// 过去一个月
				} else if (position < firstDayWeek) {
					rows[j].cells[i] = new Cell(new CustomDate(mShowDate.year, mShowDate.month - 1, lastMonthDays
							- (firstDayWeek - position - 1)), State.PAST_MONTH_DAY, i, j);
					// 下个月
				} else if (position >= firstDayWeek + currentMonthDays) {
					rows[j].cells[i] = new Cell((new CustomDate(mShowDate.year, mShowDate.month + 1, position - firstDayWeek
							- currentMonthDays + 1)), State.NEXT_MONTH_DAY, i, j);
				}
			}
		}
		mCellClickListener.changeDate(mShowDate);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < TOTAL_ROW; i++) {
			if (rows[i] != null) {
				rows[i].drawCells(canvas);
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mViewWidth = w;
		mViewHeight = h;
		// 单元间隔距离
		// mCellSpace = Math.min(mViewHeight / TOTAL_ROW, mViewWidth /
		// TOTAL_COL);
		mCellSpace = mViewWidth / TOTAL_COL;
		if (!callBackCellSpace) {
			callBackCellSpace = true;
		}
		mTextPaint.setTextSize(mCellSpace / 3);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// 记录触摸点坐标
				mDownX = event.getX();
				mDownY = event.getY();
				break;
			case MotionEvent.ACTION_UP:
				// 偏移量
				float disX = event.getX() - mDownX;
				float disY = event.getY() - mDownY;
				if (Math.abs(disX) < touchSlop && Math.abs(disY) < touchSlop) {
					col = (int) (mDownX / mCellSpace);
					row = (int) (mDownY / mCellSpace);
					measureClickCell(col, row);
				}
				break;
			default:
				break;
		}

		return true;
	}

	/**
	 * 计算点击的单元格
	 *
	 * @param col
	 * @param row
	 */
	private void measureClickCell(int col, int row) {
		if (col >= TOTAL_COL || row >= TOTAL_ROW)
			return;
		if (mClickCell != null) {
			rows[mClickCell.j].cells[mClickCell.i] = mClickCell;
		}
		if (rows[row] != null) {
			mClickCell = new Cell(rows[row].cells[col].date, rows[row].cells[col].state, rows[row].cells[col].i, rows[row].cells[col].j);
			CustomDate date = rows[row].cells[col].date;
			// --------------
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();

			System.out.println("当前天：" + sf.format(c.getTime()));
			String curTime = sf.format(c.getTime());// 当前时间
			System.out.println("选中天：" + date);
			c.add(Calendar.DAY_OF_MONTH, 7);
			System.out.println("加7天后：" + sf.format(c.getTime()));
			/** 将String转成date */
			java.text.SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			// String strClick = "2011-07-09 ";//选中时间
			String strClick = date.toString();// 选中时间

			// 要变色的天
			String getmoney = "2016-05-13";

			String strLimit = sf.format(c.getTime()).toString();// 当前时间+7后的时间
			Date dateClick = null;
			Date dateLimit = null;
			Date dateCurrent = null;
			Date datemoney = null;
			try {
				datemoney = formatter.parse(getmoney);
				dateClick = formatter.parse(strClick);
				dateCurrent = formatter.parse(curTime);
				dateLimit = formatter.parse(strLimit);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			date.week = col;
			mCellClickListener.clickDate(date);
			update();
		}
	}

	/**
	 * 组元素
	 *
	 * @author huangyi
	 *
	 */
	class Row {
		public int j;

		Row(int j) {
			this.j = j;
		}

		public Cell[] cells = new Cell[TOTAL_COL];// 单元格元素

		// 绘制单元格
		public void drawCells(Canvas canvas) {
			for (int i = 0; i < cells.length; i++) {
				if (cells[i] != null) {
					cells[i].drawSelf(canvas);
				}
			}
		}

	}

	/**
	 * 单元格元素
	 *
	 * @author huangyi
	 *
	 */
	class Cell {
		public CustomDate date;
		public State state;
		public int i;
		public int j;

		public Cell(CustomDate date, State state, int i, int j) {
			super();
			this.date = date;
			this.state = state;
			this.i = i;
			this.j = j;
		}

		public void drawSelf(Canvas canvas) {
			switch (state) {
				case CHECKED_DAY: // 被选中的天
					customPaint(Paint.Style.FILL, Color.parseColor("#F39700"), Color.parseColor("#fffffe"), canvas);
					break;
				case CURRENT_MONTH_DAY: // 当前月日期
					mTextPaint.setColor(Color.BLACK);
					break;
				case PAST_MONTH_DAY: // 过去一个月
				case NEXT_MONTH_DAY: // 下一个月
					mTextPaint.setColor(Color.parseColor("#fffffe"));
					break;
				case UNREACH_DAY: // 还未到的天
					mTextPaint.setColor(Color.BLACK);
					break;
				case TODAY:// 今天
					customPaint(Paint.Style.FILL, Color.parseColor("#d0d0d0"), Color.parseColor("#fffffe"), canvas);
					break;
				case MARK_DAY:// 标记的天
					customPaint(Paint.Style.STROKE, Color.parseColor("#F39700"), Color.parseColor("#F39700"), canvas);
					break;

				default:
					break;
			}
			// 绘制文字
			String content = date.day + "";

			canvas.drawText(content, (float) ((i + 0.5) * mCellSpace - mTextPaint.measureText(content) / 2), (float) ((j + 0.7)
					* mCellSpace - mTextPaint.measureText(content, 0, 1) / 2), mTextPaint);
		}

		/**
		 * 绘制圆圈样式颜色、文字样式颜色
		 *
		 * @param cirleStyle
		 * @param cirlePaintColor
		 * @param textPaintColor
		 * @param canvas
		 */
		public void customPaint(Style cirleStyle, int cirlePaintColor, int textPaintColor, Canvas canvas) {
			mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mCirclePaint.setAntiAlias(true);
			mCirclePaint.setStyle(cirleStyle);// 圆圈的样式
			mCirclePaint.setColor(cirlePaintColor); // 圆圈的颜色
			mTextPaint.setColor(textPaintColor);// 文字的颜色
			canvas.drawCircle((float) (mCellSpace * (i + 0.5)), (float) ((j + 0.5) * mCellSpace), mCellSpace / 3, mCirclePaint);
		}
	}

	/**
	 *
	 * @author huangyi 单元格的状态 当前月日期，过去的月的日期，下个月的日期
	 */
	enum State {
		TODAY, CURRENT_MONTH_DAY, PAST_MONTH_DAY, NEXT_MONTH_DAY, UNREACH_DAY, CHECKED_DAY, MARK_DAY;
	}

	// 从左往右划，上一个月
	public void leftSlide() {
		if (mShowDate.month == 1) {
			mShowDate.month = 12;
			mShowDate.year -= 1;
		} else {
			mShowDate.month -= 1;
		}
		update();
		fill();
		invalidate();
	}

	// 从右往左划，下一个月
	public void rightSlide() {
		if (mShowDate.month == 12) {
			mShowDate.month = 1;
			mShowDate.year += 1;
		} else {
			mShowDate.month += 1;
		}
		// update();
		fill();
		invalidate();
	}

	public void update() {
		fillDate();
		invalidate();
	}

}
