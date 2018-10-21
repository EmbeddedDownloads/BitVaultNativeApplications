package com.embedded.contacts.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.embedded.contacts.R;
import com.embedded.contacts.model.Mobile;
import com.embedded.contacts.utils.BitVaultFont;
import com.embedded.contacts.utils.Utils;

import java.util.List;

/**
 * Created Dheeraj Bansal root on 13/6/17.
 * version 1.0.0
 * show mobile number
 */

public class PhoneViewAdapter extends RecyclerView.Adapter<PhoneViewAdapter.PhoneViewHolder> {

    private List<Mobile> mobileList;
    private Activity activity;

    public PhoneViewAdapter(Activity activity, List<Mobile> mobileList) {
        this.mobileList = mobileList;
        this.activity = activity;
    }

    @Override
    public PhoneViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.phone_view_cell, parent, false);

        return new PhoneViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PhoneViewHolder holder, final int position) {

        holder.msgTxt.setTag(position);
        holder.callTxt.setTag(position);

        Mobile mobile = mobileList.get(position);
        if (mobile.getNumber() != null && mobile.getNumberType() != null) {
            holder.setMobileTxt(mobile.getNumber());
            holder.setMobileTypeTxt(mobile.getNumberType());
        }
    }

    @Override
    public int getItemCount() {
        return mobileList.size();
    }

    class PhoneViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mobileTxt;
        private TextView mobileTypeTxt;
        private BitVaultFont callTxt;
        private BitVaultFont msgTxt;

        private PhoneViewHolder(View itemView) {
            super(itemView);
            mobileTxt = (TextView) itemView.findViewById(R.id.mobileTxt);
            mobileTypeTxt = (TextView) itemView.findViewById(R.id.mobileTypeTxt);
            callTxt = (BitVaultFont) itemView.findViewById(R.id.callTxt);
            msgTxt = (BitVaultFont) itemView.findViewById(R.id.msgTxt);
            callTxt.setOnClickListener(this);
            msgTxt.setOnClickListener(this);
        }

        private void setMobileTxt(String mobile) {
            mobileTxt.setText(mobile);
        }

        private void setMobileTypeTxt(String mobileType) {
            mobileTypeTxt.setText(mobileType);
        }

        @Override
        public void onClick(View view) {
            int position = (int) view.getTag();
            switch (view.getId()) {

                case R.id.callTxt:
                    // dial number
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    if (mobileList.get(position).getNumber() != null) {
                        intent.setData(Uri.parse("tel:" + mobileList.get(position).getNumber()));
                        activity.startActivity(intent);
                    }
                    break;

                case R.id.msgTxt:
                    // send message
                    /*Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    if (mobileList.get(position).getNumber() != null) {
                        sendIntent.putExtra(activity.getResources().getString(R.string.intent_public_key),
                                mobileList.get(position).getNumber());
                    }
                    activity.startActivity(sendIntent);*/
                    break;
            }
        }
    }

}
