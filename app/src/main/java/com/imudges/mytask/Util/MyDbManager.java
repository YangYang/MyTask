package com.imudges.mytask.Util;

import org.nutz.dao.DB;
import org.xutils.DbManager;
import org.xutils.db.table.TableEntity;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;
import java.util.List;

/**
 * 单例实现的dbManager，避免在项目的不同Activity中重复实例化DbManager
 */
public class MyDbManager{
    private static DbManager dbManager = null;
    private final static MyDbManager myDbManager = new MyDbManager();
    private MyDbManager() {}
    private static void initDb() {
        DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
                .setDbName("my_task")//设置数据库名
                .setDbVersion(1)//设置数据库版本,每次启动应用时将会检查该版本号,
                //发现数据库版本低于这里设置的值将进行数据库升级并触发DbUpgradeListener
                .setAllowTransaction(true)//设置是否开启事物，默认关闭
                .setTableCreateListener(new DbManager.TableCreateListener() {
                    @Override
                    public void onTableCreated(DbManager dbManager, TableEntity<?> tableEntity) {
                        //数据库创建时的Listener
                    }
                })
                .setDbDir(new File("/sdcard/download/"))
                .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                    @Override
                    public void onUpgrade(DbManager dbManager, int i, int i1) {
                        //设置数据库升级时的Listener，这里可以执行相关数据表的相关修改，比如增加字段等
                    }
                });
        dbManager = x.getDb(daoConfig);
    }


    public static MyDbManager getMyDbManager(){
        return myDbManager;
    }

    public static DbManager getDbManagerObj(){
        if(dbManager == null){
            initDb();
        }
        return dbManager;
    }

    /**
     * 将本地T类型的数据库清空
     * */
    public static<T> boolean cleanLocalData(Class<T> type){
        try {
            dbManager.delete(type);
            return true;
        } catch (DbException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 清空T类型的数据，并插入数据
     * */
    public static<T> boolean cleanLocalDataAndInsert(Class<T> type, List<T> newDataList){
        cleanLocalData(type);
        for(T t : newDataList){
            try {
                dbManager.saveBindingId(t);
            } catch (DbException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
