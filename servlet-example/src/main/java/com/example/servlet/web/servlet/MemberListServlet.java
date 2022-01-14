package com.example.servlet.web.servlet;

import com.example.servlet.domain.member.Member;
import com.example.servlet.domain.member.MemberRepository;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "memberListServlet", urlPatterns = "/servlet/members")
public class MemberListServlet extends HttpServlet {

  private MemberRepository memberRepository = MemberRepository.getInstance();

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType("text/html");
    response.setCharacterEncoding("utf-8");

    List<Member> members = memberRepository.findAll();

    PrintWriter writer = response.getWriter();
    writer.write("<html>");
    writer.write("<head>");
    writer.write(" <meta charset=\"UTF-8\">");
    writer.write(" <title>Title</title>");
    writer.write("</head>");
    writer.write("<body>");
    writer.write("<a href=\"/index.html\">메인</a>");
    writer.write("<table>");
    writer.write(" <thead>");
    writer.write(" <th>id</th>");
    writer.write(" <th>username</th>");
    writer.write(" <th>age</th>");
    writer.write(" </thead>");
    writer.write(" <tbody>");

    /*
   writer.write(" <tr>");
   writer.write(" <td>1</td>");
   writer.write(" <td>userA</td>");
   writer.write(" <td>10</td>");
   writer.write(" </tr>");
    */
    for (Member member : members) {
      writer.write(" <tr>");
      writer.write(" <td>" + member.getId() + "</td>");
      writer.write(" <td>" + member.getUsername() + "</td>");
      writer.write(" <td>" + member.getAge() + "</td>");
      writer.write(" </tr>");
    }
    writer.write(" </tbody>");
    writer.write("</table>");
    writer.write("</body>");
    writer.write("</html>");

  }
}
