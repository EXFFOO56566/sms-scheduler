package com.dmbteam.scheduler.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dmbteam.scheduler.MainActivity;
import com.dmbteam.scheduler.R;
import com.dmbteam.scheduler.cmn.Contact;
import com.dmbteam.scheduler.util.ContactsManager;
import com.dmbteam.scheduler.util.ImageOptionsBuilder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by dobrikostadinov on 5/7/15.
 */
public class AdapterCompose extends ArrayAdapter<Contact> {

    private final DisplayImageOptions mDisplayImageOptions;
    private final ImageLoader mImageLoader;

    public AdapterCompose(Context context, int resource, List<Contact> adapterData) {
        super(context, resource, adapterData);

        this.mDisplayImageOptions = ImageOptionsBuilder
                .buildGeneralImageOptions(true, R.drawable.choose_ic_contact_placeholder);
        this.mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = getInflater().inflate(R.layout.grid_item_compose, parent, false);

            holder.mainContainer = convertView.findViewById(R.id.grid_item_main);
            holder.infoContainer = convertView.findViewById(R.id.grid_item_info_container);
            holder.toTextview = (TextView) convertView.findViewById(R.id.grid_item_compose_to);
            holder.avatar = (ImageView) convertView.findViewById(R.id.grid_item_compose_image);
            holder.name = (TextView) convertView.findViewById(R.id.grid_item_compose_name);
            holder.cancelView = convertView.findViewById(R.id.grid_item_compose_cancel);
            holder.addImage = convertView.findViewById(R.id.grid_item_add);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == 0) {
            holder.toTextview.setVisibility(View.VISIBLE);
        } else {
            holder.toTextview.setVisibility(View.GONE);
        }

        final Contact currentContact = getItem(position);

        if (getCount() > 1 && getItem(0).getId() != -1) {
            holder.mainContainer.setOnClickListener(null);

            if (position == 1) {
                holder.addImage.setVisibility(View.VISIBLE);
            } else {
                holder.addImage.setVisibility(View.GONE);
            }
        } else if (getCount() == 2 && getItem(0).getId() == -1 && getItem(1).getId() == -1) {

            holder.mainContainer.setOnClickListener(new OpenContactsScreenListener());

            holder.addImage.setVisibility(View.GONE);

            holder.infoContainer.setVisibility(View.GONE);

        } else {
            holder.mainContainer.setOnClickListener(null);

            if (position == 0) {
                holder.addImage.setVisibility(View.VISIBLE);
            } else {
                holder.addImage.setVisibility(View.GONE);
            }

        }


        holder.name.setText(currentContact.getName());
        holder.name.setSelected(true);
        mImageLoader.displayImage(currentContact.getImageThumb(), holder.avatar, mDisplayImageOptions);

        holder.addImage.setOnClickListener(new OpenContactsScreenListener());

        holder.cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContactsManager.getInstance(getContext()).removeContactFromScheduler(currentContact);

                ((MainActivity) getContext()).refreshComposeFragmentList();
            }
        });

        return convertView;
    }

    private LayoutInflater getInflater() {

        return LayoutInflater.from(getContext());
    }

    private class OpenContactsScreenListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ((MainActivity) getContext()).showContactsScreen();
        }
    }


    public static class ViewHolder {

        View mainContainer;
        View infoContainer;
        TextView toTextview;
        ImageView avatar;
        TextView name;
        View cancelView;
        View addImage;

    }

}
