package com.guokrspace.dududriver.model;

/**
 * Created by daddyfang on 15/11/4.
 */
public class BaseInfo {

    public FeeRule getFeeRule() {
        return feeRule;
    }

    public void setFeeRule(FeeRule feeRule) {
        this.feeRule = feeRule;
    }

    public DriverInfo getDriverInfo() {
        return driverInfo;
    }

    public void setDriverInfo(DriverInfo driverInfo) {
        this.driverInfo = driverInfo;
    }

    public CompanyInfo getCompanyInfo() {
        return companyInfo;
    }

    public void setCompanyInfo(CompanyInfo companyInfo) {
        this.companyInfo = companyInfo;
    }

    private FeeRule feeRule;
    private DriverInfo driverInfo;
    private CompanyInfo companyInfo;

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

    public class DriverInfo{
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }

        private String name;
        private String age;

    }

    public class FeeRule{
        public String getLaunchDistance() {
            return launchDistance;
        }

        public void setLaunchDistance(String launchDistance) {
            this.launchDistance = launchDistance;
        }

        public String getLaunchPrice() {
            return launchPrice;
        }

        public void setLaunchPrice(String launchPrice) {
            this.launchPrice = launchPrice;
        }

        public String getLowSpeedPrice() {
            return lowSpeedPrice;
        }

        public void setLowSpeedPrice(String lowSpeedPrice) {
            this.lowSpeedPrice = lowSpeedPrice;
        }

        public String getPerMilesPrice() {
            return perMilesPrice;
        }

        public void setPerMilesPrice(String perMilesPrice) {
            this.perMilesPrice = perMilesPrice;
        }

        private String launchDistance;
        private String launchPrice;
        private String perMilesPrice;
        private String lowSpeedPrice;

    }
}
