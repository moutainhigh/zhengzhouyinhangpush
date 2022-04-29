package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.checkpoint.security.SecurityCheckpointImpl;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/6/21
 */
public class TranslationCommandlineCheckPoint extends TranslationCommandline {

    public TranslationCommandlineCheckPoint() {
        this.generatorBean = new SecurityCheckpointImpl();
    }

}
