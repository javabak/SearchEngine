package BypassingSitePages;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.TreeSet;

public class SiteParser {
    private static Connection connection;
    private static String USER_NAME = "root";
    private static String PASSWORD = "Alimnikas299";
    private static String URL = "jdbc:mysql://127.0.0.1:3306/page?serverTimezone=UTC";
    private static String PATH = "http://www.playback.ru/";

    private static TreeSet<String> links = new TreeSet<>();
    private static StringBuilder builder = new StringBuilder();



    public static void main(String[] args) throws IOException, SQLException {
        connectToDateBase();
        pageParser(PATH);
    }

    public static void pageParser(String path) throws IOException, SQLException {
        Document document = Jsoup.connect(path)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com").maxBodySize(0).get();

        Elements elements = document.getElementsByTag("a");

        for (Element element : elements) {
            links.add(element.absUrl("href"));

            String sql = "INSERT INTO page(path, code, content) " +
                    "VALUES('" + element.absUrl("href") + "', '" + 200 + "', '" + element.html() + "')";

            connection.createStatement().executeUpdate(sql);
        }
    }

    public static Connection connectToDateBase() {
        if (connection == null) {
            try {

                connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);

                connection.createStatement().execute("DROP TABLE IF EXISTS page");
                connection.createStatement().execute("CREATE TABLE page (" +
                        "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                        "path TEXT NOT NULL," +
                        "code INT NOT NULL," +
                        "content MEDIUMTEXT NOT NULL)");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return connection;
    }
}
