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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
//import androidx.FloatingActionButton;
import android.speech.RecognizerIntent;
import android.text.DynamicLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The main (and only) activity of this demo app. Displays a text box which updates as messages are
 * received.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SmartReplyDemo";
    private SmartReplyClient client;
    private Handler handler;
    private ArrayList<String> s_words;
    private Translate tradutor;
    private ArrayList<TextView> sugg = new ArrayList<>();
    private TextView suggestion1;
    private TextView suggestion2;
    private TextView suggestion3;
    private TextView suggestion4;
    private TextView suggestion5;
    private TextView suggestion6;
    private static final int NUBER_OF_SUGGESTIONS = 5;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private EditText meuAudioEmTexto;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.tradutor = Translate.getInstance(this);
        setContentView(R.layout.main_activity);
        suggestion1 = findViewById(R.id.suggestion1);
        suggestion2 = findViewById(R.id.suggestion2);
        suggestion3 = findViewById(R.id.suggestion3);
        suggestion4 = findViewById(R.id.suggestion4);
        suggestion5 = findViewById(R.id.suggestion5);
        suggestion6 = findViewById(R.id.suggestion6);
        meuAudioEmTexto = findViewById(R.id.theTextToSpeechTextView);

        // Negocios do SmartReply
        client = new SmartReplyClient(getApplicationContext());
        handler = new Handler();
        s_words = new ArrayList<String>();
        sugg.add(suggestion1);
        sugg.add(suggestion2);
        sugg.add(suggestion3);
        sugg.add(suggestion4);
        sugg.add(suggestion5);
        sugg.add(suggestion6);

        // Deixa teclado sempre aberto
        upKeyboard(meuAudioEmTexto);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
        handler.post(
                () -> {
                    client.loadModel();
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
                    //passar uma string como entrada, o client SMART REPLY, uma lista de respostas 's_words', e a Main Activity
                    tradutor.traduzirPT_EN(new String[] {message}, client, s_words, this);
                    meuAudioEmTexto.setText("");
                });
    }

    public void appendMessage(final String message, final int i) {
        handler.post(
                () -> {
                    if ( i <= NUBER_OF_SUGGESTIONS ) {
                        sugg.get(i).setText(message);
                    }
                });
    }

    //Deixa teclado sempre visivel
    public void upKeyboard(View view) {
        meuAudioEmTexto.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(meuAudioEmTexto, InputMethodManager.SHOW_IMPLICIT);
    }

    //Metodos de ação de clique
    public void ouvir(View view) {
        upKeyboard(view);
        limpar(view);
        startVoiceInput();
    }

    public void limpar(View view) {
        meuAudioEmTexto.setText("");
        //textoSugeridoSelecionado.setText("");
        suggestion1.setText("--");
        suggestion2.setText("--");
        suggestion3.setText("--");
        suggestion4.setText("--");
        suggestion5.setText("--");
        suggestion6.setText("--");
        //string = "";
    }

    //Metodos de audição
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ouvindo...");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    meuAudioEmTexto.setText(result.get(0));
                    send(result.get(0));
                }
                break;
            }
        }
    }

}