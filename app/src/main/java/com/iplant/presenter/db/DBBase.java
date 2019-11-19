package com.iplant.presenter.db;

import com.j256.ormlite.dao.Dao;

import java.io.Serializable;
import java.sql.SQLException;

public abstract class DBBase<T> implements Serializable {
    /**
     * @Fields serialVersionUID : TODO
     */
    private static final long serialVersionUID = -3417224987439760806L;

    protected abstract T getMySelf();
    
    private Dao<T, ?> getDao() throws SQLException{
        @SuppressWarnings("unchecked")
        Dao<T, ?> myDao = (Dao<T, ?>) DBManage.getManage(getMySelf().getClass());
        return myDao;
    }
    
    public int delete() throws SQLException {
        return getDao().delete(getMySelf());
    }
    
    public void insertOrUpdate()throws SQLException {
        getDao().createOrUpdate(getMySelf());
    }
    
    public long countOf() throws SQLException{
       return getDao().countOf(); 
    }
}
