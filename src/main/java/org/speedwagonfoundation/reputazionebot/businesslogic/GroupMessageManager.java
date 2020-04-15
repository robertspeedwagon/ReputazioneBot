package org.speedwagonfoundation.reputazionebot.businesslogic;

import org.speedwagonfoundation.reputazionebot.businesslogic.constants.CommandConstants;
import org.speedwagonfoundation.reputazionebot.businesslogic.usersmanagement.UserManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class GroupMessageManager {
    public static SendMessage manageMessageFromGroup(Update update) {
        SendMessage message = null;
        if(update.getMessage().hasText()
            && CommandConstants.increaseReputation.equals(update.getMessage().getText())
            && update.getMessage().getReplyToMessage() != null) {
            message = new SendMessage();
            StringBuilder builder = new StringBuilder()
                .append("@" + update.getMessage().getFrom().getUserName())
                .append(" ha aumentato la reputazione di: @" + update.getMessage().getReplyToMessage().getFrom().getUserName())
                .append(" (" + UserManager.scoreUp(update.getMessage().getReplyToMessage().getFrom().getId()) + ").");
            message
                .setChatId(update.getMessage().getChatId())
                .setText(builder.toString());
        }
        return message;
    }
}
