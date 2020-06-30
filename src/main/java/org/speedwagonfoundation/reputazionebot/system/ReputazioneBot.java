package org.speedwagonfoundation.reputazionebot.system;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.speedwagonfoundation.reputazionebot.businesslogic.GroupMessageManager;
import org.speedwagonfoundation.reputazionebot.businesslogic.UserMessageManager;
import org.speedwagonfoundation.reputazionebot.businesslogic.administration.AdminManager;
import org.speedwagonfoundation.reputazionebot.businesslogic.constants.CommandConstants;
import org.speedwagonfoundation.reputazionebot.system.log.Log;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ReputazioneBot extends TelegramLongPollingBot {

    public static AdminManager adminManager;
    public static Properties config;

    public ReputazioneBot() throws IOException {
        config = new Properties();
        Log.log("Avvio di ReputazioneBot in corso...");
        adminManager = new AdminManager();
        try {
            config.load(new FileReader("config.properties"));
            if(config.get("telegram.apikey") == null || config.get("telegram.username") == null){
                Log.logError("Parametri telegram.apikey e/o telegram.username non impostati in config.properties! Esco dal bot!");
                System.exit(2);
            }
        } catch (FileNotFoundException e){
            Log.logError("File config.properties non trovato! Esco dal bot!");
            System.exit(1);
        }
        if(config.getProperty("telegram.chatid") == null){
            Log.logWarning("Parametro telegram.chatid non impostato in config.properties! " +
                    "Il bot funzionerà ma i comandi di amministrazione non saranno disponibili!");
        } else{
            try {
                adminManager.init(execute(new GetChatAdministrators().setChatId(config.getProperty("telegram.chatid"))));
            } catch (TelegramApiException e) {
                Log.logException(e);
            }
        }
        Log.log("ReputazioneBot caricato.");
    }

    @Override
    public void onUpdateReceived(Update update) {
        // TODO: Trovare un metodo migliore per loggare le eccezioni rispetto a rinchiudere tutto il bot in un try
        try {
            if(update != null && update.hasMessage() && (update.getMessage().getChat().isSuperGroupChat() || update.getMessage().getChat().isGroupChat()) && update.getMessage().getChatId().toString().equals(config.getProperty("telegram.chatid"))) {
                PartialBotApiMethod<Message> response = GroupMessageManager.manageMessageFromGroup(update);
                if(response != null){
                    Message telegramResp;
                    if(response instanceof SendAnimation){
                         telegramResp = execute((SendAnimation) response);
                    } else if(response instanceof SendPhoto){
                        telegramResp = execute((SendPhoto)response);
                    } else if(response instanceof SendVideo){
                        telegramResp = execute((SendVideo)response);
                    } else {
                        telegramResp = execute((SendMessage)response);
                    }
                    if(update.getMessage() != null && GroupMessageManager.isRepIncrease(update.getMessage().getText()) && response instanceof SendMessage){
                        if(GroupMessageManager.getLastReputationUpMessage() != null) {
                            execute(new DeleteMessage(update.getMessage().getChatId(), GroupMessageManager.getLastReputationUpMessage()));
                        }
                        GroupMessageManager.setLastReputationUpMessage(telegramResp.getMessageId());
                    }
                }
            } else if(update != null && update.hasMessage() && update.getMessage().getChat().isUserChat()){
                SendMessage message = UserMessageManager.manageUserMessage(update);
                if(message != null){
                    execute(message);
                }
            }
        } catch(Exception e){
            Log.logException(e);
            reportErrorToDeveloper(e);
        }
    }

    public void reportErrorToDeveloper(Exception e) {
        SendMessage message = new SendMessage()
            .setText("Maestro, si è verificato un errore! Qui i dettagli:\n" + ExceptionUtils.getStackTrace(e))
            .setChatId(config.getProperty("debug.developertelegramid"));
        try {
            execute(message);
        } catch (TelegramApiException telegramApiException) {
            Log.logException(e, "Errore nell'invio del messaggio di segnalazione. Brutta giornata eh, maestro Speedwagon?");
        }
    }

    @Override
    public String getBotUsername() {
        return config.getProperty("telegram.username");
    }

    @Override
    public String getBotToken() {
        return config.getProperty("telegram.apikey");
    }
}
