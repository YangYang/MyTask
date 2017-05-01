package com.imudges.mytask.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.gson.*;
import com.imudges.mytask.Adapter.MyAdapter;
import com.imudges.mytask.Bean.Task;
import com.imudges.mytask.Bean.User;
import com.imudges.mytask.Listener.MyClickListener;
import com.imudges.mytask.R;
import com.imudges.mytask.Util.ConfigReader;
import com.imudges.mytask.Util.MyParamsBuilder;
import com.imudges.mytask.Util.Toolkit;
import es.dmoral.toasty.Toasty;
import org.xutils.DbManager;
import org.xutils.common.Callback;
import org.xutils.db.table.TableEntity;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    @ViewInject(R.id.listview)
    private ListView listView;
    private String ak = null;
    private List<Map<String, String>> taskList = null;
    private BaseAdapter simpleAdapter = null;
    private DbManager dbManager;
    private String userId = null;

    @ViewInject(R.id.btn_login)
    private Button btnLogin;

    @Event(value = R.id.btn_login,type = View.OnClickListener.class)
    public void login(View view){
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //本地数据的初始化
    private void initDb() {
        DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
                .setDbName("my_task")//设置数据库名
                .setDbVersion(1)//设置数据库版本,每次启动应用时将会检查该版本号,
                //发现数据库版本低于这里设置的值将进行数据库升级并触发DbUpgradeListener
                .setAllowTransaction(true)//设置是否开启事物，默认关闭
                .setTableCreateListener(new DbManager.TableCreateListener() {
                    @Override
                    public void onTableCreated(DbManager dbManager, TableEntity<?> tableEntity) {
                        //数据库创建时的Listener
                    }
                })
                .setDbDir(new File("/sdcard/download/"))
                .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                    @Override
                    public void onUpgrade(DbManager dbManager, int i, int i1) {
                        //设置数据库升级时的Listener，这里可以执行相关数据表的相关修改，比如增加字段等
                    }
                });
        dbManager = x.getDb(daoConfig);
    }

    private MyClickListener myClickListener = new MyClickListener() {

        @Override
        public void edit(int position, View v) {
            Toasty.info(MainActivity.this, "点击了编辑", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void abandon(int position, View v) {
            Toasty.info(MainActivity.this, "点击了放弃", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void changeStatus(int position, View v) {
            Toasty.info(MainActivity.this, "点击了改变状态", Toast.LENGTH_SHORT).show();
        }
    };
//    @ViewInject(R.id.btn_test)
//    private Button btnTest;
//
//    @Event(value = R.id.btn_test,type = View.OnClickListener.class)
//    public void testButtonClick(View v) { // 方法签名必须和接口中的要求一致
//        Toasty.success(MainActivity.this,"hello world",Toast.LENGTH_SHORT,true).show();
//        RequestParams params = new MyParamsBuilder(MainActivity.this,"public/get_user_info.html",true)
//                .builder();
//        x.http().get(params, new Callback.CommonCallback<String>() {
//            @Override
//            public void onSuccess(String s) {
//                Toasty.info(MainActivity.this, s,Toast.LENGTH_SHORT).show();
//
//            }
//
//            @Override
//            public void onError(Throwable throwable, boolean b) {
//                Toasty.error(MainActivity.this, "网络连接失败",Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onCancelled(CancelledException e) {
//
//            }
//
//            // 不管成功或者失败最后都会回调该接口
//            @Override
//            public void onFinished() {
//
//            }
//        });
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDb();
        init();

//        try {
//            dbManager.deleteById(User.class,1);
//        } catch (DbException e) {
//            e.printStackTrace();
//        }
    }


    private void init() {
        userId = getIntent().getStringExtra("userId");
        if (userId != null) {
            //修改本地数据库 userId为当前登录用户的userId
            try {
                dbManager.delete(User.class);
                User user = new User();
                user.setUsername(userId);
                dbManager.saveBindingId(user);
            } catch (DbException e) {
                e.printStackTrace();
            }

            //用户已登录，从服务器获取数据
            RequestParams params = new MyParamsBuilder(MainActivity.this, "public/get_task_info.html", true)
                    .builder();
            x.http().get(params, new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    String res = initNetWorkData(s);
                    if (res.equals("0")) {
                        return;
                    } else {
                        Toasty.error(MainActivity.this, new ConfigReader().read(res), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Throwable throwable, boolean b) {
                    //请求失败时，用数据库内部数据作为数据源
                    initInnerData();
                    Toasty.info(MainActivity.this, "test", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(CancelledException e) {

                }

                @Override
                public void onFinished() {

                }
            });
        } else {
            //TODO 不能读取数据库内部信息
            List <User> list = null;
            try {
                list = dbManager.selector(User.class)
                        .findAll();
            } catch (DbException e) {
                e.printStackTrace();
            }
            if(list!=null && list.size() !=0 ){
                userId = list.get(0).getUsername();
                initInnerData();
            } else {
                //用户未登录，从本地获取数据
                initInnerData();
            }
        }
    }

    //从本地数据库内加载数据
    private void initInnerData() {
        try {
            //查询结果
            List<Task> list = dbManager.selector(Task.class)
                    .where("userId", "=", userId)
                    .findAll();
            this.taskList = new ArrayList<>();
            if (list != null) {
                for (Task task : list) {
                    Map<String, String> map = new HashMap<>();
                    map.put("objId", task.getId() + "");
                    map.put("userId", task.getUserId());
                    map.put("tv_task_name", task.getTaskName());
                    map.put("tv_add_time", task.getAddTime() + "");
                    map.put("tv_summary", task.getSummary());
                    if (task.getStatus() == 0) {
                        map.put("tv_task_status", "完成");
                    }
                    if (task.getStatus() == 1) {
                        map.put("tv_task_status", "未完成");
                    }
                    if (task.getStatus() == -1) {
                        map.put("tv_task_status", "放弃");
                    }
                    map.put("tv_task_name", task.getTaskName());
                    this.taskList.add(map);
                }
                this.simpleAdapter = new MyAdapter(MainActivity.this, this.taskList, myClickListener);
                this.listView.setAdapter(this.simpleAdapter);
                this.listView.setTextFilterEnabled(true);
            }
            return;
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    //从服务器加载数据
    private String initNetWorkData(String s) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject) jsonParser.parse(s);
        int code = jsonObject.get("code").getAsInt();
        //检测请求是否成功
        if (code == 0) {
            //请求成功，清空数据库内部Task
            try {
                dbManager.delete(Task.class);
            } catch (DbException e) {
                e.printStackTrace();
            }
            //向adapter中添加数据集
            JsonArray jsonArray = jsonObject.get("data").getAsJsonObject().get("tasks").getAsJsonArray();
            taskList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject t = jsonArray.get(i).getAsJsonObject();
                Task task = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create()
                        .fromJson(t, Task.class);
                Map<String, String> map = new HashMap<>();
                map.put("objId", task.getId() + "");
                map.put("userId", task.getUserId());
                map.put("tv_task_name", task.getTaskName());
                map.put("tv_add_time", task.getAddTime() + "");
                map.put("tv_summary", task.getSummary());
                if (task.getStatus() == 0) {
                    map.put("tv_task_status", "完成");
                }
                if (task.getStatus() == 1) {
                    map.put("tv_task_status", "未完成");
                }
                if (task.getStatus() == -1) {
                    map.put("tv_task_status", "放弃");
                }
                map.put("tv_task_name", task.getTaskName());
                taskList.add(map);
                //保存数据到数据库内
                try {
                    dbManager.save(task);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
            simpleAdapter = new MyAdapter(MainActivity.this, taskList, myClickListener);
            listView.setAdapter(simpleAdapter);
            listView.setTextFilterEnabled(true);
            return "0";
        } else {
            return jsonObject.get("msg").getAsString();
        }
    }

}
