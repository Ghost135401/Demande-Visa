<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>QR Code - Back-Office Visa Madagascar</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/sidebar-fix.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/modern.css">
</head>
<body>
<div class="layout-shell">
    <%@ include file="/WEB-INF/jsp/backoffice/common/fragments/sidebar.jspf" %>
    <div class="main-area">
        <%@ include file="/WEB-INF/jsp/backoffice/common/fragments/topbar.jspf" %>

        <main>
            <section class="panel">
                <div class="panel-header">
                    <div>
                        <p class="eyebrow">Verification documentaire</p>
                        <h2>Generation des QR codes</h2>
                    </div>
                </div>
                <p>Les QR codes sont generes depuis le dashboard et les workflows metier.</p>
                <p>Cette page centralise les controles QR et la lecture des informations encodees pour chaque dossier.</p>
            </section>
        </main>
    </div>
</div>
</body>
</html>
