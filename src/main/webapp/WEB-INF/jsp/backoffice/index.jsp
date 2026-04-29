<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Back-Office Visa Madagascar</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/sidebar-fix.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/modern.css">
</head>
<body>
<script>
    window.APP_CONTEXT_PATH = '${pageContext.request.contextPath}';
</script>
<div class="layout-shell">
    <%@ include file="/WEB-INF/jsp/backoffice/common/fragments/sidebar.jspf" %>
    <div class="main-area">
        <%@ include file="/WEB-INF/jsp/backoffice/common/fragments/topbar.jspf" %>
        <%@ include file="/WEB-INF/jsp/backoffice/common/fragments/dashboard.jspf" %>
        <%@ include file="/WEB-INF/jsp/backoffice/common/fragments/forms.jspf" %>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/backoffice/common/fragments/dialogs.jspf" %>
<div id="toast" class="toast hidden"></div>
<script src="${pageContext.request.contextPath}/js/common/backoffice.js"></script>
</body>
</html>
