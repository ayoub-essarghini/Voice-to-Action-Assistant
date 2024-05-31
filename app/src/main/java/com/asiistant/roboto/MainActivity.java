package com.asiistant.roboto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private ViewGroup customTTSLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textToSpeech = new TextToSpeech(this, this);

        // Inflate the custom TTS layout
        LayoutInflater inflater = getLayoutInflater();
        customTTSLayout = (ViewGroup) inflater.inflate(R.layout.custom_tts_layout, null);

        // Add the custom layout to your activity's content view
        ViewGroup rootView = findViewById(android.R.id.content);
        rootView.addView(customTTSLayout);

        // Hide the custom TTS layout initially
//        customTTSLayout.setVisibility(View.GONE);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        // Check and request permission for microphone
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_SPEECH_INPUT);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.US);

            // Start listening for voice input
            startVoiceInput();
        } else {
            Toast.makeText(this, "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...");

        startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String voiceInput = result.get(0);
                processVoiceCommand(voiceInput);
                Toast.makeText(this, "done", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processVoiceCommand(String command) {
        if (command.toLowerCase().contains("call")) {
            // Extract contact name or number
            String contact = command.substring(command.toLowerCase().indexOf("call") + 5).trim();


                // Make a call
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + ""));
                startActivity(callIntent);


        } else if (command.toLowerCase().contains("message")) {
            // Extract contact name or number and message content
            String[] parts = command.split("message");
            String contact = parts[0].toLowerCase().replace("send", "").trim();
            String message = parts[1].trim();

            // Send a text message
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + contact));
            smsIntent.putExtra("sms_body", message);
            startActivity(smsIntent);
        } else {
            speak("Sorry, I didn't understand that command.");
        }
    }

    private void speak(String text) {
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {}

            @Override
            public void onDone(String utteranceId) {
                // Start listening for voice input again after speaking
                startVoiceInput();
            }

            @Override
            public void onError(String utteranceId) {}
        });

        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "assistantUtterance");
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}
