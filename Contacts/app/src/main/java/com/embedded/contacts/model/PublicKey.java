package com.embedded.contacts.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created Dheeraj Bansal root on 13/6/17.
 * version 1.0.0
 * Bean class for storing public key and address
 */

public class PublicKey implements Parcelable {

    public static final Creator<PublicKey> CREATOR = new Creator<PublicKey>() {
        @Override
        public PublicKey createFromParcel(Parcel in) {
            return new PublicKey(in);
        }

        @Override
        public PublicKey[] newArray(int size) {
            return new PublicKey[size];
        }
    };
    private String publicKey;

    public PublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public PublicKey() {
    }

    protected PublicKey(Parcel in) {
        publicKey = in.readString();
    }

    public static int emptyCounter(ArrayList<PublicKey> publicKeyList) {
        int counter = 0;
        for (int i = 0; i < publicKeyList.size(); i++) {
            if (publicKeyList.get(i).getPublicKey() == null || publicKeyList.get(i).getPublicKey().length() == 0) {
                counter++;
            }
        }
        return counter;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(publicKey);
    }
}
