package main;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.sql.SQLException;

@SpringBootApplication
public class Main {
    public static void main(String[] args) throws IOException, SQLException {
       SpringApplication.run(Main.class, args);
    }
}


