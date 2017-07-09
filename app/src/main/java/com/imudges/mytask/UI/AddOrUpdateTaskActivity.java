package com.imudges.mytask.UI;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.imudges.mytask.Bean.Task;
import com.imudges.mytask.R;
import com.imudges.mytask.Util.MyDbManager;
import com.jaredrummler.materialspinner.MaterialSpinner;
import es.dmoral.toasty.Toasty;
import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.Date;

/**
 * 添加Task的Activity
 */
@ContentView(R.layout.activity_add_or_update_task)
public class AddOrUpdateTaskActivity extends BaseActivity {
    private String taskType;
    private String userId;
    private final String ACTION_NAME = "REFRESH_LIST";
    private String objId;
    private boolean isUpdate = false;//默认为添加
    private Task task = null;

    @ViewInject(R.id.tv_title)
    private TextView tvTitle;


    @ViewInject(R.id.spinner)
    private MaterialSpinner materialSpinner;
    private String[] taskLevel = {"紧急而且重要", "紧急但不是很重要", "不怎么紧急但是很重要", "不紧急而且不重要"};

    @ViewInject(R.id.btn_back_to_home)
    private Button btnBackToHome;

    @Event(value = R.id.btn_back_to_home,type = View.OnClickListener.class)
    private void onClickBackToHome(View view){
        Intent intent = new Intent(AddOrUpdateTaskActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    //数据库对象的初始化
    private DbManager dbManager;
    private MyDbManager myDbManager;

    private void initDb() {
        //单例MyDbManager
        myDbManager = MyDbManager.getMyDbManager();
        dbManager = myDbManager.getDbManagerObj();
    }


    private void initEvents() {
        materialSpinner.setItems(taskLevel);
        if(objId !=null){
            materialSpinner.setSelectedIndex(Integer.parseInt(taskType));
        } else {
            taskType = "0";
        }
        materialSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                Toasty.info(AddOrUpdateTaskActivity.this, "当前任务为：" + item, Toast.LENGTH_LONG).show();
                switch (position) {
                    case 0:
                        taskType = "0";
                        break;
                    case 1:
                        taskType = "1";
                        break;
                    case 2:
                        taskType = "2";
                        break;
                    case 3:
                        taskType = "3";
                        break;
                    default:
                        Toasty.info(AddOrUpdateTaskActivity.this, "数据错误", 0).show();
                        break;
                }
            }
        });
    }

    private void initViews() {
        if (objId == null) {


        } else {
            //更新
            tvTitle.setText("更新数据");
            BtnSave.setText("更新");
            try {
                task = dbManager.selector(Task.class)
                        .where("id","=",objId)
                        .findFirst();
                if(task != null){
                    etTaskTitle.setText(task.getTaskName());
                    etTaskSummary.setText(task.getSummary());
                    taskType = task.getType() + "";
                } else {
                    Toasty.info(AddOrUpdateTaskActivity.this,"本地数据库加载失败，请登录后重新选择Task进行编辑",0).show();
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
    }

    @ViewInject(R.id.et_add_task_task_title)
    private EditText etTaskTitle;

    @ViewInject(R.id.et_add_task_task_summary)
    private EditText etTaskSummary;

    @ViewInject(R.id.btn_save)
    private Button BtnSave;

    @Event(value = R.id.btn_save, type = View.OnClickListener.class)
    public void save(View view) {
        if (objId != null) {
            //更新
            isUpdate = true;
            addOrUpdateTask();
        } else {
            //添加
            addOrUpdateTask();
        }
    }

    private boolean judgeLegality(){
        if(!TextUtils.isEmpty(etTaskSummary.getText()) && !TextUtils.isEmpty(etTaskTitle.getText())){
            return true;
        } else {
            return false;
        }
    }

    private void addOrUpdateTask() {
        if (!isUpdate) {
            //添加
            if(judgeLegality()){
                Task task = new Task();
                task.setAddTime(new Date(System.currentTimeMillis()));
                task.setSummary(etTaskSummary.getText().toString());
                task.setSyncStatus("null");
                task.setTaskName(etTaskTitle.getText().toString());
                task.setUserId(userId);
                task.setStatus(1);
                task.setType(Integer.parseInt(taskType));
                task.setTaskWebId(null);
                try {
                    dbManager.saveBindingId(task);
                    Toasty.success(AddOrUpdateTaskActivity.this, "添加成功", 0).show();
                    Intent intent = new Intent(ACTION_NAME);//此处放入的在广播处使用getAction()接收
                    //发送广播
                    sendBroadcast(intent);
                    finish();
                    return;
                } catch (DbException e) {
                    e.printStackTrace();
                    Toasty.error(AddOrUpdateTaskActivity.this, "添加发生错误", 0).show();
                    return;
                }
            } else {
                Toasty.warning(AddOrUpdateTaskActivity.this,"请完整填写任务信息后添加！",0).show();
            }
        } else {
            //更新
            task.setTaskName(etTaskTitle.getText().toString());
            task.setSummary(etTaskSummary.getText().toString());
            task.setType(Integer.parseInt(taskType));
            task.setSyncStatus("1");
            try {
                dbManager.saveOrUpdate(task);
                Toasty.success(AddOrUpdateTaskActivity.this,"更新成功",0).show();
                Intent intent = new Intent(ACTION_NAME);//此处放入的在广播处使用getAction()接收
                //发送广播
                sendBroadcast(intent);
                finish();
                return;
            } catch (DbException e) {
                e.printStackTrace();
                return ;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = getIntent().getStringExtra("userId");
        objId = getIntent().getStringExtra("objId");
        initDb();
        initViews();
        initEvents();
    }
}
