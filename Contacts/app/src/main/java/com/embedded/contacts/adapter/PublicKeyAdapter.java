package com.embedded.contacts.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.embedded.contacts.AddContactActivity;
import com.embedded.contacts.R;
import com.embedded.contacts.model.PublicKey;
import com.embedded.contacts.utils.BitVaultFont;
import com.embedded.contacts.utils.Utils;

import java.util.ArrayList;

/**
 * Created Dheeraj Bansal root on 13/6/17.
 * version 1.0.0
 * Add new phone number in case of multiple
 */

@SuppressWarnings("WeakerAccess")
public class PublicKeyAdapter extends RecyclerView.Adapter<PublicKeyAdapter.PhoneViewHolder> {

    private ArrayList<PublicKey> publicKeyList;
    private AddContactActivity activity;

    private boolean isDataSet = true;

    private boolean isFosused;

    public PublicKeyAdapter(AddContactActivity activity, ArrayList<PublicKey> publicKeyList) {
        this.publicKeyList = publicKeyList;
        this.activity = activity;
    }

    public void updateList(ArrayList<PublicKey> publicKeyList) {
        this.publicKeyList = publicKeyList;
        notifyDataSetChanged();
    }

    @Override
    public PhoneViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.public_key_cell, parent, false);

        return new PhoneViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PhoneViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        PublicKey publicKey = publicKeyList.get(position);
        isDataSet = false;
        try {
            if (publicKey.getPublicKey() != null) {
                holder.publicKeyEdt.setText(publicKey.getPublicKey());
                int txtPos = holder.publicKeyEdt.length();
                Editable etext = holder.publicKeyEdt.getText();
                Selection.setSelection(etext, txtPos);
                if (publicKey.getPublicKey().length() > 0) {
                    holder.closeTxt.setVisibility(View.VISIBLE);
                } else {
                    holder.closeTxt.setVisibility(View.INVISIBLE);
                }
            }
            if (publicKeyList.size() - 1 == position) {
                holder.publicKeyEdt.setText("");
                publicKeyList.get(position).setPublicKey("");
                holder.closeTxt.setVisibility(View.INVISIBLE);
            }
            holder.closeTxt.setTag(position);
            holder.barCodeTxt.setTag(position);

        } catch (Exception e) {
        }
        isDataSet = true;
    }

    @Override
    public int getItemCount() {
        return publicKeyList.size();
    }


    public class PhoneViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private EditText publicKeyEdt;
        private BitVaultFont closeTxt;
        private BitVaultFont barCodeTxt;

        public PhoneViewHolder(View itemView) {
            super(itemView);
            publicKeyEdt = (EditText) itemView.findViewById(R.id.id_publickey_edt);
            closeTxt = (BitVaultFont) itemView.findViewById(R.id.id_publickey_close);
            barCodeTxt = (BitVaultFont) itemView.findViewById(R.id.id_publickey_barcodeTxt);
            barCodeTxt.setOnClickListener(this);
            closeTxt.setOnClickListener(this);

            publicKeyEdt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (isDataSet) {
                        int pos = getAdapterPosition();
                        if (pos > -1) {
                            if (publicKeyList.size() - 1 == pos &&
                                    publicKeyEdt.getText().toString().length()>32) {
                                publicKeyList.add(new PublicKey());
                                notifyItemChanged(pos);
                                isFosused=true;
                            }
                            else{
                                isFosused=false;
                            }
                            if (publicKeyList.size() > pos) {

                                publicKeyList.get(pos).setPublicKey(publicKeyEdt.getText().toString());
                            }
                            if (publicKeyEdt.getText().length() > 32) {
                                closeTxt.setVisibility(View.VISIBLE);
                            } else {
                                closeTxt.setVisibility(View.INVISIBLE);
                            }
                            if (PublicKey.emptyCounter(publicKeyList) > 1) {
                                publicKeyList.remove(pos);
                                notifyItemRemoved(pos);
                            }
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.id_publickey_close:
                    if (getAdapterPosition() > -1 && publicKeyList.size() > getAdapterPosition()) {
                        publicKeyList.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                    }
                    break;
                case R.id.id_publickey_barcodeTxt:
                    Utils.keyboardDown(activity);
                    publicKeyEdt.requestFocus();
                    activity.scanBarCode(getAdapterPosition());
                    break;
            }
        }
    }

}
