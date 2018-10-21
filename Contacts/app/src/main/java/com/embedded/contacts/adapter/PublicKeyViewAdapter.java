package com.embedded.contacts.adapter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.embedded.contacts.R;
import com.embedded.contacts.model.PublicKey;
import com.embedded.contacts.utils.BitVaultFont;

import java.util.List;

/**
 * Created Dheeraj Bansal root on 13/6/17.
 * version 1.0.0
 * Display public key
 */

public class PublicKeyViewAdapter extends RecyclerView.Adapter<PublicKeyViewAdapter.PhoneViewHolder> {

    private List<PublicKey> publicKeyList;
    private Activity activity;

    public PublicKeyViewAdapter(Activity activity, List<PublicKey> publicKeyList) {
        this.publicKeyList = publicKeyList;
        this.activity = activity;
    }

    @Override
    public PhoneViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.publickey_view_cell, parent, false);

        return new PhoneViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PhoneViewHolder holder, final int position) {

        final PublicKey key = publicKeyList.get(position);
        if (key.getPublicKey() != null && key.getPublicKey().length() > 0) {
            holder.setKey(key.getPublicKey());
        }

        holder.msgImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // send intent to secure messaging app
               if(isPackageExisted(activity.getString(R.string.package_secure_msg))){
                Intent sendIntent = activity.getPackageManager().getLaunchIntentForPackage(activity.getString(R.string.package_secure_msg));
                if (key.getPublicKey() != null && sendIntent != null) {
                    sendIntent.putExtra(activity.getResources().getString(R.string.intent_public_key), key.getPublicKey().toString());
                }
                activity.startActivity(sendIntent);}
            }
        });
        holder.secure_cal_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // send intent to secure calling app
                if(isPackageExisted(activity.getString(R.string.package_secure_cal))){
                Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getString(R.string.package_secure_cal));
                if (key.getPublicKey() != null && intent != null) {
                    intent.putExtra(activity.getResources().getString(R.string.intent_public_key),key.getPublicKey().toString());
                    activity.startActivity(intent);
                }}
            }
        });
    }

    /*
    check for is above defined package existed or not
     */
    public boolean isPackageExisted(String targetPackage){

        PackageManager pm=activity.getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }
    @Override
    public int getItemCount() {
        return publicKeyList.size();
    }

    public class PhoneViewHolder extends RecyclerView.ViewHolder {

        private ImageView msgImageView;
        private ImageView secure_cal_key;
        private TextView publicKeyTxt;

        public PhoneViewHolder(View itemView) {
            super(itemView);
            msgImageView = (ImageView) itemView.findViewById(R.id.msgImg);
            secure_cal_key = (ImageView) itemView.findViewById(R.id.secure_cal_key);
            publicKeyTxt = (TextView) itemView.findViewById(R.id.publicKeyTxt);
        }

        public void setKey(String key) {
            publicKeyTxt.setText(key);
        }
    }


}
