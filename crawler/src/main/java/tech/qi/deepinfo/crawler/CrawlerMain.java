package tech.qi.deepinfo.crawler;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangqi
 */
@Configuration
@ComponentScan(basePackages = {"tech.qi.deepinfo.crawler"})
@EnableAutoConfiguration(exclude={HibernateJpaAutoConfiguration.class})
public class CrawlerMain {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(CrawlerMain.class, args);
    }
}
