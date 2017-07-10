package com.imudges.mytask.UI;

import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
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
import com.imudges.mytask.Service.UserService;
import com.imudges.mytask.Util.*;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.yalantis.phoenix.PullToRefreshView;
import es.dmoral.toasty.Toasty;
import org.xutils.DbManager;
import org.xutils.common.Callback;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nutz.log.Logs.init;

//TODO 注销和添加的时候需要强制更新数据库内部数据
//TODO 抽取同步数据的方法

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    @ViewInject(R.id.listview)
    private ListView listView;
    private String ak = null;
    private List<Map<String, String>> taskList = null;
    private BaseAdapter simpleAdapter = null;
    private String userId = null;
    private SlidingMenu slidingMenu;
    private final String ACTION_NAME = "REFRESH_LIST";
    private final String CLOSE_MAIN_ACTIVITY = "CLOSE_MAIN_ACTIVITY";
    private UserService userService = new UserService();

    //下拉刷新延迟时间
    private static long REFRESH_DELAY = 1000;
    @ViewInject(R.id.pull_to_refresh)
    private PullToRefreshView mPullToRefreshView;

    //TODO about
    @ViewInject(R.id.btn_about)
    private Button menuBtnAbout;

    public void about(View view) {
        Toasty.info(MainActivity.this, "点击了关于作者", Toast.LENGTH_SHORT).show();
    }


    @ViewInject(R.id.btn_login)
    private Button btnLogin;

    @Event(value = R.id.btn_login, type = View.OnClickListener.class)
    public void login(View view) {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

//    @ViewInject(R.id.btn_delete)
//    private Button btnDelete;

    @ViewInject(R.id.btn_exit)
    private Button menuBtnExit;


    @Event(value = R.id.btn_exit, type = View.OnClickListener.class)
    public void exit(View view) {
        finish();
    }


    @ViewInject(R.id.btn_logout)
    private Button menuBtnLogout;


    @Event(value = R.id.btn_logout, type = View.OnClickListener.class)
    public void logout(View view) {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        List<Task> list = null;
        try {
            list = dbManager.findAll(Task.class);
            userService.syncData(MainActivity.this,list);
        } catch (DbException e) {
            e.printStackTrace();
        }
        finish();
    }

    //TODO 重放问题
    @ViewInject(R.id.btn_change_password)
    private Button menuBtnModifyPassword;

    @Event(value = R.id.btn_change_password, type = View.OnClickListener.class)
    public void modifyPassword(View view) {
        modifyPasswordDialog();
    }

    @ViewInject(R.id.btn_add_task)
    private ImageButton btnAddTask;

    @Event(value = R.id.btn_add_task, type = View.OnClickListener.class)
    public void addTask(View view) {
        Toasty.info(MainActivity.this, "点击了添加", 0).show();
        Intent intent = new Intent(MainActivity.this, AddOrUpdateTaskActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
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
            //TODO 编辑一条未完成的 Task
            Toasty.info(MainActivity.this, "点击了" + position + "位置的编辑", Toast.LENGTH_SHORT).show();
            if(taskList.get(position).get("tv_task_status").equals("完成")){
                Toasty.info(MainActivity.this,"此任务已完成，不能编辑了哟~",Toast.LENGTH_SHORT).show();
            } else if(taskList.get(position).get("tv_task_status").equals("已放弃")) {
                Toasty.info(MainActivity.this,"此任务已放弃，不能编辑了哟~",Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MainActivity.this, AddOrUpdateTaskActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("objId",taskList.get(position).get("objId"));
                startActivity(intent);
            }
        }

        @Override
        public void abandon(int position, View v) {
            if (taskList.get(position).get("tv_task_status").equals("完成")) {
                Toasty.info(MainActivity.this, "该任务已完成，不可放弃", Toast.LENGTH_SHORT).show();
                return;
            }
            if (taskList.get(position).get("tv_task_status").equals("已放弃")) {
                Toasty.info(MainActivity.this, "该任务已放弃，不可重复放弃", Toast.LENGTH_SHORT).show();
                return;
            }
            giveUpTaskDialog(position, v);
        }

        @Override
        public void changeStatus(int position, View v) {
            if (taskList.get(position).get("tv_task_status").equals("完成")) {
                Toasty.info(MainActivity.this, "任务已经完成", Toast.LENGTH_SHORT).show();
            }
            if (taskList.get(position).get("tv_task_status").equals("未完成")) {
                taskList.get(position).put("tv_task_status", "完成");
                //更新数据库
                try {
                    Task task = dbManager.findById(Task.class,taskList.get(position).get("objId"));
                    task.setStatus(0);
                    task.setSyncStatus("1");
                    dbManager.saveOrUpdate(task);
                } catch (DbException e) {
                    e.printStackTrace();
                }
                Toasty.success(MainActivity.this,"更新成功",0).show();
                Intent intent = new Intent(ACTION_NAME);//此处放入的在广播处使用getAction()接收
                //发送广播
                sendBroadcast(intent);
            }
            if (taskList.get(position).get("tv_task_status").equals("已放弃")) {
                Toasty.info(MainActivity.this, "该任务已放弃！", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void delete(int position, View v) {
            try {
                Task task = dbManager.findById(Task.class,taskList.get(position).get("objId"));
                task.setStatus(-2);
                task.setSyncStatus("1");
                dbManager.saveOrUpdate(task);
            } catch (DbException e) {
                e.printStackTrace();
            }
            Toasty.success(MainActivity.this,"删除成功",0).show();
            Intent intent = new Intent(ACTION_NAME);//此处放入的在广播处使用getAction()接收
            //发送广播
            sendBroadcast(intent);
        }
    };

    /**
     * 删除任务的
     * */

    /**
     * 放弃任务的dialog
     */
    private void giveUpTaskDialog(final int position, View v) {
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
                Log.v("text",taskList.get(position) + "");
                taskList.get(position).put("tv_task_status", "已放弃");
                String id = taskList.get(position).get("objId");
                updateLocalDBOneData(id);
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

    /**
     * 更新本地数据库内部某条数据
     */
    private void updateLocalDBOneData(String id) {
        if (id == null || id.equals("")) {
            return;
        }
        try {
            Task task = dbManager.selector(Task.class)
                    .where("id", "=", Integer.parseInt(id))
                    .findFirst();
            if (task != null) {
                task.setStatus(-1);
                task.setSyncStatus("1");
                dbManager.saveOrUpdate(task);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etRePassword;

    /**
     * 修改密码的dialog
     */
    private void modifyPasswordDialog() {
        AlertDialog.Builder giveUpTaskDialog =
                new AlertDialog.Builder(MainActivity.this);
        View dialogView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialoig_modify_password, null);
        giveUpTaskDialog.setTitle("修改密码");
        giveUpTaskDialog.setView(dialogView);
        etOldPassword = (EditText) dialogView.findViewById(R.id.et_old_password);
        etRePassword = (EditText) dialogView.findViewById(R.id.et_re_password);
        etNewPassword = (EditText) dialogView.findViewById(R.id.et_new_password);

        //设置button以及点击事件
        giveUpTaskDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                modifyPassword(etOldPassword.getText().toString(), etNewPassword.getText().toString(), etRePassword.getText().toString());
            }
        });
        giveUpTaskDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        giveUpTaskDialog.show();
    }

    /**
     * 修改密码逻辑
     * -1:旧密码错误
     * 0 :成功
     */
    private void modifyPassword(String oldPassword, String newPassword, String rePassword) {
        if (!newPassword.equals(rePassword)) {
            Toasty.error(MainActivity.this, "两次输入的密码不匹配", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestParams params = new MyParamsBuilder(MainActivity.this, "public/midifiy_password.html", true)
                .addParameter("old_password", Toolkit._3DES_encode(Config.PASSWORD_KEY.getBytes(), oldPassword.getBytes()))
                .addParameter("new_password", Toolkit._3DES_encode(Config.PASSWORD_KEY.getBytes(), newPassword.getBytes()))
                .builder();
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String s) {
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = (JsonObject) jsonParser.parse(s);
                int res = jsonObject.get("code").getAsInt();
                switch (res) {
                    case 0:
                        Toasty.success(MainActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        break;
                    case -5:
                        Toasty.error(MainActivity.this, "旧密码输入错误", Toast.LENGTH_SHORT).show();
                        break;
                }
                return;
            }

            @Override
            public void onError(Throwable throwable, boolean b) {

            }

            @Override
            public void onCancelled(CancelledException e) {

            }

            @Override
            public void onFinished() {

            }
        });

    }

    /**
     * 广播，当添加Task或编辑Task时刷新主界面
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_NAME)) {
                //更新数据
                try {
                    List<Task> list = dbManager.selector(Task.class)
                            .where("userId", "=", userId)
                            .findAll();
                    taskList.clear();
                    for (Task task : list) {
                        Map<String, String> map = new HashMap<>();
                        map.put("objId", task.getId() + "");
                        map.put("userId", task.getUserId());
                        map.put("tv_task_name", task.getTaskName());
                        map.put("tv_add_time", task.getAddTime() + "");
                        map.put("tv_summary", task.getSummary());
                        if (task.getStatus() == 0) {
                            map.put("tv_task_status", "完成");
                            map.put("tv_task_status", "完成");
                        } else if (task.getStatus() == 1) {
                            map.put("tv_task_status", "未完成");
                        } else if (task.getStatus() == -1) {
                            map.put("tv_task_status", "已放弃");
                        } else if(task.getStatus() == -2){
                            continue ;
                        }
                        map.put("tv_task_name", task.getTaskName());
                        taskList.add(map);
                    }
                    simpleAdapter.notifyDataSetChanged();
                    //TODO 什么意思？？
                    mPullToRefreshView.setRefreshing(false);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            } else {
                Toasty.error(MainActivity.this, "添加错误", 0).show();
                mPullToRefreshView.setRefreshing(false);
            }
            if (action.equals(CLOSE_MAIN_ACTIVITY)) {
                finish();
            }
        }
    };

    /**
     * 注册广播
     */
    public void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NAME);
        intentFilter.addAction(CLOSE_MAIN_ACTIVITY);
        //注册广播
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDb();
        initData();
        init();
    }

    /**
     * 初始化一些数据
     */
    private void init() {
        registerBroadcastReceiver();
        initPullRefresh();
        initSlidingMenu();
    }

    /**
     * 下拉刷新
     */
    private void initPullRefresh() {
        mPullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.setRefreshing(false);
                        try {
                            List<Task> list = dbManager.findAll(Task.class);
                            userService.syncData(MainActivity.this,list);
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                        initData();
                    }
                }, REFRESH_DELAY);
            }
        });
    }

    /**
     * 初始化ListView数据
     */
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
     */
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
                        map.put("tv_task_status", "完成");
                    } else if (task.getStatus() == 1) {
                        map.put("tv_task_status", "未完成");
                    } else if (task.getStatus() == -1) {
                        map.put("tv_task_status", "已放弃");
                    } else if(task.getStatus() == -2){
                        continue ;
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
     */
    private String initNetWorkData(String s) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject) jsonParser.parse(s);
        int code = jsonObject.get("code").getAsInt();
        //检测请求是否成功
        if (code == 0) {
            //存入数据库的数据集
            List<Task> taskDataSet = new ArrayList<Task>();

            /**
             * 先存数据库，再添加到adapter里
             * */
            //向adapter中添加数据集
            JsonArray jsonArray = jsonObject.get("data").getAsJsonObject().get("tasks").getAsJsonArray();
            taskList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject t = jsonArray.get(i).getAsJsonObject();
                Task task = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create()
                        .fromJson(t, Task.class);
                taskDataSet.add(task);
            }

            //清空数据库并插入请求得到的数据
            MyDbManager.cleanLocalDataAndInsert(Task.class, taskDataSet);

            for(Task task : taskDataSet){
                Map<String, String> map = new HashMap<>();
                map.put("objId", task.getId() + "");
                map.put("userId", task.getUserId());
                map.put("tv_task_name", task.getTaskName());
                map.put("tv_add_time", task.getAddTime() + "");
                map.put("tv_summary", task.getSummary());
                if (task.getStatus() == 0) {
                    map.put("tv_task_status", "完成");
                    map.put("tv_task_status", "完成");
                } else if (task.getStatus() == 1) {
                    map.put("tv_task_status", "未完成");
                } else if (task.getStatus() == -1) {
                    map.put("tv_task_status", "已放弃");
                } else if(task.getStatus() == -2){
                    continue ;
                }
                map.put("tv_task_name", task.getTaskName());
                taskList.add(map);
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
     */
    private void initSlidingMenu() {
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
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

    }


    private long exitTime = 0;

    /**
     * 再按一次退出功能
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
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
