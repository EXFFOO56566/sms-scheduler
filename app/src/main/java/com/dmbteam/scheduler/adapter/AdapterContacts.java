package com.dmbteam.scheduler.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dmbteam.scheduler.MainActivity;
import com.dmbteam.scheduler.R;
import com.dmbteam.scheduler.cmn.Contact;
import com.dmbteam.scheduler.util.ContactsManager;
import com.dmbteam.scheduler.util.ImageOptionsBuilder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dobrikostadinov on 5/7/15.
 */
public class AdapterContacts extends RecyclerView.Adapter<AdapterContacts.ViewHolder> implements Filterable {

    public static final String TAG = AdapterContacts.class.getSimpleName();
    private EditText mFilteredEt;

    public void setFilteredEt(EditText filteredEt) {
        mFilteredEt = filteredEt;
    }


    private final List<Contact> mAdapterData;
    private List<Contact> mFilteredData;

    private final DisplayImageOptions mDisplayImageOptions;
    private final ImageLoader mImageLoader;
    private Context mContext;

    private Filter mItemFilter;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout mainContainer;
        ImageView avatar;
        TextView nameTv;

        public ViewHolder(LinearLayout v) {
            super(v);

            mainContainer = v;
            avatar = (ImageView) v.findViewById(R.id.list_item_contact_image);
            nameTv = (TextView) v.findViewById(R.id.list_item_contact_name);

        }
    }

    public AdapterContacts(List<Contact> adapterData, Context context) {

        this.mItemFilter = new ItemFilter();

        this.mContext = context;

        this.mAdapterData = adapterData;
        this.mFilteredData = adapterData;

        this.mDisplayImageOptions = ImageOptionsBuilder
                .buildGeneralImageOptions(true, R.drawable.choose_ic_contact_placeholder);
        this.mImageLoader = ImageLoader.getInstance();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        LinearLayout v = (LinearLayout) getInflater()
                .inflate(R.layout.list_item_contact, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {

        final Contact currentContact = mFilteredData.get(i);

        viewHolder.nameTv.setText(currentContact.getName());

        mImageLoader.displayImage(currentContact.getImageThumb(), viewHolder.avatar, mDisplayImageOptions);

        viewHolder.mainContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactsManager.getInstance(mContext).getAllScheduledContacts().add(currentContact);

                ((MainActivity) mContext).getSupportFragmentManager().popBackStack();
            }
        });
    }


    @Override
    public int getItemCount() {
        return mFilteredData.size();
    }

    public LayoutInflater getInflater() {
        return LayoutInflater.from(mContext);
    }

    @Override
    public Filter getFilter() {
        return mItemFilter;
    }


    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<Contact> list = mAdapterData;

            int count = list.size();
            final ArrayList<Contact> nlist = new ArrayList<Contact>(count);

            Contact filterableContact;

            for (int i = 0; i < count; i++) {
                filterableContact = list.get(i);

                if (filterableContact.getPhone().toLowerCase().contains(filterString) || filterableContact.getName().toLowerCase().contains(filterString)) {
                    nlist.add(filterableContact);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredData = (ArrayList<Contact>) results.values;
            notifyDataSetChanged();
        }

    }
}
