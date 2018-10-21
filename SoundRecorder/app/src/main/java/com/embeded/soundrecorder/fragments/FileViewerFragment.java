package com.embeded.soundrecorder.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.embeded.soundrecorder.R;
import com.embeded.soundrecorder.adapters.FileViewerAdapter;

import java.io.File;

public class FileViewerFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = "FileViewerFragment";
    MenuItem menu1, menu2;
    RecyclerView mRecyclerView;
    private int position;
    private FileViewerAdapter mFileViewerAdapter;
    FileObserver observer =
            new FileObserver(android.os.Environment.getExternalStorageDirectory().toString()
                    + R.string.Sound_Recorder) {
                // set up a file observer to watch this directory on sd card
                @Override
                public void onEvent(int event, String file) {
                    if (event == FileObserver.DELETE) {
                        // user deletes a recording file out of the app

                        String filePath = android.os.Environment.getExternalStorageDirectory().toString()
                                + R.string.Sound_Recorder + file + "]";

                        Log.d(LOG_TAG, "File deleted ["
                                + android.os.Environment.getExternalStorageDirectory().toString()
                                + R.string.Sound_Recorder + file + "]");

                        // remove file from database and recyclerview
                        mFileViewerAdapter.removeOutOfApp(filePath);
                    }
                }
            };

    public static FileViewerFragment newInstance(int position) {
        FileViewerFragment f = new FileViewerFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        observer.startWatching();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        menu1 = menu.findItem(R.id.id_delete);
        menu2 = menu.findItem(R.id.id_deselect);
        if (mFileViewerAdapter.entrys.size() > 0) {
            //Option icon hide
            menu1.setVisible(true);
            menu2.setVisible(true);

            mFileViewerAdapter.entrys.clear();
            mFileViewerAdapter.addcount = 0;
            menu1.setVisible(false);
            menu2.setVisible(false);
            mFileViewerAdapter.notifyDataSetChanged();
        }

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.id_delete:
                //delete files
                delete();
                return false;

            case R.id.id_deselect:
                // deselect files
                mFileViewerAdapter.entrys.clear();
                mFileViewerAdapter.addcount = 0;
                menu1.setVisible(false);
                mFileViewerAdapter.notifyDataSetChanged();
                menu2.setVisible(false);
                return false;

            default:
                break;
        }

        return false;
    }

    public void showUpButton() {
        menu2.setVisible(true);
    } //deselect icon visible

    public void hideUpButton() {
        menu2.setVisible(false);
    }// deselect icon invisible

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_file_viewer, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        return v;
    }



    public void menuIconVisivility(boolean isVisible) {
        menu1.setVisible(isVisible);
    }

    private void delete() {
        //files deleted from database and screen too
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.delete_dialogue, null);
        // confirmDelete.setTitle(getActivity().getString(R.string.dialog_title_delete));
        //confirmDelete.setMessage(getActivity().getString(R.string.dialog_text_delete1));
        confirmDelete.setCancelable(true);
        confirmDelete.setView(view);
        final AlertDialog alert = confirmDelete.create();
        alert.show();
        final Button btn_yes = (Button) view.findViewById(R.id.btn_yes);
        final Button btn_no = (Button) view.findViewById(R.id.btn_no);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //remove item from database, recyclerview, and storage
                    for (int i = 0; i < mFileViewerAdapter.entrys.size(); i++) {
                        mFileViewerAdapter.removeAllOneByOne(mFileViewerAdapter.entrys.get(i));
                    }
                    alert.dismiss();
                    mFileViewerAdapter.entrys.clear();
                    mFileViewerAdapter.addcount = 0;
                    menu1.setVisible(false);
                    menu2.setVisible(false);
                    mFileViewerAdapter.notifyDataSetChanged();
                    File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/");
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(f));
                    getActivity().sendBroadcast(intent);
                } catch (Exception e) {
                }
            }
        });
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alert.dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mFileViewerAdapter = new FileViewerAdapter(getActivity(), this, llm);
        mRecyclerView.setAdapter(mFileViewerAdapter);
    }

    public void clearList(){
        if (mFileViewerAdapter.entrys.size() > 0) {
            mFileViewerAdapter.entrys.clear();
            mFileViewerAdapter.addcount = 0;
            menu1.setVisible(false);
            menu2.setVisible(false);
            mFileViewerAdapter.notifyDataSetChanged();
        }else {
            System.exit(0);
        }
    }
}


