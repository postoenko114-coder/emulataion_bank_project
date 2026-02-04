const API_BASE = '/api/v1';

// === 1. INITIALIZATION ===
document.addEventListener('DOMContentLoaded', async () => {
    checkAuth();
    loadExchangeRates();

    if (document.getElementById('services-container')) loadServices();
    if (document.getElementById('branches-container')) loadBranches();

    const rateInput = document.getElementById('calc-rate');
    if (rateInput) {
        rateInput.addEventListener('input', calculateLoan);
        calculateLoan();
    }
});

// === 2. SERVICES LOGIC ===
let allServices = [];

async function loadServices() {
    const container = document.getElementById('services-container');
    if (!container) return;

    try {
        const response = await fetch(`${API_BASE}/services`);
        if (!response.ok) throw new Error('Network response was not ok');
        allServices = await response.json();
        renderServices(allServices);
    } catch (e) {
        container.innerHTML = '<div class="col-12 text-center text-danger">Unable to load services at this time.</div>';
    }
}

async function searchService() {
    const name = document.getElementById('service-search').value.trim();
    if (!name) {
        loadServices();
        return;
    }
    try {
        const response = await fetch(`${API_BASE}/services/filter/name?name=${encodeURIComponent(name)}`);
        if (response.ok) {
            const service = await response.json();
            // API возвращает один объект или массив, приводим к массиву
            renderServices(Array.isArray(service) ? service : [service]);
        } else {
            document.getElementById('services-container').innerHTML = '<div class="col-12 text-center text-muted py-5">No services found matching your criteria.</div>';
        }
    } catch (e) {
        console.error(e);
    }
}

function renderServices(list) {
    const container = document.getElementById('services-container');
    container.innerHTML = '';

    if (!list || list.length === 0) {
        container.innerHTML = '<div class="col-12 text-center text-muted">No services found.</div>';
        return;
    }

    list.forEach((s) => {
        let icon = 'fa-sack-dollar';
        let colorClass = 'text-primary';
        const name = s.bankServiceName.toLowerCase();

        if (name.includes('loan')) { icon = 'fa-hand-holding-dollar'; colorClass = 'text-warning'; }
        else if (name.includes('card')) { icon = 'fa-credit-card'; colorClass = 'text-info'; }
        else if (name.includes('insurance')) { icon = 'fa-shield-heart'; colorClass = 'text-danger'; }

        container.innerHTML += `
            <div class="col-md-6 col-lg-4">
                <div class="card h-100 p-4 hover-card border-0" onclick="openServiceDetails(${s.id})">
                    <div class="${colorClass} mb-4" style="font-size: 2.2rem;"><i class="fa-solid ${icon}"></i></div>
                    <h5 class="fw-bold mb-3">${s.bankServiceName}</h5>
                    <p class="text-muted small text-truncate" style="max-height: 4.5em; overflow: hidden;">
                        ${s.description || 'No description available'}
                    </p>
                    <div class="mt-auto pt-3 border-top d-flex justify-content-between align-items-center">
                        <small class="fw-bold text-muted"><i class="fa-regular fa-clock me-1"></i> ${s.duration}</small>
                        <span class="text-primary small fw-bold">Details <i class="fa-solid fa-arrow-right ms-1"></i></span>
                    </div>
                </div>
            </div>`;
    });
}

async function openServiceDetails(id) {
    let service = allServices.find(s => s.id === id);
    if (!service) {
        try {
            const res = await fetch(`${API_BASE}/services/${id}`);
            service = await res.json();
        } catch (e) { return; }
    }

    document.getElementById('modal-service-name').textContent = service.bankServiceName;
    document.getElementById('modal-service-desc').textContent = service.description;
    document.getElementById('modal-service-duration').textContent = service.duration;

    new bootstrap.Modal(document.getElementById('serviceDetailsModal')).show();
}

// === 3. BRANCHES LOGIC ===
let allBranches = [];

