package com.guokrspace.dududriver.model;

import com.guokrspace.dududriver.common.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by daddyfang on 15/11/4.
 */
public class BaseInfo {

    public Map<String, Object> getBaseInfo(){
        Map<String, Object> info = new HashMap<String, Object>();

        info.put(Constants.PREFERENCE_KEY_DRIVER_NAME, driver.getName() == null ? "" : driver.getName());
        info.put(Constants.PREFERENCE_KEY_DRIVER_AVATAR, driver.getAvatar() == null ? "" : driver.getAvatar() );
        info.put(Constants.PREFERENCE_KEY_DRIVER_PLATE, driver.getPlate() == null ? "0000" : driver.getPlate());
        info.put(Constants.PREFERNECE_KEY_DRIVER_RATING, driver.getRating() == null ? "0.0" : driver.getRating());
        info.put(Constants.PREFERENCE_KEY_DRIVER_TOTAL_ORDER, driver.getTotal_order() == null ? "0" : driver.getTotal_order());
        info.put(Constants.PREFERENCE_KEY_DRIVER_BALANCE, driver.getBalance() == null ? "0.0" : driver.getBalance());
        info.put(Constants.PREFERENCE_KEY_DRIVER_FAVORABLE_RATE, driver.getFavorable_rate() == null ? "0.0" : driver.getFavorable_rate());

        info.put(Constants.PREFERENCE_KEY_WEBVIEW_ABOUT, webview.getAbout() == null ? "" : webview.getAbout());
        info.put(Constants.PREFERENCE_KEY_WEBVIEW_JOIN, webview.getJoin() == null ? "" : webview.getJoin());
        info.put(Constants.PREFERENCE_KEY_WEBVIEW_CONTACT, webview.getContact() == null ? "" : webview.getContact());
        info.put(Constants.PREFERENCE_KEY_WEBVIEW_CLAUSE, webview.getClause() == null ? "" : webview.getClause());
        info.put(Constants.PREFERENCE_KEY_WEBVIEW_MONEY, webview.getMoney() == null ? "" : webview.getMoney());
        info.put(Constants.PREFERENCE_KEY_WEBVIEW_PERFORMANCE, webview.getPerformance() == null ? "" : webview.getPerformance());
        info.put(Constants.PREFERENCE_KEY_WEBVIEW_BILLS, webview.getSj_account() == null ? "" : webview.getSj_account());
        info.put(Constants.PREFERENCE_KEY_WEBVIEW_PERSONAL, webview.getDriver_personal() == null ? "" : webview.getDriver_personal());


        return info;
    }

    public BaseInfo(){


    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    private Driver driver;
    private String message_id;

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getCMD() {
        return CMD;
    }

    public void setCMD(String CMD) {
        this.CMD = CMD;
    }

    private String CMD;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status;

    public WebView getWebivew() {
        return webview;
    }

    public void setWebivew(WebView webivew) {
        this.webview = webivew;
    }

    private WebView webview;

    public class Driver{
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        private String name;
        private String plate;


        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        private String avatar;

        public String getPlate() {
            return plate;
        }

        public void setPlate(String plate) {
            this.plate = plate;
        }

        public String getRating() {
            return rating;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }

        public String getFavorable_rate() {
            return favorable_rate;
        }

        public void setFavorable_rate(String favorable_rate) {
            this.favorable_rate = favorable_rate;
        }

        public String getBalance() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }

        public String getTotal_order() {
            return total_order;
        }

        public void setTotal_order(String total_order) {
            this.total_order = total_order;
        }

        private String rating;
        private String total_order;
        private String balance;
        private String favorable_rate;
    }

    public class WebView{
        public String getAbout() {
            return about;
        }

        public void setAbout(String about) {
            this.about = about;
        }

        public String getJoin() {
            return join;
        }

        public void setJoin(String join) {
            this.join = join;
        }

        public String getContact() {
            return contact;
        }

        public void setContact(String contact) {
            this.contact = contact;
        }

        public String getClause() {
            return clause;
        }

        public void setClause(String clause) {
            this.clause = clause;
        }

        private String about;
        private String join;
        private String contact;
        private String clause;
        private String money;

        public String getDriver_personal() {
            return driver_personal;
        }

        public void setDriver_personal(String driver_personal) {
            this.driver_personal = driver_personal;
        }

        private String driver_personal;

        public String getPerformance() {
            return performance;
        }

        public void setPerformance(String performance) {
            this.performance = performance;
        }

        public String getMoney() {
            return money;
        }

        public void setMoney(String money) {
            this.money = money;
        }

        public String getSj_account() {
            return sj_account;
        }

        public void setSj_account(String sj_account) {
            this.sj_account = sj_account;
        }

        private String performance;
        private String sj_account;
    }

    public class Charge_rule{
        public String getStarting_price() {
            return starting_price;
        }

        public void setStarting_price(String starting_price) {
            this.starting_price = starting_price;
        }

        public String getStarting_distance() {
            return starting_distance;
        }

        public void setStarting_distance(String starting_distance) {
            this.starting_distance = starting_distance;
        }

        public String getKm_price() {
            return km_price;
        }

        public void setKm_price(String km_price) {
            this.km_price = km_price;
        }

        public String getLow_speed_price() {
            return low_speed_price;
        }

        public void setLow_speed_price(String low_speed_price) {
            this.low_speed_price = low_speed_price;
        }

        private String starting_price;
        private String starting_distance;
        private String km_price;
        private String low_speed_price;

    }
}
