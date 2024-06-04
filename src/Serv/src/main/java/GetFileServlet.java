package Serv.src.main.java;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class GetFileServlet extends HttpServlet {
    private final int BYTE_SIZE = 256*10;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        final String fileName = req.getParameter("name");
        final String path = "/WEB-INF/doc/" + fileName;

        resp.setContentType("text/plain");
        resp.setHeader("Content-disposition", "attachment; filename=" + fileName);
        resp.addHeader("Access-Control-Allow-Origin", "*");

        try(InputStream in = req.getServletContext().getResourceAsStream(path);
            OutputStream out = resp.getOutputStream()) {

            byte[] buffer = new byte[BYTE_SIZE];

            int numBytesRead;
            while ((numBytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, numBytesRead);
            }
        }
    }
}
