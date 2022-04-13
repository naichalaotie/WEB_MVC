
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>tets2</title>
</head>
<body>
<%System.out.println("test.jsp页面的输出语句。str1参数的值："+request.getAttribute("str1"));%>
<%=request.getParameter("str1")%>
<%=request.getAttribute("str1")%>
</body>
</html>
