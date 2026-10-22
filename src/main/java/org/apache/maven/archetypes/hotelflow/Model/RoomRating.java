package org.apache.maven.archetypes.hotelflow.Model;



import java.time.LocalDateTime;

public class RoomRating {
    private Long ratingId;
    private Long roomId;
    private int score;
    private String comment;
    private LocalDateTime ratingDate;

    public RoomRating() {
        this.ratingDate = LocalDateTime.now();
    }

    public RoomRating(Long ratingId, Long roomId, int score, String comment) {
        this.ratingId = ratingId;
        this.roomId = roomId;
        this.score = score;
        this.comment = comment;
        this.ratingDate = LocalDateTime.now();
    }

    public boolean validateScore() {
        return this.score >= 1 && this.score <= 5;
    }

    public Long getRatingId() { return ratingId; }
    public void setRatingId(Long ratingId) { this.ratingId = ratingId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getRatingDate() { return ratingDate; }
    public void setRatingDate(LocalDateTime ratingDate) { this.ratingDate = ratingDate; }
}