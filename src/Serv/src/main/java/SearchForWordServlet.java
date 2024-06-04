package Serv.src.main.java;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class SearchForWordServlet extends HttpServlet
{
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        final String textToSearch = req.getParameter("text");
        List<SearchResult> lst = MyDatabase.QueryBestMatch(textToSearch.toLowerCase());

        Gson jsonBuilder = new GsonBuilder().disableHtmlEscaping().create();
        String employeeJsonString = jsonBuilder.toJson(lst);

        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        res.addHeader("Access-Control-Allow-Origin", "*");

        PrintWriter out = res.getWriter();
        out.print(employeeJsonString);
        out.flush();
    }
}
