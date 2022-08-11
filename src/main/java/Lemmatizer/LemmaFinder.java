package Lemmatizer;

import BypassingSitePages.SiteParser;
import Entities.Index;
import Entities.Lemma;
import Entities.Page;
import Repositories.IndexRepository;
import Repositories.LemmaRepository;
import Repositories.PageRepository;
import org.apache.commons.math3.util.Precision;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;


public class LemmaFinder {
    private final static String REGEX = "[^А-Яа-я]+";

    private final LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};

    private HashMap<String, Integer> lemmasFrequency = new HashMap<>();
    private HashMap<String, Double> lemmasRank = new HashMap<>();
    private HashMap<String, Integer> lemmasFromQuery = new HashMap<>();
    private String words = "купить гб штатив";

    private static final Lemma lemma = new Lemma();
    private static final Index index = new Index();
    private static final Page page = new Page();

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private IndexRepository indexRepository;

    @Autowired
    private PageRepository pageRepository;


    public LemmaFinder() throws IOException {
    }


    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        LemmaFinder lemmaFinder = new LemmaFinder();
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(new SiteParser());

        lemmaFinder.classMethods();
    }

    public void getTitleLemmasFromMainPage() throws IOException {
        String path = SiteParser.path;
        Document document = Jsoup.connect(path)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .maxBodySize(0)
                .get();

        String title = document.title();

        String words = title.replaceAll(REGEX, " ").toLowerCase().trim();

        for (String word : words.toLowerCase().split(" ")) {

            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);

            String normalWord = normalForms.get(0);

            if (lemmasFrequency.containsKey(normalWord)) {
                lemmasFrequency.put(normalWord, lemmasFrequency.get(normalWord) + 1);
                lemma.setLemma(normalWord);
                lemma.setFrequency(lemmasFrequency.get(normalWord) + 1);
                index.setLemmaId(lemma.getId());
                lemmaRepository.save(lemma);
            } else {
                lemmasFrequency.put(normalWord, 1);
                lemma.setLemma(normalWord);
                lemma.setFrequency(1);
                index.setLemmaId(lemma.getId());
                lemmaRepository.save(lemma);
            }
        }
        double rank;
        for (Map.Entry<String, Integer> lemma : lemmasFrequency.entrySet()) {
            rank = Precision.round(lemma.getValue(), 1);
            lemmasRank.put(lemma.getKey(), rank);
            index.setRank(rank);

        }
    }


    public void getBodyLemmasFromMainPage() throws IOException {
        String path = SiteParser.path;
        Document document = Jsoup.connect(path)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .maxBodySize(0)
                .get();

        Element body = document.body();

        String words = body.toString().replaceAll(REGEX, " ").toLowerCase();

        for (String word : words.toLowerCase().trim().split(" ")) {

            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);

            String normalWord = normalForms.get(0);

            if (lemmasFrequency.containsKey(normalWord)) {
                lemmasFrequency.put(normalWord, lemmasFrequency.get(normalWord) + 1);
                lemma.setLemma(normalWord);
                lemma.setFrequency(lemmasFrequency.get(normalWord) + 1);
                index.setLemmaId(lemma.getId());
                lemmaRepository.save(lemma);
            } else {
                lemmasFrequency.put(normalWord, 1);
                lemma.setLemma(normalWord);
                lemma.setFrequency(1);
                index.setLemmaId(lemma.getId());
                lemmaRepository.save(lemma);
            }
        }
        double rank;
        for (Map.Entry<String, Integer> lemma : lemmasFrequency.entrySet()) {
            rank = Precision.round(lemma.getValue() * 0.8, 1);
            lemmasRank.put(lemma.getKey(), rank);
            index.setRank(rank);
        }
    }

    public void getBodyLemmasFromEachPage() throws IOException {

        for (String link : SiteParser.links) {

            Document document = Jsoup.connect(link)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .maxBodySize(0)
                    .get();

            Element body = document.body();

            String words = body.toString().replaceAll(REGEX, " ").toLowerCase();

            for (String word : words.toLowerCase().trim().split(" ")) {

                List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticle(wordBaseForms)) {
                    continue;
                }

                List<String> normalForms = luceneMorphology.getNormalForms(word);

                String normalWord = normalForms.get(0);

                if (lemmasFrequency.containsKey(normalWord)) {
                    lemmasFrequency.put(normalWord, lemmasFrequency.get(normalWord) + 1);
                    lemma.setLemma(normalWord);
                    lemma.setFrequency(lemmasFrequency.get(normalWord) + 1);
                    index.setLemmaId(lemma.getId());
                    lemmaRepository.save(lemma);
                } else {
                    lemmasFrequency.put(normalWord, 1);
                    lemma.setLemma(normalWord);
                    lemma.setFrequency(1);
                    index.setLemmaId(lemma.getId());
                    lemmaRepository.save(lemma);
                }
            }
            double rank;
            for (Map.Entry<String, Integer> lemma : lemmasFrequency.entrySet()) {
                rank = Precision.round(lemma.getValue() * 0.8, 1);
                lemmasRank.put(lemma.getKey(), rank);
                index.setRank(rank);
            }
        }
    }

    public void getTitleLemmasFromEachPage() throws IOException {

        for (String link : SiteParser.links) {

            Document document = Jsoup.connect(link)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .maxBodySize(0)
                    .get();

            String title = document.title();

            String words = title.replaceAll(REGEX, " ").toLowerCase();

            for (String word : words.toLowerCase().trim().split(" ")) {

                List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticle(wordBaseForms)) {
                    continue;
                }

                List<String> normalForms = luceneMorphology.getNormalForms(word);

                String normalWord = normalForms.get(0);

                if (lemmasFrequency.containsKey(normalWord)) {
                    lemmasFrequency.put(normalWord, lemmasFrequency.get(normalWord) + 1);
                    lemma.setLemma(normalWord);
                    lemma.setFrequency(lemmasFrequency.get(normalWord) + 1);
                    index.setLemmaId(lemma.getId());
                    lemmaRepository.save(lemma);

                } else {
                    lemmasFrequency.put(normalWord, 1);
                    lemma.setLemma(normalWord);
                    lemma.setFrequency(1);
                    index.setLemmaId(lemma.getId());
                    lemmaRepository.save(lemma);
                }
            }
            double rank;
            for (Map.Entry<String, Integer> lemma : lemmasFrequency.entrySet()) {
                rank = Precision.round(lemma.getValue(), 1);
                lemmasRank.put(lemma.getKey(), rank);
                index.setRank(rank);
            }
        }
    }


    public void saveLemmasFromQuery(String words) throws SQLException {
        Connection connection = DriverManager.getConnection(SiteParser.Url, SiteParser.USER_NAME, SiteParser.PASSWORD);

        String word1 = words.replaceAll(REGEX, " ").toLowerCase();

        for (String word : word1.toLowerCase().trim().split(" ")) {

            List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = luceneMorphology.getNormalForms(word);

            String normalWord = normalForms.get(0);


            String sql = "SELECT lemma, frequency FROM page.lemma ORDER BY frequency DESC";
            ResultSet resultSet = connection.createStatement().executeQuery(sql);

            while (resultSet.next()) {
                if (resultSet.getString("lemma").equals(normalWord)) {
                    if (resultSet.getInt("frequency") > 300) {
                        lemmasFromQuery.put(normalWord, resultSet.getInt("frequency") / 10);
                    } else {
                        lemmasFromQuery.put(normalWord, resultSet.getInt("frequency"));
                    }
                }
            }
        }
    }
    
    public void printSitesWhichContainsLemmasFroQuery() throws IOException {
        for (String link : SiteParser.links) {
            for (Map.Entry<String, Integer> entry : lemmasFromQuery.entrySet()) {

                Document document = Jsoup.connect(link)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .maxBodySize(0)
                        .get();

                Element body = document.body();

                String words = body.toString().replaceAll(REGEX, " ").toLowerCase();

                for (String word : words.toLowerCase().trim().split(" ")) {

                    List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                    if (anyWordBaseBelongToParticle(wordBaseForms)) {
                        continue;
                    }
                    List<String> normalForms = luceneMorphology.getNormalForms(word);

                    String normalWord = normalForms.get(0);

                    if (entry.getKey().equals(normalWord)) {
                        System.out.println("Слово " + entry.getKey() + "встречается на сайте: " + link);
                    } else {
                        continue;
                    }
                }
            }
            break;
        }
    }


    public void insertLemmaIntoDataBase() throws SQLException {
        Connection connection = DriverManager.getConnection(SiteParser.Url, SiteParser.USER_NAME, SiteParser.PASSWORD);

        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS lemma");
        connection.createStatement().executeUpdate("CREATE TABLE lemma (" +
                "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "lemma VARCHAR(255) NOT NULL," +
                "frequency INT NOT NULL)");

        for (Map.Entry<String, Integer> lemma : lemmasFrequency.entrySet()) {

            String sql = "INSERT INTO lemma(`lemma`, frequency) " +
                    "VALUES('" + lemma.getKey() + "', '" + lemma.getValue() + "')";

            connection.createStatement().executeUpdate(sql);
        }
    }

    public void insertLemmaRankIntoDataBase() throws SQLException {
        Connection connection = DriverManager.getConnection(SiteParser.Url, SiteParser.USER_NAME, SiteParser.PASSWORD);

        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS `index`");
        connection.createStatement().executeUpdate("CREATE TABLE `index` (" +
                "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "page_id INT NOT NULL," +
                "lemma_id INT NOT NULL," +
                "`rank` FLOAT NOT NULL)");

        for (Map.Entry<String, Double> doubleEntry : lemmasRank.entrySet()) {

            String sql = "INSERT INTO `index`(page_id, lemma_id, `rank`) VALUES('" + page.getId() + "'," +
                    " '" + lemma.getId() + "', " +
                    "'" + doubleEntry.getValue() + "')";

            connection.createStatement().executeUpdate(sql);
        }
    }


    public boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticleProperty);
    }

    public boolean hasParticleProperty(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    private void printAllLemmas() {
        for (Map.Entry<String, Integer> w : lemmasFrequency.entrySet()) {
            System.out.println(w.getKey() + " - " + w.getValue());
        }
    }

    private void printLemmaRank() {
        for (Map.Entry<String, Double> doubleEntry : lemmasRank.entrySet()) {
            System.out.println(doubleEntry.getKey() + " - " + doubleEntry.getValue());
        }
    }

    private void classMethods() throws SQLException, IOException {
        getTitleLemmasFromMainPage();
        getBodyLemmasFromMainPage();

        getTitleLemmasFromEachPage();
        getBodyLemmasFromEachPage();

        insertLemmaIntoDataBase();
        insertLemmaRankIntoDataBase();

        saveEntities();
        saveLemmasFromQuery(words);
        printSitesWhichContainsLemmasFroQuery();
    }

    public void saveEntities() {
        indexRepository.save(index);
        lemmaRepository.save(lemma);
    }
}