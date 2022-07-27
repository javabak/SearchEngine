package BypassingSitePages;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Base64;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;


public class SiteParser extends RecursiveAction {
    private static Connection connection;
    private static String USER_NAME = "root";
    private static String PASSWORD = "Alimnikas299";
    private static String Url = "jdbc:mysql://127.0.0.1:3306/page?serverTimezone=UTC";
    private static String PATH = "http://www.playback.ru/";



    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(new SiteParser());

    }

    public static void pageParser(String path) throws IOException, SQLException, InterruptedException {
        Document document = Jsoup.connect(path)
                 .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com").maxBodySize(0).get();

        String el = document.html();
        String encodedString = Base64.getEncoder().encodeToString(el.getBytes());


        Elements elements = document.getElementsByAttribute("href");

        for (Element element : elements) {
            String link = element.absUrl("href");

            if (link.startsWith(PATH) && link.endsWith(".html")) {

                String sql = "INSERT INTO page(path, code, content) " +
                        "VALUES('" + link.substring(22) + "', '" + 200 + "', '" + encodedString + "')";

                connection.createStatement().executeUpdate(sql);
            }
        }
    }

    public static Connection connectToDateBase() {
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
        return connection;
    }

    @Override
    protected void compute() {
        connectToDateBase();
        try {
            pageParser(PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        invokeAll();
    }
}
