package com.iplant.model;

import com.iplant.MyError;
import com.iplant.presenter.db.DBBase;

public abstract class ModelBase<T> extends DBBase<T> {
	private static final long serialVersionUID = 7734079229751621301L;
	public int errorcode;
    public String erorMsg;
	
    /**
     * 是否当前实体有效
     * @return
     */
    public boolean isValid(){
    	return errorcode == MyError.SUCCESS;
    }
    
    @Override
    protected T getMySelf() {
    	return null;
    }
}
