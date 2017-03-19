<%--
  Created by IntelliJ IDEA.
  User: lixiang
  Date: 3/8/2017
  Time: 4:11 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Searching.PageDao" %>
<%@ page import="org.jsoup.Jsoup" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.util.List" %>
<html>
<head>
    <title>
        <%= "Results of <" + request.getAttribute("query") + ">" %>
    </title>
    <link rel="stylesheet" href="../CSS/display.css" type="text/css" />
</head>

<body>
<div id="header">
	<img id="logo" src="logo.png" />
	<form action="${pageContext.request.contextPath}/servlet" method="GET">
        <input id = "inputText" type = "text" name = "query">
		<input id = "inputBtn" type = "submit" value="Go!">
    </form>
</div>

<div id="pageList">
    <%
        List<PageDao> list = (List<PageDao>) request.getAttribute("list");
        for(PageDao item : list){
    %>
    <div class="item">
        <h1 class="title"><%= item.getTitle()%></h1>
        <a class="url" href = "<%= item.getUrl()%> " ><%= item.getUrl() %></a>
        <div class="text">
            <%
                URL uri = new URL("http://" + item.getUrl());
                URLConnection ec = uri.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(ec.getInputStream(), "UTF-8"));
                String inputLine;
                StringBuilder a = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    a.append(inputLine);
                }
                in.close();
                String docText = Jsoup.parse(a.toString()).text();
                String showText = docText.length() <= 300 ? docText : docText.substring(0, 300);
                out.print(showText);
            %>
        </div>
        <p class="totalScore"><%= "Total Score : " + item.getTotalScore()%></p>
    </div>
    <%
        }
    %>
</div>
</body>
</html>
