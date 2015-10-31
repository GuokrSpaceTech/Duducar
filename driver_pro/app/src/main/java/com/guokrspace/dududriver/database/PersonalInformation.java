package com.guokrspace.dududriver.database;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table PERSONAL_INFORMATION.
 */
public class PersonalInformation {

    private Long id;
    private String token;
    private String mobile;

    public PersonalInformation() {
    }

    public PersonalInformation(Long id) {
        this.id = id;
    }

    public PersonalInformation(Long id, String token, String mobile) {
        this.id = id;
        this.token = token;
        this.mobile = mobile;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

}