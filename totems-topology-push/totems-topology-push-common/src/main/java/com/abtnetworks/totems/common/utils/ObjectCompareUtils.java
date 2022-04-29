package com.abtnetworks.totems.common.utils;

import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地址对象对比工具
 * @Author: zhoumuhua
 * @Date: 2021/9/8
 */
public class ObjectCompareUtils {
    /**
     * 对象对比方法
     * @param originalMap 基线对象map,key为当前对象名称，value为对象内容
     * @param currentMap 更改后的对象map,key为当前对象名称，value为对象内容
     * @return currentMap对比originalMap结果
     * 返回数据结构：Map key为add即为更改后新增对象，key为del即为更改后删除对象,无对应key值代表无对应变化
     */
    public static Map<String, Map<String, List<String>>> compareObjectMapList(Map<String, List<String>> originalMap, Map<String, List<String>> currentMap) {
        //返回结果map
        Map<String, Map<String, List<String>>> resultMap = new HashMap();
        Map<String ,List<String>> addMap = new HashMap<>();
        Map<String ,List<String>> delMap = new HashMap<>();

        for(Map.Entry<String, List<String>> entry : currentMap.entrySet()) {
            if (originalMap.containsKey(entry.getKey())) {
                //如果更改后map中存在当前对象，则代表更改地址对象内容
                List<String> originalList = entry.getValue();
                List<String> currentList = originalMap.get(entry.getKey());

                Map<String, List<String>> resMap = compareList(originalList, currentList);
                if (resMap.containsKey("add")) {
                    addMap.put(entry.getKey(), resMap.get("add"));
                }

                if (resMap.containsKey("del")) {
                    delMap.put(entry.getKey(), resMap.get("del"));
                }
            } else {
                //代表当前对象需要新增
                addMap.put(entry.getKey(), entry.getValue());
            }
        }

        if (!addMap.isEmpty()) {
            resultMap.put("add", addMap);
        }

        if (!delMap.isEmpty()) {
            resultMap.put("del", delMap);
        }

        return resultMap;
    }


    /**
     *
     * @param originalList 基线地址对象List
     * @param currentList 更改后地址对象list
     * @return 返回currentList对比originalList结果
     * 返回数据结构：Map key为add即为更改后新增对象，key为del即为更改后删除对象,无对应key值代表无对应变化
     */
    public static Map<String, List<String>> compareList(List<String> originalList, List<String> currentList) {
        Map<String, List<String>> resultMap = new HashMap<>();
        List<String> addList = new ArrayList<>();
        List<String> delList = new ArrayList<>();
        //地址对象是否存在改变
        //设备上已有的地址对象
        if (ObjectUtils.isEmpty(originalList)) {
            //如果初始化列表全为空，代表设备上已有的对象全删除
            for (String addr : currentList) {
                delList.add(addr);
            }
        } else {
            //以基线为标准比对
            for (String addr : originalList) {
                if (!currentList.contains(addr)) {
                    //如果初始化列表中的对象在设备上没有，则新增对象
                    addList.add(addr);
                }
            }
            //以设备为标准比对
            for (String deviceAddr : currentList) {
                if (!originalList.contains(deviceAddr)) {
                    //如果墙上地址对象在初始化列表中没有，则删除对象
                    delList.add(deviceAddr);
                }
            }
        }

        if (ObjectUtils.isNotEmpty(addList)) {
            resultMap.put("add", addList);
        }

        if (ObjectUtils.isNotEmpty(delList)) {
            resultMap.put("del", delList);
        }

        return resultMap;
    }
}
