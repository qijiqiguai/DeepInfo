package tech.qi.deepinfo.crawler;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import tech.qi.deepinfo.crawler.spider.SpiderHandler;
import tech.qi.deepinfo.frame.context.ShutdownHook;
import tech.qi.deepinfo.frame.core.LifecycleException;

/**
 * @author wangqi
 */
@Configuration
@ComponentScan(basePackages = {"tech.qi.deepinfo.crawler"})
@EnableAutoConfiguration(exclude={HibernateJpaAutoConfiguration.class})
public class CrawlerMain {
    public static void main(String[] args) throws LifecycleException {
        ApplicationContext context = SpringApplication.run(CrawlerMain.class, args);

        SpiderHandler spiderHandler = context.getBean(SpiderHandler.class);
        spiderHandler.init();
        spiderHandler.start();

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }
}
