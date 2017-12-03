package tech.qi.deepinfo.proxy;


/**
 * @author wangqi
 */
@Configuration
@ComponentScan(basePackages = {"tech.qi.deepinfo.proxy"})
@EnableAutoConfiguration(exclude={HibernateJpaAutoConfiguration.class})
public class ProxyMain {
    public static void main(String[] args) {
        ApplicationContext context =SpringApplication.run(CrawlerMain.class, args);
        SpringContextUtil.setApplicationContext(context);
    }
}
