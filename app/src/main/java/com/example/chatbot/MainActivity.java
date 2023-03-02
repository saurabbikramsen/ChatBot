package com.example.chatbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private RecyclerView chatsRV;
    private EditText userMsgEdit;
    private FloatingActionButton sendMsgFAB;
    private ChatRVAdapter chatRVAdapter;
    private String secret_key = "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";
    OkHttpClient client = new OkHttpClient();


    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    ArrayList<ChatModel> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatsRV = findViewById(R.id.idRVChats);
        userMsgEdit = findViewById(R.id.idEdtMessage);
        sendMsgFAB = findViewById(R.id.idFABSend);
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

    void networkCall(String user_message) {
        chatList.add(new ChatModel(user_message, ChatModel.user_key));
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
//                msg_rv.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

}
