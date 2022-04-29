package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.h3c.security.SecurityH3cSecPathV5Impl;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/5/26
 */
public class TranslationCommandlineH3CV5 extends TranslationCommandline {

    public TranslationCommandlineH3CV5() {
        this.generatorBean = new SecurityH3cSecPathV5Impl();
    }
}
