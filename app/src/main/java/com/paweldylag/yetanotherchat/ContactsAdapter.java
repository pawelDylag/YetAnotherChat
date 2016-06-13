package com.paweldylag.yetanotherchat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.paweldylag.yetanotherchat.model.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pawel Dylag (pawel.dylag@estimote.com)
 */
public class ContactsAdapter extends RecyclerView.Adapter{

  private final List<Contact> contacts = new ArrayList<>();
  private final OnContactClickListener onContactClickListener;

  public interface OnContactClickListener {
    void onClick(Contact contact);
  }

  public ContactsAdapter(OnContactClickListener onClickListener) {
    this.onContactClickListener = onClickListener;
  }

  private class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private TextView name, description;
    private ImageView status;

    public ContactViewHolder(View itemView) {
      super(itemView);
      name = (TextView) itemView.findViewById(R.id.name_view);
      status = (ImageView) itemView.findViewById(R.id.status_view);
      description = (TextView) itemView.findViewById(R.id.description_view);
      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
      onContactClickListener.onClick(contacts.get(getAdapterPosition()));
    }
  }

  public void setData (List<Contact> list) {
    this.contacts.clear();
    this.contacts.addAll(list);
    notifyDataSetChanged();
  }

  @Override
  public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.contact, parent, false);
    return new ContactViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    ContactViewHolder vh = (ContactViewHolder) holder;
    Contact contact = contacts.get(position);
    vh.name.setText(contact.name);
    if (contact.online) {
      vh.status.setImageResource(R.drawable.available_circle);
    } else {
      vh.status.setImageResource(R.drawable.offline_circle);
    }
    vh.description.setText(contact.description);
  }

  @Override
  public int getItemCount() {
    return contacts.size();
  }
}
