package com.embedded.contacts;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created Dheeraj Bansal root on 14/6/17.
 * version 1.0.0
 * Permission dialog before delete contact
 */

public class DeleteContactDialog extends Dialog implements View.OnClickListener {


    private OnDeleteDialogListener listener;

    public DeleteContactDialog(@NonNull Context context, OnDeleteDialogListener listener) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_delete);
        this.listener = listener;
        Window window = getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        findViewById(R.id.id_dialog_yes).setOnClickListener(this);
        findViewById(R.id.id_dialog_no).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.id_dialog_yes:
                listener.onDialogButtonPress(true);
                dismiss();
                break;
            case R.id.id_dialog_no:
                listener.onDialogButtonPress(false);
                dismiss();
                break;
        }
    }
}
