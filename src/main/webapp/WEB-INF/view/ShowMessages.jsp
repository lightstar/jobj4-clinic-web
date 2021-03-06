<%--@elvariable id="prefix" type="java.lang.String"--%>
<%--@elvariable id="messages" type="java.util.List<ru.lightstar.clinic.model.Message>"--%>
<%--@elvariable id="_csrf" type="org.springframework.security.web.csrf.CsrfToken"--%>
<%--@elvariable id="me" type="java.lang.String"--%>

<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="title" value="Messages" scope="page"/>
<c:set var="current" value="main" scope="page"/>
<%@include file="Header.jsp" %>

<c:if test="${param.name != me}">
<p>
    This is messages of client <b><c:out value="${param.name}"/></b>
</p>
</c:if>

<form action="<c:url value='${prefix}/client/message/add'/>" method="post" class="above-list message-form"
      onsubmit="return validateForm(this);">
    <label for="text">Text:</label>
    <textarea id="text" name="text"><c:out value="${param.text}"/></textarea>

    <input type="hidden" name="name" value="<c:out value='${param.name}'/>"/>
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
    <input type="submit" class="button" value="Add" style="vertical-align: top;">
</form>

<c:if test="${messages.size() == 0}">
    <p>
        No messages found.
    </p>
</c:if>

<c:if test="${messages.size() > 0}">
    <table class="list message-list">
        <tr>
            <th>Message</th>
            <th>&nbsp;</th>
        </tr>
        <c:forEach items="${messages}" var="message">
            <tr>
                <td>
                    ${message.formattedText}
                </td>
                <td>
                    <a href="#" onclick="deleteMessage('<c:out value="${param.name}"/>',
                            '<c:out value="${message.id}"/>', '${_csrf.parameterName}',
                            '${_csrf.token}');">Delete</a>
                </td>
            </tr>
        </c:forEach>
    </table>
</c:if>

<%@include file="Footer.jsp" %>