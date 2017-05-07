package com.imudges.mytask.UI;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.imudges.mytask.R;

import com.jaredrummler.materialspinner.MaterialSpinner;
import es.dmoral.toasty.Toasty;
import org.angmarch.views.NiceSpinner;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by yangyang on 2017/5/7.
 */
@ContentView(R.layout.activity_add_task)
public class AddTaskActivity extends BaseActivity {
    @ViewInject(R.id.spinner)
    private MaterialSpinner materialSpinner;
    private String[] taskLevel = {"紧急而且重要","紧急但不是很重要","不怎么紧急但是很重要","不紧急而且不重要"};


    private void initEvents(){
        materialSpinner.setItems(taskLevel);
        materialSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {

                Toasty.info(AddTaskActivity.this, "Clicked " + item, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initEvents();
    }
}
