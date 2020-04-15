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
    Integer userId;
    Long score;

    public UserTracker() {}

    public UserTracker(Integer userId){
        this.userId = userId;
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
}
