package com.iplant.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "tb_account")
public class Account extends ModelBase<Account> {
	private static final long serialVersionUID = 2946265455063484510L;

    @DatabaseField(generatedId=true)  
    public int id; 
	
    @DatabaseField
    public String account;
	
    @DatabaseField
    public String encryptPwd;
    
    //名称
    @DatabaseField
    public String name;
    
    //部门
    @DatabaseField
    public String department;

    //名称
    @DatabaseField
    public String encryptAccount;

    @DatabaseField
    public String ADUserID;
    
    //工作
    @DatabaseField
    public String role;

    @DatabaseField
    public  int companyID;

    @DatabaseField
    public  int MessageCount;

	@Override
	protected Account getMySelf() {
		// TODO Auto-generated method stub
		return this;
	}

}
