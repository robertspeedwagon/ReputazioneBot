package org.speedwagonfoundation.reputazionebot.businesslogic;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.speedwagonfoundation.reputazionebot.businesslogic.constants.CommandConstants;
import org.speedwagonfoundation.reputazionebot.businesslogic.usersmanagement.UserManager;
import org.speedwagonfoundation.reputazionebot.system.ReputazioneBot;
import org.speedwagonfoundation.reputazionebot.system.log.Log;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GroupMessageManager {

    private static final String[] insulti;
    private static final Random rand;

    private static Integer lastReputationUpMessage;
    private static HashMap<Long, String> easterEggs;

    static{
        ArrayList<String> insultiArrayList = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("messages.txt"), StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                insultiArrayList.add(line.replace("\\n", "\n"));
            }
            if(insultiArrayList.isEmpty()){
                insultiArrayList.add("Non si bara!");
            }
        } catch (IOException e) {
            insultiArrayList.add("Non si bara!");
            Log.logError("Errore nell'accesso al file della lista degli insulti. Uso insulti di default.");
        }
        insulti = insultiArrayList.toArray(new String[0]);
        rand = new Random();
        easterEggs = initEasterEggs();
    }

    private static HashMap<Long, String> initEasterEggs() {
        HashMap<Long, String> easterEggs = new HashMap<>();
        String eggsFolder = "custom/eggs";
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(eggsFolder + "/eggs.json"), StandardCharsets.UTF_8);
            JSONTokener tokener = new JSONTokener(reader);
            JSONArray array = new JSONArray(tokener);
            for(int i = 0; i < array.length(); i++){
                JSONObject obj = array.getJSONObject(i);
                easterEggs.put(obj.getLong("points"), eggsFolder + "/" + obj.getString("image"));
            }
        } catch (Exception e) {
            Log.logException(e, "Errore nell'inizializzazione degli easter egg. Dettagli sull'errore:");
            Log.logError("Easter egg disabilitati");
        }
        Log.log("Easter egg abilitati con successo");
        return easterEggs;
    }

    public static PartialBotApiMethod<Message> manageMessageFromGroup(Update update) throws Exception{
        SendMessage message = null;
        if(update.getMessage().hasText()) {
            String inText = update.getMessage().getText();
            if(inText.endsWith("@" + ReputazioneBot.config.getProperty("telegram.username"))){
                inText = StringUtils.substringBeforeLast(inText, "@");
            }
            if (CommandConstants.INCREASE_REPUTATION.equals(inText)
                    && update.getMessage().getReplyToMessage() != null){
                message = new SendMessage();
                if(update.getMessage().getFrom().getId().equals(update.getMessage().getReplyToMessage().getFrom().getId())){
                    message
                            .setChatId(update.getMessage().getChatId())
                            .setText(insulti[rand.nextInt(insulti.length)]);
                    Log.logSelfReputation(update.getMessage().getFrom());
                } else{
                    Long newScore = UserManager.scoreUp(update.getMessage().getReplyToMessage().getFrom());
                    StringBuilder builder = new StringBuilder();
                    builder
                        .append(update.getMessage().getFrom().getFirstName());
                    if(update.getMessage().getFrom().getLastName() != null) {
                        builder
                            .append(" ")
                            .append(update.getMessage().getFrom().getLastName());
                    }
                    builder
                        .append(" (")
                        .append(UserManager.getOrCreateUserTracker(update.getMessage().getFrom()).getScore())
                        .append(") ha aumentato la reputazione di: ")
                        .append(update.getMessage().getReplyToMessage().getFrom().getFirstName());
                        if(update.getMessage().getReplyToMessage().getFrom().getLastName() != null){
                            builder
                                .append(" ")
                                .append(update.getMessage().getReplyToMessage().getFrom().getLastName());
                        }

                    builder
                        .append(" (")
                        .append(newScore)
                        .append(").");
                    message
                        .setChatId(update.getMessage().getChatId())
                        .setText(builder.toString());
                    Log.logReputationUp(update.getMessage().getReplyToMessage().getFrom(), update.getMessage().getFrom());
                    if(easterEggs.containsKey(newScore)){
                        if(StringUtils.substringAfterLast(easterEggs.get(newScore), ".").equals("gif")){
                            SendAnimation sendAnimation = sendMessageToSendAnimation(message);
                            sendAnimation.setAnimation(new File(easterEggs.get(newScore)));
                            return sendAnimation;
                        } else if(StringUtils.substringAfterLast(easterEggs.get(newScore), ".").equals("mp4")){
                            SendVideo sendVideo = sendMessageToSendVideo(message);
                            sendVideo.setVideo(new File(easterEggs.get(newScore)));
                            return sendVideo;
                        } else {
                            SendPhoto sendPhoto = sendMessageToSendPhoto(message);
                            sendPhoto.setPhoto(new File(easterEggs.get(newScore)));
                            return sendPhoto;
                        }
                    }
                }
            } else if(CommandConstants.PROFILE.equals(inText)) {
                message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText(UserManager.getOrCreateUserTracker(update.getMessage().getFrom()).toString());
                Log.log("L'utente " + update.getMessage().getFrom().getUserName() + " [ID: " + update.getMessage().getFrom().getId()
                        + "] ha richiesto il suo profilo");
            }else if(CommandConstants.RANKING.equals(inText)) {
                message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText(UserManager.getRanking());
                Log.log("L'utente " + update.getMessage().getFrom().getUserName() + " [ID: " + update.getMessage().getFrom().getId()
                    + "] ha richiesto la classifica della reputazione");
            }else if(StringUtils.startsWithAny(inText, CommandConstants.ADMIN_COMMANDS)){
                if(ReputazioneBot.adminManager.isAdministrator(update.getMessage().getFrom().getId())){
                    message = manageAdminCommands(update);
                }else {
                    Log.logAccessDenied(update.getMessage().getFrom());
                }
            }
        } else if(update.getMessage().getLeftChatMember() != null) {
            if(update.getMessage().getLeftChatMember().getId().equals(update.getMessage().getFrom().getId())){
                UserManager.removeUser(update.getMessage().getLeftChatMember());
            } else {
                UserManager.removeUser(update.getMessage().getLeftChatMember(), update.getMessage().getFrom());
            }

        } else if(update.getMessage().getNewChatMembers() != null){
            update
                .getMessage()
                .getNewChatMembers()
                .forEach(user -> UserManager.addUser(user));
        } //else if(update.)
        return message;
    }

    private static SendVideo sendMessageToSendVideo(SendMessage message) {
        return new SendVideo()
            .setCaption(message.getText())
            .setChatId(message.getChatId());
    }

    private static SendPhoto sendMessageToSendPhoto(SendMessage message) {
        return new SendPhoto()
            .setCaption(message.getText())
            .setChatId(message.getChatId());
    }

    private static SendAnimation sendMessageToSendAnimation(SendMessage message) {
        return new SendAnimation()
            .setCaption(message.getText())
            .setChatId(message.getChatId());
    }

    private static SendMessage manageAdminCommands(Update update) {
        SendMessage message = null;
        if(StringUtils.startsWith(update.getMessage().getText(), CommandConstants.SET_POINTS)
                && update.getMessage().getReplyToMessage() != null) {
            message = new SendMessage();
            String[] splitMessage = update.getMessage().getText().split(" ");
            if(splitMessage.length == 2 && NumberUtils.isCreatable(splitMessage[1])){
                UserManager.setScore(update.getMessage().getReplyToMessage().getFrom(), Long.parseLong(splitMessage[1]));
                message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("Reputazione di @" + update.getMessage().getReplyToMessage().getFrom().getUserName() + " modificata a " + splitMessage[1]);
                Log.logReputationChange(update.getMessage().getReplyToMessage().getFrom(), update.getMessage().getFrom(), Long.parseLong(splitMessage[1]));
            }
        } else if(StringUtils.startsWith(update.getMessage().getText(), CommandConstants.ADD_POINTS)
                && update.getMessage().getReplyToMessage() != null){
            String[] splitMessage = update.getMessage().getText().split(" ");
            if(splitMessage.length == 2 && NumberUtils.isCreatable(splitMessage[1])){
                Long newScore = UserManager.addScore(update.getMessage().getReplyToMessage().getFrom(), Long.parseLong(splitMessage[1]));
                message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText("Reputazione di @" + update.getMessage().getReplyToMessage().getFrom().getUserName() + " modificata a " + newScore);
                Log.logReputationChange(update.getMessage().getReplyToMessage().getFrom(), update.getMessage().getFrom(), newScore);
            }
        }
        return message;
    }

    public static Integer getLastReputationUpMessage(){
        return lastReputationUpMessage;
    }

    public static void setLastReputationUpMessage(Integer msgId){
        lastReputationUpMessage = msgId;
    }
}