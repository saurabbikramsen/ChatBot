package com.example.chatbot;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



public class MainActivity extends AppCompatActivity implements TextToSpeech.OnUtteranceCompletedListener {

    private RecyclerView chatsRV;
    private EditText userMsgEdit;
    private FloatingActionButton sendMsgFAB;
    private ChatRVAdapter chatRVAdapter;
    private String secret_key = "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";
    OkHttpClient client = new OkHttpClient();
    int REQUEST_CODE_SPEECH_INPUT = 22222;
    TextToSpeech t1;

    boolean autoSpeak = true;
    MediaPlayer mp;



    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    ArrayList<ChatModel> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
       actionBar.setHomeAsUpIndicator(R.drawable.bot45real);// set drawable icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        actionBar.setIcon(R.drawable.bot);
        actionBar.setTitle("VoiceMate");


        mp = MediaPlayer.create(this, R.raw.google);

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);

                }
                if(status == TextToSpeech.SUCCESS) {
                    t1.setOnUtteranceCompletedListener(MainActivity.this);
                }
            }


        });



        chatsRV = findViewById(R.id.idRVChats);
        userMsgEdit = findViewById(R.id.idEdtMessage);
        sendMsgFAB = findViewById(R.id.idFABSend);
        findViewById(R.id.txt_mic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           openGoogleAssist();

            }

        });
        chatList = new ArrayList<>();
        chatRVAdapter = new ChatRVAdapter(chatList, this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        chatsRV.setLayoutManager(manager);
        chatsRV.setAdapter(chatRVAdapter);

        sendMsgFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userMsgEdit.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter your message", Toast.LENGTH_SHORT).show();
                    return;
                }
//                getResponse(userMsgEdit.getText().toString());
                networkCall(userMsgEdit.getText().toString());
                userMsgEdit.setText("");

            }
        });

    }
    private void  openGoogleAssist(){
        Intent intent
                = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");
        mp.start();

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast
                    .makeText(MainActivity.this, " " + e.getMessage(),
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuitems, menu);
        MenuItem item = (MenuItem) menu.findItem(R.id.show_secure);
        item.setActionView(R.layout.menu_entity);

        Switch switchAB = item
                .getActionView().findViewById(R.id.switchAB);
        ImageView speak = item
                .getActionView().findViewById(R.id.speaker);
        switchAB.setChecked(true);


        switchAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    switchAB.setChecked(true);
                    autoSpeak = true;
                    Toast.makeText(getApplicationContext(),"Automatic Mode",Toast.LENGTH_SHORT).show();

                } else {
                    switchAB.setChecked(false);
                    if(t1.isSpeaking()){
                        t1.stop();
                    }
                    autoSpeak = false;
                    Toast.makeText(getApplicationContext(),"Manual Mode",Toast.LENGTH_SHORT).show();

                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                if (autoSpeak) {

//                    chatList.add(new ChatModel(Objects.requireNonNull(result).get(0), ChatModel.user_key));
//                    chatRVAdapter.notifyDataSetChanged();
                    networkCall(Objects.requireNonNull(result).get(0));

                } else {
                    userMsgEdit.setText(
                            Objects.requireNonNull(result).get(0));
                }
            }
        }
    }

    void networkCall(String user_message) {
        try {

            chatList.add(new ChatModel(user_message, ChatModel.user_key));
            chatsRV.scrollToPosition(chatList.size());

            chatRVAdapter.notifyDataSetChanged();

            //okhttp
            chatList.add(new ChatModel("Typing... ", ChatModel.bot_key));

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("model", "text-davinci-003");
                jsonBody.put("prompt", user_message);
                jsonBody.put("max_tokens", 4000);
                jsonBody.put("temperature", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody body = RequestBody.create(JSON, jsonBody.toString());
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/completions")
                    .header("Authorization", "Bearer " + secret_key)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response.body().string());
                            JSONArray jsonArray = jsonObject.getJSONArray("choices");
                            String result = jsonArray.getJSONObject(0).getString("text");
                            if (autoSpeak) {
                                HashMap<String, String> myHashAlarm = new HashMap<String, String>();
//                                myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
                                myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");
                                t1.speak(result, TextToSpeech.QUEUE_FLUSH, myHashAlarm);

                            }
                            addResponse(result.trim());
                        } catch (JSONException e) {
                            Log.d("message", "onResponse: " + e.getMessage());
                            e.printStackTrace();
                        }


                    } else {
                        addResponse("Failed to load response due to " + response.body().toString());
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    addResponse("Failed to load response due to " + e.getMessage());

                }

            });
        } catch (Exception ignored) {

        }
    }

    void addResponse(String response) {
        chatList.remove(chatList.size() - 1);
        message_add(response, ChatModel.bot_key);
    }

    void message_add(String message, String source) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatList.add(new ChatModel(message, source));
                chatRVAdapter.notifyDataSetChanged();
                chatsRV.scrollToPosition(chatList.size());

//                msg_rv.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    @Override
    public void onUtteranceCompleted(String utteranceId) {
        Log.d("completed", "onUtteranceCompleted: ");
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                if(!t1.isSpeaking() && autoSpeak){

                    openGoogleAssist();

                }
            }
        }, 500);


    }

}
