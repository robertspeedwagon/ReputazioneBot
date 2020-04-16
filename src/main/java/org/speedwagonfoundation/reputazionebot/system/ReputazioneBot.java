package org.speedwagonfoundation.reputazionebot.system;

import org.speedwagonfoundation.reputazionebot.businesslogic.GroupMessageManager;
import org.speedwagonfoundation.reputazionebot.businesslogic.administration.AdminManager;
import org.speedwagonfoundation.reputazionebot.businesslogic.constants.CommandConstants;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ReputazioneBot extends TelegramLongPollingBot {

    public static AdminManager adminManager;

    public ReputazioneBot(){
        adminManager = new AdminManager();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.getMessage().getChat().isSuperGroupChat() || update.getMessage().getChat().isGroupChat()) {
            if(!adminManager.isInitialized()){
                try {
                    adminManager.init(execute(new GetChatAdministrators().setChatId(update.getMessage().getChat().getId())));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            try {
                SendMessage response = GroupMessageManager.manageMessageFromGroup(update);
                if(response != null){
                    Message telegramResp = execute(response);
                    if(update.getMessage() != null && CommandConstants.INCREASE_REPUTATION.equals(update.getMessage().getText())){
                        if(GroupMessageManager.getLastReputationUpMessage() != null) {
                            execute(new DeleteMessage(update.getMessage().getChatId(), GroupMessageManager.getLastReputationUpMessage()));
                        }
                        GroupMessageManager.setLastReputationUpMessage(telegramResp.getMessageId());
                    }
                }
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if(update.getMessage().getChat().isUserChat()){
            if(update.getMessage().getText().equals(CommandConstants.INFO)){
                SendMessage response = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("Bot sviluppato da @Muso97 per Hentai Club" +
                                "\nCodice sorgente: https://github.com/robertspeedwagon/ReputazioneBot");
                try {
                    execute(response);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
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
