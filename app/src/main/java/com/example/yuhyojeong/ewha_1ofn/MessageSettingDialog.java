package com.example.yuhyojeong.ewha_1ofn;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

public class MessageSettingDialog extends Dialog implements View.OnClickListener {
    private static final String TAG = "CreateRoom 다이얼로그";

    private static final int LAYOUT = R.layout.dialog_setting_messageinfo;

    private Context context;


    private TextView okTv;
    private TextView cancelTv;
    private TextInputEditText menu, price, place;
    private TimePicker timePicker;

    private DialogListener dialogListener;

    int hour, minute;

    public MessageSettingDialog(@NonNull Context context) {
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
        cancelTv = findViewById(R.id.dialog_setting_cancel);
        okTv = findViewById(R.id.dialog_setting_ok);

        menu = findViewById(R.id.dialog_setting_menu);
        price = findViewById(R.id.dialog_setting_price);
        place = findViewById(R.id.dialog_setting_place);
        timePicker = findViewById(R.id.dialog_setting_timepicker);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }

        cancelTv.setOnClickListener(this);
        okTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dialog_setting_cancel:
                dialogListener.onNegativeClicked();
                break;

            case R.id.dialog_setting_ok:
                SettingModel setting = new SettingModel(
                    menu.getText().toString().trim(),
                    Integer.parseInt(price.getText().toString().trim()),
                    place.getText().toString().trim(),
                    hour+"시 "+minute+"분"
                );
                dialogListener.onPositiveClicked(setting);
                break;
        }
    }

//    class Setting{
//        String menu, place, time;
//        int price;
//
//        public Setting(String menu, int price, String place, String time) {
//            this.menu = menu;
//            this.price = price;
//            this.place = place;
//            this.time = time;
//        }
//
//        public Map<String,Object> toMap() {
//            HashMap<String, Object> result = new HashMap<>();
//            result.put("menu", menu);
//            result.put("price", price);
//            result.put("place", place);
//            result.put("time", time);
//
//            return result;
//        }
    }

