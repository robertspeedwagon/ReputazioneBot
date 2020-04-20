package org.speedwagonfoundation.reputazionebot.businesslogic;

import org.apache.commons.lang3.StringUtils;
import org.speedwagonfoundation.reputazionebot.businesslogic.constants.CommandConstants;
import org.speedwagonfoundation.reputazionebot.system.ReputazioneBot;
import org.speedwagonfoundation.reputazionebot.system.log.Log;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UserMessageManager {
    public static SendMessage manageUserMessage(Update update) throws Exception{
        SendMessage response = null;
        if(CommandConstants.INFO.equals(update.getMessage().getText())){
            response = new SendMessage()
                .setChatId(update.getMessage().getChatId())
                .setText("Bot sviluppato da @Muso97 per Hentai Club" +
                    "\nCodice sorgente: https://github.com/robertspeedwagon/ReputazioneBot");
            Log.log(update.getMessage().getFrom().getUserName() + " [ID: " + update.getMessage().getFrom().getId() + "] ha richiesto le info sul bot");
        } else if(CommandConstants.COMMANDS.equals(update.getMessage().getText())){
            StringBuilder sb = new StringBuilder("Lista comandi:\n");
            sb.append("_Comandi per chat privata:_\n")
                .append("/info: Mostra informazioni sul bot\n")
                .append("/comandi: Mostra questa guida\n\n")
                .append("_Comandi per il gruppo:_\n")
                .append("/profilo: Mostra il profilo dell'utente\n")
                .append("/classifica: Mostra i 10 utenti con più reputazione");
            if(ReputazioneBot.adminManager.isAdministrator(update.getMessage().getFrom().getId())){
                sb.append("\n\n_Comandi di amministrazione per il gruppo:_\n")
                    .append("/setpoints *\\<punti\\>*: Usato in risposta ad un messaggio imposta il punteggio della persona che ha inviato quel messaggio al numero di punti specificato\n")
                    .append("/addpoints *\\<punti\\>*: Usato in risposta ad un messaggio aumenta il punteggio della persona che ha inviato quel messaggio del numero di punti specificato\n");
                Log.logCommands(update.getMessage().getFrom(), true);
            } else{
                Log.logCommands(update.getMessage().getFrom(), false);
            }
            response = new SendMessage().enableMarkdownV2(true).setChatId(update.getMessage().getChatId()).setText(sb.toString());
        } else if(ReputazioneBot.adminManager.isAdministrator(update.getMessage().getFrom().getId()) && StringUtils.startsWith(update.getMessage().getText(), CommandConstants.MESSAGE)) {
            response = new SendMessage();
            response
                .setChatId(ReputazioneBot.config.getProperty("telegram.chatid"))
                .setText(StringUtils.substringAfter(update.getMessage().getText(), CommandConstants.MESSAGE + " "));
            Log.log("Messaggio inviato al gruppo: " + StringUtils.substringAfter(update.getMessage().getText(), CommandConstants.MESSAGE + " "));
        }
        return response;
    }
}
