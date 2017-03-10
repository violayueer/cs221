<%--
  Created by IntelliJ IDEA.
  User: lixiang
  Date: 3/7/2017
  Time: 3:24 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Search Engine</title>
    <link href="../CSS/index.css" rel="stylesheet" type="text/css">
</head>

<body>
    <div id="searchDiv">
         <h1>ICS Search Engine </h1>
         <form action="${pageContext.request.contextPath}/servlet" method="GET">
             <input type = "text" name = "query">
         </form>
    </div>
</body>

</html>