function findNearestBranches() {
    const statusEl = document.getElementById('geo-status');
    if (!navigator.geolocation) {
        showStatus('Geolocation not supported', 'text-danger');
        return;
    }

    showStatus('Locating...', 'text-primary');

    navigator.geolocation.getCurrentPosition(async (position) => {
        const { latitude, longitude } = position.coords;
        try {
            const response = await fetch(`${API_BASE}/branches/filter/nearest?latitude=${latitude}&longitude=${longitude}`);
            if (!response.ok) throw new Error();

            allBranches = await response.json();
            renderBranches(allBranches);

            showStatus('Nearest branches found!', 'text-success');
            document.getElementById('branch-search').value = '';
        } catch (e) {
            showStatus('Error finding branches.', 'text-danger');
        }
    }, () => {
        showStatus('Location access denied.', 'text-danger');
    });
}

async function loadBranches() {
    const container = document.getElementById('branches-container');
    if (!container) return;

    showStatus('', '');
    const query = document.getElementById('branch-search')?.value || '';

    let url = `${API_BASE}/branches`;
    if (query) {
        url = `${API_BASE}/branches/filter/location?city=${encodeURIComponent(query)}&address=${encodeURIComponent(query)}`;
    }

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error();
        allBranches = await response.json();
        renderBranches(allBranches);
    } catch (e) {
        container.innerHTML = '<div class="col-12 text-center text-danger">Failed to load branches.</div>';
    }
}

function renderBranches(branches) {
    const container = document.getElementById('branches-container');
    container.innerHTML = '';

    if (!branches || branches.length === 0) {
        container.innerHTML = '<div class="col-12 text-center text-muted">No branches found matching criteria.</div>';
        return;
    }

    branches.forEach(b => {
        container.innerHTML += `
            <div class="col-md-6">
                <div class="card h-100 p-4 hover-card border-0" onclick="openBranchDetails(${b.id})">
                    <div class="d-flex align-items-center">
                        <div class="bg-light rounded-circle p-3 me-3 text-primary">
                            <i class="fa-solid fa-building-columns fa-lg"></i>
                        </div>
                        <div class="flex-grow-1">
                            <h6 class="fw-bold mb-1">${b.bankBranchName}</h6>
                            <p class="text-muted small mb-0">
                                <i class="fa-solid fa-location-dot me-1"></i> ${b.locationDTO?.city}, ${b.locationDTO?.address}
                            </p>
                        </div>
                        <div class="text-muted ms-2"><i class="fa-solid fa-chevron-right"></i></div>
                    </div>
                </div>
            </div>`;
    });
}

function openBranchDetails(id) {
    const branch = allBranches.find(b => b.id === id);
    if (!branch) return;

    document.getElementById('modal-branch-name').textContent = branch.bankBranchName;

    const addrText = branch.locationDTO
        ? `${branch.locationDTO.address}, ${branch.locationDTO.city}`
        : "Address not available";
    document.getElementById('modal-branch-addr').textContent = addrText;

    const servicesContainer = document.getElementById('modal-branch-services');
    servicesContainer.innerHTML = '';

    if (branch.bankServices && branch.bankServices.length > 0) {
        branch.bankServices.forEach(service => {
            const serviceName = (typeof service === 'object') ? service.bankServiceName : service;

            const badge = `
                <div class="d-inline-flex align-items-center px-3 py-2" 
                     style="background-color: #f1f2f6; color: var(--primary); border-radius: 50px; font-weight: 600; font-size: 0.85rem;">
                    <i class="fa-solid fa-check-circle me-2" style="opacity: 0.7;"></i>
                    ${serviceName}
                </div>
            `;
            servicesContainer.innerHTML += badge;
        });
    } else {
        servicesContainer.innerHTML = `<span style="color: var(--text-muted); font-size: 0.9rem;">No services listed for this branch.</span>`;
    }

    const scheduleContainer = document.getElementById('modal-branch-schedule');
    scheduleContainer.innerHTML = '';

    if (branch.schedule && branch.schedule.length > 0) {
        branch.schedule.forEach(day => {
            scheduleContainer.innerHTML += `
                <div class="d-flex justify-content-between mb-2 pb-1 border-bottom" style="border-color: #f1f2f6 !important;">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">${day.dayOfWeek}</span>
                    <span class="fw-bold" style="color: var(--text-main); font-size: 0.9rem;">${day.openTime} - ${day.closeTime}</span>
                </div>`;
        });
    } else {
        scheduleContainer.innerHTML = '<p style="color: var(--text-muted);">Schedule unavailable.</p>';
    }

    const mapBtn = document.getElementById('btn-google-maps');
    if (mapBtn) {
        const query = encodeURIComponent(addrText);
        mapBtn.href = `https://www.google.com/maps/search/?api=1&query=${query}`;
    }

    new bootstrap.Modal(document.getElementById('branchDetailsModal')).show();
}

