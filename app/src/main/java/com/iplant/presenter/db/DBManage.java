/**   
 * @Title:         DBModule.java
 * @Description:   
 */
package com.iplant.presenter.db;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManage extends OrmLiteSqliteOpenHelper {
    private static final String tag = "DBProcess";
    
    private Class<?>[] mDBTableArray;
    
    /** DB实现类 **/
    private static DBManage mDBHelp;
    
    private DBUpgradeListener mDBUpgradleListener;
    
    /**
     * 网络请求的回调
     */
    public static interface DBUpgradeListener {
        /**
         * @param database
         * @param connectionSource
         * @param oldVersion
         * @param newVersion
         * @return: 
         *      true表示内部升级成功， false表示失败，通过删除表结构
         */
        public boolean onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, 
                int oldVersion, int newVersion);
    }
    
    /**
     * 初始化模块，主要完成数据库建表操作
     * 
     * @param c
     *            上下文环境
     * @param dbVersion
     *            程序内部的数据库版本，便于后续的线上数据库升级管理
     * @param dbName
     *            数据库名称，如"wasu.db"
     * @param tableArray
     *            数据库表列表，需要符合ormlite注解规范，如：
     * @DatabaseTable(tableName = "dummyName") public class dummyClass {
     * @DatabaseField(generatedId = true) public long id; }
     */
    public synchronized static DBManage create(Context c, int dbVersion, String dbName, 
            Class<?>[] tableArray, DBUpgradeListener l) {
        if (mDBHelp != null){
            closeSelf();
        }
        
        mDBHelp = new DBManage(c, dbVersion, dbName, tableArray, l);
        
        return mDBHelp;
    }

    /** 
     * @Name: deleteTable 
     * @Description: 删除表,调用必须在create以后
     * @param clazz 
     *  表的类名称
     * @throws SQLException 
     */
    public static void deleteTable(Class<?> clazz) throws SQLException{
        TableUtils.dropTable(mDBHelp.getConnectionSource(), clazz, true);
    }
    
    /** 
     * @Name: clearTable 
     * @Description:清空表数据,调用必须在create以后
     * @param clazz
     *  表的类名称
     * @throws SQLException 
     */
    public static void clearTable(Class<?> clazz) throws SQLException{
        TableUtils.clearTable(mDBHelp.getConnectionSource(), clazz);
    }
    
    public static void closeSelf(){
        mDBHelp.close();
        mDBHelp    = null;
    }
    
    /** 
     * @Name: countOf 
     * @Description:
     * @param clazz 
     *  表类名
     * @return
     *        总的行数
     * @throws SQLException 
     */
    public static long countOf(Class<?> clazz) throws SQLException{
        return mDBHelp.getDao(clazz).countOf();
    }
    
    /** 
     * @Name: queryAll 
     * @Description:
     *  查询这个表的所有行
     * @param clazz
     *  表类名
     * @return
     *  所有的行数据
     * @throws SQLException 
     */
    public static <T> List<T> queryAll(Class<T> clazz) throws SQLException{
        return mDBHelp.getDao(clazz).queryForAll();
    }
    
    /** 
     * @Name: queryAndOrder 
     * @Description:
     *  相比于queryAll，返回排序后的所有行 
     * @param clazz
     *  表类名
     * @param columnName
     *  以这个列名称作为排序规则
     * @param ascending
     *  是否升序
     * @return
     *  所有的行数据
     * @throws SQLException 
     */
    public static <T> List<T> queryAndOrder(Class<T> clazz, String columnName, boolean ascending) throws SQLException{
        return mDBHelp.getDao(clazz).queryBuilder().orderBy(columnName, ascending).query();
    }
    
    /** 
     * @Name: queryListBy 
     * @Description: 查询列内容等于指定值的行
     * @param clazz
     *  表类名
     * @param columnName
     *  列名称
     * @param value
     *  列内容
     * @return
     *  行数据列表   
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static <T> List<T> queryListBy(Class<T> clazz, String columnName, Object value) throws SQLException{
        return mDBHelp.getDao(clazz).queryBuilder()
        		.where()
        		.eq(columnName, value)
        		.query();
    }
    
    public static <T> List<T> queryListBy(Class<T> clazz, String columnName, Object value, 
    		String andColumnName, Object andValue) throws SQLException{
        return mDBHelp.getDao(clazz).queryBuilder()
        		.where()
        		.eq(columnName, value)
        		.and()
        		.eq(andColumnName, andValue)
        		.query();
    }
    
    /** 
     * @Name: queryEq 
     * @Description: 查询列内容等于指定值的行
     * @param clazz
     *  表类名
     * @param columnName
     *  列名称
     * @param value
     *  列内容
     * @return
     *  单个行数据，如果大于一行，返回第一行
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static <T> T queryBy(Class<T> clazz, String columnName, Object value) throws SQLException, InstantiationException, IllegalAccessException{ 
        T result = mDBHelp.getDao(clazz).queryBuilder().where().eq(columnName, value).queryForFirst();
        if (result == null){
            result = clazz.newInstance();
        }
        return result;
    }
    
    /** 
     * @Name: queryRaw 
     * @Description:
     *  通过sql语句查询
     * @param clazz
     *  表类名
     * @param query
     *  查询语句
     * @param map
     *  表映射关系
     * @return
     *  查询的结果
     * @throws SQLException 
     */
    public static <T> List<T> queryRaw(Class<T> clazz, String query, RawRowMapper<T> map) throws SQLException{
        GenericRawResults<T> result = mDBHelp.getDao(clazz).queryRaw(query, map);
        return result.getResults();
    }
    
    /** 
     * @Title: executeRaw 
     * @Description: 执行基本的sql语句 
     * @param @param clazz 表类名
     * @param @param query sql语句
     * @param @return
     *          number of rows affected
     * @param @throws SQLException 
     * @return int 
     * @throws 
     */
    public static int executeRaw(Class<?> clazz, String query) throws SQLException{
        return mDBHelp.getDao(clazz).executeRawNoArgs(query);
    }
    
    /** 
     * @Name: deleteBy 
     * @Description:
     *  删除列的数值等于value的数据
     * @param clazz
     *  表类名
     * @param columnName
     *  列名称
     * @param value
     *  匹配的列内容
     * @throws SQLException 
     */
    public static void deleteBy(Class<?> clazz, String columnName, Object value) throws SQLException{
        DeleteBuilder<?, ?> deleteBuild = mDBHelp.getDao(clazz).deleteBuilder();
        deleteBuild.where().eq(columnName, value);
        deleteBuild.delete();
    }
    
    /**
     * 获取数据库具体表的操作类
     * 
     * @param clazz
     *            表的注解类名
     * @return 数据库操作类，具体见ormlite
     * @throws SQLException
     */
    public static <D extends Dao<T, ?>, T> D getManage(Class<T> clazz) throws SQLException {
        return mDBHelp.getDao(clazz);
    }
    
	// 数据库version
	protected DBManage(Context context, int dbVersion, String dbName, 
	        Class<?>[] dbTables, DBUpgradeListener l) {
		super(context, dbName, null, dbVersion);
		
		mDBTableArray        = dbTables;
		mDBUpgradleListener  = l;
	}

	@Override
	public void onCreate(SQLiteDatabase database,
			ConnectionSource connectionSource) {
		try {
			for (Class<?> dbClass : mDBTableArray) {
				TableUtils.createTable(connectionSource, dbClass);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(tag, "db create failed: " + e.toString());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase database,
			ConnectionSource connectionSource, int oldVersion,
			int newVersion) {
		try {
			//外部动态修改，不处理
			if (mDBUpgradleListener == null ||
				!mDBUpgradleListener.onUpgrade(database, connectionSource, oldVersion, newVersion)){
				for (Class<?> dbClass : mDBTableArray) {
					TableUtils.dropTable(connectionSource, dbClass, true);
				}
				onCreate(database, connectionSource);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(tag, "db upgrade failed: " + e.toString()
					+ ">>old version: " + oldVersion + ";new version: "
					+ newVersion);
		}
	}
}
