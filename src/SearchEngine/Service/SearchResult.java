package SearchEngine.Service;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lixiang on 3/7/2017.
 */

public class SearchResult extends HttpServlet{

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

        String query = request.getParameter("query");

        PageService results = new PageService();
        List<PageDao> list = results.getPageList(query, 10);

        request.setAttribute("list", list);
        request.setAttribute("query", query);
        RequestDispatcher view=request.getRequestDispatcher("display.jsp");
        view.forward(request,response);

        /*
        PrintWriter out = response.getWriter();
        for(String item : list){
            out.println(item);
        }
        */

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
