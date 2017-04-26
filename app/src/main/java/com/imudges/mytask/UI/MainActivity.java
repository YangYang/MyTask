package com.imudges.mytask.UI;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import es.dmoral.toasty.Toasty;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {


    @ViewInject(R.id.btn_test)
    private Button btnTest;

    @Event(value = R.id.btn_test,type = View.OnClickListener.class)
    public void testButtonClick(View v) { // 方法签名必须和接口中的要求一致
        Toasty.success(MainActivity.this,"hello world",Toast.LENGTH_SHORT,true).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
