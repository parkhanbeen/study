package com.helloboot.parkhanbeen;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class HellobootApplication {

    public static void main(String[] args) {
        ServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();
        WebServer webServer = serverFactory.getWebServer(servletContext -> {
          servletContext.addServlet("frontController", new HttpServlet() {
              @Override
              protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

                  if (req.getRequestURI().equals("/hello") && req.getMethod().equals(HttpMethod.GET.name())) {
                      String name = req.getParameter("name");

                      resp.setStatus(HttpStatus.OK.value());
                      resp.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
                      resp.getWriter().println("hello " + name);
                  } else if (req.getRequestURI().equals("/user")) {
                      resp.setStatus(HttpStatus.NOT_FOUND.value());

                  } else {

                  }


              }
          }).addMapping("/*");

        });
        webServer.start();
    }

}
