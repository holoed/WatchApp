package com.gattaca.watchapp;

import com.google.android.gms.cast.MediaInfo;

import java.util.HashMap;

/**
 * Created by epentangelo on 9/21/14.
 */
public interface MoviesCatalogService {

    void BuildMediaInfos();

    HashMap<String, MediaInfo> getMovies();
}
