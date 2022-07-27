package Lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

public class Lemma {
    private static String TEXT = "Повторное появление леопарда в Осетии позволяет предположить, что " +
            "леопард постоянно обитает в некоторых районах Северного Кавказа";

    private static Map<String, Integer> occurrences = new HashMap<String, Integer>();

    public static void main(String[] args) throws IOException {
        findAllLemmas(TEXT);
        printAllLemmas();
    }

    public static void findAllLemmas(String text) throws IOException {
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();


        String s[] = text.toLowerCase().replace(",", "").split("\\s+");

        for (int i = 0; i < s.length; i++) {

            for (String word : luceneMorph.getNormalForms(s[i])) {
                Integer oldCount = occurrences.get(word);
                if (oldCount == null) {
                    oldCount = 0;
                }
                occurrences.put(word, oldCount + 1);
            }
        }
    }

    public static void printAllLemmas() {
        for (Map.Entry<String, Integer> entry : occurrences.entrySet()) {
            System.out.println(entry.getKey() + "(" + entry.getValue() + ")\t");
        }
    }
}


