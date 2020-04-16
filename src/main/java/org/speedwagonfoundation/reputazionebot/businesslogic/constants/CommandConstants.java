package org.speedwagonfoundation.reputazionebot.businesslogic.constants;

import org.apache.commons.lang3.StringUtils;

public interface CommandConstants {
    String INCREASE_REPUTATION = "+";
    String INFO = "/info";
    String SET_POINTS = "/setpoints";
    String ADD_POINTS = "/addpoints";
    String PROFILE = "/profilo";
    String RANKING = "/classifica";
    String COMMANDS = "/comandi";
    String[] ADMIN_COMMANDS = new String[]{ SET_POINTS, ADD_POINTS };
}
