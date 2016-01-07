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
        Schema schema = new Schema(1002, "com.guokrspace.dududriver.database");

        addConfig(schema);
        addOrder(schema);
        addBaseNotice(schema);
        addBill(schema);

        new DaoGenerator().generateAll(schema, "/Users/daddyfang/Duducar/driver_pro/app/src/main/java");
    }

    private static void addBill(Schema schema) {
        Entity bill = schema.addEntity("BillRecord");
        bill.addLongProperty("id").primaryKey();
        bill.addStringProperty("money");
        bill.addStringProperty("description");
        bill.addStringProperty("time");
        bill.addIntProperty("type");
        bill.addStringProperty("opposite");
    }

    private static void addConfig(Schema schema) {
        Entity person = schema.addEntity("PersonalInformation");
        person.addIdProperty().autoincrement();
        person.addStringProperty("token");
        person.addStringProperty("mobile");
        person.addStringProperty("password");
    }

    private static void addOrder(Schema schema){
        Entity order = schema.addEntity("OrderRecord");
        order.addIdProperty().autoincrement();

        order.addStringProperty("orderNum");
        order.addStringProperty("driver_id");
        order.addStringProperty("passenger_id");
        order.addStringProperty("passenger_mobile");
        order.addStringProperty("start");
        order.addStringProperty("destination");
        order.addStringProperty("start_lat");
        order.addStringProperty("start_lng");
        order.addStringProperty("destination_lat");
        order.addStringProperty("destination_lng");
        order.addStringProperty("start_time");
        order.addStringProperty("end_time");
        order.addStringProperty("car_type");
        order.addStringProperty("rent_type");
        order.addStringProperty("additional_price");
        order.addStringProperty("mileage");
        order.addStringProperty("sumprice");
        order.addStringProperty("org_price");
        order.addStringProperty("create_time");
        order.addStringProperty("pay_time");
        order.addStringProperty("pay_role");
        order.addStringProperty("status");
        order.addStringProperty("rating");
    }

    private static void addBaseNotice(Schema schema){
        Entity notice = schema.addEntity("BaseNotice");
        notice.addIdProperty().autoincrement();
        notice.addStringProperty("date");
        notice.addStringProperty("type");
        notice.addStringProperty("messageBody");
        notice.addBooleanProperty("outOfTime");
        notice.addIntProperty("noticeId");
    }
}
