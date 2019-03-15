package com.example.firebasetest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SelectChatWithActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_chat);

        ListView chat_user = findViewById(R.id.lv_chat_user);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,

                getResources().getStringArray(R.array.chat_user) );


        chat_user.setAdapter(adapter);

        chat_user.setOnItemClickListener((adapterView, view, i, l ) -> {

            Log.d("chat: ", "huh doch");
            TextView textView = (TextView) view;
            String chatWith = textView.getText().toString();
            Intent intent = new Intent(SelectChatWithActivity.this, ChatActivity.class);
            intent.putExtra("chatWith", chatWith);
            startActivity(intent);
        });
    }
}
