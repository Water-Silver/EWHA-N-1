package com.example.yuhyojeong.ewha_1ofn;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "메세지 액티비티";

    // firebase
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference chatroomRef = FirebaseDatabase.getInstance().getReference().child("chatroom");
    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
    DatabaseReference settingRef = FirebaseDatabase.getInstance().getReference().child("Setting");
    ValueEventListener settingListener;

    // var
    public String uid, title, key;
    ChatRoom current_chatroom; // 현재 채팅방

    // view
    private Button button_send;
    private ImageButton button_menu, button_back, button_exit;
    private EditText editText;
    private TextView text_title;
    private LinearLayout chatbox;

    // view in sliding panel
    private TextView info_pop, info_menu, info_place, info_time, info_price;
    private Button info_setting;

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
    public HashMap<String, UserModel> string_user = new HashMap<>();

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
        key = intent.getStringExtra("key");

        // key 값으로 데이터베이스에서 데이터 조회하기
        // 받은 데이터를 current_chatroom에 저장한다.
        chatroomRef.orderByKey().equalTo(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom= item.getValue(ChatRoom.class);
                    current_chatroom = chatRoom;
                    Log.d(TAG, "onDataChange: "+chatRoom.users.size() +"명이 있다");

                    // 인원수 설정
                    info_pop.setText(current_chatroom.users.size() + "/" + current_chatroom.targetPop);

                    if(recyclerView_user == null) {
                        Log.d(TAG, "onDataChange: recyclerView_user가 null");
                        recyclerView_user = findViewById(R.id.messageActivity_recyclerview_user);
                        recyclerView_user.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                        userAdapter = new UserListAdapter(MessageActivity.this);
                        recyclerView_user.setAdapter(userAdapter);
                    } else {
                        Log.d(TAG, "onDataChange: recyclerView_user가 null이 아니다");
                    }
                }
            } // datachanged 끝

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        settingRef.orderByKey().equalTo(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    SettingModel setting = item.getValue(SettingModel.class);
                    info_menu.setText(setting.menu);
                    info_place.setText(setting.place);
                    setting.price = setting.price/current_chatroom.users.size();
                    info_price.setText(setting.price+"");
                    info_time.setText(setting.time);
                    Toast.makeText(MessageActivity.this, "메뉴가 설정되었습니다. 채팅방 정보를 확인하세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // findViewById
        button_back = findViewById(R.id.messageActivity_button_back);
        button_send = findViewById(R.id.messageActivity_button);
        button_menu = findViewById(R.id.messageActivity_button_menu);
        button_exit = findViewById(R.id.messageActivity_button_exit);
        editText = findViewById(R.id.messageActivity_edit_message);
        text_title = findViewById(R.id.messageActivity_text_title);
        chatbox = findViewById(R.id.layout_chatbox);

        info_menu = findViewById(R.id.messageActivity_info_menu);
        info_price = findViewById(R.id.messageActivity_info_price);
        info_place = findViewById(R.id.messageActivity_info_place);
        info_pop = findViewById(R.id.messageActivity_info_pop);
        info_time = findViewById(R.id.messageActivity_info_time);
        info_setting = findViewById(R.id.messageActivity_info_settingBtn);

        // setText
        text_title.setText(title);


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

        // 채팅방 타이틀의 왼쪽 화살표 버튼을 클릭하면 메세지 액티비티를 지운다
        // -> 뒤에 있던 메인 액티비티가 보인다.
        // -> 메인 액티비티는 onResume() 를 실행한다.
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick:  뒤로가기 버튼을 클릭해서 finish!");
                finish();
            }
        });

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


        // 채팅방 메뉴에서 나가기 버튼을 클릭하는 경우
        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: 아이디: "+mAuth.getCurrentUser().getUid());
                chatroomRef.child(key).child("users").child(mAuth.getCurrentUser().getUid()).removeValue();
                chatroomRef.child(key).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                             //유저가 한명도 없으면 채팅방 데이터를 파이어베이스에서 삭제한다.
                            chatroomRef.child(key).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                // 채팅방을 나가고 메인 액티비티를 보여준다.
                // -> 메인 액티비티의 onResume()이 호출된다.
                Log.d(TAG, "onClick: 나가기 버튼을 클릭해서 finish!");

                Log.d(TAG, "onClick: 파이어베이스 채팅방 status를 yet으로 변경한다.");
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("status", "yet");
                chatroomRef.child(key).updateChildren(childUpdates);

                finish();
            }
        });


        // 보내기 버튼은 메세지를 입력했을 때만 보인다.
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

        // 메세지 전송 버튼을 클릭하는 경우
        // 파이어베이스의 데이터베이스에 메세지 내용을 입력하고
        // 메세지 editText는 빈칸으로 재설정한다.
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatRoom.Comment comment = new ChatRoom.Comment();
                comment.uid = uid;
                comment.message = editText.getText().toString();
                comment.timestamp = ServerValue.TIMESTAMP;

                chatroomRef.child(key).child("comments").push().setValue(comment);
                editText.setText("");
            }
        });

        info_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 방을 만드는 다이얼로그를 띄운다
                // 방 제목, 인원수, 메뉴명을 입력받고
                // 생성 버튼을 누른 경우라면
                // 그 값들과 mapPointGeo의 좌표를 나타내는
                // ChatRoom 객체를 생성한다.
                // 취소 버튼을 누르면 다이얼로그를 닫는다
                final MessageSettingDialog dialog = new MessageSettingDialog(MessageActivity.this);
                dialog.setDialogListener(new DialogListener() {
                    @Override
                    public void onPositiveClicked(Object object) {
                        SettingModel setting = (SettingModel) object;
                        Log.d(TAG, "onPositiveClicked: 실행");

                        writeSetting(setting);
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegativeClicked() {
                        Log.d(TAG, "onNegativeClicked: 실행");
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });



        recyclerView_message = findViewById(R.id.messageActivity_recyclerview_message);
        recyclerView_message.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageListAdapter(MessageActivity.this);
        recyclerView_message.setAdapter(messageAdapter);


    }

    private void writeSetting(SettingModel setting) {
        Map<String,Object> settingVal = setting.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, settingVal);
        settingRef.updateChildren(childUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: 셋팅 데이터 입력 완료");
                    }
                });
    }


    ///////////////////////////////////////////////////////////////////////////



    class MessageListAdapter extends RecyclerView.Adapter {
        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

        private Context mContext;
        private ArrayList<ChatRoom.Comment> comments = new ArrayList<>();

        public MessageListAdapter(Context context) {
            mContext = context;

            chatroomRef.child(key).child("comments").addValueEventListener(new ValueEventListener() {
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

            Log.d(TAG, "bind: " + comment.uid);
            if (string_user.get(comment.uid) == null || string_user.get(comment.uid).equals("")) {
                userRef.child(comment.uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserModel user = dataSnapshot.getValue(UserModel.class);
                        Log.d(TAG, "user name: " + user.name);
                        string_user.put(comment.uid, user);
                        nameText.setText(user.name);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else {
                nameText.setText(string_user.get(comment.uid).name);
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

            chatroomRef.child(key).child("users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        Log.d(TAG, "recruitRef.child(..title).order...: " + item.getKey());
                        String uid = item.getKey();
                        userRef.child(uid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                UserModel user = dataSnapshot.getValue(UserModel.class);
                                users.add(user);
                                Log.d(TAG, "onDataChange: "+user.toString());
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
            userHolder.name.setText(users.get(i).name);
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
