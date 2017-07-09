package com.imudges.mytask.Service;


import android.content.Context;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.imudges.mytask.Bean.Task;
import com.imudges.mytask.UI.LoginActivity;
import com.imudges.mytask.Util.MyDbManager;
import com.imudges.mytask.Util.MyParamsBuilder;
import es.dmoral.toasty.Toasty;
import org.nutz.json.Json;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangyang on 2017/4/28.
 */
public class UserService extends Thread{


    /**
     * 向服务器同步数据
     */
    public void syncData(final Context context, List<Task> taskList) {
        RequestParams params = new MyParamsBuilder(context, "public/upload_task.html", false)
                .builder();
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        final String json = gson.toJson(taskList);
        params.addBodyParameter("tasks", json);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(final String s) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JsonParser jsonParser = new JsonParser();
                        //Gson 解析
                        JsonObject jsonObject = (JsonObject) jsonParser.parse(s);
                        int code = jsonObject.get("code").getAsInt();
                        String jsonData = jsonObject.get("data").toString();
                        //同步成功
                        if (code == 0) {
                            List<Task> taskDataSet = new ArrayList<>();
                            //Nutz Json解析
                            if (jsonData != null && !jsonData.equals("")) {
                                taskDataSet = Json.fromJsonAsList(Task.class, jsonData);

                                //清空数据库并插入数据
                                MyDbManager.cleanLocalDataAndInsert(Task.class, taskDataSet);
                                Toasty.success(context, "本地数据同步成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toasty.error(context, "数据源出现错误，请联系管理员", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            return ;
                        }
                        //同步失败
                        Toasty.error(context, "本地数据同步失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(final Throwable throwable, boolean b) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Toasty.error(context, throwable.getMessage().toString(), Toast.LENGTH_SHORT).show();
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
