package com.jetbrainsinternship.chillcoder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class YouTubeSearch {
    private static final String API_KEY = "YOUTUBEAPIKEY";
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";

    public static List<VideoResult> searchVideos(String query) {
        List<VideoResult> results = new ArrayList<>();
        try {
            OkHttpClient client = new OkHttpClient();
            String url = YOUTUBE_SEARCH_URL + "?part=snippet&q=" + query + "&key=" + API_KEY + "&type=video";
            Request request = new Request.Builder().url(url).build();

            Response response = client.newCall(request).execute();
            assert response.body() != null;
            String jsonResponse = response.body().string();

            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray items = jsonObject.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                JSONObject snippet = item.getJSONObject("snippet");

                String title = snippet.getString("title");
                String channel = snippet.getString("channelTitle");
                String videoId = item.getJSONObject("id").getString("videoId");
                String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

                results.add(new VideoResult(title, channel, videoUrl));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
}
