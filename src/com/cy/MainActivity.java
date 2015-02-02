package com.cy;

import com.test.robodemo.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import de.greenrobot.event.EventBus;

public class MainActivity extends Activity{
    private TextView textView;
    /** 主线程到主线程A */
    private Button btnMain2MainA;
    /** 主线程到主线程B */
    private Button btnMain2MainB;
    /** 跳到另一个线程 */
    private Button btnToSecondActivity;
    /** 异步执行 */
    private Button btnAsync;
    /** 非UI线程传给主线程 */
    private Button btnNonUIThread2MainThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        IntervalThread mIntervalThread = new IntervalThread();
        mIntervalThread.startTimer(0, 1000);
        
        initView();


        //注册：三个参数分别是，消息订阅者（接收者），接收方法名，事件类
        EventBus.getDefault().register(this,"setTextA",SetTextAEvent.class);
        EventBus.getDefault().register(this,"setTextB",SetTextBEvent.class);
//        EventBus.getDefault().register(this,"messageFromSecondActivity",SecondActivityEvent.class);
        EventBus.getDefault().registerSticky(this, "messageFromSecondActivity", SecondActivityEvent.class);
        EventBus.getDefault().register(this,"countDown",CountDownEvent.class);
    }
    private void initView() {
        setContentView(R.layout.activity_main);
        textView = (TextView)findViewById(R.id.textView);
        btnMain2MainA = (Button)findViewById(R.id.btnMain2MainA);
        btnMain2MainB = (Button)findViewById(R.id.btnMain2MainB);
        btnToSecondActivity = (Button)findViewById(R.id.btnToSecondActivity);
        btnAsync = (Button)findViewById(R.id.btnAsync);
        btnNonUIThread2MainThread = (Button)findViewById(R.id.btnNonUIThread2MainThread);


        btnMain2MainA.setOnClickListener(mOnClickListener);
        btnMain2MainB.setOnClickListener(mOnClickListener);
        btnToSecondActivity.setOnClickListener(mOnClickListener);
        btnAsync.setOnClickListener(mOnClickListener);
        btnNonUIThread2MainThread.setOnClickListener(mOnClickListener);
    }
    /**
     * 与注册对应的方法名和参数,没有后缀，默认使用PostThread模式，即跟发送事件在同一线程执行
     * @param event
     */
    public void setTextAMainThread(SetTextAEvent event)
    {
        if(event.text==null){
            textView.setText("A");            
        }else{
            textView.setText(event.text);
        }

    }
    public void setTextB(SetTextBEvent event)
    {
        textView.setText("B");
    }
    public void messageFromSecondActivity(SecondActivityEvent event)
    {
        //textView.setText(event.getText());  //不能直接更新text，会calledfromwrongthreadexception
    	EventBus.getDefault().post(new SetTextAEvent(event.getText()));
    }
    
    /**
     * 加Async后缀，异步执行。还有MainThread和BackgroundThread，分别是在主线程（UI）执行和后台线程（单一）执行
     * @param event
     */
    public void countDownAsync(CountDownEvent event)
    {
        for(int i=event.getMax();i>0;i--)
        {
            Log.v("CountDown", String.valueOf(i));
            SystemClock.sleep(1000);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        //解注册
        EventBus.getDefault().unregister(this);
    }
    OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
             switch (v.getId()) {
             case R.id.btnMain2MainA:
                 EventBus.getDefault().post(new SetTextAEvent());
             break;
             case R.id.btnMain2MainB:
                 EventBus.getDefault().post(new SetTextBEvent());
             break;
             case R.id.btnToSecondActivity:
                 //测试SecondActivity中发送事件，MainActivity接收
                 Intent intent=new Intent(getApplicationContext(), SecondActivity.class);
                 startActivity(intent);
                 //finish();
             break;
             case R.id.btnAsync:
                 EventBus.getDefault().post(new CountDownEvent(99));
             break;
             case R.id.btnNonUIThread2MainThread:
                 new Thread(new Runnable() {
                     
                     @Override
                     public void run() {
                         try {
                             Thread.sleep(3000);
                         } catch (InterruptedException e) {
                         }
                         EventBus.getDefault().post(new SetTextAEvent("from other thread"));
                     }
                 }).start();
             break;
         }
       }
     };
}
