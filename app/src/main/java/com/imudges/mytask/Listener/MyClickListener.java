package com.imudges.mytask.Listener;

import android.view.View;

/**
 * Created by yangyang on 2017/4/29.
 */
public interface MyClickListener {
    public void commit(int position,View v);
    public void edit(int position,View v);
    public void abandon(int position , View v);
}
