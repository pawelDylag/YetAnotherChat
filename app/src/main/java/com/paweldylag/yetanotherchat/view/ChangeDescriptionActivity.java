package com.paweldylag.yetanotherchat.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.paweldylag.yetanotherchat.R;

public class ChangeDescriptionActivity extends AppCompatActivity {

  EditText editText;
  Button button;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_change_description);
    editText = (EditText) findViewById(R.id.edit_text_description);
    button = (Button) findViewById(R.id.button_change_description);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Reset errors.
        editText.setError(null);
        String description = editText.getText().toString();
        if(!description.isEmpty()) {
          if (description.length() < 36) {
            Bundle data = new Bundle();
            data.putString("description", description);
            Intent intent = new Intent();
            intent.putExtras(data);
            setResult(RESULT_OK, intent);
            finish();
          } else {
            editText.setError("Description length must be less than 24 characters ");
            editText.requestFocus();
          }
        } else {
          editText.setError("Description cannot be empty");
          editText.requestFocus();
        }
      }
    });

  }
}
