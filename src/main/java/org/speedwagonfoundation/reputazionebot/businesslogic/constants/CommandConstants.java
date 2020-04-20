package org.speedwagonfoundation.reputazionebot.businesslogic.constants;

import org.apache.commons.lang3.StringUtils;

public interface CommandConstants {
    // Comandi del gruppo
    String INCREASE_REPUTATION = "+";
    String SET_POINTS = "/setpoints";
    String ADD_POINTS = "/addpoints";
    String PROFILE = "/profilo";
    String RANKING = "/classifica";
    // Comandi messsaggio privato bot
    String INFO = "/info";
    String COMMANDS = "/comandi";
    String MESSAGE = "/msg";

    String[] ADMIN_COMMANDS = new String[]{ SET_POINTS, ADD_POINTS, MESSAGE };
}
