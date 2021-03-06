package com.guokrspace.duducar.database;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table PERSONAL_INFORMATION.
 */
public class PersonalInformation {

    private Long id;
    private String token;
    private String mobile;
    private String nickname;
    private String sex;
    private String age;
    private String industry;
    private String company;
    private String profession;
    private String signature;
    private String realname_certify_status;
    private String driver_certify_status;

    public PersonalInformation() {
    }

    public PersonalInformation(Long id) {
        this.id = id;
    }

    public PersonalInformation(Long id, String token, String mobile, String nickname, String sex, String age, String industry, String company, String profession, String signature, String realname_certify_status, String driver_certify_status) {
        this.id = id;
        this.token = token;
        this.mobile = mobile;
        this.nickname = nickname;
        this.sex = sex;
        this.age = age;
        this.industry = industry;
        this.company = company;
        this.profession = profession;
        this.signature = signature;
        this.realname_certify_status = realname_certify_status;
        this.driver_certify_status = driver_certify_status;
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getRealname_certify_status() {
        return realname_certify_status;
    }

    public void setRealname_certify_status(String realname_certify_status) {
        this.realname_certify_status = realname_certify_status;
    }

    public String getDriver_certify_status() {
        return driver_certify_status;
    }

    public void setDriver_certify_status(String driver_certify_status) {
        this.driver_certify_status = driver_certify_status;
    }

}
