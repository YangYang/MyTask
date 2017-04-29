package com.imudges.mytask.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.imudges.mytask.Listener.MyClickListener;
import com.imudges.mytask.R;
import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

/**
 * Created by yangyang on 2017/4/29.
 */
public class MyAdapter extends BaseAdapter {

    private List<Map<String,String>> mContentList;
    private MyClickListener myClickListener;
    private LayoutInflater mInflater;

    public MyAdapter(Context context, List<Map<String, String>> mContentList, MyClickListener myClickListener) {
        this.mContentList = mContentList;
        this.myClickListener = myClickListener;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mContentList.size();
    }

    @Override
    public Object getItem(int position) {
        return mContentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.item_list_view,null);
            holder = new ViewHolder();
            holder.imgTaskType = (ImageView) convertView.findViewById(R.id.img_task_type);
            holder.tvTaskName = (TextView) convertView.findViewById(R.id.tv_task_name);
            holder.tvAddTime = (TextView) convertView.findViewById(R.id.tv_add_time);
            holder.tvTaskStatus = (TextView) convertView.findViewById(R.id.tv_task_status);
            holder.tvSummary = (TextView) convertView.findViewById(R.id.tv_summary);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imgTaskType.setImageResource(R.mipmap.level);
        holder.tvTaskName.setText(mContentList.get(position).get("tv_task_name"));
        holder.tvAddTime.setText(mContentList.get(position).get("tv_add_time"));
        holder.tvTaskStatus.setText(mContentList.get(position).get("tv_task_status"));
        holder.tvSummary.setText(mContentList.get(position).get("tv_summary"));


        holder.btnCommit = (Button) convertView.findViewById(R.id.btn_commit);
        holder.btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.btnEdit = (Button) convertView.findViewById(R.id.btn_edit);
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.btnAbandon = (Button) convertView.findViewById(R.id.btn_abandon);
        holder.btnAbandon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.btnTaskStatus = (Button) convertView.findViewById(R.id.btn_task_status);
        holder.btnTaskStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return convertView;
    }

    public class ViewHolder{
        public ImageView imgTaskType;//任务类型
        public TextView tvTaskName;
        public TextView tvAddTime;

        public TextView tvTaskStatus;//任务状态
        public Button btnTaskStatus;

        public TextView tvSummary;//任务描述
        public Button btnCommit;//提交
        public Button btnEdit;//编辑
        public Button btnAbandon;//放弃
    }
}
