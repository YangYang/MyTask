package com.imudges.mytask.util;

import org.nutz.mvc.annotation.Fail;
import org.xutils.http.RequestParams;

import java.io.File;

/**
 * 这个参数构造器主要用来创建xUtils http请求所用的参数对象
 * */
public class MyParamsBuilder {
    private RequestParams params = null;
    private boolean  isGet;

    public MyParamsBuilder(String detailUrl,boolean isGet){
        if (detailUrl.length() > 0 && detailUrl.charAt(0) == '/' ){
            detailUrl = detailUrl.substring(1, detailUrl.length());
        }
        if (params == null){
             params = new RequestParams(Config.BASE_URL + detailUrl);
        }
        this.isGet = isGet;
    }

    public MyParamsBuilder addParameter(String key,Object value){
        if (isGet){
            params.addQueryStringParameter(key,(String) value);
        }else {
            if (value instanceof File){
                params.addBodyParameter(key,(File)value);
            }else {
                params.addBodyParameter(key,(String)value);
            }
        }
        return this;
    }

    public RequestParams builder(){
        long ts = System.currentTimeMillis();
        if (isGet){
            params.addQueryStringParameter("ts",ts + "");
            params.addQueryStringParameter("sk",MD5.encryptTimeStamp(ts));
        }else {
            params.addBodyParameter("ts",ts + "");
            params.addBodyParameter("sk",MD5.encryptTimeStamp(ts));
        }
        return params;
    }
}
