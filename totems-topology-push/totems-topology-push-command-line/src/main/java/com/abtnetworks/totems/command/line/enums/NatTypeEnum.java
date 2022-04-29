package com.abtnetworks.totems.command.line.enums;
/**
 * @author zakyoung
 * @Title:
 * @Description: 请写注释类
 * @date 2020-06-05
 */
public enum NatTypeEnum {
    SRC(6,"SRC","源NAT"),
    DST(7,"DST","目的NAT"),
    BI_DIR(5,"STATIC","静态NAT"),
    BOTH(9,"BOTH", "双向NAT")
    ;
    /****类型code***/
    private int  typeCode;
    /****类型nat***/
    private String  natField;

    private String desc;

    NatTypeEnum(int typeCode, String natField, String desc){
        this.typeCode = typeCode;
        this.natField = natField;
        this.desc = desc;
    }

    /**
     * 根据类型code获取
     * @param typeCode
     * @return
     */
    public static NatTypeEnum getNatByCode(int typeCode){
        if(typeCode == SRC.getTypeCode()){
            return SRC;
        }else if(typeCode == DST.getTypeCode()){
            return DST;
        }else if(typeCode == BI_DIR.getTypeCode()){
            return BI_DIR;
        }else{
            return BOTH;
        }
    }
    public int getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(int typeCode) {
        this.typeCode = typeCode;
    }

    public String getNatField() {
        return natField;
    }

    public void setNatField(String natField) {
        this.natField = natField;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

