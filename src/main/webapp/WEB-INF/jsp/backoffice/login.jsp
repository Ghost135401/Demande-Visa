<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connexion - Backoffice</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/modern.css">
</head>
<body class="login-page">
<div class="login-shell">
    <h1>Connexion Back-Office</h1>
    <form id="login-form" action="${pageContext.request.contextPath}/auth/login" method="post">
        <label>Nom d'utilisateur
            <input type="text" name="username" required>
        </label>
        <label>Mot de passe
            <input type="password" name="password" required>
        </label>
        <div class="form-actions">
            <button class="button primary important-blue" type="submit">Se connecter</button>
            <a class="button link" href="${pageContext.request.contextPath}/backoffice">Retour</a>
        </div>
    </form>
    <div id="login-error" class="error hidden"></div>
</div>
<script>
    // submit as JSON to REST endpoint and handle session storage
    document.getElementById('login-form').addEventListener('submit', async function (ev) {
        ev.preventDefault();
        const form = ev.target;
        const data = {username: form.username.value, password: form.password.value};
        try {
            const r = await fetch(form.action, {
                method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data)
            });
            if (!r.ok) throw new Error('Echec');
            const j = await r.json();
            if (j.success) {
                window.location.href = '${pageContext.request.contextPath}/backoffice';
            } else {
                document.getElementById('login-error').textContent = j.message; document.getElementById('login-error').classList.remove('hidden');
            }
        } catch (e) {
            document.getElementById('login-error').textContent = 'Erreur pendant l\'authentification'; document.getElementById('login-error').classList.remove('hidden');
        }
    })
</script>
</body>
</html>
