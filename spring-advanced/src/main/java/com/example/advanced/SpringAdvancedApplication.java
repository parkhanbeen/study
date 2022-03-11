package com.example.advanced;

import com.example.advanced.proxy.config.AppV1Config;
import com.example.advanced.proxy.config.AppV2Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({AppV1Config.class, AppV2Config.class})
@SpringBootApplication(scanBasePackages = "com.example.advanced.proxy.app")
public class SpringAdvancedApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringAdvancedApplication.class, args);
  }

}
