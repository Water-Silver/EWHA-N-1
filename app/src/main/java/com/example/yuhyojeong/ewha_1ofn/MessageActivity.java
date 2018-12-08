package com.example.yuhyojeong.ewha_1ofn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import net.daum.android.map.MapActivity;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.sql.Ref;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MessageActivity extends AppCompatActivity {


    private static final String TAG = "메세지 액티비티";

    // firebase
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
    DatabaseReference recruitRef = FirebaseDatabase.getInstance().getReference().child("Recruiting");

    // var
    public String uid, title;
    ChatRoom current_chatroom; // 현재 채팅방

    // view
//    MapView mapView;
    private Button button_send;
    private ImageButton button_menu, button_exit;
    private EditText editText;
    private TextView text_title;
    private LinearLayout chatbox;

    // for animation
    Animation translateLeftAnim;
    Animation translateRightAnim;
    LinearLayout slidingPanel;

    boolean isPageOpen = false;

    // recyclerview
    private RecyclerView recyclerView_message;
    private RecyclerView recyclerView_user;
    MessageListAdapter messageAdapter;
    UserListAdapter userAdapter;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
    public HashMap<String, ChatRoom> string_user = new HashMap<>();

    @Override
    public void onBackPressed() {
        if (isPageOpen) {
            slidingPanel.startAnimation(translateRightAnim);
            slidingPanel.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        uid = mAuth.getCurrentUser().getUid(); // 현재 로그인한 아이디 (채팅방을 만드는 아이디)
        Intent intent = getIntent();
        title = intent.getStringExtra("title");


        // mapView 띄우기
//        mapView = new MapView(this);
//        mapView.setDaumMapApiKey("7f79f8a60fed7aca35c2e2638c658363");
//        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.messageActivity_map);
//        mapViewContainer.addView(mapView);

//        recruitRef.orderByKey().equalTo(title).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot item : dataSnapshot.getChildren()) {
//                    currentRecruit = item.getValue(RecruitModel.class);
//                    Log.d(TAG, "orderByKey().equalTo(title)");
//
//                    addMarker(currentRecruit);
//                    LatLng sydney = new LatLng(currentRecruit.latitude, currentRecruit.longitude);
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f));
//
//
//                    recyclerView_user = findViewById(R.id.messageActivity_recyclerview_user);
//                    recyclerView_user.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
//                    userAdapter = new UserListAdapter(MessageActivity.this);
//                    recyclerView_user.setAdapter(userAdapter);
//                }
//            } // datachanged 끝
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        button_send = findViewById(R.id.messageActivity_button);
        button_menu = findViewById(R.id.messageActivity_button_menu);
        button_exit = findViewById(R.id.messageActivity_button_exit);
        editText = findViewById(R.id.messageActivity_edit_message);
        text_title = findViewById(R.id.messageActivity_text_title);
        text_title.setText(title);
        chatbox = findViewById(R.id.layout_chatbox);

        // for animation
        translateLeftAnim = AnimationUtils.loadAnimation(this, R.anim.translate_left);
        translateRightAnim = AnimationUtils.loadAnimation(this, R.anim.translate_right);

        translateLeftAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isPageOpen) {
                    slidingPanel.setVisibility(View.INVISIBLE);
                    isPageOpen = false;
                    chatbox.setVisibility(View.VISIBLE);
                } else {
                    isPageOpen = true;
                    chatbox.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        translateRightAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isPageOpen) {
                    slidingPanel.setVisibility(View.INVISIBLE);
                    isPageOpen = false;
                    chatbox.setVisibility(View.VISIBLE);
                } else {
                    isPageOpen = true;
                    chatbox.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        slidingPanel = (LinearLayout) findViewById(R.id.slidingPanel);

        button_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPageOpen) {
                    slidingPanel.startAnimation(translateRightAnim);
                    slidingPanel.setVisibility(View.GONE);
                } else {
                    slidingPanel.startAnimation(translateLeftAnim);
                    slidingPanel.setVisibility(View.VISIBLE);
                }
            }
        });

        ImageButton button_x = findViewById(R.id.button_x);
        button_x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPageOpen) {
                    slidingPanel.startAnimation(translateRightAnim);
                    slidingPanel.setVisibility(View.GONE);
                } else {
                    slidingPanel.startAnimation(translateLeftAnim);
                    slidingPanel.setVisibility(View.VISIBLE);
                }
            }
        });

        //


//        button_exit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                recruitRef.child(currentRecruit.title).child("users").child(mAuth.getCurrentUser().getUid()).removeValue();
//                recruitRef.child(currentRecruit.title).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (!dataSnapshot.exists()) {
//                             유저가 한명도 없으면
//                            recruitRef.child(title).removeValue();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//                startActivity(new Intent(MessageActivity.this, MapActivity.class));
//                finish();
//            }
//        });

        button_send.setVisibility(View.INVISIBLE);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!editText.getText().toString().equals("")) {
                    button_send.setVisibility(View.VISIBLE);
                } else
                    button_send.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatRoom.Comment comment = new ChatRoom.Comment();
                comment.uid = uid;
                comment.message = editText.getText().toString();
                comment.timestamp = ServerValue.TIMESTAMP;

                recruitRef.child(title).child("comments").push().setValue(comment);
                editText.setText("");
            }
        });

        recyclerView_message = findViewById(R.id.messageActivity_recyclerview_message);
        recyclerView_message.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageListAdapter(MessageActivity.this);
        recyclerView_message.setAdapter(messageAdapter);


    }

    // 지도가 뜨면 마커를 표시해야한다
