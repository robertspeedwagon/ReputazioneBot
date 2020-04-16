package org.speedwagonfoundation.reputazionebot.businesslogic.usersmanagement;

import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

@Indices({
        @Index(value = "userId", type = IndexType.Unique)
})
public class UserTracker implements Serializable {
    @Id
    private Integer userId;
    private Long score;
    private String username;
    private Date quitOn;

    public UserTracker() {}

    public UserTracker(Integer userId, String username){
        this.userId = userId;
        this.username = username;
        this.score = 0L;
        quitOn = null;
    }

    public Long scoreUp() {
        return ++score;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Profilo dell'utente @");
        sb.append(username)
            .append(" [ID: ")
            .append(userId)
            .append("]:\n")
            .append("Punteggio: ")
            .append(score);
        return sb.toString();
    }

    public Date getQuitOn() {
        return quitOn;
    }

    public void setQuitOn(Date quitOn) {
        this.quitOn = quitOn;
    }
}
