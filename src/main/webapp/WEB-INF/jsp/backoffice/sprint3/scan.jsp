<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Scan des données - Back-Office Visa Madagascar</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/sidebar-fix.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/sprint3/scan.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/modern.css">
</head>
<body>
<script>
    window.APP_CONTEXT_PATH = '${pageContext.request.contextPath}';
    // Récupérer l'ID de la demande depuis l'URL
    const urlParams = new URLSearchParams(window.location.search);
    window.DEMANDE_ID = urlParams.get('id');
</script>
<div class="layout-shell">
    <%@ include file="/WEB-INF/jsp/backoffice/common/fragments/sidebar.jspf" %>
    <div class="main-area">
        <%@ include file="/WEB-INF/jsp/backoffice/common/fragments/topbar.jspf" %>

        <div class="panel">
            <div class="panel-header">
                <h2>Scan des données</h2>
                <div id="demande-info" class="hidden">
                    <span class="tag">Demande #<span id="demande-id"></span></span>
                </div>
                <button id="start-scan" class="button">Lancer le scan</button>
            </div>

            <div id="scan-container" class="scan-container hidden">
                <div class="scan-animation">
                    <div class="scan-line"></div>
                    <div class="scan-progress">
                        <div class="progress-bar"></div>
                    </div>
                    <div class="scan-status">Initialisation...</div>
                </div>

                <div id="scan-results" class="scan-results hidden">
                    <h3>Résultats du scan</h3>
                    <div class="scan-summary">
                        <div class="summary-item">
                            <span class="label">Total scanné:</span>
                            <span class="value" id="total-scanned">0</span>
                        </div>
                        <div class="summary-item">
                            <span class="label">Erreurs trouvées:</span>
                            <span class="value error" id="total-errors">0</span>
                        </div>
                        <div class="summary-item">
                            <span class="label">Logique métier respectée:</span>
                            <span class="value success" id="business-logic-ok">0%</span>
                        </div>
                    </div>

                    <div id="scan-errors" class="scan-errors"></div>
                    <div id="scan-details" class="scan-details"></div>
                    <div style="margin-top:12px;">
                        <button id="scan-complete" class="button success hidden">Scan terminé</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="toast" class="toast hidden"></div>
<script src="${pageContext.request.contextPath}/js/sprint3/scan.js"></script>
</body>
</html>
