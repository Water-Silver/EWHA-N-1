package com.example.yuhyojeong.ewha_1ofn;

import java.util.HashMap;
import java.util.Map;

public class SettingModel {
    String menu, place, time;
    int price;

    public SettingModel() {
    }

    public SettingModel(String menu, int price, String place, String time) {
        this.menu = menu;
        this.price = price;
        this.place = place;
        this.time = time;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("menu", menu);
        result.put("price", price);
        result.put("place", place);
        result.put("time", time);

        return result;
    }
}
