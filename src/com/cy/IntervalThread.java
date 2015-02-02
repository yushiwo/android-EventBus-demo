package com.cy;

import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

import android.os.Handler;
import android.os.Message;


/**
 * 保活线程,基于timer实现
 * @author zhengrui
 *
 */
public class IntervalThread {
	/** 周期获取定位信息 的timer的handler */
//	private Handler mPeriodLocationHandlerTimer;
	/** 周期获取定位信息 的thread的handler */
	
	/** 验证码定时器 */
	private Timer mCodeTimer;
	/** 验证码定时器任务 */
	private TimerTask mCodeTimerTask;
	/** 点击获取验证码按钮后的倒计时时长 */
	private static int TIME = 60;
	
	
	private Handler mPeriodLocationHandlerTimer = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
				case 1:
					TIME--;
					new Thread(new Runnable() {

						@Override
						public void run() {
							System.out.println("TIME = " + TIME);
							EventBus.getDefault().postSticky(new SecondActivityEvent("Message From SecondActivity " + TIME));
						}
					}).start();
					
					if(TIME == 0){
						TIME = 60;
					}
			}
		}

	};
	
	/**
	 * 开启timer
	 * @param delay  timer开启延时
	 * @param period  timer间隔
	 */
	public void startTimer(int delay, int period) {
		if (mCodeTimer == null) {
			mCodeTimer = new Timer();
		}
		if (mCodeTimerTask == null) {
			mCodeTimerTask = new TimerTask() {

				@Override
				public void run() {
					Message mMessage = new Message();
					mMessage.what = 1;
					mPeriodLocationHandlerTimer.sendMessage(mMessage);
				}
			};
		}
		if (mCodeTimer != null && mCodeTimerTask != null) {
			mCodeTimer.schedule(mCodeTimerTask, delay, period);
		}
	}

	/**
	 * 关闭timer
	 */
	public void stopTimer() {
		if (mCodeTimer != null) {
			mCodeTimer.cancel();
			mCodeTimer = null;
		}
		if (mCodeTimerTask != null) {
			mCodeTimerTask.cancel();
			mCodeTimerTask = null;
		}
		TIME = 60;
	}
}
