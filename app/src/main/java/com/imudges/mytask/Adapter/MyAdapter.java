package com.imudges.mytask.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.imudges.mytask.Listener.MyClickListener;
import com.imudges.mytask.R;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yangyang on 2017/4/29.
 */
public class MyAdapter extends BaseAdapter implements Filterable{

    private List<Map<String,String>> mContentList;
    private List<Map<String,String>> mBackContentList;//作为备用
    private MyClickListener myClickListener;
    private LayoutInflater mInflater;
    MyFilter mFilter = null;

    public MyAdapter(Context context, List<Map<String, String>> mContentList, MyClickListener myClickListener) {
        this.mContentList = mContentList;
        this.myClickListener = myClickListener;
        mInflater = LayoutInflater.from(context);
        mBackContentList = mContentList;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
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
        //需要修改图片等级
        holder.imgTaskType.setImageResource(R.mipmap.level);
        holder.tvTaskName.setText(mContentList.get(position).get("tv_task_name"));
        holder.tvAddTime.setText(mContentList.get(position).get("tv_add_time"));
        holder.tvTaskStatus.setText(mContentList.get(position).get("tv_task_status"));
        holder.tvSummary.setText(mContentList.get(position).get("tv_summary"));



        holder.btnEdit = (Button) convertView.findViewById(R.id.btn_edit);
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myClickListener.edit(position,v);
            }
        });

        holder.btnAbandon = (Button) convertView.findViewById(R.id.btn_abandon);
        holder.btnAbandon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myClickListener.abandon(position,v);
            }
        });

        holder.imgBtnTaskStatus = (ImageButton) convertView.findViewById(R.id.btn_task_status);
        if(holder.tvTaskStatus.getText().toString().equals("完成")){
            holder.imgBtnTaskStatus.setImageResource(R.drawable.selected);
        } else if( holder.tvTaskStatus.getText().toString().equals("放弃") ||  holder.tvTaskStatus.getText().toString().equals("未完成")){
            holder.imgBtnTaskStatus.setImageResource(R.drawable.select);
        }
        holder.imgBtnTaskStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myClickListener.changeStatus(position,v);
            }
        });

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new MyFilter();
        }
        return mFilter;
    }

    public class ViewHolder{
        public ImageView imgTaskType;//任务类型
        public TextView tvTaskName;
        public TextView tvAddTime;

        public TextView tvTaskStatus;//任务状态
        public ImageButton imgBtnTaskStatus;

        public TextView tvSummary;//任务描述
        public Button btnEdit;//编辑
        public Button btnAbandon;//放弃
    }

    //搜索用
    //我们需要定义一个过滤器的类来定义过滤规则
    class MyFilter extends Filter{

        //我们在performFiltering(CharSequence charSequence)这个方法中定义过滤规则
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Map<String,String>> list;
            //当过滤的关键字为空时，我们显示所有的数据
            if(TextUtils.isDigitsOnly(constraint)){
                list = mContentList;
            } else {
                list = new ArrayList<>();
                for(Map<String,String> map :mBackContentList){
                    //添加符合过滤原则的数据
                    if(map.get("tv_task_name").contains(constraint) ||
                            map.get("tv_summary").contains(constraint) ||
                            map.get("tv_add_time").contains(constraint) ||
                            map.get("tv_task_status").contains(constraint)){
                        list.add(map);
                    }
                }
            }
            results.values = list;//将得到的数据集放到FilterResult的value中
            results.count = list.size();//将得到的数据集的大小放到FilterResult的count中
            return results;
        }

        //在publishResults方法中告诉适配器更新界面
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mContentList = (List<Map<String, String>>) results.values;
            if(results.count > 0){
                notifyDataSetChanged();//通知数据发生了变化
            } else {
                notifyDataSetInvalidated();//数据加载失败
            }
        }
    }
}
