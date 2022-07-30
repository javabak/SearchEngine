package BypassingSitePages;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;


public class SiteParser extends RecursiveAction {
    private static Connection connection;
    public final static String USER_NAME = "root";
    public final static String PASSWORD = "Alimnikas299";
    public final static String Url = "jdbc:mysql://127.0.0.1:3306/page?serverTimezone=UTC";
    public final static String PATH = "http://www.playback.ru/";

    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(new SiteParser());
    }

    private static void pageParser() throws IOException, SQLException, InterruptedException {

        org.jsoup.Connection.Response document = Jsoup.connect(SiteParser.PATH)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .maxBodySize(0)
                .execute();


        Elements elements = document.parse().getElementsByAttribute("href");

        for (Element element : elements) {
            String link = element.absUrl("href");

            if (link.startsWith(PATH) && link.endsWith(".html")) {

                String sql = "INSERT INTO page(path, code, content) " +
                        "VALUES('" + link.substring(22) + "', '" + document.statusCode() + "', '" + document + "')";

                connection.createStatement().executeUpdate(sql);
            }
        }
    }

    private static void connectToDateBase() {
        if (connection == null) {
            try {

                connection = DriverManager.getConnection(Url, USER_NAME, PASSWORD);

                connection.createStatement().executeUpdate("DROP TABLE IF EXISTS page");
                connection.createStatement().executeUpdate("CREATE TABLE page (" +
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
        connectToDateBase();
        try {
            pageParser();
        } catch (IOException | SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        invokeAll();
    }
}
