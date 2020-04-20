package org.speedwagonfoundation.reputazionebot.businesslogic.usersmanagement;

import org.apache.commons.lang3.StringUtils;
import org.dizitart.no2.*;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.jetbrains.annotations.NotNull;
import org.speedwagonfoundation.reputazionebot.system.ReputazioneBot;
import org.speedwagonfoundation.reputazionebot.system.log.Log;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.File;
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
        new File(StringUtils.substringBeforeLast(ReputazioneBot.config.getProperty("db.path"), "/")).mkdirs();
        userDatabase = Nitrite.builder()
                .compressed()
                .filePath(ReputazioneBot.config.getProperty("db.path"))
                .openOrCreate();
        userCollection = userDatabase.getRepository(UserTracker.class);
        deleteTimer = new Timer("removeDeletedUsers", true);
        deleteTimer.schedule(new RemoveExitedUsers(), 0, 60 * 60 * 1000);
        Log.log("Database utenti caricato");
    }

    public static Long scoreUp(User user) {
        UserTracker tracker = getOrCreateUserTracker(user);
        Long score = tracker.scoreUp();
        userCollection.update(tracker);
        userDatabase.commit();
        return score;
    }

    @NotNull
    public static UserTracker getOrCreateUserTracker(User user) {
        UserTracker tracker = userCollection.find(ObjectFilters.eq("userId", user.getId())).firstOrDefault();
        if (tracker == null) {
            tracker = new UserTracker(user.getId(), user.getUserName());
            userCollection.insert(tracker);
        } else if(user != null && user.getUserName() != null && !user.getUserName().equals(tracker.getUsername())){
            tracker.setUsername(user.getUserName());
            userCollection.update(tracker);
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
        setScore(user, tracker.getScore() + addScore);
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
        removeUser(leftChatMember, true);
    }

    public static void removeUser(User leftChatMember, boolean logRemoval) {
        UserTracker removedUser = getOrCreateUserTracker(leftChatMember);
        removedUser.setQuitOn(Date.from(Instant.now()));
        userCollection.update(removedUser);
        userDatabase.commit();
        if(logRemoval) {
            Log.log("Utente " + leftChatMember.getUserName() + " [ID: " + leftChatMember.getId() + " uscito dal gruppo.");
        }
    }

    public static void addUser(User user) {
        UserTracker userTracker = getOrCreateUserTracker(user);
        if (userTracker.getQuitOn() != null) {
            userTracker.setQuitOn(null);
            userCollection.update(userTracker);
        }
        userDatabase.commit();
        Log.log("Utente " + user.getUserName() + " [ID: " + user.getId() + "] entrato nel gruppo.");
    }

    public static void removeUser(User leftChatMember, User kickedBy) {
        removeUser(leftChatMember, false);
        Log.log("Utente " + leftChatMember.getUserName() + " [ID: " + leftChatMember.getId() + "] rimosso dal gruppo da " + kickedBy.getUserName() + " [ID: " + kickedBy.getId() + "]");
    }

    private static class RemoveExitedUsers extends TimerTask {
        @Override
        public void run() {
            Date oneWeekAgo = Date.from(Instant.now().minus(Long.parseLong(ReputazioneBot.config.getProperty("misc.daysBeforeRemovingUser", "7")), ChronoUnit.DAYS));
            WriteResult result = userCollection.remove(ObjectFilters.lt("quitOn", oneWeekAgo));
            result.forEach(nitriteId -> Log.log("Rimosso definitivamente l'utente con il NitriteId: " + nitriteId.toString()));
            if(result.getAffectedCount() > 0 || userDatabase.hasUnsavedChanges()){
                userDatabase.commit();
            }
        }
    }
}
