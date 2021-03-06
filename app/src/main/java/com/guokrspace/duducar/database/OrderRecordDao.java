package com.guokrspace.duducar.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.guokrspace.duducar.database.OrderRecord;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table ORDER_RECORD.
*/
public class OrderRecordDao extends AbstractDao<OrderRecord, Long> {

    public static final String TABLENAME = "ORDER_RECORD";

    /**
     * Properties of entity OrderRecord.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "ID");
        public final static Property OrderNum = new Property(1, String.class, "orderNum", false, "ORDER_NUM");
        public final static Property Passenger_mobile = new Property(2, String.class, "passenger_mobile", false, "PASSENGER_MOBILE");
        public final static Property Start = new Property(3, String.class, "start", false, "START");
        public final static Property Destination = new Property(4, String.class, "destination", false, "DESTINATION");
        public final static Property Start_lat = new Property(5, Double.class, "start_lat", false, "START_LAT");
        public final static Property Start_lng = new Property(6, Double.class, "start_lng", false, "START_LNG");
        public final static Property Destination_lat = new Property(7, Double.class, "destination_lat", false, "DESTINATION_LAT");
        public final static Property Destination_lng = new Property(8, Double.class, "destination_lng", false, "DESTINATION_LNG");
        public final static Property Start_time = new Property(9, Long.class, "start_time", false, "START_TIME");
        public final static Property End_time = new Property(10, Long.class, "end_time", false, "END_TIME");
        public final static Property Mileage = new Property(11, String.class, "mileage", false, "MILEAGE");
        public final static Property Sumprice = new Property(12, String.class, "sumprice", false, "SUMPRICE");
        public final static Property Car_type = new Property(13, Integer.class, "car_type", false, "CAR_TYPE");
        public final static Property Rent_type = new Property(14, Integer.class, "rent_type", false, "RENT_TYPE");
        public final static Property Additional_price = new Property(15, String.class, "additional_price", false, "ADDITIONAL_PRICE");
        public final static Property Org_price = new Property(16, String.class, "org_price", false, "ORG_PRICE");
        public final static Property Add_price1 = new Property(17, String.class, "add_price1", false, "ADD_PRICE1");
        public final static Property Add_price2 = new Property(18, String.class, "add_price2", false, "ADD_PRICE2");
        public final static Property Add_price3 = new Property(19, String.class, "add_price3", false, "ADD_PRICE3");
        public final static Property IsCancel = new Property(20, Integer.class, "isCancel", false, "IS_CANCEL");
        public final static Property Low_speed_time = new Property(21, String.class, "low_speed_time", false, "LOW_SPEED_TIME");
        public final static Property IsCityline = new Property(22, Integer.class, "isCityline", false, "IS_CITYLINE");
        public final static Property Cityline_id = new Property(23, Integer.class, "cityline_id", false, "CITYLINE_ID");
        public final static Property Pay_time = new Property(24, Long.class, "pay_time", false, "PAY_TIME");
        public final static Property Pay_type = new Property(25, Integer.class, "pay_type", false, "PAY_TYPE");
        public final static Property Pay_role = new Property(26, Integer.class, "pay_role", false, "PAY_ROLE");
        public final static Property Status = new Property(27, Integer.class, "status", false, "STATUS");
        public final static Property Rating = new Property(28, Integer.class, "rating", false, "RATING");
        public final static Property Company = new Property(29, String.class, "company", false, "COMPANY");
        public final static Property Passenger_id = new Property(30, Integer.class, "passenger_id", false, "PASSENGER_ID");
        public final static Property Driver_id = new Property(31, long.class, "driver_id", false, "DRIVER_ID");
    };


    public OrderRecordDao(DaoConfig config) {
        super(config);
    }
    
    public OrderRecordDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'ORDER_RECORD' (" + //
                "'ID' INTEGER PRIMARY KEY ," + // 0: id
                "'ORDER_NUM' TEXT NOT NULL ," + // 1: orderNum
                "'PASSENGER_MOBILE' TEXT," + // 2: passenger_mobile
                "'START' TEXT," + // 3: start
                "'DESTINATION' TEXT," + // 4: destination
                "'START_LAT' REAL," + // 5: start_lat
                "'START_LNG' REAL," + // 6: start_lng
                "'DESTINATION_LAT' REAL," + // 7: destination_lat
                "'DESTINATION_LNG' REAL," + // 8: destination_lng
                "'START_TIME' INTEGER," + // 9: start_time
                "'END_TIME' INTEGER," + // 10: end_time
                "'MILEAGE' TEXT," + // 11: mileage
                "'SUMPRICE' TEXT," + // 12: sumprice
                "'CAR_TYPE' INTEGER," + // 13: car_type
                "'RENT_TYPE' INTEGER," + // 14: rent_type
                "'ADDITIONAL_PRICE' TEXT," + // 15: additional_price
                "'ORG_PRICE' TEXT," + // 16: org_price
                "'ADD_PRICE1' TEXT," + // 17: add_price1
                "'ADD_PRICE2' TEXT," + // 18: add_price2
                "'ADD_PRICE3' TEXT," + // 19: add_price3
                "'IS_CANCEL' INTEGER," + // 20: isCancel
                "'LOW_SPEED_TIME' TEXT," + // 21: low_speed_time
                "'IS_CITYLINE' INTEGER," + // 22: isCityline
                "'CITYLINE_ID' INTEGER," + // 23: cityline_id
                "'PAY_TIME' INTEGER," + // 24: pay_time
                "'PAY_TYPE' INTEGER," + // 25: pay_type
                "'PAY_ROLE' INTEGER," + // 26: pay_role
                "'STATUS' INTEGER," + // 27: status
                "'RATING' INTEGER," + // 28: rating
                "'COMPANY' TEXT," + // 29: company
                "'PASSENGER_ID' INTEGER," + // 30: passenger_id
                "'DRIVER_ID' INTEGER NOT NULL );"); // 31: driver_id
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_ORDER_RECORD_ID ON ORDER_RECORD" +
                " (ID);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'ORDER_RECORD'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, OrderRecord entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getOrderNum());
 
        String passenger_mobile = entity.getPassenger_mobile();
        if (passenger_mobile != null) {
            stmt.bindString(3, passenger_mobile);
        }
 
        String start = entity.getStart();
        if (start != null) {
            stmt.bindString(4, start);
        }
 
        String destination = entity.getDestination();
        if (destination != null) {
            stmt.bindString(5, destination);
        }
 
        Double start_lat = entity.getStart_lat();
        if (start_lat != null) {
            stmt.bindDouble(6, start_lat);
        }
 
        Double start_lng = entity.getStart_lng();
        if (start_lng != null) {
            stmt.bindDouble(7, start_lng);
        }
 
        Double destination_lat = entity.getDestination_lat();
        if (destination_lat != null) {
            stmt.bindDouble(8, destination_lat);
        }
 
        Double destination_lng = entity.getDestination_lng();
        if (destination_lng != null) {
            stmt.bindDouble(9, destination_lng);
        }
 
        Long start_time = entity.getStart_time();
        if (start_time != null) {
            stmt.bindLong(10, start_time);
        }
 
        Long end_time = entity.getEnd_time();
        if (end_time != null) {
            stmt.bindLong(11, end_time);
        }
 
        String mileage = entity.getMileage();
        if (mileage != null) {
            stmt.bindString(12, mileage);
        }
 
        String sumprice = entity.getSumprice();
        if (sumprice != null) {
            stmt.bindString(13, sumprice);
        }
 
        Integer car_type = entity.getCar_type();
        if (car_type != null) {
            stmt.bindLong(14, car_type);
        }
 
        Integer rent_type = entity.getRent_type();
        if (rent_type != null) {
            stmt.bindLong(15, rent_type);
        }
 
        String additional_price = entity.getAdditional_price();
        if (additional_price != null) {
            stmt.bindString(16, additional_price);
        }
 
        String org_price = entity.getOrg_price();
        if (org_price != null) {
            stmt.bindString(17, org_price);
        }
 
        String add_price1 = entity.getAdd_price1();
        if (add_price1 != null) {
            stmt.bindString(18, add_price1);
        }
 
        String add_price2 = entity.getAdd_price2();
        if (add_price2 != null) {
            stmt.bindString(19, add_price2);
        }
 
        String add_price3 = entity.getAdd_price3();
        if (add_price3 != null) {
            stmt.bindString(20, add_price3);
        }
 
        Integer isCancel = entity.getIsCancel();
        if (isCancel != null) {
            stmt.bindLong(21, isCancel);
        }
 
        String low_speed_time = entity.getLow_speed_time();
        if (low_speed_time != null) {
            stmt.bindString(22, low_speed_time);
        }
 
        Integer isCityline = entity.getIsCityline();
        if (isCityline != null) {
            stmt.bindLong(23, isCityline);
        }
 
        Integer cityline_id = entity.getCityline_id();
        if (cityline_id != null) {
            stmt.bindLong(24, cityline_id);
        }
 
        Long pay_time = entity.getPay_time();
        if (pay_time != null) {
            stmt.bindLong(25, pay_time);
        }
 
        Integer pay_type = entity.getPay_type();
        if (pay_type != null) {
            stmt.bindLong(26, pay_type);
        }
 
        Integer pay_role = entity.getPay_role();
        if (pay_role != null) {
            stmt.bindLong(27, pay_role);
        }
 
        Integer status = entity.getStatus();
        if (status != null) {
            stmt.bindLong(28, status);
        }
 
        Integer rating = entity.getRating();
        if (rating != null) {
            stmt.bindLong(29, rating);
        }
 
        String company = entity.getCompany();
        if (company != null) {
            stmt.bindString(30, company);
        }
 
        Integer passenger_id = entity.getPassenger_id();
        if (passenger_id != null) {
            stmt.bindLong(31, passenger_id);
        }
        stmt.bindLong(32, entity.getDriver_id());
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public OrderRecord readEntity(Cursor cursor, int offset) {
        OrderRecord entity = new OrderRecord( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // orderNum
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // passenger_mobile
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // start
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // destination
            cursor.isNull(offset + 5) ? null : cursor.getDouble(offset + 5), // start_lat
            cursor.isNull(offset + 6) ? null : cursor.getDouble(offset + 6), // start_lng
            cursor.isNull(offset + 7) ? null : cursor.getDouble(offset + 7), // destination_lat
            cursor.isNull(offset + 8) ? null : cursor.getDouble(offset + 8), // destination_lng
            cursor.isNull(offset + 9) ? null : cursor.getLong(offset + 9), // start_time
            cursor.isNull(offset + 10) ? null : cursor.getLong(offset + 10), // end_time
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // mileage
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // sumprice
            cursor.isNull(offset + 13) ? null : cursor.getInt(offset + 13), // car_type
            cursor.isNull(offset + 14) ? null : cursor.getInt(offset + 14), // rent_type
            cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15), // additional_price
            cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16), // org_price
            cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17), // add_price1
            cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18), // add_price2
            cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19), // add_price3
            cursor.isNull(offset + 20) ? null : cursor.getInt(offset + 20), // isCancel
            cursor.isNull(offset + 21) ? null : cursor.getString(offset + 21), // low_speed_time
            cursor.isNull(offset + 22) ? null : cursor.getInt(offset + 22), // isCityline
            cursor.isNull(offset + 23) ? null : cursor.getInt(offset + 23), // cityline_id
            cursor.isNull(offset + 24) ? null : cursor.getLong(offset + 24), // pay_time
            cursor.isNull(offset + 25) ? null : cursor.getInt(offset + 25), // pay_type
            cursor.isNull(offset + 26) ? null : cursor.getInt(offset + 26), // pay_role
            cursor.isNull(offset + 27) ? null : cursor.getInt(offset + 27), // status
            cursor.isNull(offset + 28) ? null : cursor.getInt(offset + 28), // rating
            cursor.isNull(offset + 29) ? null : cursor.getString(offset + 29), // company
            cursor.isNull(offset + 30) ? null : cursor.getInt(offset + 30), // passenger_id
            cursor.getLong(offset + 31) // driver_id
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, OrderRecord entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setOrderNum(cursor.getString(offset + 1));
        entity.setPassenger_mobile(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setStart(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setDestination(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setStart_lat(cursor.isNull(offset + 5) ? null : cursor.getDouble(offset + 5));
        entity.setStart_lng(cursor.isNull(offset + 6) ? null : cursor.getDouble(offset + 6));
        entity.setDestination_lat(cursor.isNull(offset + 7) ? null : cursor.getDouble(offset + 7));
        entity.setDestination_lng(cursor.isNull(offset + 8) ? null : cursor.getDouble(offset + 8));
        entity.setStart_time(cursor.isNull(offset + 9) ? null : cursor.getLong(offset + 9));
        entity.setEnd_time(cursor.isNull(offset + 10) ? null : cursor.getLong(offset + 10));
        entity.setMileage(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setSumprice(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setCar_type(cursor.isNull(offset + 13) ? null : cursor.getInt(offset + 13));
        entity.setRent_type(cursor.isNull(offset + 14) ? null : cursor.getInt(offset + 14));
        entity.setAdditional_price(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
        entity.setOrg_price(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
        entity.setAdd_price1(cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17));
        entity.setAdd_price2(cursor.isNull(offset + 18) ? null : cursor.getString(offset + 18));
        entity.setAdd_price3(cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19));
        entity.setIsCancel(cursor.isNull(offset + 20) ? null : cursor.getInt(offset + 20));
        entity.setLow_speed_time(cursor.isNull(offset + 21) ? null : cursor.getString(offset + 21));
        entity.setIsCityline(cursor.isNull(offset + 22) ? null : cursor.getInt(offset + 22));
        entity.setCityline_id(cursor.isNull(offset + 23) ? null : cursor.getInt(offset + 23));
        entity.setPay_time(cursor.isNull(offset + 24) ? null : cursor.getLong(offset + 24));
        entity.setPay_type(cursor.isNull(offset + 25) ? null : cursor.getInt(offset + 25));
        entity.setPay_role(cursor.isNull(offset + 26) ? null : cursor.getInt(offset + 26));
        entity.setStatus(cursor.isNull(offset + 27) ? null : cursor.getInt(offset + 27));
        entity.setRating(cursor.isNull(offset + 28) ? null : cursor.getInt(offset + 28));
        entity.setCompany(cursor.isNull(offset + 29) ? null : cursor.getString(offset + 29));
        entity.setPassenger_id(cursor.isNull(offset + 30) ? null : cursor.getInt(offset + 30));
        entity.setDriver_id(cursor.getLong(offset + 31));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(OrderRecord entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(OrderRecord entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
