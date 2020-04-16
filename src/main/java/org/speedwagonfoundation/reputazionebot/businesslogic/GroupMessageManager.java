package org.speedwagonfoundation.reputazionebot.businesslogic;

import org.apache.commons.lang3.StringUtils;
import org.speedwagonfoundation.reputazionebot.businesslogic.constants.CommandConstants;
import org.speedwagonfoundation.reputazionebot.businesslogic.usersmanagement.UserManager;
import org.speedwagonfoundation.reputazionebot.system.ReputazioneBot;
import org.speedwagonfoundation.reputazionebot.system.log.Log;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class GroupMessageManager {

    private static Integer lastReputationUpMessage;

    public static SendMessage manageMessageFromGroup(Update update) {
        SendMessage message = null;
        if(update.getMessage().hasText()) {
            if (CommandConstants.INCREASE_REPUTATION.equals(update.getMessage().getText())
                    && update.getMessage().getReplyToMessage() != null){
                message = new SendMessage();
                if(update.getMessage().getFrom().getId().equals(update.getMessage().getReplyToMessage().getFrom().getId())){
                    message
                            .setChatId(update.getMessage().getChatId())
                            .setText("Non si bara!");
                    Log.logSelfReputation(update.getMessage().getFrom());
                } else{
                    StringBuilder builder = new StringBuilder();
                    builder
                            .append("@")
                            .append(update.getMessage().getFrom().getUserName())
                            .append(" ha aumentato la reputazione di: @")
                            .append(update.getMessage().getReplyToMessage().getFrom().getUserName())
                            .append(" (")
                            .append(UserManager.scoreUp(update.getMessage().getReplyToMessage().getFrom()))
                            .append(").");
                    message
                            .setChatId(update.getMessage().getChatId())
                            .setText(builder.toString());
                    Log.logReputationUp(update.getMessage().getReplyToMessage().getFrom(), update.getMessage().getFrom());
                }
            } else if(CommandConstants.PROFILE.equals(update.getMessage().getText())) {
                message = new SendMessage()
                    .setChatId(update.getMessage().getChatId())
                    .setText(UserManager.getOrCreateUserTracker(update.getMessage().getFrom()).toString());
                Log.log("L'utente " + update.getMessage().getFrom().getUserName() + " [ID: " + update.getMessage().getFrom().getId()
                        + "] ha richiesto il suo profilo");
            }else if(CommandConstants.RANKING.equals(update.getMessage().getText())) {
                message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText(UserManager.getRanking());
                Log.log("L'utente " + update.getMessage().getFrom().getUserName() + " [ID: " + update.getMessage().getFrom().getId()
                    + "] ha richiesto la classifica della reputazione");
            }else if(StringUtils.startsWithAny(update.getMessage().getText(), CommandConstants.ADMIN_COMMANDS)){
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
        }
        return message;
    }

    private static SendMessage manageAdminCommands(Update update) {
        SendMessage message = null;
        if(StringUtils.startsWith(update.getMessage().getText(), CommandConstants.SET_POINTS)
                && update.getMessage().getReplyToMessage() != null) {
            message = new SendMessage();
            String[] splitMessage = update.getMessage().getText().split(" ");
            if(splitMessage.length == 2 && StringUtils.isNumeric(splitMessage[1])){
                UserManager.setScore(update.getMessage().getReplyToMessage().getFrom(), Long.parseLong(splitMessage[1]));
                message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("Reputazione di @" + update.getMessage().getReplyToMessage().getFrom().getUserName() + " modificata a " + splitMessage[1]);
                Log.logReputationChange(update.getMessage().getReplyToMessage().getFrom(), update.getMessage().getFrom(), Long.parseLong(splitMessage[1]));
            }
        } else if(StringUtils.startsWith(update.getMessage().getText(), CommandConstants.ADD_POINTS)
                && update.getMessage().getReplyToMessage() != null){
            String[] splitMessage = update.getMessage().getText().split(" ");
            if(splitMessage.length == 2 && StringUtils.isNumeric(splitMessage[1])){
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
