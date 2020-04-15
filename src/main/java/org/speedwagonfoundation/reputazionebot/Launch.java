package org.speedwagonfoundation.reputazionebot;

import org.speedwagonfoundation.reputazionebot.system.ReputazioneBot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Launch {
    public static void main(String[] args){
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try{
            botsApi.registerBot(new ReputazioneBot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
}