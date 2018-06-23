package com.example.jddata.Entity;

public class SearchRecommend {
    public String title;
    public String price;
    public String comment;
    public String likePercent;

    public SearchRecommend(String title, String price, String comment, String likePercent) {
        this.title = title;
        this.price = price;
        this.comment = comment;
        this.likePercent = likePercent;
    }
}
