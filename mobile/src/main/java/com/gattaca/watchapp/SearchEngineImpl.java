package com.gattaca.watchapp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by epentangelo on 9/21/14.
 */
public class SearchEngineImpl implements SearchEngine {
    private String[] _data;
    private Map<String, Integer[]> _index;

    @Override
    public void Initialize(String[] data) {
        _data = data;
        _index = InvertedIndex.index(data);
    }

    @Override
    public String[] Search(String searchTxt) {
        Integer foundIndex = InvertedIndex.search(searchTxt, _index);
        if (foundIndex > -1)
            return new String[]{  _data[foundIndex] };
        else
            return new String[0];
    }
}
