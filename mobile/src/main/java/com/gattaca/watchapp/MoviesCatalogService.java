package com.gattaca.watchapp;

import com.google.android.gms.cast.MediaInfo;
import java.util.HashMap;

import rx.Observable;

/**
 * Created by epentangelo on 9/21/14.
 */
public interface MoviesCatalogService {

    Observable<HashMap<String, MediaInfo>> BuildMediaInfos();

}
