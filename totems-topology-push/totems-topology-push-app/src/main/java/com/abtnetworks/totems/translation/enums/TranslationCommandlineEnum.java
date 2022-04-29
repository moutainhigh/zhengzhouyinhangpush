package com.abtnetworks.totems.translation.enums;

import com.abtnetworks.totems.translation.commandline.extended.*;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/5/26
 */
public enum TranslationCommandlineEnum {

    ABTNETWORKS(26, "abtnetworks", "安博通", TranslationCommandlineAbt.class),
    WESTONE(29, "V2.1.5i-s", "龙马卫士", TranslationCommandlineLongMa.class),

    HILLSTONE_R5(21, "HillstoneStoneOS", "山石R5", TranslationCommandlineHillstone.class),
    H3CV5(70, "H3C SecPath V5", "华三v5", TranslationCommandlineH3CV5.class),
    H3CV7(80, "H3C SecPath V7", "华三v7", TranslationCommandlineH3CV7.class),
    SANG_FOR_IMAGE(170, "Firewall af v8.0.25","深信服v8.0.25", TranslationCommandlineSangfor.class),
    TOPSEC_TOS_005(30, "Topsec TOS 005",  "天融信", TranslationCommandlineTopsecTos005.class),
    TOPSEC_TOS_010_020(30, "Topsec TOS 010-020",  "天融信", TranslationCommandlineTopsecTos006.class),
    DPTECHR003(51, "DPTech Firewall R003", "迪普R003", TranslationCommandlineDptechr003.class),
    DPTECHR004(50, "DPTech Firewall R004", "迪普R004", TranslationCommandlineDptechr004.class),
    CISCO_ASA_99(50, "Cisco ASA 9.9", "思科ASA9.9", TranslationCommandlineCiscoAsa99.class),
    SRX(90, "JuniperSRX", "Juniper SRX", TranslationCommandlineJuniperSRX.class),
    USG2000(111, "USG2000", "华为USG2000", TranslationCommandlineUSG6000.class),
    USG6000(111, "USG6000", "华为U6000", TranslationCommandlineUSG6000.class),
    CHECKPOINT(120, "checkpoint", "checkpoint", TranslationCommandlineCheckPoint.class),
    PALOALOT(130, "Palo Alto Firewall", "paloalto", TranslationCommandlinePaloalot.class),
    ;

    private int code;
    private String key;
    private String desc;
    private Class translationImplClass;

    TranslationCommandlineEnum(int code, String key, String desc, Class translationImplClass) {
        this.code = code;
        this.desc = desc;
        this.key = key;
        this.translationImplClass = translationImplClass;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getKey() {
        return key;
    }

    public Class getTranslationImplClass() {
        return translationImplClass;
    }

    private void setTranslationImplClass(Class translationImplClass) {
        this.translationImplClass = translationImplClass;
    }

    public static TranslationCommandlineEnum fromString(String text) {
        for (TranslationCommandlineEnum translationCommandlineEnum : TranslationCommandlineEnum.values()) {
            if (translationCommandlineEnum.key.equals(text)) {
                return translationCommandlineEnum;
            }
        }
        return null;
    }

}
