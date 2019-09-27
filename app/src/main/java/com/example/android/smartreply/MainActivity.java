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
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
//import androidx.FloatingActionButton;
import android.speech.RecognizerIntent;
import android.text.DynamicLayout;
import android.util.Log;
import android.view.MotionEvent;
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
    private ServiceFalar ttsManager = null;
    RoletaDaEscolha roletaDaEscolha = new RoletaDaEscolha();
    public String escolha;

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

        // Negocios da Fala
        ttsManager = new ServiceFalar();
        ttsManager.init(this);
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
                    if (i == NUBER_OF_SUGGESTIONS + 1) {
                        roletaDaEscolha.execute(NUBER_OF_SUGGESTIONS + 2);
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

    //Metodos de ação de clique/toque
    public void ouvir(View view) {
        upKeyboard(view);
        limpar(view);
        startVoiceInput();
    }

    public void limpar(View view) {
        meuAudioEmTexto.setText("");
        suggestion1.setText("--");
        suggestion2.setText("--");
        suggestion3.setText("--");
        suggestion4.setText("--");
        suggestion5.setText("--");
        suggestion6.setText("--");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        ttsManager.initQueue(escolha);
        Log.e("Enviado para Falar", escolha);
        roletaDaEscolha.cancel(true);
        return false;
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

    // Faz a rodizio para escolha das sugestões
    class RoletaDaEscolha extends AsyncTask< Integer, Integer, String > {

        @Override
        protected String doInBackground(Integer... integers) {

            int numero = integers[0];
            for(int i=0; i<numero; i++) {

                publishProgress(i);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            return "Finalizado";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            switch (values[0]) {
                case 0:
                    setEscolha(suggestion1.getText().toString());
                    destaca(suggestion1);
                    break;
                case 1:
                    setEscolha(suggestion2.getText().toString());
                    destaca(suggestion2);
                    restaura(suggestion1);
                    break;
                case 2:
                    setEscolha(suggestion3.getText().toString());
                    destaca(suggestion3);
                    restaura(suggestion2);
                    break;
                case 3:
                    setEscolha(suggestion4.getText().toString());
                    destaca(suggestion4);
                    restaura(suggestion3);
                    break;
                case 4:
                    setEscolha(suggestion5.getText().toString());
                    destaca(suggestion5);
                    restaura(suggestion4);
                    break;
                case 5:
                    setEscolha(suggestion6.getText().toString());
                    destaca(suggestion6);
                    restaura(suggestion5);
                    break;
                case 6:
                    restaura(suggestion6);
                    break;
            }
            //progressBar.setProgress( values[0] );
            //textView.setText(String.valueOf( values[0] ));

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //progressBar.setProgress(0);
            //progressBar.setVisibility(View.INVISIBLE);

        }

        public TextView restaura(TextView textView) {
            textView.setTextColor(getResources().getColor(R.color.normalTextColor));
            textView.setBackgroundResource(R.color.normal);
            textView.setTypeface(null, Typeface.NORMAL);
            return textView;
        }

        public TextView destaca(TextView textView) {
            textView.setTextColor(getResources().getColor(R.color.destaqueTextColor));
            textView.setBackgroundResource(R.color.destaque);
            textView.setTypeface(null, Typeface.BOLD);
            return textView;
        }

        public void setEscolha(String string) {
            escolha = string;
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onStart() {
        super.onStart();
        upKeyboard(meuAudioEmTexto);
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

    //Desaloca os recursos usados pelo TextToSpeech
    @Override
    public void onDestroy() {
        super.onDestroy();
        ttsManager.shutDown();
    }
}