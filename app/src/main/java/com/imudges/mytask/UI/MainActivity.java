package com.imudges.mytask.UI;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.google.gson.*;
import com.imudges.mytask.Adapter.MyAdapter;
import com.imudges.mytask.Bean.Task;
import com.imudges.mytask.Bean.User;
import com.imudges.mytask.Listener.MyClickListener;
import com.imudges.mytask.R;
import com.imudges.mytask.Util.ConfigReader;
import com.imudges.mytask.Util.MyDbManager;
import com.imudges.mytask.Util.MyParamsBuilder;
import com.imudges.mytask.Util.Toolkit;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.yalantis.phoenix.PullToRefreshView;
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

//TODO 注销和添加的时候需要强制更新数据库内部数据

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    @ViewInject(R.id.listview)
    private ListView listView;
    private String ak = null;
    private List<Map<String, String>> taskList = null;
    private BaseAdapter simpleAdapter = null;
    private String userId = null;
    private SlidingMenu slidingMenu;

    //下拉刷新延迟时间
    private static long REFRESH_DELAY = 1000;
    @ViewInject(R.id.pull_to_refresh)
    private PullToRefreshView mPullToRefreshView;

    //TODO
    @ViewInject(R.id.btn_about)
    private Button menuBtnAbout;
    @Event(value = R.id.btn_about, type = View.OnClickListener.class)
    public void about(View view){
        Toasty.info(MainActivity.this,"点击了关于作者",Toast.LENGTH_SHORT).show();
    }

    @ViewInject(R.id.btn_login)
    private Button btnLogin;
    @Event(value = R.id.btn_login, type = View.OnClickListener.class)
    public void login(View view) {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @ViewInject(R.id.btn_exit)
    private Button menuBtnExit;
    @Event(value = R.id.btn_exit, type = View.OnClickListener.class)
    public void exit(View view){
        finish();
    }

    //TODO
    @ViewInject(R.id.btn_logout)
    private Button menuBtnLogout;
    @Event(value = R.id.btn_logout, type = View.OnClickListener.class)
    public void logout(View view){
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //TODO
    @ViewInject(R.id.btn_change_password)
    private Button menuBtnModifyPassword;
    @Event(value = R.id.btn_change_password, type = View.OnClickListener.class)
    public void modifyPassword(View view){
        Toasty.info(MainActivity.this,"点击了修改密码",Toast.LENGTH_SHORT).show();
    }

    //数据库对象的初始化
    private DbManager dbManager;
    private MyDbManager myDbManager;
    private void initDb() {
        //单例MyDbManager
        myDbManager = MyDbManager.getMyDbManager();
        dbManager = myDbManager.getDbManagerObj();
    }

    /**
     * 点击的监听者
     */
    private MyClickListener myClickListener = new MyClickListener() {

        @Override
        public void edit(int position, View v) {
            Toasty.info(MainActivity.this, "点击了" + position + "位置的编辑", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void abandon(int position, View v) {
            if (taskList.get(position).get("tv_task_status").equals("完成")) {
                Toasty.info(MainActivity.this, "该任务已完成，不可放弃", Toast.LENGTH_SHORT).show();
                return;
            }
            if (taskList.get(position).get("tv_task_status").equals("放弃")) {
                Toasty.info(MainActivity.this, "该任务已放弃，不可重复放弃", Toast.LENGTH_SHORT).show();
                return;
            }
            giveUpTask(position, v);
//            dbManager.saveOrUpdate();
            //Toasty.info(MainActivity.this, "点击了" + position +"位置的放弃", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void changeStatus(int position, View v) {
            if (taskList.get(position).get("tv_task_status").equals("完成")) {
                Toasty.info(MainActivity.this, "任务已经完成", Toast.LENGTH_SHORT).show();
            }
            if (taskList.get(position).get("tv_task_status").equals("未完成")) {
                taskList.get(position).put("tv_task_status", "完成");
                simpleAdapter.notifyDataSetChanged();
            }
            if (taskList.get(position).get("tv_task_status").equals("放弃")) {
                Toasty.info(MainActivity.this, "该任务已放弃！", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 放弃任务的dialog
     */
    private void giveUpTask(final int position, View v) {
        AlertDialog.Builder giveUpTaskDialog =
                new AlertDialog.Builder(MainActivity.this);
        View dialogView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialog_give_up_task, null);
        giveUpTaskDialog.setTitle("放弃任务");
        giveUpTaskDialog.setView(dialogView);
        //设置button以及点击事件
        giveUpTaskDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                taskList.get(position).put("tv_task_status", "放弃");
                simpleAdapter.notifyDataSetChanged();
                Toasty.info(MainActivity.this, "下次制定一个容易一些的任务吧~", Toast.LENGTH_SHORT).show();
            }
        });
        giveUpTaskDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toasty.info(MainActivity.this, "对于任务，我们要不抛弃不放弃！", Toast.LENGTH_SHORT).show();
            }
        });
        giveUpTaskDialog.show();
    }
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
        initData();
        initPullRefresh();
        initSlidingMenu();
    }
    /**
     * 下拉刷新
     * */
    private void initPullRefresh() {
        mPullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.setRefreshing(false);
                        initData();
                    }
                }, REFRESH_DELAY);
            }
        });
    }

    /**
     * 初始化ListView数据
     * */
    private void initData() {

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
            //用户未登录，从本地获取数据
            List<User> list = null;
            try {
                list = dbManager.selector(User.class)
                        .findAll();
            } catch (DbException e) {
                e.printStackTrace();
            }
            if (list != null && list.size() != 0) {
                userId = list.get(0).getUsername();
                initInnerData();
            } else {
                Toasty.error(MainActivity.this, "本地数据读取失败，请登陆后从服务器获取数据", Toast.LENGTH_SHORT).show();
                initInnerData();
            }
        }
    }

    /**
     * 从本地数据库加载数据
     * */
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

    /**
     * 从服务器加载数据
     * */
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
                    dbManager.saveBindingId(task);
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
    /**
     * 侧滑菜单
     * */
    private void initSlidingMenu(){
        //配置sliding menu
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);


        //设置侧滑菜单布局
        slidingMenu.setMenu(R.layout.menu_left_sliding_menu);

        //设置触摸屏幕的模式
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        //设置侧滑菜单的效果
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);

        // 设置滑动菜单视图的宽度
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);

        //设置SlidingMenu是否淡入/淡出
        slidingMenu.setFadeEnabled(true);

        //设置淡入淡出效果的值
        slidingMenu.setFadeDegree(0.4f);
        slidingMenu.attachToActivity(this,SlidingMenu.SLIDING_CONTENT);

    }




























    private long exitTime = 0;
    /**
     * 再按一次退出功能
     * */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
