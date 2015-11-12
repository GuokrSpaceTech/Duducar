package com.guokrspace.dududriver.model;

import java.util.HashMap;

/**
 * Created by daddyfang on 15/11/10.
 */
public class DistanceInfoListItem {

    private static HashMap<Integer, DistanceInfo> distanceInfoHashMap;

    public DistanceInfo getById(int id){
        return distanceInfoHashMap.get(id);
    }

}
