package com.abtnetworks.totems.translation.commandline.extended;

import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.vender.Juniper.security.SecurityJuniperSRXImpl;

/**
 * @Author: WangCan
 * @Description
 * @Date: 2021/5/26
 */
public class TranslationCommandlineJuniperSRX extends TranslationCommandline {

    public TranslationCommandlineJuniperSRX() {
        this.generatorBean = new SecurityJuniperSRXImpl();
    }
}
