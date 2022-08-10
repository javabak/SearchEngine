package BypassingSitePages;

import Entities.Page;
import Repositories.PageRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

@Controller
@Component
public class SiteParser extends RecursiveAction {
    public final static String USER_NAME = "root";
    public final static String PASSWORD = "Alimnikas299";
    public static String Url = "jdbc:mysql://127.0.0.1:3306/page?serverTimezone=UTC";
    public final static String PATH = "https://www.playback.ru/";
    private final static String regex = "[^А-Яа-яA-Za-z<>/\\s+!-]+";
    private static String url = "https://www.playback.ru/";


    private static Connection connection;
    public static TreeSet<String> links = new TreeSet<>();
    public static Page page = new Page();

    @Autowired
    private PageRepository pageRepository;


    public static void main(String[] args) throws IOException {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(new SiteParser());
        forkJoinPool.shutdownNow();
    }

    public void mainPageParser() throws IOException, SQLException, InterruptedException {
        org.jsoup.Connection.Response d = Jsoup.connect(PATH).execute();
        Document document = Jsoup.connect(SiteParser.PATH)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .maxBodySize(0)
                .get();

        Elements elements = document.getElementsByAttribute("href");

        for (Element element : elements) {
            String link = element.absUrl("href");

            if (link.startsWith(PATH) && link.endsWith(".html")) {

                links.add(link);
                links.add(PATH);

//                page.setCode(d.statusCode());
//                page.setContent(document.toString().replaceAll(regex, ""));
//                page.setPath(link.substring(22));
//
//                pageRepository.save(page);

                String sql = "INSERT INTO page(path, code, content) " +
                        "VALUES('" + link.substring(22) + "', '" + d.statusCode() + "', '"
                        + document.toString().replaceAll(regex, "") + "')";

                connection.createStatement().executeUpdate(sql);
            }
        }
    }

    public void connectToDateBase() {
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
            mainPageParser();
        } catch (IOException | SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        invokeAll();
    }
}