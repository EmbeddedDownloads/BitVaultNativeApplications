package com.embedded.contacts.task;

import com.embedded.contacts.model.Contacts;

import java.util.List;

/**
 * Created Dheeraj Bansal root on 13/6/17.
 * version 1.0.0
 * Callback after read all contacts
 */

public interface OnContactsReadListener {

    void onReadComplete(List<Contacts> list);
}
