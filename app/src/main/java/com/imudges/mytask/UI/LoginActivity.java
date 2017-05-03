package com.imudges.mytask.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.imudges.mytask.Bean.Task;
import com.imudges.mytask.R;
import com.imudges.mytask.Util.Config;
import com.imudges.mytask.Util.MyDbManager;
import com.imudges.mytask.Util.MyParamsBuilder;
import com.imudges.mytask.Util.Toolkit;
import es.dmoral.toasty.Toasty;
import org.xutils.DbManager;
import org.xutils.common.Callback;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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

    private String userId = null;

    //数据库对象的初始化
    private DbManager dbManager;
    private MyDbManager myDbManager;

    private void initDb() {
        //单例MyDbManager
        myDbManager = MyDbManager.getMyDbManager();
        dbManager = myDbManager.getDbManagerObj();
    }

    @Event(value = R.id.btn_login,
            type = View.OnClickListener.class)
    private void onLoginClick(View view) {
        //确认用户名与密码
        if (etUsername.getText().toString().equals("") || etPassword.getText().toString().equals("")) {
            Toasty.error(LoginActivity.this, "请输入用户名与密码", Toast.LENGTH_SHORT).show();
            return;
        }
        //获取输入的用户名与密码
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        //创建请求参数对象
        RequestParams params = new MyParamsBuilder("public/login.html", true)
                .addParameter("username", username)
                .addParameter("password", Toolkit._3DES_encode(Config.PASSWORD_KEY.getBytes(), password.getBytes()))
                .builder();

        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String s) {
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = (JsonObject) jsonParser.parse(s);
                int code = jsonObject.get("code").getAsInt();
                if (code == 0) {
                    SharedPreferences sharedPreferences = LoginActivity.this.getSharedPreferences("config", LoginActivity.this.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("ak", jsonObject.get("data").getAsJsonObject().get("ak").getAsString());
                    userId = jsonObject.get("userId").getAsString();
                    editor.commit();
                    //Toasty.success(LoginActivity.this,s,Toast.LENGTH_SHORT).show();
                    goHome();
                } else {
                    Toasty.error(LoginActivity.this, jsonObject.get("msg").getAsString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                Toasty.error(LoginActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(Callback.CancelledException e) {

            }

            // 不管成功或者失败最后都会回调该接口
            @Override
            public void onFinished() {

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDb();
    }

    private void goHome() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Task> taskList = new ArrayList<>();
                try {
                    taskList = dbManager.selector(Task.class).where("userId","=",userId).findAll();
                } catch (DbException e) {
                    e.printStackTrace();
                }
                syncData(taskList);
            }
        }).start();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

    /**
     * 向服务器同步数据
     */
    private void syncData(List<Task> taskList) {
        RequestParams params = new MyParamsBuilder(LoginActivity.this, "public/upload_task.html", false)
                .builder();
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
//        //格式化taskList的时间格式
//        for(int i = 0;i<taskList.size();i++){
//            taskList.get(i).setAddTime( taskList.get(i).getAddTime());
//        }
        String json = gson.toJson(taskList);
        params.addBodyParameter("tasks",json);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(final String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JsonParser jsonParser = new JsonParser();
                        JsonObject jsonObject = (JsonObject) jsonParser.parse(s);
                        int code = jsonObject.get("code").getAsInt();
                        if(code == 0){
                            //TODO 更新本地数据库
                            Toasty.success(LoginActivity.this,"本地数据同步成功",Toast.LENGTH_SHORT).show();
                        } else {
                            Toasty.success(LoginActivity.this,"本地数据同步失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onError(final Throwable throwable, boolean b) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toasty.error(LoginActivity.this,throwable.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(CancelledException e) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

}
