/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.example.android.smartreply;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
//import androidx.FloatingActionButton;
import android.text.DynamicLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The main (and only) activity of this demo app. Displays a text box which updates as messages are
 * received.
 */
public class MainActivity extends Activity {
    private static final String TAG = "SmartReplyDemo";
    private SmartReplyClient client;

    private Button sendButton;
    private TextView messageTextView;
    private EditText messageInput;
    private FloatingActionButton tempButton;

    private Handler handler;

    private ArrayList<String> s_words;
    private Translate tradutor;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.tradutor = Translate.getInstance(this);
        Log.v(TAG, "onCreate");
        setContentView(R.layout.main_activity);

        client = new SmartReplyClient(getApplicationContext());
        handler = new Handler();

        sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(
                (View v) -> {
                    send(messageInput.getText().toString());
                });

        messageTextView = findViewById(R.id.message_text);
        messageInput = findViewById(R.id.message_input);
//        tempButton = findViewById(R.id.floatButton);
        s_words = new ArrayList<String>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
        handler.post(
                () -> {
                    client.loadModel();
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop");
        handler.post(
                () -> {
                    client.unloadModel();
                });
    }

    private void send(final String message) {
        handler.post(
                () -> {
                    messageTextView.append("Input: " + message + "\n");
                    //passar uma string como entrada, o client SMART REPLY, uma lista de respostas 's_words', e a Main Activity
                    tradutor.traduzirPT_EN(new String[] {message}, client, s_words, this);

                    appendMessage("------");
                    messageInput.setText("");

                });
    }

    public void appendMessage(final String message) {
        handler.post(
                () -> {
                    messageTextView.append(message + "\n");
                });
    }
}