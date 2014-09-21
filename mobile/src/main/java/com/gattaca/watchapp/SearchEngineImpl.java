package com.gattaca.watchapp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Created by epentangelo on 9/21/14.
 */
public class SearchEngineImpl implements SearchEngine {
    private HashSet<String> _hashSet;

    @Override
    public void Initialize(String[] data) {

        _hashSet = new HashSet<String>();
        for (String title : data) {
            String txt = title.toLowerCase().replaceAll("\\s+", "");
            _hashSet.add(txt);
        }
    }

    @Override
    public String[] Search(String searchTxt) {
        if (_hashSet.contains(searchTxt)) {
            return new String[]{ searchTxt };
        }
        else return new String[0];
    }
}
