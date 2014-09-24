package com.gattaca.watchapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by epentangelo on 24/09/2014.
 */
public class InvertedIndex {
    private static Tuple<Integer, String>[] numLines(String[] lines) {
        Tuple<Integer, String>[] values = new Tuple[lines.length];
        int index = 0;
        for (String line : lines) {
            values[index] = new Tuple<Integer, String>(index, line);
            index++;
        }
        return values;
    }

    private static String[] words(String line) {
        return line.split("[\\W]");
    }

    private static String[] cleanWords(String line) {
        String[] words = words(line);
        List<String> cleanedWords = new ArrayList<String>();
        for (String word : words) {
            if (!word.matches("\\W") && word.trim().length() > 0) {
                cleanedWords.add(word.trim().toLowerCase());
            }
        }
        return cleanedWords.toArray(new String[cleanedWords.size()]);
    }

    private static Tuple<Integer, String>[] allNumWords(Tuple<Integer, String>[] lines) {
        List<Tuple<Integer, String>> pairs = new ArrayList<Tuple<Integer, String>>();
        for (Tuple<Integer, String> line : lines) {
            String[] words = cleanWords(line.y);
            for (String word : words) {
                pairs.add(new Tuple<Integer, String>(line.x, word));
            }
        }
        return pairs.toArray(new Tuple[pairs.size()]);
    }

    private static Tuple<Integer, String>[] sortLs(Tuple<Integer, String>[] items) {
        Tuple<Integer, String>[] newItems = java.util.Arrays.copyOf(items, items.length);
        java.util.Arrays.sort(newItems, new Comparator<Tuple<Integer, String>>() {
            @Override
            public int compare(Tuple<Integer, String> o1, Tuple<Integer, String> o2) {
                Integer i1 = o1.x;
                Integer i2 = o2.x;
                String w1 = o1.y;
                String w2 = o2.y;
                if  (w1.compareTo(w2) < 0 || (w1 == w2 && i1 < i2)) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return newItems;
    }

    private static Tuple<Integer[], String>[] makeLists(Tuple<Integer, String>[] xs) {
        Tuple<Integer[], String>[] ys = new Tuple[xs.length];
        Integer index = 0;
        for (Tuple<Integer, String> x : xs) {
            ys[index++] = new Tuple<Integer[], String>(new Integer[]{x.x}, x.y);
        }
        return ys;
    }

    private static Tuple<Integer[], String>[] accumulate(Tuple<Integer[], String>[] xs) {
        List<Tuple<Integer[], String>> list = new ArrayList<Tuple<Integer[], String>>();
        for (Tuple<Integer[], String> x : xs) {
            if (list.isEmpty()) {
                list.add(x);
            }
            else {
                Tuple<Integer[], String> last = list.get(list.size() - 1);
                if (Objects.equals(x.y, last.y)) {
                    List<Integer> newIndexes =  new ArrayList<Integer>();
                    for (Integer i : x.x) {
                        newIndexes.add(i);
                    }
                    for (Integer i : last.x) {
                        newIndexes.add(i);
                    }
                    list.set(list.size() - 1, new Tuple<Integer[], String>(newIndexes.toArray(new Integer[newIndexes.size()]), x.y));
                }
                else {
                    list.add(x);
                }
            }
        }
        return list.toArray(new Tuple[list.size()]);
    }

    private static Map<String, Integer[]> toMap(Tuple<Integer[], String>[] xs) {
        Map<String, Integer[]> dict = new HashMap<String, Integer[]>();
        for (Tuple<Integer[], String> x : xs) {
            dict.put(x.y, x.x);
        }
        return dict;
    }

    public static Map<String, Integer[]> index(String[] lines) {
        return toMap(accumulate(makeLists(sortLs(allNumWords(InvertedIndex.numLines(lines))))));
    }

    public static Integer search(String line, Map<String, Integer[]> index) {
        String[] cleanedWords = cleanWords(line);
        List<Integer> indexes = new ArrayList<Integer>();
        for (String word : cleanedWords) {
            if (index.containsKey(word)) {
                for (Integer i : index.get(word)) {
                    indexes.add(i);
                }
            }
        }

        List<List<Integer>> groupedIndexes = new ArrayList<List<Integer>>();
        for (Integer i : indexes) {
            if (groupedIndexes.isEmpty()) {
                List<Integer> childIndexes = new ArrayList<Integer>();
                childIndexes.add(i);
                groupedIndexes.add(childIndexes);
            }
            else {
                List<Integer> lastList = groupedIndexes.get(groupedIndexes.size() - 1);
                Integer last = lastList.get(0);
                if (Objects.equals(i, last)) {
                    lastList.add(i);
                }
                else {
                    List<Integer> childIndexes = new ArrayList<Integer>();
                    childIndexes.add(i);
                    groupedIndexes.add(childIndexes);
                }
            }
        }
        List<Integer>[] groupedIndexesArray = groupedIndexes.toArray(new List[groupedIndexes.size()]);
        Arrays.sort(groupedIndexesArray, new Comparator<List<Integer>>() {
            @Override
            public int compare(List<Integer> o1, List<Integer> o2) {
                return Integer.compare(o1.size(), o2.size());
            }
        });

        if (groupedIndexesArray.length > 0) {
            List<Integer> out = groupedIndexesArray[groupedIndexes.size() - 1];
            if (out.size() > 0)
                return out.get(0);
            else
                return -1;
        }
        else return - 1;
    }
}