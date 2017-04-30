package com.imudges.mytask.UI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.google.gson.*;
import com.imudges.mytask.Adapter.MyAdapter;
import com.imudges.mytask.Bean.Task;
import com.imudges.mytask.Listener.MyClickListener;
import com.imudges.mytask.R;
import com.imudges.mytask.Util.ConfigReader;
import com.imudges.mytask.Util.MyParamsBuilder;
import com.imudges.mytask.Util.Toolkit;
import es.dmoral.toasty.Toasty;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    @ViewInject(R.id.listview)
    private ListView listView;

    private String ak = null;

    private List<Map<String,String>> taskList = null;

    private BaseAdapter simpleAdapter = null;
    private MyClickListener myClickListener = new MyClickListener() {

        @Override
        public void edit(int position, View v) {
            Toasty.info(MainActivity.this,"点击了编辑",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void abandon(int position, View v) {
            Toasty.info(MainActivity.this,"点击了放弃",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void changeStatus(int position, View v) {
            Toasty.info(MainActivity.this,"点击了改变状态",Toast.LENGTH_SHORT).show();
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
        init();
    }

    private void init() {
//        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences("config",MainActivity.this.MODE_PRIVATE);
//        ak = sharedPreferences.getString("ak",null);
//        if(ak!=null){

        //发送请求的Builder中添加了ak
        RequestParams params = new MyParamsBuilder(MainActivity.this, "public/get_task_info.html", true)
                .builder();
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String s) {
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = (JsonObject) jsonParser.parse(s);
                int code = jsonObject.get("code").getAsInt();
                //检测请求是否成功
                if (code == 0) {
                    JsonArray jsonArray = jsonObject.get("data").getAsJsonObject().get("tasks").getAsJsonArray();
                    taskList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject t = jsonArray.get(i).getAsJsonObject();
                        Task task = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                                .create()
                                .fromJson(t, Task.class);
                        Map<String,String> map = new HashMap<>();
                        map.put("objId",task.getId() + "");
                        map.put("userId",task.getUserId());
                        map.put("tv_task_name",task.getTaskName());
                        map.put("tv_add_time",task.getAddTime() + "");
                        map.put("tv_summary",task.getSummary());
                        if(task.getStatus() == 0){
                            map.put("tv_task_status","完成");
                        }
                        if(task.getStatus() == 1){
                            map.put("tv_task_status","未完成");
                        }
                        if(task.getStatus() == -1){
                            map.put("tv_task_status","放弃");
                        }
                        map.put("tv_task_name",task.getTaskName());
                        taskList.add(map);
                    }

                    simpleAdapter = new MyAdapter(MainActivity.this,taskList,myClickListener);
                    listView.setAdapter(simpleAdapter);
                    listView.setTextFilterEnabled(true);
                    return ;
                }
                Toasty.error(MainActivity.this,new ConfigReader().read(code + ""),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                Toasty.error(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(CancelledException e) {

            }

            @Override
            public void onFinished() {

            }
        });

//        simpleAdapter = new SimpleAdapter(MainActivity.class, )
    }
}
