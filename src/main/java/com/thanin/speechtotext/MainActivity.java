package com.thanin.speechtotext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int RECORD_AUDIO_RC = 1;
    private SpeechRecognizer speechRecognizer;
    private TextView editText;
    private FloatingActionButton micButton;
    private Spinner languageSpinner;
    private Intent speechRecognizerIntent;

    private static final String[][] languages = {
            {"English", "en-US"}, {"French", "fr-FR"}, {"Thai", "th-TH"},
            {"Khmer", "km-KH"}, {"Vietnamese", "vi-VN"}, {"Japanese", "ja-JP"},
            {"Korean", "ko-KR"}
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check and request microphone permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestMicrophonePermission();
        }

        // Initialize UI components
        editText = findViewById(R.id.text);
        micButton = findViewById(R.id.button);
        languageSpinner = findViewById(R.id.language_spinner);

        // Set up language spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getLanguageNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);
        languageSpinner.setSelection(0); // Default to first language

        setupLanguageSpinner();
        setupMicButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Initialize SpeechRecognizer when the activity is visible
        if (speechRecognizer == null) {
            initializeSpeechRecognizer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure that SpeechRecognizer is properly initialized when the activity is active
        if (speechRecognizer == null) {
            initializeSpeechRecognizer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop listening to release resources when activity is partially hidden
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Release SpeechRecognizer resources when activity is no longer visible
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources when the activity is destroyed
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_RC);
        }
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {
                editText.setText("");
                editText.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                micButton.setImageResource(R.drawable.mic);
                Toast.makeText(MainActivity.this, "Speech recognition error. Please try again.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                micButton.setImageResource(R.drawable.mic);
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null && !data.isEmpty()) {
                    editText.setText(data.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void setupMicButton() {
        micButton.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                micButton.setImageResource(R.drawable.mic);
                if (speechRecognizer != null) {
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (speechRecognizer != null) {
                    speechRecognizer.stopListening();
                }
            }
            return false;
        });
    }

    private void setupLanguageSpinner() {
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languages[position][1]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            }
        });
    }

    private String[] getLanguageNames() {
        String[] languageNames = new String[languages.length];
        for (int i = 0; i < languages.length; i++) {
            languageNames[i] = languages[i][0];
        }
        return languageNames;
    }
}
