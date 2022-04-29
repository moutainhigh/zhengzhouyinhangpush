package com.abtnetworks.totems.push.enums;

/**
 * @Description
 * @Author guanduo.su
 * @Date: 2021/4/13 10:57
 **/
public class CommonEnum {

    /**
     * 操作状态
     */
    public enum OperationFlag {
        /**
         * 新增
         */
        ADD("新增"),

        /**
         * 修改
         */
        AUPT("修改"),

        /**
         * 修改
         */
        DEL("删除");


        private String value;

        OperationFlag(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }
}
