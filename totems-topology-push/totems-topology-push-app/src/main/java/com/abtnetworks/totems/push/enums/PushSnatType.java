package com.abtnetworks.totems.push.enums;

        import io.swagger.models.auth.In;
        import org.apache.xmlbeans.impl.xb.xsdschema.Public;

/**
 * @author lifei
 * @desc snat类型（f5）
 * @date 2021/8/8 18:50
 */
public enum PushSnatType {

    SNAT(1, "SNAT", "SNAT类型"),

    NONE(2, "NONE", "NONE类型"),

    AUTOMAP(3, "AUTOMAP", "AUTOMAP 类型");

    private Integer code;

    private String desc;

    private String msg;


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    PushSnatType(Integer code, String desc, String msg) {
        this.code = code;
        this.desc = desc;
        this.msg = msg;
    }

    public static String getDescByCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (PushSnatType item : PushSnatType.values()) {
            if (code.equals(item.getCode())) {
                return item.getDesc();
            }
        }
        return null;
    }
}
