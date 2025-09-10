package com.site.lms.dto;

// import com.fasterxml.jackson.annotation.JsonProperty; // 이 임포트는 필요 없어집니다.

import java.util.List;
import java.util.Map;

public class VideoProcessResponse {
    private String filename;
    private List<String> tokens;
    private List<List<Object>> top_keywords;
    private List<Map<String, Object>> segments;
    private String raw_text;

    // !!!!! 이 부분을 수정합니다: @JsonProperty 어노테이션을 제거합니다. !!!!!
    private String summary; // Flask가 "summary" 키로 보내므로, DTO 필드명과 일치하면 자동으로 매핑됩니다.

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }

    public List<List<Object>> getTop_keywords() {
        return top_keywords;
    }

    public void setTop_keywords(List<List<Object>> top_keywords) {
        this.top_keywords = top_keywords;
    }

    public List<Map<String, Object>> getSegments() {
        return segments;
    }

    public void setSegments(List<Map<String, Object>> segments) {
        this.segments = segments;
    }

    public String getRaw_text() {
        return raw_text;
    }

    public void setRaw_text(String raw_text) {
        this.raw_text = raw_text;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
