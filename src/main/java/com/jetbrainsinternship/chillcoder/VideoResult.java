package com.jetbrainsinternship.chillcoder;

public class VideoResult {
    private final String title;
    private final String channel;
    private final String videoUrl;

    public VideoResult(String title, String channel, String videoUrl) {
        this.title = title;
        this.channel = channel;
        this.videoUrl = videoUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    @Override
    public String toString() {
        return title + " (" + channel + ")";
    }
}
