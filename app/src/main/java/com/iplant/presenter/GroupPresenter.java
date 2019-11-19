package com.iplant.presenter;

import com.iplant.GudData;
import com.iplant.MyError;
import com.iplant.model.Group;
import com.iplant.model.ModelBase;
import com.iplant.model.ToolBox;
import com.iplant.presenter.db.DBManage;
import com.iplant.presenter.http.DataFetchListener.JsonListener;
import com.iplant.presenter.http.DataFetchModule;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupPresenter {

    //用于传递刷新操作
    public static class GroupUpdateResult extends ModelBase<GroupUpdateResult> {
        private static final long serialVersionUID = -7307165540593626000L;
        public static boolean IsOk = true;
        //有新的未读消息
        public int haveNewMsg;
    }

    /**
     * 用户登录
     *
     * @param account
     * @param pwd
     */
    public synchronized void update(final boolean forceRefresh, final String userInfo, final String passWord) {

        try {
            JsonListener jsonListener = new JsonListener() {

                @Override
                public void onJsonGet(int retcode, String extraMsg, JSONObject jsondata) {
                    GroupUpdateResult result = new GroupUpdateResult();
                    if (retcode != MyError.SUCCESS) {
                        result.errorcode = retcode;
                        result.erorMsg = extraMsg;
                        EventBus.getDefault().post(result);
                        return;
                    }

                    //获取常用的
                    //  HashMap<String, Integer> mFavorite = new HashMap<String, Integer>();
                    HashMap<String, ToolBox> wCacheList = new HashMap<String, ToolBox>();
                    try {

                        //获取缓存的
                        List<ToolBox> toolList = DBManage.queryListBy(ToolBox.class, "userid", userInfo);
                        if (toolList != null && toolList.size() > 0) {
                            for (ToolBox tools : toolList) {
                                wCacheList.put(tools.moduleId, tools);
                            }
                        }

                        //获取最新的
                        JSONObject wJSONObject = jsondata.getJSONObject("info");
                        JSONArray array = wJSONObject.getJSONArray("module");
                        List<ToolBox> newList = new ArrayList<ToolBox>();
                        List<Group> groupList = new ArrayList<Group>();

                        //获取收藏模块
//                        JSONObject wFavoriteObject = array.getJSONObject(4);
//                        if (wFavoriteObject != null) {
//                            JSONArray wModuleArr = wFavoriteObject.optJSONArray("modules");
//                            for (int i = 0; i < wModuleArr.length(); i++) {
//                                JSONObject wModuleObj = wModuleArr.getJSONObject(i);
//                                mFavorite.put(wModuleObj.getString("module_id").trim(), 1);
//                            }
//                        }


                        for (int i = 0; i < array.length(); i++) {
                            JSONObject groupObj = array.getJSONObject(i);
                            Group myGroup = new Group();
                            myGroup.groupName = groupObj.optString("Name", "");
                            myGroup.groupID = groupObj.optString("ID", "");
                            groupList.add(myGroup);

                            JSONArray moduleArr = groupObj.optJSONArray("ModuleList");
                            if (moduleArr != null) {
                                for (int j = 0; j < moduleArr.length(); j++) {
                                    JSONObject toolObj = moduleArr.getJSONObject(j);
                                    ToolBox myBox = new ToolBox();
                                    myBox.moduleId = toolObj.getString("ID").trim();

//                                    myBox.groupType = mFavorite.get(myBox.moduleId) == null ? 0 : 1;

                                    myBox.groupName = myGroup.groupID;

                                    myBox.unReadCount = toolObj.getInt("MessageCount");
                                    myBox.name = toolObj.getString("Name").trim();
                                    myBox.companyid = 0;
                                    String wImgUrl= toolObj.optString("Icon", "");
                                    String wPackageName= wImgUrl.substring(1,wImgUrl.indexOf("/",2));
                                    if(GudData.DOMAIN.contains(wPackageName))
                                    {
                                        wImgUrl=wImgUrl.substring(wImgUrl.indexOf("/",2)+1);
                                    }
                                    myBox.imgUrl = GudData.DOMAIN + wImgUrl;
                                    myBox.linkUrl = GudData.DOMAIN + toolObj.optString("Url", "").trim() + "?cadv_ao=" + userInfo + "&cade_po=" + passWord;
                                    myBox.runType = toolObj.optString("Type", "").trim();
                                    myBox.userid = userInfo;
                                    newList.add(myBox);
                                }
                            }
                        }

                        //判断有没有更新
                        boolean bHaveUpdate = false;
                        int newMsg = 0;
                        for (ToolBox tools : newList) {
                            ToolBox cache = wCacheList.get(tools.moduleId);
                            if (cache == null) {
                                bHaveUpdate = true;
                            } else {
                                if (!tools.equals(cache)) {
                                    bHaveUpdate = true;
                                    if (tools.unReadCount > cache.unReadCount) {
                                        newMsg += tools.unReadCount - cache.unReadCount;
                                    }
                                }
                            }

                        }

                        if (newList.size() != wCacheList.size() || newMsg > 0) {
                            bHaveUpdate = true;
                        }

                        if (bHaveUpdate) {
                            //删除全部数据，全量更新
                            DBManage.clearTable(Group.class);
                            DBManage.clearTable(ToolBox.class);

                            //插入新数据
                            for (Group myGroup : groupList) {
                                myGroup.insertOrUpdate();
                            }

                            for (ToolBox tools : newList) {

                                tools.insertOrUpdate();
                            }
                        }

                        if (bHaveUpdate || forceRefresh) {
                            result.haveNewMsg = newMsg;
                            EventBus.getDefault().post(result);
                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            };
            DataFetchModule.getInstance().fetchJsonGet(GudData.DOMAIN + "api/HomePage/Show?cadv_ao=" + userInfo + "&cade_po=" + passWord + "&Type=3", jsonListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
