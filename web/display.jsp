<%--
  Created by IntelliJ IDEA.
  User: lixiang
  Date: 3/8/2017
  Time: 4:11 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="SearchEngine.Dao.PageDao" %>
<html>
<head>
    <title>
        <%= "Results of <" + request.getAttribute("query") + ">" %>
    </title>
    <link rel="stylesheet" href="../CSS/display.css" type="text/css" />
</head>
<body>
    <div id="pageList">
    <%
        List<PageDao> list = (List<PageDao>) request.getAttribute("list");
        for(PageDao item : list){
            %>
            <div class="item">
                <h1 class="title"><% out.print(item.getTitle());%></h1>
                <p class="totalScore"><% out.print("Total Score : " + item.getTotalScore());%></p>
                <a class="url" href = "<% out.print(item.getUrl()); %> " ><% out.print(item.getUrl()); %></a>
            </div>
            <%
        }
    %>
    </div>
</body>
</html>
