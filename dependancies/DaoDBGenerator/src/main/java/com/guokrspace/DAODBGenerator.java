package com.guokrspace;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class DAODBGenerator {


    /**
     * Generates entities and DAOs for the example project DaoExample.
     * <p/>
     * Run it as a Java application (not Android).
     *
     * @author Markus
     */

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1002, "com.guokrspace.duducar.database");

        addConfig(schema);
        addOrder(schema);
        addSearchRecord(schema);
        addDriver(schema);
        addMessage(schema);

//        new DaoGenerator().generateAll(schema, "src-gen");
        new DaoGenerator().generateAll(schema, "/Users/hyman/Duducar/app/src/main/java");
    }

    private static void addMessage(Schema schema) {
        Entity notice = schema.addEntity("MessageInfo");
        notice.addIntProperty("id");
        notice.addStringProperty("title");
        notice.addStringProperty("date");
        notice.addStringProperty("imgurl");
        notice.addStringProperty("content");
        notice.addStringProperty("url");
    }

    private static void addConfig(Schema schema) {
        Entity person = schema.addEntity("PersonalInformation");
        person.addIdProperty().autoincrement();
        person.addStringProperty("token");
        person.addStringProperty("mobile");
    }

    /*private static void addOrder(Schema schema){
        Entity order = schema.addEntity("OrderRecord");
        order.addIdProperty().autoincrement();
        order.addStringProperty("startAddr");
        order.addStringProperty("destAddr");
        order.addStringProperty("startLat");
        order.addStringProperty("startLng");
        order.addStringProperty("destLat");
        order.addStringProperty("destLng");
        order.addStringProperty("mileage");
        order.addStringProperty("price");
        order.addStringProperty("carType");
        order.addStringProperty("orderTime");
    }*/

    private static void addSearchRecord(Schema schema)
    {
        Entity searchHistory = schema.addEntity("SearchHistory");
        searchHistory.addIdProperty().primaryKey().autoincrement();
        searchHistory.addStringProperty("address");
        searchHistory.addStringProperty("details");
    }

    private static void addOrder(Schema schema) {
        Entity orderBean = schema.addEntity("OrderRecord");
        orderBean.addLongProperty("id").primaryKey().index();
        orderBean.addStringProperty("orderNum").notNull();
        orderBean.addStringProperty("passenger_mobile");
        orderBean.addStringProperty("start");
        orderBean.addStringProperty("destination");
        orderBean.addDoubleProperty("start_lat");
        orderBean.addDoubleProperty("start_lng");
        orderBean.addDoubleProperty("destination_lat");
        orderBean.addDoubleProperty("destination_lng");
        orderBean.addLongProperty("start_time");
        orderBean.addLongProperty("end_time");
        orderBean.addStringProperty("mileage");
        orderBean.addStringProperty("sumprice");
        orderBean.addIntProperty("car_type");
        orderBean.addIntProperty("rent_type");
        orderBean.addStringProperty("additional_price");
        orderBean.addStringProperty("org_price");
        orderBean.addStringProperty("add_price1");
        orderBean.addStringProperty("add_price2");
        orderBean.addStringProperty("add_price3");
        orderBean.addIntProperty("isCancel");
        orderBean.addStringProperty("low_speed_time");
        orderBean.addIntProperty("isCityline");
        orderBean.addIntProperty("cityline_id");
        orderBean.addLongProperty("pay_time");
        orderBean.addIntProperty("pay_type");
        orderBean.addIntProperty("pay_role");
        orderBean.addIntProperty("status");
        orderBean.addIntProperty("rating");
        orderBean.addStringProperty("company");
        orderBean.addIntProperty("passenger_id");
        orderBean.addLongProperty("driver_id").notNull();
    }

    private static void addDriver(Schema schema) {
        Entity driverBean = schema.addEntity("DriverRecord");
        driverBean.addLongProperty("id").primaryKey().index();
        driverBean.addStringProperty("name");
        driverBean.addStringProperty("mobile");
        driverBean.addStringProperty("avatar");
        driverBean.addStringProperty("plate");
        driverBean.addStringProperty("picture");
        driverBean.addStringProperty("description");
    }
}
