package com.gattaca.watchapp;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.HashMap;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by epentangelo on 9/21/14.
 */
public class MoviesCatalogServiceImpl implements MoviesCatalogService {

    private static String TAG_CATEGORIES = "categories";
    private static String TAG_NAME = "name";
    private static String TAG_STUDIO = "studio";
    private static String TAG_SOURCES = "sources";
    private static String TAG_SUBTITLE = "subtitle";
    private static String TAG_THUMB = "image-480x270"; // "thumb";
    private static String TAG_IMG_780_1200 = "image-780x1200";
    private static String TAG_TITLE = "title";
    private static String TAG_MEDIA = "videos";
    private static String CATALOG_URL = "http://192.168.0.8:8000/Catalog"; //TODO: Move to configuration file.

    @Override
    public Observable<HashMap<String, MediaInfo>> BuildMediaInfos() {
        return Observable.create(new Observable.OnSubscribe<HashMap<String, MediaInfo>>() {
            @Override
            public void call(final Subscriber<? super HashMap<String, MediaInfo>> subscriber) {
                new AsyncTask<Void, Void, HashMap<String, MediaInfo>>(){
                    @Override
                    protected HashMap<String, MediaInfo> doInBackground(Void... voids) {
                        return buildMedia(CATALOG_URL);
                    }

                    @Override
                    protected void onPostExecute(HashMap<String, MediaInfo> mediaInfos) {
                        super.onPostExecute(mediaInfos);
                        subscriber.onNext(mediaInfos);
                        subscriber.onCompleted();
                    }
                }.execute();
            }
        });
    }

    private HashMap<String, MediaInfo> buildMedia(String url) {

        try {
            HashMap<String, MediaInfo> mediaList = new HashMap<String, MediaInfo>();
            JSONObject jsonObj = parseUrl(url);
            JSONArray categories = jsonObj.getJSONArray(TAG_CATEGORIES);
            if (null != categories) {
                for (int i = 0; i < categories.length(); i++) {
                    JSONObject category = categories.getJSONObject(i);
                    category.getString(TAG_NAME);
                    JSONArray videos = category.getJSONArray(TAG_MEDIA);
                    if (null != videos) {
                        for (int j = 0; j < videos.length(); j++) {
                            JSONObject video = videos.getJSONObject(j);
                            String subTitle = video.getString(TAG_SUBTITLE);
                            JSONArray videoUrls = video.getJSONArray(TAG_SOURCES);
                            if (null == videoUrls || videoUrls.length() == 0) {
                                continue;
                            }
                            String videoUrl = videoUrls.getString(0);
                            String imageUrl = video.getString(TAG_THUMB);
                            String bigImageUrl = video.getString(TAG_IMG_780_1200);
                            String title = video.getString(TAG_TITLE);
                            String studio = video.getString(TAG_STUDIO);
                            mediaList.put(title.toLowerCase().replaceAll("\\s+", ""), buildMediaInfo(title, studio, subTitle, videoUrl, imageUrl,
                                    bigImageUrl));
                        }
                    }
                }
            }
            return mediaList;
        }
        catch (Exception ex) {
            return new HashMap<String, MediaInfo>();
        }
    }

    private static MediaInfo buildMediaInfo(String title,
                                            String subTitle, String studio, String url, String imgUrl, String bigImageUrl) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        movieMetadata.putString(MediaMetadata.KEY_STUDIO, studio);
        movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
        movieMetadata.addImage(new WebImage(Uri.parse(bigImageUrl)));

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("video/mp4")
                .setMetadata(movieMetadata)
                .build();
    }

    private JSONObject parseUrl(String urlString) {
        InputStream is = null;
        try {
            java.net.URL url = new java.net.URL(urlString);
            URLConnection urlConnection = url.openConnection();
            is = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();
            return new JSONObject(json);
        } catch (Exception e) {
            Log.d("", "Failed to parse the json for media list", e);
            return null;
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
