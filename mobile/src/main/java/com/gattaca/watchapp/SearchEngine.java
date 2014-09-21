package com.gattaca.watchapp;

/**
 * Created by epentangelo on 9/21/14.
 */
public interface SearchEngine {

    void Initialize(String[] data);

    String[] Search(String searchTxt);
}
