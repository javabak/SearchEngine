package Lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Lemma {
    private static String TEXT = "Повторное появление леопарда в Осетии позволяет предположить, что " +
            "леопард постоянно обитает в некоторых районах Северного Кавказа";

    public static void main(String[] args) throws IOException {
        findAllLemmas(TEXT);
    }

    public static void findAllLemmas(String text) throws IOException {
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();


        String s[] = text.toLowerCase().replace(",", "").split("\\s+");

        for (int i = 0; i < s.length; i++) {

            for (int g = 0; g < s.length; g++) {

                System.out.println(luceneMorph.getNormalForms(s[i]) + " - ");
            }
        }
    }

//        List<String> wordBaseForms = luceneMorph.getNormalForms("кйкй");

//        String res = wordBaseForms.toString() + " - " + wordBaseForms.stream().count();
//
//        System.out.println(res);

//        wordBaseForms.forEach(System.out::println);
}

