package com.example.myweixin;

public class Msg {
     public static final int TYPE_RECEIVED = 0;
     public static final int TYPE_SENT = 1;
     private String content;
     private int type;
     public Msg(String content, int type){
         this.type = type;
         this.content = content;
     }

     public String getContent(){
         return content;
     }

     public int getType(){
         return type;
     }

}
