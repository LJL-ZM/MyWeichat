package com.example.recyclerviewtest;

public class MengFuFu {
    private String name;
    private int imageId;

    public MengFuFu(String name, int imageId){
        this.imageId = imageId;
        this.name = name;
     public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
