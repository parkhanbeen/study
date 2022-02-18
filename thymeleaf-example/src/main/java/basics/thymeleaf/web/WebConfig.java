package basics.thymeleaf.web;

import javax.servlet.Filter;

import basics.thymeleaf.web.filter.LogFilter;
import basics.thymeleaf.web.filter.LoginCheckFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

  @Bean
  public FilterRegistrationBean logFilter() {
    FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
    filterRegistrationBean.setFilter(new LogFilter());
    filterRegistrationBean.setOrder(1);
    filterRegistrationBean.addUrlPatterns("/*");
    return filterRegistrationBean;
  }

  @Bean
  public FilterRegistrationBean loginCheckFilter() {
    FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
    filterRegistrationBean.setFilter(new LoginCheckFilter());
    filterRegistrationBean.setOrder(2);
    filterRegistrationBean.addUrlPatterns("/*");
    return filterRegistrationBean;
  }
}
