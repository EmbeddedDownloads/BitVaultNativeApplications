package com.embedded.contacts.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.embedded.contacts.AddContactActivity;
import com.embedded.contacts.R;
import com.embedded.contacts.model.Mobile;
import com.embedded.contacts.utils.BitVaultFont;
import com.embedded.contacts.utils.Utils;

import java.util.ArrayList;

/**
 * Created Dheeraj Bansal root on 13/6/17.
 * version 1.0.0
 * Add new phone number in case of multiple
 */
@SuppressWarnings("WeakerAccess")
public class PhoneAdapter extends RecyclerView.Adapter<PhoneAdapter.PhoneViewHolder> {

    private ArrayList<Mobile> mobileList;
    private ArrayAdapter<String> spinnerOwnerType;
    private boolean isDataSet;

    public PhoneAdapter(AddContactActivity activity, ArrayList<Mobile> mobileList) {
        this.mobileList = mobileList;
        String[] phoneType = activity.getResources().getStringArray(R.array.phoneType);
        spinnerOwnerType = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item,
                phoneType);

        spinnerOwnerType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public PhoneViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.phone_cell, parent, false);

        return new PhoneViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PhoneViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        final Mobile mobile = mobileList.get(position);
        isDataSet = false;
        if (mobile.getNumber() != null) {
            holder.phoneEdt.setText(mobile.getNumber());
            if (mobile.getNumber().length() > 0) {
                holder.closeTxt.setVisibility(View.VISIBLE);
            } else {
                holder.closeTxt.setVisibility(View.INVISIBLE);
            }
        }
        if (mobileList.size() - 1 == position) {
            holder.phoneEdt.setText("");
            mobileList.get(position).setNumber("");
            holder.closeTxt.setVisibility(View.INVISIBLE);
        }
        holder.closeTxt.setTag(position);

        if (!(mobile.getNumberType() == null || mobile.getNumberType().length() < 2)) {
            holder.phoneTypeSpinner.setSelection(Utils.getPosition(mobile.getNumberType()));
        }
        isDataSet = true;
    }

    @Override
    public int getItemCount() {
        return mobileList.size();
    }

    public class PhoneViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private EditText phoneEdt;
        private BitVaultFont closeTxt;
        private Spinner phoneTypeSpinner;

        public PhoneViewHolder(View itemView) {
            super(itemView);
            phoneEdt = (EditText) itemView.findViewById(R.id.id_phone_edt);
            closeTxt = (BitVaultFont) itemView.findViewById(R.id.id_phone_close);
            phoneTypeSpinner = (Spinner) itemView.findViewById(R.id.id_phone_type);

            phoneTypeSpinner.setAdapter(spinnerOwnerType);
            closeTxt.setOnClickListener(this);
            phoneEdt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    int pos = getAdapterPosition();
                    if (isDataSet) {
                        if (pos > -1) {
                            if (mobileList.size() - 1 == pos) {
                                String type;
                                if (mobileList.size() + 1 < 7) {
                                    type = Utils.getNumberTypeFromPos(mobileList.size() + 1 - 2);
                                } else {
                                    type = Utils.getNumberTypeFromPos(4);
                                }
                                mobileList.add(new Mobile(null, type));
                                notifyItemChanged(pos);
                            }
                            if (mobileList.size() > pos) {
                                mobileList.get(getLayoutPosition()).setNumber(phoneEdt.getText().toString());
                            }
                            if (phoneEdt.getText().length() > 0) {
                                closeTxt.setVisibility(View.VISIBLE);
                            } else {
                                closeTxt.setVisibility(View.INVISIBLE);
                                if (Mobile.emptyCounter(mobileList) > 1) {
                                    mobileList.remove(pos);
                                    notifyItemRemoved(pos);
                                }
                            }
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    phoneEdt.setSelection(phoneEdt.getText().toString().length());
                }
            });
            phoneTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mobileList.size() > getLayoutPosition()) {
                        mobileList.get(getLayoutPosition()).setNumberType((String) adapterView.getSelectedItem());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.id_phone_close) {
                if (getAdapterPosition() > -1 && mobileList.size() > getAdapterPosition()) {
                    mobileList.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                }
            }
        }

    }


}
