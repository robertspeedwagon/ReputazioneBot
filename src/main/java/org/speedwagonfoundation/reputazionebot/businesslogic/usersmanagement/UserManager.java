package org.speedwagonfoundation.reputazionebot.businesslogic.usersmanagement;

import java.util.HashMap;

public class UserManager {
    private final static HashMap<Integer, UserTracker> userScores;

    static {
        userScores = new HashMap<>();
    }

    public static Long scoreUp(Integer userId){
        UserTracker tracker = userScores.get(userId);
        if(tracker == null){
            tracker = new UserTracker(userId);
            userScores.put(userId, tracker);
        }
        return tracker.scoreUp();
    }
}
