package org.speedwagonfoundation.reputazionebot.system.log;

import org.speedwagonfoundation.reputazionebot.system.ReputazioneBot;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private static PrintWriter logger;

    private static final String PREFIX_INFO = "[INFO]";
    private static final String PREFIX_WARNING = "[WARNING]";
    private static final String PREFIX_ERROR = "[ERROR]";

    private static final DateFormat FORMATTER;

    static {
        FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            // TODO: Replace with logfolder maybe?
            File logFile = new File(ReputazioneBot.config.getProperty("misc.logfile", "reputazionebot.log"));
            logFile.createNewFile();
            logger = new PrintWriter(logFile);
        } catch (IOException e) {
            Log.logException(e);
        }
    }

    public static void log(String text) {
        System.out.println(PREFIX_INFO + FORMATTER.format(new Date()) + " - " + text);
        logger.println(PREFIX_INFO + FORMATTER.format(new Date()) + " - " + text);
        logger.flush();
    }

    public static void logWarning(String text) {
        System.err.println(PREFIX_WARNING + FORMATTER.format(new Date()) + " - " + text);
        logger.println(PREFIX_WARNING + FORMATTER.format(new Date()) + " - " + text);
        logger.flush();
    }

    public static void logError(String text) {
        System.err.println(PREFIX_ERROR + FORMATTER.format(new Date()) + " - " + text);
        logger.println(PREFIX_ERROR + FORMATTER.format(new Date()) + " - " + text);
        logger.flush();
    }

    public static void logAccessDenied(User user){
        logWarning(user.getUserName() + " [ID: " +user.getId() + "] ha tentato di usare un comando di amministrazione ma non è stato autorizzato!");
    }

    public static void logReputationUp(User originalPoster, User replier){
        log(replier.getUserName() + " [ID: " + replier.getId() + "] ha aumentato la reputazione di " + originalPoster.getUserName()  + " [ID: " + originalPoster.getId() + "]");
    }

    public static void logSelfReputation(User poster){
        log(poster.getUserName() + " [ID: " + poster.getId() + "] ha tentato di aumentarsi la reputazione da solo! Birbantello!");
    }

    public static void logException(Exception e){
        logError("Errore durate l'esecuzione del bot! Dettaglio errore: ");
        e.printStackTrace();
        e.printStackTrace(logger);
        logger.flush();
    }

    public static void logReputationChange(User changedReputation, User reputationChanger, long newReputation) {
        log(reputationChanger.getUserName() + " [ID: " + reputationChanger.getId() + "] " +
                "ha cambiato la reputazione di " + changedReputation.getUserName() + " [ID: " + changedReputation.getId() + "] a " + newReputation);
    }

    public static void logCommands(User user, boolean isAdmin) {
        if(isAdmin){
            log("Inviati comandi di amministrazione a: " + user.getUserName() + " [ID: " + user.getId() + "]");
        }else{
            log("Inviati comandi a: " + user.getUserName() + " [ID: " + user.getId() + "]");
        }
    }
}
