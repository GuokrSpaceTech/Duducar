package com.guokrspace.duducar.communication.http.model;

import java.io.Serializable;

/**
 * Created by hyman on 15/12/19.
 */
public class WebViewUrls extends BaseModel implements Serializable {

    /*
     * 关于嘟嘟
     */
    private String about;

    /*
     * 乘客指南
     */
    private String help;

    /*
     * 法律条款
     */
    private String clause;

    public WebViewUrls() {
        super();
    }

    public WebViewUrls(String about, String help, String clause) {
        this.about = about;
        this.help = help;
        this.clause = clause;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public String getClause() {
        return clause;
    }

    public void setClause(String clause) {
        this.clause = clause;
    }
}
