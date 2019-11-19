package com.iplant.presenter.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.iplant.R;

import java.util.List;
import java.util.Map;

/**
 * Created by shris on 2017/3/11.
 */

public class PicPickerAdapter extends SimpleAdapter {

    private Context mContext;
    List<Map<String, Object>> mData;
    /**
     * Constructor
     *
     * @param context  The context where the View associated with this SimpleAdapter is running
     * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
     *                 Maps contain the data for each row, and should include all the entries specified in
     *                 "from"
     * @param resource Resource identifier of a view layout that defines the views for this list
     *                 item. The layout file should include at least those named views defined in "to"
     * @param from     A list of column names that will be added to the Map associated with each
     *                 item.
     * @param to       The views that should display column in the "from" parameter. These should all be
     *                 TextViews. The first N views in this list are given the values of the first N columns
     */
    public PicPickerAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        mContext=context;
        mData=(List<Map<String, Object>>)data;
    }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
       Map<String, Object> wMap=mData.get(position);
       View oneView = null;
       if(convertView==null)
       {
           oneView= LayoutInflater.from(mContext).inflate(R.layout.picturepicker_item, parent, false);
       }
       else
       {
           oneView= convertView;
       }
       ImageView wImageView =(ImageView)oneView.findViewById(R.id.iv_PicPickitem);
       wImageView.setImageBitmap((Bitmap)wMap.get("iamge_item"));
       oneView.setId(position);
       return oneView;
   }




}
