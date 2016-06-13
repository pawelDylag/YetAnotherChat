package com.paweldylag.yetanotherchat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.paweldylag.yetanotherchat.model.Message;
import com.paweldylag.yetanotherchat.model.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pawel Dylag (pawel.dylag@estimote.com)
 */
public class ChatAdapter extends RecyclerView.Adapter{

  private final List<Message> messages = new ArrayList<>();
  private final String username;

  public interface OnContactClickListener {
    void onClick(Contact contact);
  }

  public ChatAdapter(String username) {
    this.username = username;
  }

  private class ContactViewHolder extends RecyclerView.ViewHolder  {

    private TextView name, text;

    public ContactViewHolder(View itemView) {
      super(itemView);
      name = (TextView) itemView.findViewById(R.id.chat_name_view);
      text = (TextView) itemView.findViewById(R.id.chat_text_view);
    }

  }

  public void setMessages(List<Message> messages) {
    this.messages.clear();
    this.messages.addAll(messages);
    notifyDataSetChanged();

  }

  public void addNewMessage (Message message) {
    this.messages.add(message);
    notifyDataSetChanged();
  }

  @Override
  public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView;
    if (viewType == 0) {
     itemView = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.chat_item, parent, false);
    } else {
      itemView = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.chat_item_self, parent, false);
    }

    return new ContactViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    ContactViewHolder vh = (ContactViewHolder) holder;
    Message message = messages.get(position);
    if (vh.name != null) {
      vh.name.setText(message.from);
    }
    vh.text.setText(message.text);
  }

  @Override
  public int getItemCount() {
    return messages.size();
  }

  @Override
  public int getItemViewType(int position) {
    if (messages.get(position).from.equals(username)){
      return 1;
    } else return 0;
  }
}
