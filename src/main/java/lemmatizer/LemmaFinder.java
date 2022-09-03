package lemmatizer;

import bypassingSitePages.SiteParser;
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
import java.util.TreeMap;
import java.util.concurrent.ForkJoinPool;

public class LemmaFinder {
    public final static String REGEX = "[^А-Яа-я]+";

    private final LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};

    private final HashMap<String, Integer> lemmasFrequency = new HashMap<>();
    private final HashMap<String, Double> lemmasRank = new HashMap<>();
    private final HashMap<String, Integer> lemmasFromQueryWithFrequency = new HashMap<>();
    private final TreeMap<String, String> linksFromLemmasQuery = new TreeMap<>();
    private String words = "оплата корзина телефон";


    public LemmaFinder() throws IOException {
    }


    public static void main(String[] args) throws IOException, SQLException, InterruptedException {
        LemmaFinder lemmaFinder = new LemmaFinder();
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(new SiteParser());

        lemmaFinder.launchClassMethods();
    }


    public void getBodyLemmasFromEachPage() throws IOException {
        for (String link : SiteParser.pageLinks) {

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
                } else {
                    lemmasFrequency.put(normalWord, 1);
                }
            }
            double rank;
            for (Map.Entry<String, Integer> lemma : lemmasFrequency.entrySet()) {
                rank = Precision.round(lemma.getValue() * 0.8, 1);
                lemmasRank.put(lemma.getKey(), rank);
            }
        }
    }

    public void getTitleLemmasFromEachPage() throws IOException {
        for (String link : SiteParser.pageLinks) {

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
                } else {
                    lemmasFrequency.put(normalWord, 1);
                }
            }
            double rank;
            for (Map.Entry<String, Integer> lemma : lemmasFrequency.entrySet()) {
                rank = Precision.round(lemma.getValue(), 1);
                lemmasRank.put(lemma.getKey(), rank);
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

            String sql = "SELECT lemma, frequency FROM search_engine.lemma ORDER BY frequency DESC";
            ResultSet resultSet = connection.createStatement().executeQuery(sql);

            while (resultSet.next()) {
                if (normalWord.equals(resultSet.getString("lemma"))) {
                    if (resultSet.getInt("frequency") > 100) {
                        lemmasFromQueryWithFrequency.put(normalWord, resultSet.getInt("frequency") / 10);
                    } else {
                        lemmasFromQueryWithFrequency.put(normalWord, resultSet.getInt("frequency"));
                    }
                }
            }
        }
    }


    public void getSitesWhichContainsLemmasFromQuery() throws IOException {
        for (String link : SiteParser.pageLinks) {
            for (Map.Entry<String, Integer> entry : lemmasFromQueryWithFrequency.entrySet()) {

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

                    if (normalWord.equals(entry.getKey())) {
                        linksFromLemmasQuery.put(link, normalWord);
                    }
                }
            }
            continue;
        }
    }

    public void findPageRelevance() {




    }

    public void insertLemmaIntoDataBase() throws SQLException {
        Connection connection = DriverManager.getConnection(SiteParser.Url, SiteParser.USER_NAME, SiteParser.PASSWORD);

        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS lemma");
        connection.createStatement().executeUpdate("CREATE TABLE lemma (" +
                "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "lemma VARCHAR(255) NOT NULL," +
                "frequency INT NOT NULL)");

        for (Map.Entry<String, Integer> lemma : lemmasFrequency.entrySet()) {

            String sql = "INSERT INTO lemma(lemma, frequency) " +
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

            String sql = "INSERT INTO `index`(page_id, lemma_id, `rank`) VALUES('" + 0 + "'," +
                    " '" + 0 + "', " +
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

    private TreeMap<String, String> printSitesWhichContainsLemmasFromQuery() {
        for (Map.Entry<String, String> entry : linksFromLemmasQuery.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
        if (linksFromLemmasQuery.isEmpty()) {
            return new TreeMap<>();
        } else {
            return linksFromLemmasQuery;
        }
    }

    public void launchClassMethods() throws SQLException, IOException {
        getTitleLemmasFromEachPage();
        getBodyLemmasFromEachPage();

        insertLemmaIntoDataBase();
        insertLemmaRankIntoDataBase();

        saveLemmasFromQuery(words);
        getSitesWhichContainsLemmasFromQuery();
        printSitesWhichContainsLemmasFromQuery();
    }
}