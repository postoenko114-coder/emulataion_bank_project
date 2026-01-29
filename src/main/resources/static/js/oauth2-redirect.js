document.addEventListener('DOMContentLoaded', () => {
    // Получаем параметры из URL
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const error = params.get('error');

    if (token) {
        // Успех: Сохраняем токен и редиректим в личный кабинет
        sessionStorage.setItem('accessToken', token);

        // Небольшая задержка для плавности UX (опционально)
        setTimeout(() => {
            window.location.href = '/dashboard.html';
        }, 500);
    } else {
        // Ошибка: Редиректим обратно на логин с ошибкой
        const errorMsg = error || 'Authentication failed';
        window.location.href = '/login.html?error=' + encodeURIComponent(errorMsg);
    }
});