// === 4. UTILITIES & HELPERS ===
function showStatus(msg, className) {
    const el = document.getElementById('geo-status');
    if (el) {
        el.textContent = msg;
        el.className = `mt-2 small ${className}`;
    }
}

async function checkAuth() {
    const token = sessionStorage.getItem('accessToken');
    const authContainer = document.getElementById('auth-buttons');
    if (token) {
        try {
            const response = await fetch('/api/auth/me', { headers: { 'Authorization': `Bearer ${token}` } });
            if (response.ok) {
                const user = await response.json();
                if (authContainer) authContainer.innerHTML = `<a href="dashboard.html" class="btn btn-primary-custom">My Dashboard</a>`;
                const emailInput = document.getElementById('supp-email');
                if (emailInput) { emailInput.value = user.email; emailInput.readOnly = true; }
            }
        } catch (e) { /* Silent fail */ }
    }
}

async function loadExchangeRates() {
    if (!document.getElementById('rate-eur-usd')) return;
    try {
        const [resEur, resUsd] = await Promise.all([
            fetch('https://api.frankfurter.app/latest?from=EUR&to=USD,CZK'),
            fetch('https://api.frankfurter.app/latest?from=USD&to=CZK')
        ]);

        const dataEur = await resEur.json();
        const dataUsd = await resUsd.json();

        document.getElementById('rate-eur-usd').textContent = dataEur.rates.USD.toFixed(3);
        document.getElementById('rate-eur-czk').textContent = dataEur.rates.CZK.toFixed(2);
        document.getElementById('rate-usd-czk').textContent = dataUsd.rates.CZK.toFixed(2);
    } catch (e) { console.error('Rates error', e); }
}

async function sendSupport() {
    const email = document.getElementById('supp-email').value;
    const msg = document.getElementById('supp-msg').value;
    const sub = document.getElementById('supp-subject').value;

    if (!email || !msg) return alert("Please fill in all fields");

    try {
        await fetch(`${API_BASE}/support`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userEmail: email, subject: sub, message: msg, createdAt: new Date().toISOString() })
        });
        alert("Message sent successfully!");
        bootstrap.Modal.getInstance(document.getElementById('supportModal')).hide();
        document.getElementById('supportForm').reset();
    } catch (e) { alert("Error sending message."); }
}

function updateTermLabel(val) {
    document.getElementById('term-label').textContent = val + " Months";
    calculateLoan();
}

function calculateLoan() {
    const amount = parseFloat(document.getElementById('calc-amount').value);
    const months = parseInt(document.getElementById('calc-term').value);
    const rate = parseFloat(document.getElementById('calc-rate').value);

    if (!amount || !rate) {
        document.getElementById('calc-result').textContent = "0.00";
        return;
    }

    const r = rate / 100 / 12;
    const monthlyPayment = (amount * r * Math.pow(1 + r, months)) / (Math.pow(1 + r, months) - 1);

    document.getElementById('calc-result').textContent = monthlyPayment.toFixed(2);
    document.getElementById('calc-total').textContent = (monthlyPayment * months).toFixed(2);
}