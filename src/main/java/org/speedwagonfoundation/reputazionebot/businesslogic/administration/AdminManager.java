package org.speedwagonfoundation.reputazionebot.businesslogic.administration;

import org.telegram.telegrambots.meta.api.objects.ChatMember;

import java.io.Serializable;
import java.util.ArrayList;

public class AdminManager {
    private ArrayList<Integer> adminIds;
    private boolean initialized = false;

    public boolean isInitialized() {
        return initialized;
    }

    public void init(ArrayList<ChatMember> getAdminsResult) {
        if(getAdminsResult != null){
            adminIds = new ArrayList<>();
            getAdminsResult
                    .forEach(user -> adminIds.add(((ChatMember)user).getUser().getId()));
            initialized = true;
        }
    }

    public boolean isAdministrator(Integer id) {
        return adminIds.contains(id);
    }
}
