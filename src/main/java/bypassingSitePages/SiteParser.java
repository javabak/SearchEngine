package bypassingSitePages;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@Controller
public class SiteParser extends RecursiveAction {
    public final static String USER_NAME = "root";
    public final static String PASSWORD = "Alimnikas299";
    public final static String Url = "jdbc:mysql://127.0.0.1:3306/search_engine?serverTimezone=UTC";
    private final static String regex = "[^А-Яа-яA-Za-z<>/\\s+!-]+";
    public static TreeSet<String> pageLinks = new TreeSet<>();
    public static Connection connection;
    private static String site = "https://www.playback.ru";


    public static void main(String[] args) throws IOException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(new SiteParser());
        forkJoinPool.shutdown();
    }

    public void connectToDateBase() {
        if (connection == null) {
            try {

                connection = DriverManager.getConnection(Url, USER_NAME, PASSWORD);

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
    }


    @Override
    protected void compute() {
            try {
                connectToDateBase();
                org.jsoup.Connection.Response d = Jsoup.connect(site).execute();
                Document document = Jsoup.connect(site)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .maxBodySize(0)
                        .get();

                Elements elements = document.getElementsByAttribute("href");

                pageLinks.add(site);
                for (Element element : elements) {
                    String link = element.absUrl("href");
                    if (link.endsWith(".html")) {
                        pageLinks.add(link);

                        String sql = "INSERT INTO page(path, code, content) " +
                                "VALUES('" + link.substring(12) + "', '" + d.statusCode() + "', '"
                                + document.toString().replaceAll(regex, "") + "')";

                        connection.createStatement().executeUpdate(sql);
                    }
                }
                ForkJoinTask.invokeAll();
            } catch (IOException | SQLException e) {
                throw new RuntimeException(e);
        }
    }
}