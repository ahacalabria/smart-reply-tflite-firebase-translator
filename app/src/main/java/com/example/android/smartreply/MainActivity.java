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

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import android.Manifest;
import android.content.pm.PackageManager;
import android.speech.SpeechRecognizer;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity implements
        RecognitionListener {

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
    public TextView transcription;
    public TextView theSelectedTextToSpeech;
    private static final int NUBER_OF_SUGGESTIONS = 5;
    private EditText meuAudioEmTexto;
    private ServiceFalar ttsManager = null;
    RoletaDaEscolha roletaDaEscolha;
    public String escolha = "";
    public int loop = 1;
    private static final int REQUEST_RECORD_PERMISSION = 100;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "MainActivity";

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
        theSelectedTextToSpeech = findViewById(R.id.selectedTextToSpeech);
        transcription = findViewById(R.id.transcriptionid);
        progressBar = findViewById(R.id.progressBar1);

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

        // Negocios do Speech to text
        progressBar.setVisibility(View.INVISIBLE);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "pt-BR");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    // Metodos do SmartReply
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
                        if(loop == 1) { //esse teste impede que a roleta reinicie eternamente
                            roletaDaEscolha.execute(2 * NUBER_OF_SUGGESTIONS + 3);
                        }
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
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        // O teste inicial impede que o app quebre com o toque antes de utilizar
        if( event.getAction() == MotionEvent.ACTION_DOWN ) {
            if( !meuAudioEmTexto.getText().toString().equals("") ) {
                roletaDaEscolha = new RoletaDaEscolha();
                upKeyboard(meuAudioEmTexto);
                send(meuAudioEmTexto.getText().toString());
                limpar();
            } else {
                theSelectedTextToSpeech.append( escolha + " ");
            }
        }
        return true;
    }

    public void ouvir(View view) {
        roletaDaEscolha = new RoletaDaEscolha();
        upKeyboard(view);
        limpar();
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        ActivityCompat.requestPermissions
                (MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_PERMISSION);
    }

    public void limpar() {
        meuAudioEmTexto.setText("");
        suggestion1.setText("--");
        suggestion2.setText("--");
        suggestion3.setText("--");
        suggestion4.setText("--");
        suggestion5.setText("--");
        suggestion6.setText("--");
        theSelectedTextToSpeech.setText(" ");
        escolha = "";
        transcription.setText("");
    }

    // Faz a rodizio para escolha das sugestões
    class RoletaDaEscolha extends AsyncTask< Integer, Integer, String > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loop++;
        }

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
                    send(theSelectedTextToSpeech.getText().toString());//Realimenta o SmartReply ao fim do primeiro loop
                    setEscolha(suggestion1.getText().toString());
                    destaca(suggestion1);
                    restaura(suggestion6);
                    break;
                case 7:
                    setEscolha(suggestion2.getText().toString());
                    restaura(suggestion1);
                    destaca(suggestion2);
                    break;
                case 8:
                    setEscolha(suggestion3.getText().toString());
                    restaura(suggestion2);
                    destaca(suggestion3);
                    break;
                case 9:
                    setEscolha(suggestion4.getText().toString());
                    restaura(suggestion3);
                    destaca(suggestion4);
                    break;
                case 10:
                    setEscolha(suggestion5.getText().toString());
                    restaura(suggestion4);
                    destaca(suggestion5);
                    break;
                case 11:
                    setEscolha(suggestion6.getText().toString());
                    restaura(suggestion5);
                    destaca(suggestion6);
                    break;
                case 12:
                    restaura(suggestion6);
                    break;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loop = 1;
            theSelectedTextToSpeech.setTextColor(getResources().getColor(R.color.destaqueTextColor));
            theSelectedTextToSpeech.setTypeface(null, Typeface.BOLD);
            ttsManager.initQueue(theSelectedTextToSpeech.getText().toString());
            limpar();
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

    //Implementação da interface de AUDIÇÃO SpeechRecognizer
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    speech.startListening(recognizerIntent);
                } else {
                    Toast.makeText(this, "Permissão NEGADA!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.INVISIBLE);
        speech.stopListening();
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FALHOU " + errorMessage);
        Toast.makeText(this, "erro na Captação =(", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = matches.get(0);
        send(text);
        transcription.setText(text);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Erro no reconhecimento de audio";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Permissão insuficiente";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Erro de conexão com a internet";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Tempo de resposta da internet excedido";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "SpeechRecognizer ocupado";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Erro no servidor";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "Sem entrada do SpeechRecognizer";
                break;
            default:
                message = "Não entendi, por favor tente novamente";
                break;
        }
        return message;
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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

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
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ttsManager.shutDown();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
}