//    @Override
//    public void onMapReady(final GoogleMap googleMap) {
//        mMap = googleMap;
//        marker_root_view = LayoutInflater.from(this).inflate(R.layout.marker_layout, null);
//        marker_tv = (TextView) marker_root_view.findViewById(R.id.marker_tv);
//        marker_img = marker_root_view.findViewById(R.id.marker_img);
//    }

//
//    private Marker addMarker(RecruitModel recruit) {
//        MarkerOptions marker = new MarkerOptions()
//                .position(new LatLng(recruit.latitude, recruit.longitude));
//        marker_tv.setText(recruit.title);
//        marker_img.setImageResource(R.drawable.marker_blue);
//        marker.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(MessageActivity.this, marker_root_view)));
//
//        return mMap.addMarker(marker);
//    }
//

    ///////////////////////////////////////////////////////////////////////////

    class MessageListAdapter extends RecyclerView.Adapter {
        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

        private Context mContext;
        private ArrayList<ChatRoom.Comment> comments = new ArrayList<>();

        public MessageListAdapter(Context context) {
            mContext = context;

            recruitRef.child(title).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear();
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        ChatRoom.Comment comment = item.getValue(ChatRoom.Comment.class);
                        comments.add(comment);
                    }
                    notifyDataSetChanged();
                    recyclerView_message.scrollToPosition(comments.size() - 1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        @Override
        public int getItemViewType(int position) {
            ChatRoom.Comment comment = comments.get(position);

            if (comment.uid.equals(uid)) {
                return VIEW_TYPE_MESSAGE_SENT;
            } else {
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view;
            if (i == VIEW_TYPE_MESSAGE_SENT) {
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_sent, viewGroup, false);
                return new SentMessageHolder(view);

            } else if (i == VIEW_TYPE_MESSAGE_RECEIVED) {
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message_received, viewGroup, false);
                return new ReceivedMessageHolder(view);
            }

            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ChatRoom.Comment comment = comments.get(i);

            long unixTime = (long) comments.get(i).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);

            switch (viewHolder.getItemViewType()) {
                case VIEW_TYPE_MESSAGE_SENT:
                    ((SentMessageHolder) viewHolder).bind(comment);
                    ((SentMessageHolder) viewHolder).timeText.setText(time);
                    break;
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    ((ReceivedMessageHolder) viewHolder).bind(comment);
                    ((ReceivedMessageHolder) viewHolder).timeText.setText(time);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

    }

    public class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        ImageView profileImage;

        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text_body);
            timeText = itemView.findViewById(R.id.message_text_time);
            nameText = itemView.findViewById(R.id.message_text_name);
            profileImage = itemView.findViewById(R.id.message_image_profile);
        }

        void bind(final ChatRoom.Comment comment) {
            messageText.setText(comment.message);

            if (string_user.get(comment.uid) == null || string_user.get(comment.uid).equals("")) {
                userRef.child(comment.uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserModel user = dataSnapshot.getValue(UserModel.class);
                        Log.d(TAG, "user name: " + user.userName);
//                        string_user.put(comment.uid, user);
                        nameText.setText(user.userName);
//                        Glide.with(messageAdapter.mContext)
//                                .load(user.profileImageUrl)
//                                .apply(new RequestOptions().circleCrop().override(50, 50))
//                                .into(profileImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else {
//                nameText.setText(string_user.get(comment.uid).userName);
//                Glide.with(messageAdapter.mContext)
//                        .load(string_user.get(comment.uid).profileImageUrl)
//                        .apply(new RequestOptions().circleCrop().override(50, 50))
//                        .into(profileImage);
            }
        }
    }

    public class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text_body);
            timeText = itemView.findViewById(R.id.message_text_time);
        }

        void bind(ChatRoom.Comment comment) {
            messageText.setText(comment.message);
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    class UserListAdapter extends RecyclerView.Adapter<UserHolder> {
        private Context mContext;
        public ArrayList<UserModel> users = new ArrayList<>();


        public UserListAdapter(@NonNull Context context) {
            mContext = context;

            recruitRef.child(current_chatroom.title).child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        Log.d(TAG, "recruitRef.child(..title).order...: " + item.getKey());
                        String uid = item.getKey();
                        userRef.child(uid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                UserModel user = dataSnapshot.getValue(UserModel.class);
                                Log.d(TAG, "user name: " + user.userName);
                                users.add(user);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public UserHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_friend, viewGroup, false);
            return new UserHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserHolder userHolder, int i) {
//            Glide.with(userAdapter.mContext)
//                    .load(users.get(i).profileImageUrl)
//                    .apply(new RequestOptions().circleCrop().override(50, 50))
//                    .into(userHolder.profile);
            userHolder.name.setText(users.get(i).userName);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }
    }

    private class UserHolder extends RecyclerView.ViewHolder {
        ImageView profile;
        TextView name;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.frienditem_imageview);
            name = itemView.findViewById(R.id.frienditem_textview);
        }
    }
}
