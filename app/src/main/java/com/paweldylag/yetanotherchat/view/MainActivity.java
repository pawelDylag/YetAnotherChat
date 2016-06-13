package com.paweldylag.yetanotherchat.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.paweldylag.yetanotherchat.ContactsAdapter;
import com.paweldylag.yetanotherchat.R;
import com.paweldylag.yetanotherchat.SocketService;
import com.paweldylag.yetanotherchat.model.Contact;
import com.paweldylag.yetanotherchat.model.Message;
import com.paweldylag.yetanotherchat.model.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ContactsAdapter.OnContactClickListener {

  ContactsAdapter adapter;
  RecyclerView recyclerView;
  SwipeRefreshLayout swipeRefreshLayout;
  ProgressBar progressBar;
  FloatingActionButton fab;
  TextView noContactsView;
  CoordinatorLayout coordinatorLayout;
  Emitter.Listener newMessageListener;
  Gson gson = new GsonBuilder().create();

  public String currentUsername;
  public String currentUserInfoText;

  public static Intent buildIntent(Context from, String userName) {
    Intent intent = new Intent(from, MainActivity.class);
    intent.putExtra("username", userName);
    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    this.currentUsername = getIntent().getExtras().getString("username");
    getSupportActionBar().setTitle("Hello, " + currentUsername + "!");

    setupFab();

    noContactsView = (TextView) findViewById(R.id.no_contacts_view);
    progressBar = (ProgressBar) findViewById(R.id.main_progress);
    coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
    recyclerView = (RecyclerView) findViewById(R.id.list);
    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
    recyclerView.setLayoutManager(mLayoutManager);
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
       fetchContacts();

      }
    });
    adapter = new ContactsAdapter(this);
    recyclerView.setAdapter(adapter);
    recyclerView.setVisibility(View.GONE);
    noContactsView.setVisibility(View.GONE);
    progressBar.setVisibility(View.VISIBLE);
    setupMessageListener();
    fetchContacts();
  }

  private void setupMessageListener(){
    newMessageListener = new Emitter.Listener() {
      @Override
      public void call(Object... args) {
        JSONObject data = (JSONObject) args[0];
        final Message m = gson.fromJson(data.toString(), Message.class);
        if (m != null) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Snackbar.make(coordinatorLayout, m.from + ": " + ellipsize(m.text, 15), Snackbar.LENGTH_SHORT).show();
            }
          });
        }

      }
    };
  }

  private String ellipsize(String input, int maxLength) {
    String ellip = "...";
    if (input == null || input.length() <= maxLength
        || input.length() < ellip.length()) {
      return input;
    }
    return input.substring(0, maxLength - ellip.length()).concat(ellip);
  }

  public void fetchContacts() {
    SocketService.getInstance().getContacts(currentUsername, new SocketService.Callback() {
      @Override
      public void onSuccess(Object response) {
        final Response r = (Response) response;
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            swipeRefreshLayout.setRefreshing(false);
            adapter.setData(r.data);
            if (adapter.getItemCount() == 0) {
              progressBar.setVisibility(View.GONE);
              recyclerView.setVisibility(View.GONE);
              noContactsView.setVisibility(View.VISIBLE);
            } else {
              recyclerView.setVisibility(View.VISIBLE);
              progressBar.setVisibility(View.GONE);
              noContactsView.setVisibility(View.GONE);
            }
          }
        });
      }

      @Override
      public void onFailure(String message) {
        swipeRefreshLayout.setRefreshing(false);
      }
    });
  }

  private void setupFab(){
    fab = (FloatingActionButton) findViewById(R.id.fab);
    if (fab != null) {
      fab.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          SocketService.getInstance().getAllUsers(new SocketService.Callback() {
            @Override
            public void onSuccess(Object response) {
              final Response r = (Response) response;
              Iterator<Contact> iter = r.data.iterator();
              while(iter.hasNext()){
                Contact c = iter.next();
                if (c == null || c.name == null || c.name.equals(currentUsername)) {

                }
              }
              final CharSequence[] items = new CharSequence[r.data.size()];
              for (int i = 0; i < r.data.size(); i++) {
                  items[i] = r.data.get(i).name;
              }
              final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
              builder.setTitle("Add new contact");
              builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                  SocketService.getInstance().addNewContact(items[item].toString());
                  fetchContacts();
                }
              });
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  AlertDialog alert = builder.create();
                  alert.show();
                }
              });

            }

            @Override
            public void onFailure(String message) {

            }
          });
        }
      });
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    SocketService.getInstance().logout();
  }

  @Override
  public void onClick(Contact contact) {
    startActivityForResult(ChatActivity.buildIntent(MainActivity.this, currentUsername ,contact.name), 1);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_change_description:
        Intent intent = new Intent(this, ChangeDescriptionActivity.class);
        startActivityForResult(intent, 0);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (data != null && data.getExtras() != null) {
      if (data.getExtras().getString("description") != null) {
        final String description = data.getExtras().getString("description");
        Log.d("MainActivity", "New description: " + description);
        SocketService.getInstance().changeDescription(description, new SocketService.Callback() {
          @Override
          public void onSuccess(Object response) {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Snackbar.make(coordinatorLayout, "Your new description: " + description, Snackbar.LENGTH_SHORT).show();
              }
            });
          }

          @Override
          public void onFailure(String message) {
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Snackbar.make(coordinatorLayout, "Error while changing description :(", Snackbar.LENGTH_SHORT).show();
              }
            });
          }
        });
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    SocketService.getInstance().registerMessageListener(this.newMessageListener);
  }
}
