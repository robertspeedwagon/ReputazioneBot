package org.speedwagonfoundation.reputazionebot.businesslogic.usersmanagement;

import org.dizitart.no2.*;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

public class UserManager {
    private final static Nitrite userDatabase;

    static {
        userDatabase = Nitrite.builder()
                .compressed()
                .filePath("database/test.db")
                .openOrCreate("user", "password");
    }

    public static Long scoreUp(Integer userId){
        ObjectRepository<UserTracker> userCollection = userDatabase.getRepository(UserTracker.class);
        UserTracker tracker = userCollection.find(ObjectFilters.eq("userId", userId)).firstOrDefault();
        if(tracker == null){
            tracker = new UserTracker(userId);
            userCollection.insert(tracker);
        }
        Long score = tracker.scoreUp();
        userCollection.update(tracker);
        userDatabase.commit();
        return score;
    }
}
