package org.speedwagonfoundation.reputazionebot.businesslogic.administration;

import org.speedwagonfoundation.reputazionebot.system.log.Log;
import org.telegram.telegrambots.meta.api.objects.ChatMember;

import java.io.Serializable;
import java.util.ArrayList;

public class AdminManager {
    private ArrayList<Integer> adminIds;

    public void init(ArrayList<ChatMember> getAdminsResult) {
        if(getAdminsResult != null){
            adminIds = new ArrayList<>();
            getAdminsResult
                    .forEach(user -> adminIds.add(user.getUser().getId()));
        }
        Log.log("Elenco amministratori inizializzato.");
    }

    public boolean isAdministrator(Integer id) {
        return adminIds != null ? adminIds.contains(id) : false;
    }
}
