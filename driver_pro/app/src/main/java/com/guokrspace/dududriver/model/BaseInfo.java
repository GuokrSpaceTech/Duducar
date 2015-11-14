package com.guokrspace.dududriver.model;

/**
 * Created by daddyfang on 15/11/4.
 */
public class BaseInfo {

    public BaseInfo(){

    }

    public Charge_rule getCharge_rule() {
        return charge_rule;
    }

    public void setCharge_rule(Charge_rule charge_rule) {
        this.charge_rule = charge_rule;
    }

    private Charge_rule charge_rule;
    private Driver driverInfo;

    public Driver getDriverInfo() {
        return driverInfo;
    }

    public void setDriverInfo(Driver driverInfo) {
        this.driverInfo = driverInfo;
    }

    public CompanyInfo getCompanyInfo() {
        return companyInfo;
    }

    public void setCompanyInfo(CompanyInfo companyInfo) {
        this.companyInfo = companyInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private CompanyInfo companyInfo;
    private String status;

    public class CompanyInfo{
        private String companyName;

        public String getTeamName() {
            return teamName;
        }

        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        private String teamName;

    }

    public class Driver{
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private String name;
        private String plate;

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
