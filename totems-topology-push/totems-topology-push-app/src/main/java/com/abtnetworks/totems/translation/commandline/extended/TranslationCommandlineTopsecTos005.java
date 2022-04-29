package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.topsec.TOS_005.SecurityTopsec005Impl;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/6/21
 */
public class TranslationCommandlineTopsecTos005 extends TranslationCommandline {

    public TranslationCommandlineTopsecTos005() {
        this.generatorBean = new SecurityTopsec005Impl();
    }

}
