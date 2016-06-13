package com.paweldylag.yetanotherchat.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;


import android.os.Build;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.paweldylag.yetanotherchat.R;
import com.paweldylag.yetanotherchat.SocketService;

import java.net.URISyntaxException;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

  // UI references.
  private EditText mNameView;
  private EditText mPasswordView;
  private Button buttonEndpoint;
  private View mProgressView;
  private View mLoginFormView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    // Set up the login form.
    mNameView = (EditText) findViewById(R.id.email);

    mPasswordView = (EditText) findViewById(R.id.password);
    buttonEndpoint = (Button) findViewById(R.id.button_endpoint);
    mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (id == R.id.login || id == EditorInfo.IME_NULL) {
          attemptLogin();
          return true;
        }
        return false;
      }
    });

    Button buttonSignIn = (Button) findViewById(R.id.button_sign_in);
    buttonSignIn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        attemptLogin();
      }
    });
    Button buttonRegister = (Button) findViewById(R.id.button_register);
    buttonRegister.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        attemptRegister();
      }
    });

    buttonEndpoint.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(LoginActivity.this, ChangeDescriptionActivity.class);
        startActivityForResult(intent, 0);
      }
    });

    mLoginFormView = findViewById(R.id.login_form);
    mProgressView = findViewById(R.id.login_progress);

    SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
    String endpoint = sharedPref.getString("endpoint", "http://192.168.1.6:3000");
    SocketService.getInstance().setEndpoint(endpoint);

  }


  /**
   * Attempts to sign in or register the account specified by the login form.
   * If there are form errors (invalid email, missing fields, etc.), the
   * errors are presented and no actual login attempt is made.
   */
  private void attemptLogin() {
    // Store values at the time of the login attempt.
    final String name = mNameView.getText().toString();
    String password = mPasswordView.getText().toString();

    if (isInputValid()) {
      // Show a progress spinner, and kick off a background task to
      // perform the user login attempt.x
      showProgress(true);
      SocketService.getInstance().attemptLogin(name, password, new SocketService.Callback() {
        @Override
        public void onSuccess(Object response) {
          startActivity(MainActivity.buildIntent(LoginActivity.this, name));
        }

        @Override
        public void onFailure(final String message) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              showProgress(false);
              Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
          });
        }
      });

    }
  }


  private void attemptRegister() {
    // Store values at the time of the login attempt.
    String name = mNameView.getText().toString();
    String password = mPasswordView.getText().toString();

    if (isInputValid()) {
      // Show a progress spinner, and kick off a background task to
      // perform the user login attempt.
      showProgress(true);
      SocketService.getInstance().attemptRegister(name, password, new SocketService.Callback() {
        @Override
        public void onSuccess(Object response) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              showProgress(false);
              Toast.makeText(LoginActivity.this, "Registered. You can now log in.", Toast.LENGTH_SHORT).show();
            }
          });
        }

        @Override
        public void onFailure(final String message) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              showProgress(false);
              Toast.makeText(LoginActivity.this, "Not registered - user with this login already exists.", Toast.LENGTH_SHORT).show();
            }
          });
        }
      });

    }
  }

  private boolean isInputValid() {
    // Reset errors.
    mNameView.setError(null);
    mPasswordView.setError(null);

    // Store values at the time of the login attempt.
    String name = mNameView.getText().toString();
    String password = mPasswordView.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a not empty password
    if (TextUtils.isEmpty(password) ) {
      mPasswordView.setError(getString(R.string.error_field_required));
      focusView = mPasswordView;
      cancel = true;
    }

    // Check for not empty name.
    if (TextUtils.isEmpty(name)) {
      mNameView.setError(getString(R.string.error_field_required));
      focusView = mNameView;
      cancel = true;
    }
    if (cancel){
      focusView.requestFocus();
    }
    return !cancel;
  }

  /**
   * Shows the progress UI and hides the login form.
   */
  @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
  private void showProgress(final boolean show) {
    // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
    // for very easy animations. If available, use these APIs to fade-in
    // the progress spinner.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
      int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

      mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
      mLoginFormView.animate().setDuration(shortAnimTime).alpha(
          show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
      });

      mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
      mProgressView.animate().setDuration(shortAnimTime).alpha(
          show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
      });
    } else {
      // The ViewPropertyAnimator APIs are not available, so simply show
      // and hide the relevant UI components.
      mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
      mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    showProgress(false);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (data != null && data.getExtras() != null) {
      if (data.getExtras().getString("description") != null) {
        final String description = data.getExtras().getString("description");
        Log.d("LoginActivity", "New endpoint: " + description);
        SharedPreferences sharedPref = getSharedPreferences("settings" , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("endpoint", description);
        editor.apply();
        boolean validEndpoint = SocketService.getInstance().setEndpoint(description);
        if (!validEndpoint){
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(LoginActivity.this, "Not resolved host.", Toast.LENGTH_SHORT).show();
            }
          });
        }
      }
    }
  }
}

