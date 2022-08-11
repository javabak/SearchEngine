package Main;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.ClassPathXmlApplicationContext;


import java.io.IOException;
import java.sql.SQLException;

@SpringBootApplication
public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("beans.xml");
        SpringApplication.run(Main.class, args);
    }
}


