package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.cisco.security.SecurityCiscoASA99Impl;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/6/21
 */
public class TranslationCommandlineCiscoAsa99 extends TranslationCommandline {

    public TranslationCommandlineCiscoAsa99() {
        this.generatorBean = new SecurityCiscoASA99Impl();
    }

}
