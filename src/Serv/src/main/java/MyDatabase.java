package Serv.src.main.java;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MyDatabase
{
    static List<SearchResult> QueryBestMatch(String filter)
    {
        Connection c = null;
        Statement stmt = null;
        List<SearchResult> res = new ArrayList<>();

        try
        {
            Class.forName("org.postgresql.Driver");

            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/Indexer",
                            "postgres", "4444");
            c.setAutoCommit(false);

            stmt = c.createStatement();

            ResultSet rs = stmt.executeQuery( """
            SELECT t_files.name as file, t_links.cnt as count\s
            FROM t_links
            JOIN t_files ON t_links.file = t_files.id
            WHERE t_links.word IN (
                SELECT id
                FROM t_words
                WHERE word = '""" + filter + """
            ')
            ORDER BY t_links.cnt DESC
            LIMIT 100;""");

            while (rs.next())
            {
                SearchResult result = new SearchResult();

                File file = new File(rs.getString("file"));

                result.file = file.getName();
                result.count = rs.getInt("count");
                res.add(result);
            }

            rs.close();
            stmt.close();
            c.close();
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return res;
    }
}
