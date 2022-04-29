package com.abtnetworks.totems.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Author: wenjiachang
 * @Date: 2019/1/7 14:27
 */
public class TimeUtils {

    public static final String EUROPEAN_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String HUAWEI_TIME_FORMAT = "HH:mm:ss yyyy/MM/dd";

    public static final String HUAWEI_USG2000_FORMAT = "HH:mm yyyy/MM/dd";

    public static final String H3C_V7_FORMAT = "HH:mm MM/dd/yyyy";

    public static final String JUNIPER_SSG_TIME_FOMRAT = "M/d/yyyy H:m";

    public static final String JUNIPER_SRX_TIME_FORMAT = "yyyy-MM-dd.HH:mm";

    public static final String CISCO_ASA_TIME_FORMAT = "HH:mm d MMM yyyy";

    public static final String HILLSTONE_TIME_FORMAT = "MM/dd/yyyy HH:mm";

    public static final String DPTECH_FIREWALL_FORMAT = "HH:mm:ss yyyy-MM-dd";

    public static final String FORTINET_FORMAT = "HH:mm yyyy/MM/dd";

    public static final String VenustechVSOS_TIME_FORMAT = "yy-MM-dd HH:mm:ss";

    public static final String COMMON_TIME_DAY_FORMAT = "yyyyMMdd";

    public static final  String LEGEND_DATE_FORMAT = "yyyy-MM-dd";

    public static final String LEGEND_TIME_FORMAT = "HH:mm:ss";

    public static final String LEAD_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    public static final String OPPO_TIME_FORMAT = "yyyy/MM/dd@HH:mm";

    public static String transformDateFormat(String timeString, String srcFormat, String dstFormat) {
        if(AliStringUtils.isEmpty(timeString) ) {
            return null;
        }

        SimpleDateFormat srcSDF = new SimpleDateFormat(srcFormat);
        Date date = null;
        try{
            date = srcSDF.parse(timeString);
        } catch(Exception e) {
            return null;
        }

        if(date == null) {
            return null;
        }

        SimpleDateFormat dstSDF = new SimpleDateFormat(dstFormat, Locale.ENGLISH);
        return dstSDF.format(date);
    }

    /**
     * 日期格式转换yyyy-MM-dd'T'HH:mm:ss.SSS'+'SSSS TO  yyyy-MM-dd HH:mm:ss
     * 2021-01-12T16:42:43.620+0800
     * @throws ParseException
     */
    public static String dealDateFormat(String oldDateStr) throws ParseException {
        //此格式只有  jdk 1.7才支持  yyyy-MM-dd'T'HH:mm:ss.SSSXXX
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+'SSSS");
        Date  date = df.parse(oldDateStr);
        SimpleDateFormat df1 = new SimpleDateFormat ("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
        Date date1 =  df1.parse(date.toString());
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df2.format(date1);
    }

}
