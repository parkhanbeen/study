package exception.example;

import exception.example.formatter.MyNumberFormatter;
import javax.servlet.DispatcherType;
import java.util.List;

import exception.example.converter.IntegerToStringConverter;
import exception.example.converter.IpPortToStringConverter;
import exception.example.converter.StringToIntegerConverter;
import exception.example.converter.StringToIpPortConverter;
import exception.example.filter.LogFilter;
import exception.example.interceptor.LogInterceptor;
import exception.example.resolver.MyHandlerExceptionResolver;
import exception.example.resolver.UserHandlerExceptionResolver;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addFormatters(FormatterRegistry registry) {
//    registry.addConverter(new StringToIntegerConverter());
//    registry.addConverter(new IntegerToStringConverter());
    registry.addConverter(new StringToIpPortConverter());
    registry.addConverter(new IpPortToStringConverter());

    registry.addFormatter(new MyNumberFormatter());
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new LogInterceptor())
        .order(1)
        .addPathPatterns("/**")
        .excludePathPatterns("/css/**", "/*.ico"
            , "/error", "/error-page/**");
  }

  @Override
  public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
    resolvers.add(new MyHandlerExceptionResolver());
    resolvers.add(new UserHandlerExceptionResolver());
  }

  //  @Bean
  public FilterRegistrationBean logFilter() {
    FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
    filterRegistrationBean.setFilter(new LogFilter());
    filterRegistrationBean.setOrder(1);
    filterRegistrationBean.addUrlPatterns("/*");
    filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);
    return filterRegistrationBean;
  }
}
