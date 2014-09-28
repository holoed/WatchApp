package com.gattaca.watchapp;

import com.google.android.gms.cast.MediaInfo;

/**
 * Created by epentangelo on 9/21/14.
 */
public interface ChromeCastService {

    void DiscoverCastDevices();

    void PlayMovie(MediaInfo selectedMovie);

    void Pause();

    void Play();

    void Stop();

    void Exit();
}
