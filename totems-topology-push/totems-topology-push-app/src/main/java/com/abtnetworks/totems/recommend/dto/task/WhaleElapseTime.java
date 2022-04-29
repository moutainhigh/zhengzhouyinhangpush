package com.abtnetworks.totems.recommend.dto.task;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WhaleElapseTime {
    Long analyzeQueryPathTime = 0L;
    Long pathDetail = 0L;
    List<Long> ruleListTimeList = new ArrayList<>();
    List<Long> deviceTimeList  = new ArrayList<>();
    List<Long> zoneTimeList = new ArrayList<>();
    List<Long> recommendDeviceDetail = new ArrayList<>();
    List<Long> analyzeDeviceDetail = new ArrayList<>();
}
