package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.hillstone.security.SecurityHillStoneR5Impl;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/5/26
 */
public class TranslationCommandlineHillstone extends TranslationCommandline {

    public TranslationCommandlineHillstone() {
        this.generatorBean = new SecurityHillStoneR5Impl();
    }

}
