package org.speedwagonfoundation.reputazionebot.businesslogic.usersmanagement;

public class UserTracker {
    Integer userId;
    Long score;

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
}
