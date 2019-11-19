package com.iplant.model;

import java.sql.SQLException;
import java.util.List;

import com.iplant.presenter.db.DBBase;
import com.iplant.presenter.db.DBManage;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "tb_group")
public class Group extends DBBase<Group> {
	private static final long serialVersionUID = 4884385113573773616L;

	//编号  
    @DatabaseField(generatedId=true)  
    public int id; 
	
	//对应的 分组ID
    @DatabaseField
    public int groupType; 
	
    //分组名称
    @DatabaseField
    public String groupName;
    
    //分组ID
    @DatabaseField
    public String groupID;
	
    private List<ToolBox> mToolList;
    
    public boolean mbInEditMode = false;
    
	@Override
	protected Group getMySelf() {
		return this;
	}
	
	/**
	 * 获得工具箱列表
	 * @return
	 * 	列表或者null
	 */
	public List<ToolBox> getToolboxList(){
		if (mToolList == null){
			try {
				if(groupType == 1){
					mToolList = DBManage.queryListBy(ToolBox.class, "groupType", groupType); 
				}else{
					mToolList = DBManage.queryListBy(ToolBox.class, "groupName", groupName);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return mToolList;
	}

}
