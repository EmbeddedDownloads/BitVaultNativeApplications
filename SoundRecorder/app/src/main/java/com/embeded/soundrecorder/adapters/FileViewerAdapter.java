package com.embeded.soundrecorder.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.embeded.soundrecorder.DBHelper;
import com.embeded.soundrecorder.R;
import com.embeded.soundrecorder.RecordingItem;
import com.embeded.soundrecorder.fragments.FileViewerFragment;
import com.embeded.soundrecorder.fragments.PlaybackFragment;
import com.embeded.soundrecorder.listeners.OnDatabaseChangedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder>
        implements OnDatabaseChangedListener {

    private static final String LOG_TAG = "FileViewerAdapter";
    public ArrayList<String> entrys=new ArrayList<>();
    public int addcount = 0,count=0;
    RecordingItem item;
    Context mContext;
    LinearLayoutManager llm;
    private DBHelper mDatabase;
    private FileViewerFragment fragment;
    public FileViewerAdapter(Context context, FileViewerFragment fragment, LinearLayoutManager linearLayoutManager) {
        super();
        mContext = context;
        mDatabase = new DBHelper(mContext);
        mDatabase.setOnDatabaseChangedListener(this);
        llm = linearLayoutManager;

        addcount = 0;
        this.fragment = fragment;
    }

    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, int position) {

        item = getItem(position);
        long itemDuration = item.getLength();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);
        if(entrys.size()>0){
            fragment.menuIconVisivility(true);
        }
        holder.buttonViewOption.setVisibility(View.VISIBLE);
        holder.vName.setText(item.getName());
        holder.vLength.setText(String.format("%02d:%02d", minutes, seconds));
        holder.vDateAdded.setText(
                DateUtils.formatDateTime(
                        mContext,
                        item.getTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
                )
        );
        holder.buttonViewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //will show popup menu here
                PopupMenu popup = new PopupMenu(mContext, view);
                popup.getMenuInflater().inflate(R.menu.popup_install, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.id_action_rename) {
                            renameFileDialog(holder.getPosition());
                        } else {
                            deleteFileDialog(holder.getPosition());
                        }
                        return true;
                    }
                });
                popup.show();

            }
        });
        // define an on click listener to open PlaybackFragment
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addcount <= 0) {
                    try {
                        PlaybackFragment playbackFragment =
                                new PlaybackFragment().newInstance(getItem(holder.getPosition()));

                        FragmentTransaction transaction = ((FragmentActivity) mContext)
                                .getSupportFragmentManager()
                                .beginTransaction();

                        playbackFragment.show(transaction, "dialog_playback");

                    } catch (Exception e) {
                    }
                } else {

                    if (entrys.contains(mDatabase.getItemAt(holder.getPosition()).getName())) {
                        entrys.remove(mDatabase.getItemAt(holder.getPosition()).getName());
                        holder.cardView.setBackgroundResource(R.color.white);
                        addcount--;
                        holder.check.setVisibility(View.GONE);
                        holder.buttonViewOption.setVisibility(View.VISIBLE);
                        if (addcount == 0) {
                            fragment.menuIconVisivility(false);
                            fragment.hideUpButton();
                        }
                    } else {
                        addcount++;
                        entrys.add(mDatabase.getItemAt(holder.getPosition()).getName());
                        holder.cardView.setBackgroundResource(R.color.cancel_ctn);
                        fragment.menuIconVisivility(true);

                        holder.check.setVisibility(View.VISIBLE);
                        holder.buttonViewOption.setVisibility(View.GONE);
                    }
                }
            }
        });



        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                count = 0;
                addcount++;
                fragment.showUpButton();
                fragment.menuIconVisivility(true);
              // Log.e("amit","amit "+ mDatabase.getItemAt(holder.getPosition()).getName());
                entrys.add(mDatabase.getItemAt(holder.getPosition()).getName());
                holder.cardView.setBackgroundResource(R.color.cancel_ctn);
                holder.check.setVisibility(View.VISIBLE);
                holder.buttonViewOption.setVisibility(View.GONE);
                return true;
            }
        });

        if (entrys.contains(mDatabase.getItemAt(holder.getPosition()).getName())){
            holder.check.setVisibility(View.VISIBLE);
            holder.cardView.setBackgroundResource(R.color.cancel_ctn);
        }
        else{
            holder.cardView.setBackgroundResource(R.color.white);
            holder.check.setVisibility(View.GONE);
        }


    }
    public void clearList(){

    }

    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.card_view, parent, false);

        mContext = parent.getContext();

        return new RecordingsViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return mDatabase.getCount();
    }

    public RecordingItem getItem(int position) {
        return mDatabase.getItemAt(position);
    }

    @Override
    public void onNewDatabaseEntryAdded() {
        //item added to top of the list
        notifyItemInserted(getItemCount() - 1);
        llm.scrollToPosition(getItemCount() - 1);
    }

    @Override
    //TODO
    public void onDatabaseEntryRenamed() {

    }

    public void removeAllOneByOne(String value) {
        //remove item from database, recyclerview and storage
        //delete file from storage
        File file = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder/"+value);
        file.delete();

        mDatabase.removeItemWithName(value);
        count++;
    }

    public void remove(int position) {
        //remove item from database, recyclerview and storage
        //delete file from storage
        File file = new File(getItem(position).getFilePath());
        file.delete();

        Toast.makeText(mContext, String.format(mContext.getString(R.string.toast_file_delete),
                getItem(position).getName()),
                Toast.LENGTH_SHORT
        ).show();

        mDatabase.removeItemWithId(getItem(position).getId());
        notifyItemRemoved(position);
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/");
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(f));
        mContext.sendBroadcast(intent);

    }

    //TODO
    public void removeOutOfApp(String filePath) {
        //user deletes a saved recording out of the application through another application
    }

    public void rename(int position, String name) {
        //rename a file
        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/" + name;
        File f = new File(mFilePath);

        if (f.exists() && !f.isDirectory()) {
            //file name is not unique, cannot rename file.
            Toast.makeText(mContext, String.format(mContext.getString(R.string.toast_file_exists), name),
                    Toast.LENGTH_SHORT).show();

        } else {
            //file name is unique, rename file
            File oldFilePath = new File(getItem(position).getFilePath());
            oldFilePath.renameTo(f);
            mDatabase.renameItem(getItem(position), name, mFilePath);
            notifyItemChanged(position);
        }
    }

    public void shareFileDialog(int position) {
        // file share dialogue
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(getItem(position).getFilePath())));
        shareIntent.setType("audio/mp3");
        mContext.startActivity(Intent.createChooser(shareIntent, mContext.getText(R.string.send_to)));
    }

    public void renameFileDialog(final int position) {
        // File rename dialog
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.rename_dialogue, null);
      //  renameFileBuilder.setTitle(mContext.getString(R.string.dialog_title_rename));
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setView(view);
        final AlertDialog alert = renameFileBuilder.create();
        alert.show();
        final EditText input = (EditText) view.findViewById(R.id.new_name);
        final Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (input.getText().toString().equals("")) {
                        input.setError(mContext.getResources().getString(R.string.error_message));
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.file_not_rename), Toast.LENGTH_LONG).show();
                    } else {
                        String value = input.getText().toString().trim() + ".mp3";
                        rename(position, value);
                        alert.cancel();
                    }
                } catch (Exception e) {
                }

                alert.dismiss();

            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
            }
        });

    }

    public void deleteFileDialog(final int position) {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.delete_dialogue, null);
       // confirmDelete.setTitle(mContext.getString(R.string.dialog_title_delete));

        //confirmDelete.setMessage(mContext.getString(R.string.dialog_text_delete));
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
                    remove(position);

                } catch (Exception e) {
                }

                alert.dismiss();

            }
        });
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
            }
        });

    }

    public static class RecordingsViewHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected TextView vLength;
        protected TextView vDateAdded;
        protected View cardView;
        protected TextView buttonViewOption;
        protected ImageView check;

        public RecordingsViewHolder(View v) {
            super(v);
            vName = (TextView) v.findViewById(R.id.file_name_text);
            vLength = (TextView) v.findViewById(R.id.file_length_text);
            vDateAdded = (TextView) v.findViewById(R.id.file_date_added_text);
            cardView = v.findViewById(R.id.card_view);
            buttonViewOption = (TextView) itemView.findViewById(R.id.textViewOptions);
            check = (ImageView) v.findViewById(R.id.check);
        }
    }
}