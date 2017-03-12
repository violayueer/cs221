
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.net.URL" %>
<%@ page import="org.jsoup.Jsoup" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>


<%--
  Created by IntelliJ IDEA.
  User: Yue
  Date: 3/11/17
  Time: 03:08
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>

<head>
    <title>Title</title>
</head>
<body>
<div>
    <%
        URL uri = new URL("http://www.ics.uci.edu");
        URLConnection ec = uri.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(ec.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder a = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            a.append(inputLine);
            out.println(inputLine);
        }
        in.close();

        String docText = Jsoup.parse(a.toString()).body().;
        out.print(docText);
    %>


</div>


</body>
</html>
