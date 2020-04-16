package org.speedwagonfoundation.reputazionebot.businesslogic.usersmanagement;

import org.dizitart.no2.IndexType;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;

import java.io.Serializable;

@Indices({
        @Index(value = "userId", type = IndexType.Unique)
})
public class UserTracker implements Serializable {
    @Id
    private Integer userId;
    private Long score;
    private String username;

    public UserTracker() {}

    public UserTracker(Integer userId, String username){
        this.userId = userId;
        this.username = username;
        this.score = 0L;
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
}
