package com.imudges.mytask.UI;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.imudges.mytask.R;
import es.dmoral.toasty.Toasty;
import org.xutils.http.RequestParams;
import org.xutils.http.app.ParamsBuilder;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by HUPENG on 2017/4/26.
 */
@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity {

    @ViewInject(R.id.btn_login)
    private Button btnLogin;

    @ViewInject(R.id.et_username)
    private EditText etUsername;

    @ViewInject(R.id.et_password)
    private EditText etPassword;

    @Event(value = R.id.btn_login,
            type = View.OnClickListener.class)
    private void onLoginClick(View view){
        //确认用户名与密码
        if (etUsername.getText().toString().equals("") || etPassword.getText().toString().equals("")){
            Toasty.error(LoginActivity.this, "请输入用户名与密码",Toast.LENGTH_SHORT).show();
            return;
        }
        //获取输入的用户名与密码
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        //

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
