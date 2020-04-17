package org.speedwagonfoundation.reputazionebot.system;

import org.speedwagonfoundation.reputazionebot.businesslogic.GroupMessageManager;
import org.speedwagonfoundation.reputazionebot.businesslogic.administration.AdminManager;
import org.speedwagonfoundation.reputazionebot.businesslogic.constants.CommandConstants;
import org.speedwagonfoundation.reputazionebot.system.log.Log;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
            if(update != null && update.hasMessage() && (update.getMessage().getChat().isSuperGroupChat() || update.getMessage().getChat().isGroupChat())) {
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
                    Log.logException(e);
                }
            } else if(update.hasMessage() && update.getMessage().getChat().isUserChat()){
                if(update.getMessage().getText().equals(CommandConstants.INFO)){
                    SendMessage response = new SendMessage()
                            .setChatId(update.getMessage().getChatId())
                            .setText("Bot sviluppato da @Muso97 per Hentai Club" +
                                    "\nCodice sorgente: https://github.com/robertspeedwagon/ReputazioneBot");
                    Log.log(update.getMessage().getFrom().getUserName() + " [ID: " + update.getMessage().getFrom().getId() + "] ha richiesto le info sul bot");
                    try {
                        execute(response);
                    } catch (TelegramApiException e) {
                        Log.logException(e);
                    }
                } else if(update.getMessage().getText().equals(CommandConstants.COMMANDS)){
                    StringBuilder sb = new StringBuilder("Lista comandi:\n");
                    sb.append("_Comandi per chat privata:_\n")
                        .append("/info: Mostra informazioni sul bot\n")
                        .append("/comandi: Mostra questa guida\n\n")
                        .append("_Comandi per il gruppo:_\n")
                        .append("/profilo: Mostra il profilo dell'utente\n")
                        .append("/classifica: Mostra i 10 utenti con più reputazione");
                    if(adminManager.isAdministrator(update.getMessage().getFrom().getId())){
                        sb.append("\n\n_Comandi di amministrazione per il gruppo:_\n")
                            .append("/setpoints *\\<punti\\>*: Usato in risposta ad un messaggio imposta il punteggio della persona che ha inviato quel messaggio al numero di punti specificato\n")
                            .append("/addpoints *\\<punti\\>*: Usato in risposta ad un messaggio aumenta il punteggio della persona che ha inviato quel messaggio del numero di punti specificato\n");
                        Log.logCommands(update.getMessage().getFrom(), true);
                    } else{
                        Log.logCommands(update.getMessage().getFrom(), false);
                    }
                    try {
                        execute(new SendMessage().enableMarkdownV2(true).setChatId(update.getMessage().getChatId()).setText(sb.toString()));
                    } catch (TelegramApiException e) {
                        Log.logException(e);
                    }
                }
            }
        } catch(Exception e){
            Log.logException(e);
            throw e;
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
