package org.speedwagonfoundation.reputazionebot;

import org.speedwagonfoundation.reputazionebot.system.ReputazioneBot;
import org.speedwagonfoundation.reputazionebot.system.log.Log;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;

public class Launch {
    public static void main(String[] args) throws IOException, TelegramApiRequestException {
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try{
            botsApi.registerBot(new ReputazioneBot());
        } catch (TelegramApiRequestException e) {
            Log.logException(e);
        }
    }
}
