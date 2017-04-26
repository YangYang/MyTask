package com.imudges.mytask.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import com.imudges.mytask.R;
import com.imudges.mytask.util.Toolkit;

/**
 * 欢迎界面，停留三秒
 * 参考实现：http://blog.csdn.net/huplion/article/details52612098
 * */
public class WelcomeActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /**标题是属于View的，所以窗口所有的修饰部分被隐藏后标题依然有效,需要去掉标题**/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);
        handler.sendEmptyMessageDelayed(0,3000);

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            goLogin();
            super.handleMessage(msg);
        }
    };

    private void goLogin(){
//        ToastUtil.toast(this,"请先登录");
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
