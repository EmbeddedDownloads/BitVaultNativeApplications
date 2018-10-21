package com.embedded.contacts.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.embedded.contacts.ContactSearchActivity;
import com.embedded.contacts.MainActivity;
import com.embedded.contacts.R;
import com.embedded.contacts.model.Contacts;
import com.embedded.contacts.model.listcount;
import com.embedded.contacts.ui.CircularImageView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Created Dheeraj Bansal root on 12/6/17.
 * version 1.0.0
 * Display view of contact
 */

@SuppressWarnings("JavaDoc")
public class ContactsListAdapter extends BaseAdapter implements SectionIndexer {

    private List<Contacts> listContact;
    private LayoutInflater inflater;

    private Activity activity;
private listcount mlistcount;
    private String[] sections;
    private ArrayList<String> sectionsList;
    private Map<String, Integer> map = new LinkedHashMap<>();

    private boolean isSearchMode;
    private boolean isSecuredSelected = true;

    public ContactsListAdapter(Activity activity, List<Contacts> listContact) {
        mlistcount = new listcount();
        this.activity = activity;
        this.listContact = new ArrayList<>();
        updateDataSet(listContact);
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setSecuredSelected(boolean securedSelected) {
        isSecuredSelected = securedSelected;
    }

    @Override
    public int getCount() {
        return listContact.size();
    }

    @Override
    public Object getItem(int i) {
        return listContact.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void setSearchMode(boolean searchMode) {
        isSearchMode = searchMode;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final ContactViewHolder holder;

        View vi = view;
        if (view == null) {
            vi = inflater.inflate(R.layout.contact_cell, null);
            holder = new ContactViewHolder();
            holder.sepratorLout = (LinearLayout) vi.findViewById(R.id.separator);
            holder.sepratorText = (TextView) vi.findViewById(R.id.separator_text);
            holder.contactImage = (CircularImageView) vi.findViewById(R.id.contact_picture);
            holder.contactText = (TextView) vi.findViewById(R.id.contact_image);
            holder.contactNameTxt = (TextView) vi.findViewById(R.id.name);
            holder.number = (TextView) vi.findViewById(R.id.number);
            holder.public_key = (TextView) vi.findViewById(R.id.public_key);
            holder.secureImg = (ImageView) vi.findViewById(R.id.secureImg);
            vi.setTag(holder);
        } else {
            holder = (ContactViewHolder) vi.getTag();
        }

        Contacts contacts = listContact.get(position);

        if (!isSearchMode) {
            if (getPositionForSection(getSectionForPosition(position)) != position) {
                holder.sepratorLout.setVisibility(View.INVISIBLE);
            } else {
                holder.sepratorLout.setVisibility(View.VISIBLE);
                String fullName = contacts.getName();
                if (fullName != null && !fullName.isEmpty()) {
                    if (Character.isDigit(fullName.charAt(0))) {
                        fullName = "#";
                    }
                    holder.sepratorText.setText(String.valueOf(fullName.charAt(0)));
                }
            }
        } else {
            holder.sepratorLout.setVisibility(View.GONE);
        }

        if ((activity instanceof MainActivity && !((MainActivity) activity).isSecureSelected()) || !isSecuredSelected) {
            if (contacts.isSecure()) {
                holder.secureImg.setVisibility(View.VISIBLE);
            } else {
                holder.secureImg.setVisibility(View.GONE);
            }
        }

        if (contacts.getPhotoUri() != null && contacts.getPhotoUri().length() > 0) {
            holder.contactImage.setVisibility(View.VISIBLE);
            holder.contactText.setVisibility(View.GONE);
            holder.contactImage.setImageURI(Uri.parse(contacts.getPhotoUri()));
        } else {
            holder.contactImage.setVisibility(View.GONE);
            holder.contactText.setVisibility(View.VISIBLE);
        }
        setTextImage(holder, contacts, position);

        return vi;
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sectionIndex >= sections.length || sectionIndex < 0) {
            return 0;
        }
        return map.get(sections[sectionIndex]);
    }

    @Override
    public int getSectionForPosition(int position) {
        if (position >= listContact.size() || position < 0) {
            return 0;
        }
        String fullName = listContact.get(position).getName();
        if (fullName == null || fullName.isEmpty()) {
            return 0;
        }
        String letter = fullName.substring(0, 1).toUpperCase(Locale.getDefault());
        return sectionsList.indexOf(letter);
    }

    /**
     * color code randomly
     *
     * @param name
     * @return
     */
    private int getContactColorCode(String name) {
        int size = name.length();
        if (size > 2) {
            char lastChar = name.charAt(size - 1);
            char secondLastChar = name.charAt(size - 2);
            int lastASCII = (int) lastChar;
            int secondLastASCII = (int) secondLastChar;
            int Added = lastASCII + secondLastASCII;
            int remainder = (Added % 5);
            switch (remainder) {
                case 0:
                    return 0;
                case 1:
                    return 1;
                case 2:
                    return 2;
                case 3:
                    return 3;
                case 4:
                    return 4;
            }
        }
        return 5;
    }

    /**
     * Update list notify
     *
     * @param list
     */
    public void updateDataSet(List<Contacts> list) {
        listContact.clear();
        listContact.addAll(list);

        map = new LinkedHashMap<>();
        String prevLetter = null;
        for (int i = 0; i < list.size(); i++) {
            Contacts contact = list.get(i);
            String fullName = contact.getName();
            if (fullName == null || fullName.isEmpty()) {
                continue;
            }
            String firstLetter = fullName.substring(0, 1).toUpperCase(Locale.getDefault());
            if (Character.isDigit(firstLetter.charAt(0))) {
                firstLetter = "#";
            }
            if (!firstLetter.equals(prevLetter)) {
                prevLetter = firstLetter;
                map.put(firstLetter, i);
            }
        }
        sectionsList = new ArrayList<>(map.keySet());
        sections = new String[sectionsList.size()];
        sectionsList.toArray(sections);

        notifyDataSetChanged();
    }

    /**
     * CLear and add new list
     *
     * @param list
     */
    public void addList(List<Contacts> list) {
        listContact.clear();
        listContact.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * Set background color for circle text
     *
     * @param holder
     * @param contacts
     * @param position
     */
    private void setTextImage(ContactViewHolder holder, Contacts contacts, int position) {
// ui update according to search
        if (contacts.getName() != null) {
            holder.contactNameTxt.setText(contacts.getName());

            if (isSearchMode) {
                if (contacts.getListNumber().size() > 0) {
                    holder.number.setVisibility(View.VISIBLE);
                    holder.number.setText(contacts.getListNumber().get(mlistcount.getlistnumber()).getNumber());
                }else {holder.number.setVisibility(View.GONE);}
                if (contacts.getListPublicKey().size() > 0) {
                    holder.public_key.setVisibility(View.VISIBLE);
                    holder.public_key.setText(contacts.getListPublicKey().get(mlistcount.getpublickeynumber()).getPublicKey());
                } else {holder.public_key.setVisibility(View.GONE);}
            }
            else {
                holder.number.setVisibility(View.GONE);
                holder.public_key.setVisibility(View.GONE);
            }

            String name = contacts.getName().substring(0, 1);

            holder.contactText.setText(name.toUpperCase());
            int code = getContactColorCode(contacts.getName());
            if (code == 0) {
                holder.contactText.setBackground(activity.getDrawable(R.drawable.contact_circle_view_0));
            } else if (code == 1) {
                holder.contactText.setBackground(activity.getDrawable(R.drawable.contact_circle_view_1));
            } else if (code == 2) {
                holder.contactText.setBackground(activity.getDrawable(R.drawable.contact_circle_view_2));
            } else if (code == 3) {
                holder.contactText.setBackground(activity.getDrawable(R.drawable.contact_circle_view_3));
            } else if (code == 4) {
                holder.contactText.setBackground(activity.getDrawable(R.drawable.contact_circle_view_4));
            } else {
                holder.contactText.setBackground(activity.getDrawable(R.drawable.contact_circle_view));
            }
            ((ContactSearchActivity) activity).updateList(position, code);
        } else {
            for (int i = 0; i < contacts.getListNumber().size(); i++) {
                if (contacts.getListNumber().get(i) != null) {
                    holder.contactNameTxt.setText(contacts.getListNumber().get(i).getNumber());
                    holder.number.setText(contacts.getListNumber().get(0).getNumber());
                    holder.public_key.setText(contacts.getListPublicKey().get(0).getPublicKey());
                    break;
                }
            }

        }
    }

    private class ContactViewHolder {
        private LinearLayout sepratorLout;
        private TextView sepratorText;
        private CircularImageView contactImage;
        private TextView contactText;
        private TextView contactNameTxt;
        private TextView number;
        private ImageView secureImg;
        private TextView public_key;
    }

}
