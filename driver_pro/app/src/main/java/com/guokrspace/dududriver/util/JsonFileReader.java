package com.guokrspace.dududriver.util;

import com.baidu.mapapi.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by daddyfang on 16/1/13.
 */
public class JsonFileReader {

    public static File createFile(String dirPath,String fileName){
        File json = new File(dirPath, fileName);
        try {
            json.createNewFile();
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
        return json;
    }

    public static boolean fileExists(String dirPath, String fileName){
        try {
            File json = new File(dirPath, fileName);
            if(!json.exists()){
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static void delFile(String dirPath, String fileName){
        try {
            File json = new File(dirPath, fileName);
            if(!json.exists()){
                return;
            }
            json.delete();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static List<LatLng> getJson(String dirPath, String fileName) {
        List<LatLng> data = new ArrayList<LatLng>();
        try {
            File json = new File(dirPath, fileName);
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(json)));
            String line;
            while ((line = bf.readLine()) != null) {
                try {
                    data.add(new LatLng(Double.parseDouble(line.split(" ")[0]), Double.parseDouble(line.split(" ")[1])));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

}
