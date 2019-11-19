package com.iplant.presenter.view.adapter;

import java.util.List;

import com.iplant.R;
import com.iplant.model.Group;
import com.iplant.model.ToolBox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ItemMainGroupExpandableAdapter extends BaseExpandableListAdapter {
	private Context mContext;
	private List<Group> mGroupList;
	public ItemMainGroupExpandableAdapter(Context c){ 
		mContext = c;
	}
	
	public void setData(List<Group> listGroup){
		mGroupList = listGroup;
		notifyDataSetChanged();
	}
	
	public void checkBackPress(){
		if (mGroupList == null && mGroupList.size() == 0){
			return;
		}
		
		boolean bNeedUpdate = false;
		for(Group myGroup : mGroupList){
			if (myGroup.mbInEditMode){
				myGroup.mbInEditMode = false;
				bNeedUpdate = true;
			}
		}
		
		if (bNeedUpdate){
			notifyDataSetChanged();
		}
	}
	
	@Override
	public int getGroupCount() {
		if (mGroupList == null){
			return 0;
		}
		return mGroupList.size(); 
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return mGroupList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mGroupList.get(groupPosition).getToolboxList();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		Group myGroup = (Group) getGroup(groupPosition);
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_main_group, null);
		}

		ImageView mgroupimage   = (ImageView) convertView.findViewById(R.id.indicator);
		ImageView msgIndicator	= (ImageView) convertView.findViewById(R.id.msg_indicator);
		TextView  groupName		= (TextView) convertView.findViewById(R.id.groupname);
		if (isExpanded) {
			mgroupimage.setImageResource(R.drawable.rectangle_215);
		} else {
			mgroupimage.setImageResource(R.drawable.rectangle_215_copy);
		}

		if (haveUnReadMsg(myGroup)){
			msgIndicator.setVisibility(View.VISIBLE);
		}else{
			msgIndicator.setVisibility(View.INVISIBLE);
		}
		
		groupName.setText(myGroup.groupName);
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,	boolean isLastChild, View convertView, ViewGroup parent) {
		Group myGroup = (Group) getGroup(groupPosition);
		GridView gvChild = null;
		if (convertView == null){
			gvChild = (GridView) LayoutInflater.from(mContext).inflate(R.layout.grid, null);
			ItemMainChildAdapter adapter = new ItemMainChildAdapter(mContext);
			gvChild.setAdapter(adapter);
			convertView = gvChild;
		}else{
			gvChild = (GridView) convertView;
		}
		
		((ItemMainChildAdapter)gvChild.getAdapter()).setData(myGroup, myGroup.getToolboxList());
		
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	private boolean haveUnReadMsg(Group group){
		List<ToolBox> mToolBox = group.getToolboxList();
		if (mToolBox == null || mToolBox.size() == 0){
			return false;
		}
		
		for(ToolBox box : mToolBox){
			if (box.unReadCount > 0){
				return true;
			}
		}
		
		return false;
		
	}
}
