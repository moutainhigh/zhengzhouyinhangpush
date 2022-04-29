package com.abtnetworks.totems.credential.entity;

import com.abtnetworks.totems.common.tools.excel.ExcelField;
import org.apache.commons.lang3.StringUtils;

public class ExcelCredentialEntity {

    String name;

    String loginName;

    String loginPassword;

    String enableUserName;

    String enablePassword;


    @ExcelField(title="凭据名",type = 2,align=2, sort=0)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ExcelField(title="用户名",type = 2,align=2, sort=5)
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    @ExcelField(title="密码",type = 2,align=2, sort=10)
    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    @ExcelField(title="通行用户名",type = 2,align=2, sort=15)
    public String getEnableUserName() {
        return enableUserName;
    }

    public void setEnableUserName(String enableUserName) {
        this.enableUserName = enableUserName;
    }

    @ExcelField(title="通行密码",type = 2,align=2, sort=20)
    public String getEnablePassword() {
        return enablePassword;
    }

    public void setEnablePassword(String enablePassword) {
        this.enablePassword = enablePassword;
    }

    public boolean isEmpty() {
        if(StringUtils.isEmpty(name) && StringUtils.isEmpty(loginName)
                && StringUtils.isEmpty(loginPassword)) {
            return true;
        }
        return false;
    }

}
