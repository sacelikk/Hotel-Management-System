package org.apache.maven.archetypes.hotelflow.Model;

import java.time.LocalDateTime;

public class ClientRating {
    private Long ratingId;
    private Long clientId;
    private Long hotelId;
    private int score;
    private String comment;
    private LocalDateTime ratingDate;

    public ClientRating() {
        this.ratingDate = LocalDateTime.now();
    }

    public ClientRating(Long ratingId, Long clientId, Long hotelId, int score, String comment) {
        this.ratingId = ratingId;
        this.clientId = clientId;
        this.hotelId = hotelId;
        this.score = score;
        this.comment = comment;
        this.ratingDate = LocalDateTime.now();
    }

    public boolean validateScore() {
        return this.score >= 1 && this.score <= 5;
    }

    public Long getRatingId() { return ratingId; }
    public void setRatingId(Long ratingId) { this.ratingId = ratingId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getRatingDate() { return ratingDate; }
    public void setRatingDate(LocalDateTime ratingDate) { this.ratingDate = ratingDate; }
}