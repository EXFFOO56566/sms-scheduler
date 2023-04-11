package com.dmbteam.scheduler.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dmbteam.scheduler.R;
import com.dmbteam.scheduler.db.DbItem;

import java.util.List;

/**
 * Created by dobrikostadinov on 5/9/15.
 */
public class AdapterScheduled extends RecyclerView.Adapter<AdapterScheduled.ViewHolder> {

    private Context mContext;
    private List<DbItem> mAdapterData;

    public AdapterScheduled(Context context, List<DbItem> adapterData) {
        mContext = context;
        mAdapterData = adapterData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RelativeLayout v = (RelativeLayout) getInflater()
                .inflate(R.layout.list_item_scheduled, parent, false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        DbItem currentItem = mAdapterData.get(position);

        holder.name.setText(currentItem.getName());
        holder.name.setSelected(true);

        holder.date.setText(currentItem.getDateAndTimeAsString());
        holder.message.setText(currentItem.getMessage());
    }


    @Override
    public int getItemCount() {

        return mAdapterData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout mainContainer;
        TextView name;
        TextView date;
        TextView message;

        public ViewHolder(RelativeLayout v) {
            super(v);

            mainContainer = v;
            name = (TextView) v.findViewById(R.id.list_item_scheduled_name);
            date = (TextView) v.findViewById(R.id.list_item_scheduled_date);
            message = (TextView) v.findViewById(R.id.list_item_scheduled_text);

        }
    }

    public LayoutInflater getInflater() {
        return LayoutInflater.from(mContext);
    }

}
