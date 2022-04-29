package com.abtnetworks.totems.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

public class PolicyRowUilts {

    private static Logger logger = Logger.getLogger(PolicyRowUilts.class);

    /**
     * 得到指定行数的配置文本
     */
    public static String getRuleText(String rawText, String lineNumber) {
        if (StringUtils.isBlank(rawText) || StringUtils.isBlank(lineNumber)) {
            return "";
        }
        String[] lineNumbers = lineNumber.replace("[", "").replace("]", "").split("-");
        //判断linNumber行号为[0]或者为[-]
        if (lineNumbers.length<1 || "0".equals(lineNumbers[0]) || StringUtils.isBlank(lineNumbers[0])) {
            return "";
        }

        Integer start = Integer.valueOf(lineNumbers[0].trim());
        Integer end = Integer.valueOf(lineNumbers[lineNumbers.length-1].trim());

        List<String> strList = Arrays.asList(rawText.replace("\\r\\n", "?").replace("\\n", "?").split("\\?"));

        if (strList == null || strList.isEmpty()) {
            return "";
        }

        int size = strList.size();
        StringBuilder sb = new StringBuilder(2000);

        for (int num = start; num <= end; num++) {
            if (num > size || num < 1) {
                break;
            }

            String ruleText = strList.get(num - 1);
            if (StringUtils.isNotBlank(ruleText)) {
                ruleText = ruleText.trim();
                sb.append(ruleText).append("\n");
            }
        }

        if (sb.length() == 0) {
            return "";
        }
        return sb.toString();
    }

}
