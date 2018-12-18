package com.example.yuhyojeong.ewha_1ofn;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StoreLocation extends AppCompatActivity {

    private FirebaseDatabase database;
    private EditText latitude;
    private EditText longitude;
    private EditText pair; // list of (lat, long) present in database
    private Button save;
    private Button getData;

    private List<ChatRoom> chat = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_location);

        database = FirebaseDatabase.getInstance();

        latitude = (EditText) findViewById(R.id.textLat);
        longitude = (EditText) findViewById(R.id.textLong);
        pair = (EditText) findViewById(R.id.write);
        Button save = (Button) findViewById(R.id.save);



        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lat = latitude.getText().toString();
                String lot = longitude.getText().toString();
                upload(lat, lot);
            }
        });
/////////////////////////////////////////////////////////////////////////////// 데이터베이스 읽기
        Button getData = (Button) findViewById(R.id.getData);
        getData.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                database.getReference().child("chatroom").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        chat.clear();
                        String returnV = "";
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            ChatRoom CH = snapshot.getValue(ChatRoom.class);
                            returnV = returnV +"("+CH.latitude+", "+CH.longitude+") ";
                        }
                        pair.setText(returnV);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }
        });
    }
///////////////////////////////////////////////////////////////////////////// 데이터베이스 쓰기
    private void upload(String lat, String longitude){

        ChatRoom chatroom = new ChatRoom();
        chatroom.latitude = Double.parseDouble(lat);
        chatroom.longitude = Double.parseDouble(longitude);
        chatroom.title = "네네치킨 드실 분!";
        chatroom.numOfPeople = 4;

        database.getReference().child("chatroom").push().setValue(chatroom);




    }
}
