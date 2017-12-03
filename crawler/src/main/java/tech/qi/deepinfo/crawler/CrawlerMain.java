package tech.qi.deepinfo.crawler;


/**
 * @author wangqi
 */
@Configuration
@ComponentScan(basePackages = {"tech.qi.deepinfo.crawler"})
@EnableAutoConfiguration(exclude={HibernateJpaAutoConfiguration.class})
public class CrawlerMain {
    public static void main(String[] args) {
        ApplicationContext context =SpringApplication.run(CrawlerMain.class, args);
        SpringContextUtil.setApplicationContext(context);
    }
}
