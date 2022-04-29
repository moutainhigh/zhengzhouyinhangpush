package com.abtnetworks.totems.common.utils;

import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: wenjiachang
 * @Date: 2019/3/5 17:10
 */
public class ArrayUtils {

    public static List<Integer> parseIntArrayList(String listString) throws Exception {
        JSONArray jsonArray = JSONArray.parseArray(listString);
        List<Integer> numArray = new ArrayList<Integer>();
        for (int index = 0; index < jsonArray.size(); index++) {
            int value = jsonArray.getInteger(index);
            numArray.add(value);
        }
        return numArray;
    }

    public static List<String> parseStringArrayList(String listString) throws Exception {
        JSONArray jsonArray = JSONArray.parseArray(listString);
        List<String> stringArray = new ArrayList<String>();
        for (int index = 0; index < jsonArray.size(); index++) {
            stringArray.add(jsonArray.getString(index));
        }
        return stringArray;
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }
}
