package com.example.yuhyojeong.ewha_1ofn;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class CreateRoomDialog extends Dialog implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "CreateRoom 다이얼로그";

    private static final int LAYOUT = R.layout.dialog_create_room;

    private Context context;

    private TextView createTv;
    private TextView cancelTv;

    private Spinner numSpiner;
    private Spinner menuSpiner;
    private Integer[] numOfPeople;
    private String[] menu;

    private TextInputEditText titleEt;

    int result_num;
    String result_menu;

    private DialogListener dialogListener;

    public CreateRoomDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public void setDialogListener(DialogListener dialogListener){
        this.dialogListener = dialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        // find View By Id
        cancelTv = findViewById(R.id.dialog_create_cancel);
        createTv = findViewById(R.id.dialog_create_create);
        numSpiner = findViewById(R.id.dialog_create_numSpiner);
        menuSpiner = findViewById(R.id.dialog_create_menuSpiner);
        titleEt = findViewById(R.id.dialog_create_title);

        cancelTv.setOnClickListener(this);
        createTv.setOnClickListener(this);

        numOfPeople = new Integer[]{2,3,4,5};
        menu = new String[]{"치킨","피자","중국집","도시락","패스트푸드","기타"};

        ArrayAdapter<Integer> numAdapter = new ArrayAdapter<Integer>(context, android.R.layout.simple_spinner_item, numOfPeople);
        numAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numSpiner.setAdapter(numAdapter);
        numSpiner.setOnItemSelectedListener(this);

        ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, menu);
        menuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        menuSpiner.setAdapter(menuAdapter);
        menuSpiner.setOnItemSelectedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dialog_create_cancel:
                dialogListener.onNegativeClicked();
                break;

            case R.id.dialog_create_create:
                ChatRoom chatroom = new ChatRoom();
                chatroom.title = titleEt.getText().toString();
                Log.d(TAG, "onClick: title"+chatroom.title);
                chatroom.targetPop= result_num;
                chatroom.menu = result_menu;
                dialogListener.onPositiveClicked(chatroom);
                dismiss();
                break;
        }
    }

    // AdapterView.OnItemSelectedListener 인터페이스 구현
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch(adapterView.getId()){
            case R.id.dialog_create_numSpiner:
                result_num = (int) adapterView.getItemAtPosition(i);
                Log.d(TAG, "onItemSelected: result_num 변경됨: "+result_num);
                break;
            case R.id.dialog_create_menuSpiner:
                result_menu = (String) adapterView.getItemAtPosition(i);
                Log.d(TAG, "onItemSelected: result_menu 변경됨: "+result_menu);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}

