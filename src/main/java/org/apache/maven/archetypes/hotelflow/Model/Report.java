package org.apache.maven.archetypes.hotelflow.Model;



import java.time.LocalDateTime;

public class Report {
    private Long reportId;
    private String title;
    private LocalDateTime generatedAt;
    private String content;

    public Report() {
        this.generatedAt = LocalDateTime.now();
    }

    public Report(Long reportId, String title, String content) {
        this.reportId = reportId;
        this.title = title;
        this.content = content;
        this.generatedAt = LocalDateTime.now();
    }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}