package tech.qi.deepinfo.proxy;


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
@ComponentScan(basePackages = {"tech.qi.deepinfo.proxy"})
@EnableAutoConfiguration(exclude={HibernateJpaAutoConfiguration.class})
public class ProxyMain {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ProxyMain.class, args);
    }
}
