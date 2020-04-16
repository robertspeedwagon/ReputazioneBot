package org.speedwagonfoundation.reputazionebot.businesslogic.usersmanagement;

import org.dizitart.no2.*;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class UserManager {
    private static final Nitrite userDatabase;
    private static final ObjectRepository<UserTracker> userCollection;

    private static final Timer deleteTimer;

    static {
        userDatabase = Nitrite.builder()
                .compressed()
                .filePath("database/test.db")
                //.filePath("database/prod.db")
                .openOrCreate();
        userCollection = userDatabase.getRepository(UserTracker.class);
        deleteTimer = new Timer("removeDeletedUsers", true);
        deleteTimer.schedule(new RemoveExitedUsers(), 0, 120 * 1000);
    }

    public static Long scoreUp(User user){
        UserTracker tracker = getOrCreateUserTracker(user);
        Long score = tracker.scoreUp();
        userCollection.update(tracker);
        userDatabase.commit();
        return score;
    }

    @NotNull
    public static UserTracker getOrCreateUserTracker(User user) {
        UserTracker tracker = userCollection.find(ObjectFilters.eq("userId", user.getId())).firstOrDefault();
        if(tracker == null){
            tracker = new UserTracker(user.getId(), user.getUserName());
            userCollection.insert(tracker);
        }
        return tracker;
    }

    public static void setScore(User user, Long newScore) {
        UserTracker tracker = getOrCreateUserTracker(user);
        tracker.setScore(newScore);
        userCollection.update(tracker);
        userDatabase.commit();
    }

    public static Long addScore(User user, Long addScore) {
        UserTracker tracker = getOrCreateUserTracker(user);
        setScore(user,tracker.getScore() + addScore);
        return tracker.getScore() + addScore;
    }

    public static String getRanking() {
        AtomicInteger i = new AtomicInteger(1);
        StringBuilder sb = new StringBuilder("Classifica dei punteggi: ");
        userCollection
                .find(FindOptions.sort("score", SortOrder.Descending).thenLimit(0, 10))
                .forEach(userTracker -> sb.append("\n").append(buildRank(i.getAndIncrement(), userTracker)));
        return sb.toString();
    }

    private static String buildRank(int position, UserTracker userTracker) {
        return position + ". - @" + userTracker.getUsername() + " (" + userTracker.getScore() + ")";
    }

    public static void removeUser(User leftChatMember) {
        UserTracker utenteRimosso = getOrCreateUserTracker(leftChatMember);
        utenteRimosso.setQuitOn(Date.from(Instant.now()));
        userCollection.update(utenteRimosso);
        userDatabase.commit();
    }

    public static void addUser(User user) {
        UserTracker userTracker = getOrCreateUserTracker(user);
        if(userTracker.getQuitOn() != null){
            userTracker.setQuitOn(null);
            userCollection.update(userTracker);
        }else{
            userCollection.insert(userTracker);
        }
        userDatabase.commit();
    }

    private static class RemoveExitedUsers extends TimerTask {
        @Override
        public void run() {
            Date oneWeekAgo = Date.from(Instant.now().minus(7, ChronoUnit.DAYS));
            WriteResult result = userCollection.remove(ObjectFilters.lt("quitOn", oneWeekAgo));
            result.forEach(nitriteId -> System.out.println("Removed user with NitriteId " + nitriteId.toString()));
            userDatabase.commit();
        }
    }
}
