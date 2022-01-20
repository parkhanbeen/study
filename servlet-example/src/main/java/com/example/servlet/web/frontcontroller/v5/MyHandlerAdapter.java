package com.example.servlet.web.frontcontroller.v5;

import com.example.servlet.web.frontcontroller.ModelView;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface MyHandlerAdapter {

  boolean supports(Object Handler);

  ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
      ServletException, IOException;
}
