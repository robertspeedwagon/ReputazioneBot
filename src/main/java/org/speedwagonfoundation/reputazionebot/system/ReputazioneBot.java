package org.speedwagonfoundation.reputazionebot.system;

import org.speedwagonfoundation.reputazionebot.businesslogic.GroupMessageManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ReputazioneBot extends TelegramLongPollingBot {


    @Override
    public void onUpdateReceived(Update update) {
        if(update.getMessage().getChat().isSuperGroupChat() || update.getMessage().getChat().isGroupChat()) {
            try {
                SendMessage response = GroupMessageManager.manageMessageFromGroup(update);
                execute(response);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if(update.getMessage().getChat().isUserChat()){
            // Future
        }
    }

    @Override
    public String getBotUsername() {
        return "HCReputazioneBot";
    }

    @Override
    public String getBotToken() {
        return "917826203:AAE3qG7dmQd2w-tgyCvf28t3eT1AZ8NTAGs";
    }
}
