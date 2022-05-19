package com.example.android_snake;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;


import java.util.Timer;
import java.util.TimerTask;

/**
 * The type Main activity：小蛇移动及方向控制逻辑处理
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FrameLayout Living_Space;
    private boolean isstar=false;
    private Timer timer;
    private Button BT_isstar,BT_RIGHT,BT_UP,BT_DOWN,BT_LEFT;
    private TextView prompt,ScoreView,MostScore;
    private int direction;
    private int weight,height;
    private boolean islive;
    private int score,time,mostscore;
    Images images;
    MoveSnake movesnake;
    SharedPre shared;
    private Handler handler;


    class MoveSnake extends Thread{
        @SuppressLint("HandlerLeak")
       // @Override
        public void run() {
            super.run();
            Looper.prepare();
            handler=new Handler(){
                public void handleMessage(@NonNull Message msg) {
                    if(isstar==true&&islive==true){
                        //方向控制
                        if(msg.what==Macro.Right) {
                            images.setBitmapX(images.getBitmapX() + Macro.UNIT);//The constant UNIT：定义小蛇移动的基本单位,x轴+一个移动单位
                            images.setBitmapY(images.getBitmapY());
                        }
                        if(msg.what==Macro.Up) {
                            images.setBitmapY(images.getBitmapY()-Macro.UNIT);
                            images.setBitmapX(images.getBitmapX());
                        }
                        if(msg.what==Macro.Left) {
                            images.setBitmapX(images.getBitmapX()-Macro.UNIT);
                            images.setBitmapY(images.getBitmapY());
                        }
                        if(msg.what==Macro.Down) {
                            images.setBitmapY(images.getBitmapY()+Macro.UNIT);
                            images.setBitmapX(images.getBitmapX());
                        }
                        //吃掉食物处理
                        if((images.getBitmapX()==images.getFoodX())&&(images.getBitmapY()==images.getFoodY())){
                            images.setLength(images.getLength()+1);
                            int FoodX=(int)(Math.random()*(weight/Macro.UNIT))*Macro.UNIT;
                            int FoodY=(int)(Math.random()*(height/Macro.UNIT))*Macro.UNIT;
                            if(FoodX==images.getBitmapX()&&FoodY==images.getBitmapY()){
                                FoodX++;
                                FoodY++;
                            }
                            images.setFoodX(FoodX);
                            images.setFoodY(FoodY);
                            score+=Macro.SCORE_UNIT;
                            if(score%Macro.FOOD==0){
                                timer.cancel();
                                if(time<Macro.MAX_SPEED)
                                    time+=Macro.SPEED;
                                StarViev(time);
                            }
                        }
                        images.invalidate();

                        //线程中更新控件，用来计分
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ScoreView.setText(Macro.SCORE+String.valueOf(score));
                            }
                        });

                        //死亡判断
                        if(images.getBitmapX()>=weight||images.getBitmapX()<0
                                ||images.getBitmapY()<0||images.getBitmapY()>=height
                                ||images.isIslive()==false){
                            islive=false;
                            //小蛇死亡提醒
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    BT_isstar.setText("重新开始");
                                    prompt.setText(R.string.promptrestar);
                                    prompt.setVisibility(View.VISIBLE);
                                    if(score>mostscore)
                                        mostscore=score;
                                        shared.save(mostscore);
                                }
                            });
                        }
                    }
                    super.handleMessage(msg);
                }


            };
            Looper.loop();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        movesnake =new MoveSnake();
        movesnake.start();
        Init();
        StarViev(time);
        //将图片加载到布局中
        Living_Space.addView(images);
        //获取活动空间的宽和高
        Living_Space.post(new Runnable() {
            @Override
            public void run() {
                weight=Living_Space.getWidth();
                height=Living_Space.getHeight();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        timer.cancel();
    }

    /**
     * Init：初始化该界面控件及参数
     */
//初始化
    void Init(){
        Living_Space=findViewById(R.id.framelayout);
        BT_isstar=findViewById(R.id.isstar);
        BT_RIGHT=findViewById(R.id.Right);
        BT_UP=findViewById(R.id.Up);
        BT_DOWN=findViewById(R.id.Down);
        BT_LEFT=findViewById(R.id.Left);
        prompt=findViewById(R.id.prompt);
        ScoreView=findViewById(R.id.Score);
        MostScore=findViewById(R.id.MostScore);

        images=new Images(MainActivity.this);
        shared=new SharedPre(MainActivity.this);
        direction=Macro.Right;
        images.Init();
        images.Size();
        islive=true;
        score=0;
        mostscore=shared.read();
        time=0;

        BT_isstar.setOnClickListener(this);
        BT_RIGHT.setOnClickListener(this);
        BT_UP.setOnClickListener(this);
        BT_LEFT.setOnClickListener(this);
        BT_DOWN.setOnClickListener(this);
        MostScore.setText(Macro.MOST_SCORE+shared.read());
    }

    /**
     * onClick:该界面按钮监听事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //游戏的开始、暂停按钮
            case R.id.isstar:
                isstar=!isstar;
                if(isstar&&islive) {
                    BT_isstar.setText(R.string.stop);
                    prompt.setVisibility(View.GONE);
                }
                else if(isstar==false&&islive==true){
                    BT_isstar.setText("继续游戏");
                    prompt.setVisibility(View.VISIBLE);
                }
                else if(islive==false){
                    finish();
                }
                break;
            //控制小蛇方向向右
            case R.id.Right:
                if(isstar&&direction!=Macro.Left) {
                    direction = Macro.Right;
                    images.setDirection(direction);
                }
                break;
            //控制小蛇方向向上
            case R.id.Up:
                if(isstar&&direction!=Macro.Down) {
                    direction = Macro.Up;
                    images.setDirection(direction);
                }
                break;
            //控制小蛇方向向左
            case R.id.Left:
                if(isstar&&direction!=Macro.Right) {
                    direction = Macro.Left;
                    images.setDirection(direction);
                }
                break;
            //控制小蛇方向向下
            case R.id.Down:
                if(isstar&&direction!=Macro.Up) {
                    direction = Macro.Down;
                    images.setDirection(direction);
                }
        }
    }


    /**
     * Star viev：定时器，定时发送小蛇方向信息，控制小蛇速度
     *
     * @param time the time
     */

    void StarViev(int time) {
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(direction);
            }
        },Macro.DELAY,Macro.PERIOD-time);
    }
}