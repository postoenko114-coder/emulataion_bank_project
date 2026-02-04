document.addEventListener('DOMContentLoaded', () => {
    const regForm = document.querySelector('form');
    if (regForm) {
        regForm.addEventListener('submit', handleRegister);
    }
});

async function handleRegister(event) {
    event.preventDefault();

    const username = document.getElementById('reg-username').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const btn = document.querySelector('button[type="submit"]');

    if (!username || !email || !password) return alert("Please fill all fields");

    // UI Feedback
    const originalBtnText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Creating...';

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });

        if (response.ok) {
            const data = await response.json();
            sessionStorage.setItem('accessToken', data.token);
            window.location.href = 'dashboard.html';
        } else {
            if(response.status === 409){
                alert("Error: user with this email already exists!");
            }
            const err = await response.json();
            alert("Registration failed: " + (err.message || "Something went wrong"));
        }
    } catch (error) {
        console.error('Registration error:', error);
        alert("Network error. Please check your connection.");
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalBtnText;
    }
}