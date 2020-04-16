package org.speedwagonfoundation.reputazionebot.businesslogic.usersmanagement;

import org.dizitart.no2.*;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.User;

public class UserManager {
    private static final Nitrite userDatabase;
    private static final ObjectRepository<UserTracker> userCollection;

    static {
        userDatabase = Nitrite.builder()
                .compressed()
                .filePath("database/test.db")
                //.filePath("database/prod.db")
                .openOrCreate();
        userCollection = userDatabase.getRepository(UserTracker.class);
    }

    public static Long scoreUp(User user){
        UserTracker tracker = getOrCreateUser(user);
        Long score = tracker.scoreUp();
        userCollection.update(tracker);
        userDatabase.commit();
        return score;
    }

    @NotNull
    public static UserTracker getOrCreateUser(User user) {
        UserTracker tracker = userCollection.find(ObjectFilters.eq("userId", user.getId())).firstOrDefault();
        if(tracker == null){
            tracker = new UserTracker(user.getId(), user.getUserName());
            userCollection.insert(tracker);
        }
        return tracker;
    }

    public static void setScore(User user, Long newScore) {
        UserTracker tracker = getOrCreateUser(user);
        tracker.setScore(newScore);
        userCollection.update(tracker);
        userDatabase.commit();
    }

    public static Long addScore(User user, Long addScore) {
        UserTracker tracker = getOrCreateUser(user);
        setScore(user,tracker.getScore() + addScore);
        return tracker.getScore() + addScore;
    }
}
