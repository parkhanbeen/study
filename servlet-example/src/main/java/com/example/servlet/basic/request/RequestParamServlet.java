package com.example.servlet.basic.request;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "requestParamServlet", urlPatterns = "/request-param")
public class RequestParamServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    request.getParameterNames()
        .asIterator()
        .forEachRemaining(
            name -> System.out.println(name + " :" + request.getParameter(name))
        );

    // key에 여러가지 값이 있을 경우
    String[] names = request.getParameterValues("name");
    for (String name : names) {
      System.out.println("name = " + name);
    }

  }
}
