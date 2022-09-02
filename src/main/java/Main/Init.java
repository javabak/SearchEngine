package Main;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.SQLException;

@Component
public class Init {

    @PostConstruct
    public void toInit() throws SQLException {
        System.out.println("Starting create tables");

//        Connection connection = DriverManager.getConnection(SiteParser.Url, SiteParser.USER_NAME, SiteParser.PASSWORD);
//
//        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS lemma");
//        connection.createStatement().executeUpdate("CREATE TABLE lemma (" +
//                "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
//                "lemma VARCHAR(255) NOT NULL," +
//                "frequency INT NOT NULL)");
//
//
//        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS `index`");
//        connection.createStatement().executeUpdate("CREATE TABLE `index` (" +
//                "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
//                "page_id INT NOT NULL," +
//                "lemma_id INT NOT NULL," +
//                "`rank` FLOAT NOT NULL)");
//
//
//        connection.createStatement().execute("DROP TABLE IF EXISTS page");
//        connection.createStatement().execute("CREATE TABLE page (" +
//                "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
//                "path TEXT NOT NULL," +
//                "code INT NOT NULL," +
//                "content MEDIUMTEXT NOT NULL)");

        System.out.println("Creating tables ended");
    }

    @PreDestroy
    public void toDestroy() {
        System.out.println("Ended");
    }
}
