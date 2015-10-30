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
        Schema schema = new Schema(1000, "com.guokrspace");

        addConfig(schema);
        addOrder(schema);

        new DaoGenerator().generateAll(schema, "src-gen");
    }

    private static void addConfig(Schema schema) {
        Entity person = schema.addEntity("PersonalInformation");
        person.addIdProperty().autoincrement();
        person.addStringProperty("token");
        person.addStringProperty("mobile");
    }

    private static void addOrder(Schema schema){
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
    }
}
