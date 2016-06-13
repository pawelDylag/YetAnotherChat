package com.paweldylag.yetanotherchat.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.nkzawa.emitter.Emitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.paweldylag.yetanotherchat.ChatAdapter;
import com.paweldylag.yetanotherchat.R;
import com.paweldylag.yetanotherchat.SocketService;
import com.paweldylag.yetanotherchat.model.Message;
import com.paweldylag.yetanotherchat.model.Response;

import org.json.JSONObject;

public class ChatActivity extends AppCompatActivity {

  ChatAdapter adapter;
  RecyclerView recyclerView;
  EditText messageEditText;
  Button sendButton;

  public String contactName;
  public String userName;
  private Gson gson = new GsonBuilder().create();
  public Emitter.Listener messageListener = new Emitter.Listener() {
    @Override
    public void call(Object... args) {

      JSONObject data = (JSONObject) args[0];
      final Message m = gson.fromJson(data.toString(), Message.class);
      if (m != null && m.from.equals(contactName)) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            adapter.addNewMessage(new Message(m.from, m.to, m.text));
            recyclerView.smoothScrollToPosition(adapter.getItemCount());
          }
        });
      }

    }
  };

  public static Intent buildIntent(Context from, String userName, String contactName) {
    Intent intent = new Intent(from, ChatActivity.class);
    intent.putExtra("contactName", contactName);
    intent.putExtra("username", userName);
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat);
    this.userName = getIntent().getExtras().getString("username");
    this.contactName = getIntent().getExtras().getString("contactName");
    getSupportActionBar().setTitle("" + contactName);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    messageEditText = (EditText) findViewById(R.id.message_edit_text);
    sendButton = (Button) findViewById(R.id.message_send_button);
    recyclerView = (RecyclerView) findViewById(R.id.chat_list);

    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
    recyclerView.setLayoutManager(mLayoutManager);
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    adapter = new ChatAdapter(userName);
    recyclerView.setAdapter(adapter);
    SocketService.getInstance().registerMessageListener(this.messageListener);

    setupMessagingControls();
    fetchMessageHistory();
  }

  public void fetchMessageHistory() {
    SocketService.getInstance().getMessageHistory(userName,contactName, new SocketService.Callback() {
      @Override
      public void onSuccess(final Object response) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Response r = (Response) response;
            Log.d("ChatActivity", "Successfully fetched history. Items: " +r.history.size());
            adapter.setMessages(r.history);
            recyclerView.smoothScrollToPosition(r.history.size());
          }
        });
      }

      @Override
      public void onFailure(String message) {

      }
    });
  }

  public void setupMessagingControls(){
    sendButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String text = messageEditText.getText().toString();
        if (!text.isEmpty()) {
          messageEditText.setText("");
          SocketService.getInstance().sendMessage(userName, contactName, text);
          adapter.addNewMessage(new Message(userName, contactName, text));
          recyclerView.smoothScrollToPosition(adapter.getItemCount());
        }

      }
    });
  }

}
