package Lemmatizer;

import BypassingSitePages.SiteParser;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class LemmaFinder {
    private final static String REGEX = "[^А-Яа-я]+";

    private static Connection connection;
    private final LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
    private static final HashMap<String, Integer> lemmas = new HashMap<>();


    public volatile Document document = Jsoup.connect(SiteParser.PATH)
            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
            .referrer("http://www.google.com")
            .maxBodySize(0)
            .get();


    public LemmaFinder() throws IOException {
    }

    public static void main(String[] args) throws IOException, SQLException {
        LemmaFinder lemma = new LemmaFinder();
        lemma.getTitleLemmas();
        lemma.getBodyLemmas();
        lemma.insertIntoDataBase();
    }


    public void collectLemmas(String text) {
        String[] words = text.toLowerCase().replaceAll("([^а-я\\s])", " ").split("\\s+");

        for (String word : words) {

            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);

            String normalWord = normalForms.get(0);

            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }
        printAllLemmas();
    }


    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    private boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    private void getBodyLemmas() {

        Element body = document.body();

        String words = body.toString().replaceAll(REGEX, " ").toLowerCase();

        for (String word : words.toLowerCase().trim().split(" ")) {

            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);

            String normalWord = normalForms.get(0);

            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }
    }

    private void getTitleLemmas() {
        String title = document.title();

        String words = title.replaceAll(REGEX, " ").toLowerCase();

        for (String word : words.toLowerCase().trim().split(" ")) {

            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);

            String normalWord = normalForms.get(0);

            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }
    }


    private void printAllLemmas() {
        for (Map.Entry<String, Integer> w : lemmas.entrySet()) {
            System.out.println(w.getKey() + " - " + w.getValue());
        }
    }

    private void insertIntoDataBase() throws SQLException {
        connection = DriverManager.getConnection(SiteParser.Url, SiteParser.USER_NAME, SiteParser.PASSWORD);


        for (Map.Entry<String, Integer> lemma : lemmas.entrySet()) {

            ResultSet res = connection.createStatement().executeQuery("SELECT lemma FROM page.lemma");
            while (res.next()) {

                if (!res.getString(2).contains(lemma.getKey())) {
                    String sql = "INSERT INTO lemma(lemma, frequency) " +
                            "VALUES('" + lemma.getKey() + "', '" + 1 + "')";

                    connection.createStatement().execute(sql);
                } else {

                    String sql = "INSERT INTO lemma(lemma, frequency) " +
                            "VALUES('" + lemma.getKey() + "', '" + (res.getInt("frequency") + 1) + "')";


                    connection.createStatement().execute(sql);
                }
            }
        }
    }
}