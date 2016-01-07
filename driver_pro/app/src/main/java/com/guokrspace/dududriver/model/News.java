package com.guokrspace.dududriver.model;

/**
 * Created by daddyfang on 16/1/4.
 */
public class News extends BaseModel {

    private Integer id;

    private String title;

    private String time;

    private String img;

    private String content;

    private String url;

    public News() {
        super();
    }

    public News(Integer id, String title, String time, String img, String content, String url) {
        super();
        this.id = id;
        this.title = title;
        this.time = time;
        this.img = img;
        this.content = content;
        this.url = url;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
