package com.example.android.smartreply;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.UUID;

public class Translate {
    private static Translate instance = null;
    private static FirebaseTranslatorOptions optionsPT_EN;
    private static FirebaseTranslatorOptions optionsEN_PT;
    private static FirebaseModelDownloadConditions conditions;
    private static FirebaseTranslator tradutorPT_EN;
    private static FirebaseTranslator tradutorEN_PT;
    private final String REMOTE_USER_ID = UUID.randomUUID().toString();
    private static int index = 0;

    public Translate(Context app){
        FirebaseApp fapp = FirebaseApp.initializeApp(app);
        optionsPT_EN =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.PT)
                        .setTargetLanguage(FirebaseTranslateLanguage.EN)
                        .build();
        optionsEN_PT =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.EN)
                        .setTargetLanguage(FirebaseTranslateLanguage.PT)
                        .build();
        tradutorEN_PT =
                FirebaseNaturalLanguage.getInstance(fapp).getTranslator(optionsEN_PT);
        tradutorPT_EN =
                FirebaseNaturalLanguage.getInstance(fapp).getTranslator(optionsPT_EN);
        conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        tradutorEN_PT.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                // Model downloaded successfully. Okay to start translating.
                                // (Set a flag, unhide the translation UI, etc.)
                                System.err.println("baixou EN_PT");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // ...
                            }
                        });
    }

    public static Translate getInstance(Context c) {
        if(instance==null){
            instance = new Translate(c);
        }
        return instance;
    }

    public void traduzirPT_EN(String[] messages, SmartReplyClient client, ArrayList<String> s_words, MainActivity mainActivity){
//        para cada menssagem traduzir
        for (final String message : messages) {
            tradutorPT_EN.translate(message)
                    .addOnSuccessListener(
                            new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String translatedText) {
                                    // Translation successful.
                                    System.err.println("TranslatedText: " + translatedText);

                                    SmartReply[] ans = new SmartReply[0];
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                        ans = client.predict(new String[] {translatedText});
                                    }
                                    for (SmartReply reply : ans) {
                                        String s_word = reply.getText();
                                        System.err.println("s_word: " + s_word);
                                        tradutorEN_PT.translate(s_word)
                                                .addOnSuccessListener(
                                                        new OnSuccessListener<String>() {
                                                            @Override
                                                            public void onSuccess(String translatedText2) {
                                                                System.err.println("translatedText2: "+translatedText2);
                                                                mainActivity.appendMessage( translatedText2, index );
                                                                    index += 1;
                                                                s_words.add(translatedText2);
                                                            }
                                                        }).addOnFailureListener(
                                                new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        System.err.println(e.toString());
                                                        // Error.
                                                        // ...
                                                    }
                                                });

                                    }
                                    index = 0;
                                    int i = 0;
                                    /*
                                    for (String sw : s_words) {
                                        Button myButton = new Button(mainActivity);
                                        myButton.setText(sw);
                                        myButton.setId(i);
                                        i++;
                                        final int id_ = myButton.getId();

                                        LinearLayout layout = (LinearLayout) mainActivity.findViewById(R.id.linear_layout1);
                                        layout.addView(myButton);

                                        myButton.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View view) {
                                                System.err.println("CLICLED!");
                                            }
                                        });
                                    }

                                     */

                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    System.err.println(e.toString());
                                    // Error.
                                    // ...
                                }
                            });
        }
//        return chatHistory;
    }

//    public Message traduzirPT_EN(final Message message) {
//        tradutor.translate(message.text)
//                .addOnSuccessListener(
//                        new OnSuccessListener<String>() {
//                            @Override
//                            public void onSuccess(@NonNull String translatedText) {
//                                // Translation successful.
//                                System.err.println("TranslatedText: " + translatedText);
//                                message.text = translatedText;
////                                message.setMessageInEnglish(message, translatedText);
//                            }
//                        })
//                .addOnFailureListener(
//                        new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                System.err.println(e.toString());
//                                // Error.
//                                // ...
//                            }
//                        });
//        return null;
//    }
}
