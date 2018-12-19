package com.example.yuhyojeong.ewha_1ofn;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ChatRoom implements Serializable{
    String key; // 데이터베이스에 챗룸을 저장하는 키
    String title;
    double latitude;
    double longitude;
    int targetPop;
    String menu;
    String status; // yet, full

    public Map<String, Boolean> users = new HashMap<>();
    public Map<String, Comment> comments = new HashMap<>();

    public Map<String,Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("title", title);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("targetPop", targetPop);
        result.put("menu", menu);
        result.put("status", status);

        return result;
    }

    public static class Comment {
        public String uid;
        public String message;
        public Object timestamp;
    }

}