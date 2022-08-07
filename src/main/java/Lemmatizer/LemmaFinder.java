package Lemmatizer;

import BypassingSitePages.SiteParser;
import Entites.Index;
import Entites.Lemma;
import Entites.Page;
import org.apache.commons.math3.util.Precision;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;


public class LemmaFinder {
    private final static String REGEX = "[^А-Яа-я]+";

    private final LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
    private final HashMap<String, Integer> lemmas = new HashMap<>();
    private final HashMap<String, Double> lemmaRank = new HashMap<>();
    private static final TreeSet<String> treeSet = new TreeSet<>();
    private static final Lemma lemma = new Lemma();
    private static final Page page = new Page();
    private static final Index index = new Index();


    public Document document = Jsoup.connect(SiteParser.PATH)
            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
            .referrer("http://www.google.com")
            .maxBodySize(0)
            .get();


    public LemmaFinder() throws IOException {
    }

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        LemmaFinder lemmaFinder = new LemmaFinder();
        lemmaFinder.getBodyLemmas();
        lemmaFinder.getTitleLemmas();
        lemmaFinder.insertLemmaIntoDataBase();
        lemmaFinder.insertLemmaRankIntoDataBase();
    }

    private void getTitleLemmas() {
        String title = document.title();

        String words = title.replaceAll(REGEX, " ").toLowerCase().trim();

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
        double rank;
        for (Map.Entry<String, Integer> lemma : lemmas.entrySet()) {
            rank = Precision.round(lemma.getValue(), 1);
            lemmaRank.put(lemma.getKey(), rank);
        }
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
        double rank;
        for (Map.Entry<String, Integer> lemma : lemmas.entrySet()) {
            rank = Precision.round(lemma.getValue() * 0.8, 1);
            lemmaRank.put(lemma.getKey(), rank);
        }
    }



    private void insertLemmaIntoDataBase() throws SQLException {
        Connection connection = DriverManager.getConnection(SiteParser.Url, SiteParser.USER_NAME, SiteParser.PASSWORD);

        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS lemma");
        connection.createStatement().executeUpdate("CREATE TABLE lemma (" +
                "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "lemma VARCHAR(255) NOT NULL," +
                "frequency INT NOT NULL)");


        for (Map.Entry<String, Integer> lemma : lemmas.entrySet()) {


            String sql = "INSERT INTO lemma(`lemma`, frequency) " +
                    "VALUES('" + lemma.getKey() + "', '" + lemma.getValue() + "')";

            connection.createStatement().executeUpdate(sql);

        }
    }

    private void insertLemmaRankIntoDataBase() throws SQLException {
        Connection connection = DriverManager.getConnection(SiteParser.Url, SiteParser.USER_NAME, SiteParser.PASSWORD);

        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS `index`");
        connection.createStatement().executeUpdate("CREATE TABLE `index` (" +
                "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "page_id INT NOT NULL," +
                "lemma_id INT NOT NULL," +
                "`rank` FLOAT NOT NULL)");


        for (Map.Entry<String, Double> doubleEntry : lemmaRank.entrySet()) {

            String sql = "INSERT INTO `index`(page_id, lemma_id, `rank`) VALUES('" + page.getId() + "', '" + lemma.getId() + "', " +
                    "'" + doubleEntry.getValue() + "')";

            connection.createStatement().execute(sql);
        }
    }

    private void getLemmasFromQuery(String words) throws SQLException {
        Connection connection = DriverManager.getConnection(SiteParser.Url, SiteParser.USER_NAME, SiteParser.PASSWORD);

        for (String word : words.replaceAll(REGEX, " ").toLowerCase().trim().split(" ")) {

            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }


            List<String> normalForms = luceneMorphology.getNormalForms(word);
            String normalWord = normalForms.get(0);

            String sql = "SELECT lemma, frequency FROM page.lemma ORDER BY frequency DESC";

            ResultSet set = connection.createStatement().executeQuery(sql);

            while (set.next()) {
                treeSet.add(set.getString("lemma"));
            }
        }
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

    private void printAllLemmas() {
        for (Map.Entry<String, Integer> w : lemmas.entrySet()) {
            System.out.println(w.getKey() + " - " + w.getValue());
        }
    }

    private void printLemmaRank() {
        for (Map.Entry<String, Double> doubleEntry : lemmaRank.entrySet()) {
            System.out.println(doubleEntry.getKey() + " - " + doubleEntry.getValue());
        }
    }
}