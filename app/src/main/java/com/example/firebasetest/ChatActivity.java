package com.example.firebasetest;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    EditText messageArea;
    ImageView sendButton;
    LinearLayout layout;
    RelativeLayout layout_2;
    ScrollView scrollView;

    String username, chatWith;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference reference1, reference2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.layout1);
        layout_2 = findViewById(R.id.layout2);
        scrollView = findViewById(R.id.scrollView);
        messageArea = findViewById(R.id.messageArea);
        sendButton = findViewById(R.id.sendButton);

        auth =  FirebaseAuth.getInstance();

        username = auth.getCurrentUser().getEmail();
        chatWith = getIntent().getStringExtra("chatWith");

        username  = username.replace(".", "_");
        chatWith  = chatWith.replace(".", "_");


        database = FirebaseDatabase.getInstance();

        reference1 = database.getReference("messages/" + username + "_ " + chatWith);
        reference2 = database.getReference("messages/" + chatWith + "_ " + username);

        sendButton.setOnClickListener(v-> {

             String messageText =  messageArea.getText().toString();

            if(!TextUtils.isEmpty(messageText)){
                Map<String, String>map = new HashMap<>();
                map.put("message", messageText);
                map.put("user", username);
                reference1.push().setValue(map);
                reference2.push().setValue(map);
                messageArea.setText("");
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                GenericTypeIndicator<Map<String, String>> typeIndicator = new GenericTypeIndicator<Map<String, String>>() {};
                Map<String, String> map = dataSnapshot.getValue(typeIndicator);
                String message = map.get("message").toString();
                String username = map.get("user").toString();


                if(username.equals(ChatActivity.this.username)){

                    addMessageBow("You:-\n", message, 1);

                }

                else{
                    addMessageBow(ChatActivity.this.chatWith + ":-\n", message, 2);


                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addMessageBow(String user, String message, int type) {
        //  view bauen .. . :-)
        TextView textView = new TextView(this);
        textView.setText(user + message);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        lp.weight = 1.0F;
        if(type ==1){
            lp.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_in);

        }else {
            lp.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_out);
        }

        textView.setLayoutParams(lp);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                auth.signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
