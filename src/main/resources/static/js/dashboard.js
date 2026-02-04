/**
 * =========================================================
 * CLIENT DASHBOARD CONTROLLER
 * =========================================================
 */

// Configuration
const API_BASE = '/api/v1';
let currentUserId = null;
let jwtToken = null;
const PAGE_SIZE = 10;

// Data Stores
window.userAccounts = [];
window.userCards = [];
window.userTransactions = [];
window.userNotifications = [];
window.currentOperation = 'TRANSFER';

// =========================================================
// 1. INITIALIZATION & AUTH CHECK
// =========================================================
document.addEventListener('DOMContentLoaded', async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const tokenFromUrl = urlParams.get('token');

    if (tokenFromUrl) {
        sessionStorage.setItem('accessToken', tokenFromUrl);
        jwtToken = tokenFromUrl;
        // Clean URL
        window.history.replaceState({}, document.title, window.location.pathname);
    } else {
        jwtToken = sessionStorage.getItem('accessToken');
    }

    if (!jwtToken) {
        window.location.href = 'index.html'; // Redirect to login
        return;
    }

    console.log("Authenticating...");
    const userLoaded = await loadUserInfo();

    if (userLoaded && currentUserId) {
        console.log(`User ID: ${currentUserId}. App started.`);

        updateNotificationBadge();
        fetchAccountsForFilter();

        // Default load: Accounts page
        const accountsBtn = document.querySelector("a[onclick*='accounts']");
        if (accountsBtn) loadPage('accounts', accountsBtn);
    } else {
        showError("Authentication failed. Please login again.");
        setTimeout(() => logout(), 2000);
    }
});

// =========================================================
// 2. NETWORK UTILITIES
// =========================================================
async function authFetch(url, options = {}) {
    const headers = options.headers || {};
    headers['Authorization'] = `Bearer ${jwtToken}`;

    if (!headers['Content-Type'] && !(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }

    try {
        const response = await fetch(url, { ...options, headers });
        if (response.status === 401 || response.status === 403) {
            logout();
            return null;
        }
        return response;
    } catch (error) {
        console.error("Network error:", error);
        return null;
    }
}

// =========================================================
// 3. ROUTER / PAGE LOADER
// =========================================================
function loadPage(pageName, element) {
    if (!currentUserId) return;

    if (element) {
        document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
        element.classList.add('active');
    }

    const title = document.getElementById('page-title');
    const container = document.getElementById('content-area');
    const filterPanel = document.getElementById('transaction-filters');

    container.innerHTML = '<div class="col-12 text-center mt-5"><div class="spinner-border text-primary"></div></div>';

    if (filterPanel) {
        if (pageName === 'transactions') filterPanel.classList.remove('d-none');
        else filterPanel.classList.add('d-none');
    }

    switch (pageName) {
        case 'accounts':
            title.textContent = 'Accounts Overview';
            loadAccounts();
            break;
        case 'transfer':
            title.textContent = 'Operations';
            loadTransferPage();
            break;
        case 'cards':
            title.textContent = 'My Cards';
            loadCards();
            break;
        case 'transactions':
            title.textContent = 'Transaction History';
            loadTransactions(0);
            break;
        case 'reservations':
            title.textContent = 'My Reservations';
            loadReservations();
            break;
        case 'notifications':
            title.textContent = 'Notifications';
            loadNotifications(0);
            break;
        case 'profile':
            title.textContent = 'Profile Settings';
            loadProfile();
            break;
    }
}

// =========================================================
// 4. DATA LOGIC & UI BUILDERS
// =========================================================

async function loadUserInfo() {
    try {
        const response = await authFetch('/api/auth/me');
        if (!response || !response.ok) return false;
        const user = await response.json();
        currentUserId = user.id;

        document.getElementById('user-name-display').textContent = user.username || "Client";
        const avatarContainer = document.getElementById('user-avatar-container');

        if (avatarContainer) {
            if (user.photoUrl) {
                avatarContainer.innerHTML = `<img src="${user.photoUrl}" style="width: 100%; height: 100%; object-fit: cover;">`;
            } else {
                const letter = (user.username || "U").charAt(0).toUpperCase();
                avatarContainer.innerHTML = `<span style="font-weight:700; font-size: 1.2rem;">${letter}</span>`;
            }
        }
        return true;
    } catch (e) {
        return false;
    }
}

// --- CARDS ---

async function loadCards() {
    try {
        const response = await authFetch(`${API_BASE}/${currentUserId}/cards`);
        if (!response) return;
        const cards = await response.json();
        window.userCards = cards;

        const container = document.getElementById('content-area');
        container.innerHTML = `
            <div class="col-12 mb-3 text-end">
                <button class="btn btn-primary" onclick="prepareAddCardModal()">
                    <i class="fa-solid fa-plus me-2"></i>Order New Card
                </button>
            </div>
            <div class="row g-4" id="cards-grid"></div>
        `;
        const grid = document.getElementById('cards-grid');

        if (cards.length === 0) {
            grid.innerHTML = '<div class="col-12 text-center text-muted">No cards found.</div>';
            return;
        }

        cards.forEach((card, index) => {
            const currentStatus = card.statusCard || card.status;
            const isClosed = currentStatus === 'CLOSED';

            // Visual Logic
            const bgStyle = isClosed
                ? 'background: #6c757d;'
                : 'background: linear-gradient(135deg, #5f27cd 0%, #a55eea 100%);';

            const cardTypeDisplay = isClosed ? 'CLOSED' : (card.typeCard || 'DEBIT');
            const opacityClass = isClosed ? 'opacity-75' : '';

            grid.innerHTML += `
                <div class="col-md-6 col-lg-4">
                    <div class="info-card text-white clickable-card ${opacityClass}" onclick="openCardDetails(${index})"
                         style="${bgStyle} min-height: 220px; display:flex; flex-direction:column; justify-content:space-between; border:none; border-radius: 20px;">
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="opacity-75 text-uppercase fw-bold" style="font-size: 0.8rem;">${cardTypeDisplay}</span>
                            <i class="fa-brands fa-cc-visa fa-2x"></i>
                        </div>
                        <div class="fs-4 mt-3" style="font-family: monospace; letter-spacing: 2px;">${formatCardNumber(card.cardNumber)}</div>
                        <div class="d-flex justify-content-between small opacity-75 mt-3">
                            <span class="text-uppercase">${card.cardHolderName || 'CLIENT'}</span>
                            <span>${formatDate(card.expiryDate)}</span>
                        </div>
                    </div>
                </div>
             `;
        });
    } catch (e) {
        showError(e);
    }
}

function openCardDetails(index) {
    const card = window.userCards[index];
    if (!card) return;

    const currentStatus = card.statusCard || card.status;

    document.getElementById('modal-card-id').value = card.id;
    document.getElementById('modal-card-number').textContent = formatCardNumber(card.cardNumber);
    document.getElementById('modal-card-holder').textContent = card.cardHolderName;
    document.getElementById('modal-card-expiry').textContent = formatDate(card.expiryDate);
    document.getElementById('modal-card-type').textContent = card.typeCard;
    document.getElementById('modal-card-status').textContent = currentStatus;

    // Toggle Close Button
    const closeBtn = document.querySelector('#cardDetailsModal button[onclick="closeCard()"]');
    if (closeBtn) {
        closeBtn.style.display = (currentStatus === 'CLOSED') ? 'none' : 'block';
    }

    new bootstrap.Modal(document.getElementById('cardDetailsModal')).show();
}

function formatCardNumber(num) {
    if (!num) return '**** **** **** ****';
    return num.replace(/(\d{4})/g, '$1 ').trim();
}

async function prepareAddCardModal() {
    const response = await authFetch(`${API_BASE}/${currentUserId}/accounts`);
    const accounts = await response.json();
    const select = document.getElementById('new-card-account');
    select.innerHTML = '';

    let hasActive = false;
    accounts.forEach(acc => {
        if (acc.statusAccount === 'ACTIVE') {
            select.innerHTML += `<option value="${acc.accountNumber}">${acc.accountNumber} (${acc.currency})</option>`;
            hasActive = true;
        }
    });

    if (!hasActive) {
        alert("You need an active account to order a card.");
        return;
    }
    new bootstrap.Modal(document.getElementById('addCardModal')).show();
}

async function createCard() {
    const accNum = document.getElementById('new-card-account').value;
    const type = document.getElementById('new-card-type').value;

    bootstrap.Modal.getInstance(document.getElementById('addCardModal')).hide();

    try {
        const params = new URLSearchParams();
        params.append('accountNumber', accNum);
        params.append('typeCard', type);

        const url = `${API_BASE}/${currentUserId}/cards?${params.toString()}`;
        const response = await authFetch(url, { method: 'POST' });

        if (response && response.ok) {
            alert('Card ordered successfully!');
            loadCards();
        } else {
            const err = await response.json();
            alert('Error: ' + (err.message || 'Failed to create card'));
        }
    } catch (e) {
        alert('System Error');
    }
}

async function closeCard() {
    const cardId = document.getElementById('modal-card-id').value;
    if (!cardId) { alert("Error: Card ID missing."); return; }
    if (!confirm("Are you sure you want to close this card?")) return;

    try {
        const response = await authFetch(`${API_BASE}/${currentUserId}/cards/${cardId}/closeCard`, { method: 'PUT' });
        if (response && response.ok) {
            alert("Card closed successfully.");
            bootstrap.Modal.getInstance(document.getElementById('cardDetailsModal')).hide();
            loadCards();
        } else {
            alert("Error closing card.");
        }
    } catch (e) {
        alert("System Error");
    }
}


// --- ACCOUNTS ---

async function loadAccounts() {
    try {
        const response = await authFetch(`${API_BASE}/${currentUserId}/accounts`);
        if (!response) return;
        const accounts = await response.json();
        window.userAccounts = accounts;

        const container = document.getElementById('content-area');
        container.innerHTML = `
            <div class="col-12 mb-3 text-end">
                <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#addAccountModal">
                    <i class="fa-solid fa-plus me-2"></i>Open New Account
                </button>
            </div>
            <div class="row g-4" id="accounts-grid"></div>
        `;
        const grid = document.getElementById('accounts-grid');

        if (accounts.length === 0) {
            grid.innerHTML = '<div class="col-12 text-center text-muted">No accounts found.</div>';
            return;
        }

        accounts.forEach((acc, index) => {
            let badgeClass = acc.statusAccount === 'ACTIVE' ? 'bg-success' : 'bg-danger';
            grid.innerHTML += `
                <div class="col-md-6 col-lg-4">
                    <div class="info-card h-100 clickable-card" onclick="openAccountDetails(${index})">
                        <div class="d-flex justify-content-between mb-3">
                            <span class="badge bg-primary bg-opacity-10 text-primary px-3 py-2 rounded-pill">${acc.currency}</span>
                            <span class="badge ${badgeClass} small fw-bold">${acc.statusAccount}</span>
                        </div>
                        <div class="card-label">Total Balance</div>
                        <div class="card-value">${acc.balance.toFixed(2)}</div>
                        <div class="mt-4 pt-3 border-top d-flex justify-content-between text-muted small">
                            <span style="font-family: monospace;">${acc.accountNumber}</span>
                            <i class="fa-solid fa-chevron-right text-primary"></i>
                        </div>
                    </div>
                </div>
            `;
        });
    } catch (e) {
        showError(e);
    }
}

function openAccountDetails(index) {
    const acc = window.userAccounts[index];
    if (!acc) return;
    document.getElementById('modal-acc-id').value = acc.id;
    document.getElementById('modal-acc-balance').textContent = acc.balance.toFixed(2);
    document.getElementById('modal-acc-number').textContent = acc.accountNumber;
    document.getElementById('modal-acc-status').textContent = acc.statusAccount;
    document.getElementById('modal-acc-date').textContent = formatDate(acc.createdAt);
    new bootstrap.Modal(document.getElementById('accountDetailsModal')).show();
}

async function createAccount() {
    const currency = document.getElementById('new-acc-currency').value;
    bootstrap.Modal.getInstance(document.getElementById('addAccountModal')).hide();
    try {
        const response = await authFetch(`${API_BASE}/${currentUserId}/accounts?currency=${currency}`, { method: 'POST' });
        if (response && response.ok) {
            loadAccounts();
            fetchAccountsForFilter();
        } else {
            alert('Failed to create account');
        }
    } catch (e) {
        alert('Error');
    }
}

async function closeAccount() {
    const accId = document.getElementById('modal-acc-id').value;
    if (!confirm('Are you sure? Account balance must be 0.')) return;
    bootstrap.Modal.getInstance(document.getElementById('accountDetailsModal')).hide();

    try {
        const response = await fetch(`${API_BASE}/${currentUserId}/accounts/${accId}/closeAccount`, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${jwtToken}` }
        });

        if (response.ok) {
            alert('Account closed.');
            loadAccounts();
        } else if (response.status === 401 || response.status === 403) {
            logout();
        } else {
            const txt = await response.text();
            alert('Error: ' + txt);
        }
    } catch (e) {
        alert('Network Error');
    }
}

// --- OPERATIONS (Transfer, etc.) ---

async function loadTransferPage() {
    try {
        const response = await authFetch(`${API_BASE}/${currentUserId}/accounts`);
        if (!response) return;
        const accounts = await response.json();
        const container = document.getElementById('content-area');

        if (accounts.length === 0) {
            container.innerHTML = '<div class="col-12 text-center text-danger">You need an account to perform operations.</div>';
            return;
        }

        let options = accounts
            .filter(acc => acc.statusAccount === 'ACTIVE')
            .map(acc => `<option value="${acc.id}" data-currency="${acc.currency}">${acc.accountNumber} | ${acc.balance.toFixed(2)} | ${acc.currency}</option>`)
            .join('');

        container.innerHTML = `
            <div class="col-md-8 offset-md-2 col-lg-6 offset-lg-3">
                <div class="info-card">
                    <ul class="nav nav-pills nav-fill mb-4" id="pills-tab">
                        <li class="nav-item"><button class="nav-link active fw-bold" id="tab-transfer" onclick="switchOperation('TRANSFER')">Transfer</button></li>
                        <li class="nav-item"><button class="nav-link fw-bold" id="tab-deposit" onclick="switchOperation('DEPOSIT')">Deposit</button></li>
                        <li class="nav-item"><button class="nav-link fw-bold" id="tab-withdraw" onclick="switchOperation('WITHDRAWAL')">Withdraw</button></li>
                        <li class="nav-item"><button class="nav-link fw-bold" id="tab-payment" onclick="switchOperation('PAYMENT')">Payment</button></li>
                    </ul>

                    <h3 class="text-center fw-bold mb-4" id="op-title">Send Money</h3>

                    <div class="mb-4">
                        <label class="form-label-custom" id="label-account">From Account</label>
                        <select id="op-account" class="form-select form-select-custom shadow-none">${options}</select>
                    </div>

                    <div class="mb-4" id="group-recipient">
                        <label class="form-label-custom">To Account</label>
                        <input type="text" id="op-to" class="form-control form-control-custom shadow-none" placeholder="0000...">
                    </div>

                    <div class="row mb-4">
                        <div class="col-8">
                            <label class="form-label-custom">Amount</label>
                            <input type="text" id="op-amount" class="form-control form-control-custom shadow-none" placeholder="0.00" 
                                   oninput="this.value = this.value.replace(/[^0-9.]/g, '').replace(/(\\..*)\\./g, '$1');">
                        </div>
                         <div class="col-4">
                            <label class="form-label-custom">Currency</label>
                            <input type="text" id="op-currency" class="form-control form-control-custom text-center" value="${accounts[0].currency}" readonly>
                        </div>
                    </div>

                    <div class="mb-4" id="group-desc">
                        <label class="form-label-custom">Description</label>
                        <input type="text" id="op-desc" class="form-control form-control-custom shadow-none" placeholder="Payment details...">
                    </div>

                    <button class="btn btn-primary w-100 fw-bold py-3" onclick="processOperation()" id="btn-action">
                        Send Money <i class="fa-regular fa-paper-plane ms-2"></i>
                    </button>
                </div>
            </div>
        `;

        // Update currency when account changes
        const accSelect = document.getElementById('op-account');
        if (accSelect) {
            accSelect.addEventListener('change', function () {
                const selectedOption = this.options[this.selectedIndex];
                document.getElementById('op-currency').value = selectedOption.getAttribute('data-currency');
            });
        }

        switchOperation('TRANSFER');

    } catch (e) {
        showError(e);
    }
}

function switchOperation(type) {
    window.currentOperation = type;
    document.querySelectorAll('.nav-link').forEach(btn => btn.classList.remove('active'));

    const groupRecipient = document.getElementById('group-recipient');
    const groupDesc = document.getElementById('group-desc');
    const labelAccount = document.getElementById('label-account');
    const btnAction = document.getElementById('btn-action');
    const title = document.getElementById('op-title');

    const states = {
        'TRANSFER': { tab: 'tab-transfer', recipient: true, desc: true, label: 'From Account', title: 'Transfer Money', btn: 'Send Money <i class="fa-regular fa-paper-plane ms-2"></i>' },
        'DEPOSIT': { tab: 'tab-deposit', recipient: false, desc: false, label: 'Target Account', title: 'Deposit Funds', btn: 'Deposit <i class="fa-solid fa-arrow-down ms-2"></i>' },
        'WITHDRAWAL': { tab: 'tab-withdraw', recipient: false, desc: false, label: 'From Account', title: 'Withdraw Cash', btn: 'Withdraw <i class="fa-solid fa-arrow-up ms-2"></i>' },
        'PAYMENT': { tab: 'tab-payment', recipient: false, desc: false, label: 'Pay from Account', title: 'Card Payment', btn: 'Pay <i class="fa-regular fa-credit-card ms-2"></i>' }
    };

    const state = states[type];
    document.getElementById(state.tab).classList.add('active');

    if(state.recipient) groupRecipient.classList.remove('d-none'); else groupRecipient.classList.add('d-none');
    if(state.desc) groupDesc.classList.remove('d-none'); else groupDesc.classList.add('d-none');

    labelAccount.textContent = state.label;
    title.textContent = state.title;
    btnAction.innerHTML = state.btn;
}

async function processOperation() {
    const type = window.currentOperation;
    const accId = document.getElementById('op-account').value;
    const amount = document.getElementById('op-amount').value;

    if (!amount || parseFloat(amount) <= 0) { alert("Invalid amount"); return; }

    const btn = document.getElementById('btn-action');
    btn.disabled = true;
    const oldText = btn.innerHTML;
    btn.innerHTML = 'Processing...';

    try {
        let url = '';
        const params = new URLSearchParams();
        params.append('amount', amount);

        if (type === 'TRANSFER') {
            const toNum = document.getElementById('op-to').value;
            const desc = document.getElementById('op-desc').value;
            if (!toNum) throw new Error("Recipient required");
            params.append('accountToNumber', toNum);
            params.append('description', desc || 'Transfer');
            url = `${API_BASE}/${currentUserId}/accounts/${accId}/transfer?${params.toString()}`;
        } else if (type === 'DEPOSIT') {
            url = `${API_BASE}/${currentUserId}/accounts/${accId}/deposit?${params.toString()}`;
        } else if (type === 'WITHDRAWAL') {
            url = `${API_BASE}/${currentUserId}/accounts/${accId}/withdrawal?${params.toString()}`;
        } else if (type === 'PAYMENT') {
            url = `${API_BASE}/${currentUserId}/accounts/${accId}/payment?${params.toString()}`;
        }

        const response = await authFetch(url, { method: 'POST' });

        if (response && response.ok) {
            alert("Operation Successful!");
            loadPage('accounts');
            updateNotificationBadge();
        } else {
            const err = await response.json();
            alert("Error: " + (err.message || "Failed"));
        }
    } catch (e) {
        alert("Error: " + e.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = oldText;
    }
}


// --- TRANSACTIONS ---

async function loadTransactions(page, customUrl = null) {
    try {
        let url = customUrl || `${API_BASE}/${currentUserId}/transactions`;
        url += url.includes('?') ? `&page=${page}` : `?page=${page}`;

        const response = await authFetch(url);
        if (!response) return;
        const transactions = await response.json();
        window.userTransactions = transactions;

        const container = document.getElementById('content-area');
        if (transactions.length === 0 && page === 0) {
            container.innerHTML = '<div class="col-12 text-center text-muted mt-5">No transactions found.</div>';
            return;
        }

        let rows = '';
        transactions.forEach((t, index) => {
            if (t.hiddenByUser) return;

            const isDeposit = t.typeTransaction === 'DEPOSIT';
            let colorClass = isDeposit ? 'text-success' : 'text-dark';
            let sign = isDeposit ? '+' : '';
            if (['WITHDRAW', 'WITHDRAWAL', 'TRANSFER', 'CARD'].includes(t.typeTransaction)) {
                sign = '-';
            }

            rows += `
                <tr class="clickable-card" onclick="openTransactionDetails(${index})">
                    <td><div class="fw-bold text-dark">${t.typeTransaction}</div></td>
                    <td class="text-muted">${formatDate(t.createdAt)}</td>
                    <td class="text-end fw-bold ${colorClass}">${sign}${t.amount}</td>
                </tr>
            `;
        });

        const disableNext = transactions.length < PAGE_SIZE;
        const disablePrev = page === 0;

        container.innerHTML = `
            <div class="col-12"><div class="info-card p-0"><table class="table table-custom table-hover mb-0"><tbody>${rows}</tbody></table></div></div>
             <div class="col-12 d-flex justify-content-center gap-2 mt-3">
                <button class="btn btn-sm btn-outline-secondary" onclick="loadTransactions(${page - 1}, '${customUrl || ''}')" ${disablePrev ? 'disabled' : ''}><i class="fa-solid fa-chevron-left"></i> Prev</button>
                <span class="d-flex align-items-center px-3 fw-bold text-muted">Page ${page + 1}</span>
                <button class="btn btn-sm btn-outline-secondary" onclick="loadTransactions(${page + 1}, '${customUrl || ''}')" ${disableNext ? 'disabled' : ''}>Next <i class="fa-solid fa-chevron-right"></i></button>
            </div>
        `;
    } catch (e) {
        showError(e);
    }
}

function openTransactionDetails(index) {
    const tx = window.userTransactions[index];
    if (!tx) return;
    document.getElementById('modal-tx-id').value = tx.id;
    document.getElementById('modal-tx-amount').textContent = tx.amount;
    document.getElementById('modal-tx-type').textContent = tx.typeTransaction;
    document.getElementById('modal-tx-date').textContent = formatDate(tx.createdAt);
    document.getElementById('modal-tx-desc').textContent = tx.description || '-';
    document.getElementById('modal-tx-from').textContent = tx.accountFrom || 'System';
    document.getElementById('modal-tx-to').textContent = tx.accountTo || 'System';
    new bootstrap.Modal(document.getElementById('transactionDetailsModal')).show();
}

async function hideTransaction() {
    const txId = document.getElementById('modal-tx-id').value;
    if (!confirm('Hide transaction?')) return;
    bootstrap.Modal.getInstance(document.getElementById('transactionDetailsModal')).hide();
    try {
        const response = await authFetch(`${API_BASE}/${currentUserId}/transactions/${txId}`, { method: 'PUT' });
        if (response && response.ok) loadTransactions(0);
    } catch (e) {
        alert('Error');
    }
}

// --- NOTIFICATIONS ---

async function updateNotificationBadge() {
    try {
        const response = await authFetch(`${API_BASE}/${currentUserId}/notifications/unread-count`);
        if (!response) return;
        const count = await response.json();

        const navItem = document.querySelector("a[onclick*='notifications']");
        // Remove old badge
        const oldBadge = navItem.querySelector('.badge');
        if (oldBadge) oldBadge.remove();

        if (count > 0) {
            navItem.innerHTML += ` <span class="badge bg-danger rounded-pill ms-auto">${count}</span>`;
            // Also update header bell
            const headerBell = document.querySelector('.page-header-wrapper .fa-bell');
            if(headerBell) {
                const parent = headerBell.parentElement;
                if(!parent.querySelector('.badge')) {
                    parent.innerHTML += `<span class="badge bg-danger rounded-circle position-absolute top-0 start-100 translate-middle p-1 border border-light"><span class="visually-hidden">New alerts</span></span>`;
                }
            }
        }
    } catch (e) { }
}

async function loadNotifications(page) {
    try {
        const response = await authFetch(`${API_BASE}/${currentUserId}/notifications?page=${page}`);
        if (!response) return;
        const notifs = await response.json();
        window.userNotifications = notifs;

        const container = document.getElementById('content-area');

        let deleteBtnHtml = '';
        if (notifs.length > 0 || page > 0) {
            deleteBtnHtml = `
                <div class="col-12 mb-3 text-end">
                    <button class="btn btn-outline-danger btn-sm fw-bold" onclick="deleteAllNotifications()">
                        <i class="fa-solid fa-trash-can me-2"></i>Clear All
                    </button>
                </div>
            `;
        }

        if (notifs.length === 0 && page === 0) {
            container.innerHTML = `
                ${deleteBtnHtml}
                <div class="col-12 text-center text-muted mt-5">
                    <i class="fa-regular fa-bell-slash fa-3x mb-3"></i><br>
                    No notifications yet.
                </div>`;
            return;
        }

        let listHtml = '<div class="list-group list-group-flush rounded-4 shadow-sm bg-white mb-3">';

        notifs.forEach((n, index) => {
            let icon = 'fa-bell';
            let color = 'text-primary';

            if (n.typeNotification === 'DEPOSIT') { icon = 'fa-arrow-down'; color = 'text-success'; }
            else if (n.typeNotification === 'WITHDRAWAL') { icon = 'fa-arrow-up'; color = 'text-danger'; }

            const isRead = n.statusNotification === 'READ';
            const fw = isRead ? '' : 'fw-bold';
            const newBadge = !isRead
                ? `<span class="badge bg-danger rounded-pill ms-2" style="font-size: 0.7rem;">NEW</span>`
                : '';

            listHtml += `
                <div class="list-group-item p-3 d-flex align-items-center clickable-card border-0 border-bottom" onclick="openNotificationDetails(${index})">
                    <div class="position-relative">
                        <i class="fa-solid ${icon} ${color} fs-4 me-3"></i>
                    </div>
                    
                    <div class="flex-grow-1">
                        <div class="d-flex justify-content-between align-items-center">
                            <h6 class="mb-0 fw-bold">
                                ${n.typeNotification}
                                ${newBadge}  </h6>
                            <small class="text-muted">${formatDate(n.createdAt)}</small>
                        </div>
                        <small class="text-muted ${fw} text-truncate" style="max-width: 300px; display: block;">
                            ${n.message}
                        </small>
                    </div>
                </div>
            `;
        });
        listHtml += '</div>';

        const disableNext = notifs.length < PAGE_SIZE;
        const disablePrev = page === 0;

        container.innerHTML = `
            ${deleteBtnHtml}
            <div class="col-12">
                ${listHtml}
                 <div class="col-12 d-flex justify-content-center gap-2 mt-3">
                    <button class="btn btn-sm btn-outline-secondary" onclick="loadNotifications(${page - 1})" ${disablePrev ? 'disabled' : ''}><i class="fa-solid fa-chevron-left"></i></button>
                    <span class="d-flex align-items-center px-3 fw-bold text-muted">Page ${page + 1}</span>
                    <button class="btn btn-sm btn-outline-secondary" onclick="loadNotifications(${page + 1})" ${disableNext ? 'disabled' : ''}><i class="fa-solid fa-chevron-right"></i></button>
                </div>
            </div>
        `;
    } catch (e) {
        showError(e);
    }
}

async function openNotificationDetails(index) {
    const n = window.userNotifications[index];
    if (!n) return;

    document.getElementById('modal-notif-id').value = n.id;

    if (n.id && n.statusNotification !== 'READ') {
        const navItem = document.querySelector("a[onclick*='notifications']");
        const menuBadge = navItem ? navItem.querySelector('.badge') : null;

        if (menuBadge) {
            let count = parseInt(menuBadge.textContent.trim());
            if (!isNaN(count) && count > 1) {
                menuBadge.textContent = count - 1;
            } else {
                menuBadge.remove();
            }
        }

        const listItems = document.querySelectorAll('#content-area .list-group-item');
        if (listItems && listItems[index]) {
            const item = listItems[index];
            const badges = item.querySelectorAll('.badge.bg-danger');
            badges.forEach(b => { if (b.textContent.includes('NEW')) b.remove(); });
            const msgText = item.querySelector('.text-truncate');
            if (msgText) msgText.classList.remove('fw-bold');
        }

        n.statusNotification = 'READ';
        try {
            await authFetch(`${API_BASE}/${currentUserId}/notifications/${n.id}`);
        } catch (e) { console.error("Error syncing read status:", e); }
    }

    document.getElementById('modal-notif-type').textContent = n.typeNotification;
    document.getElementById('modal-notif-date').textContent = formatDate(n.createdAt);
    const formattedMessage = n.message ? n.message.replace(/\n/g, '<br>') : '';
    document.getElementById('modal-notif-msg').innerHTML = formattedMessage;

    new bootstrap.Modal(document.getElementById('notificationDetailsModal')).show();
}

async function deleteCurrentNotification() {
    const id = document.getElementById('modal-notif-id').value;

    if (!id) return;
    if (!confirm("Delete this notification permanently?")) return;

    try {
        const response = await authFetch(`${API_BASE}/${currentUserId}/notifications/${id}`, {
            method: 'DELETE'
        });

        if (response && response.ok) {
            bootstrap.Modal.getInstance(document.getElementById('notificationDetailsModal')).hide();
            loadNotifications(0);
            updateNotificationBadge();
        } else {
            alert("Failed to delete notification.");
        }
    } catch (e) {
        alert("System Error: " + e.message);
    }
}

async function deleteAllNotifications() {
    if (!confirm("Are you sure you want to delete ALL notifications? This cannot be undone.")) return;
    try {
        const response = await authFetch(`${API_BASE}/${currentUserId}/notifications`, { method: 'DELETE' });
        if (response && response.ok) {
            loadNotifications(0);
            updateNotificationBadge();
        } else {
            alert("Failed to delete notifications");
        }
    } catch (e) {
        alert("System Error: " + e.message);
    }
}


// --- RESERVATIONS ---

async function loadReservations() {
    try {
        const response = await authFetch(`${API_BASE}/${currentUserId}/reservations`);
        if (!response) return;
        const reservations = await response.json();

        const container = document.getElementById('content-area');
        container.innerHTML = '';

        container.innerHTML += `
            <div class="col-12 mb-3 text-end">
                <button class="btn btn-primary" onclick="openReservationModal()">
                    <i class="fa-solid fa-calendar-plus me-2"></i>New Reservation
                </button>
            </div>
            <div class="row g-4" id="res-grid"></div>
        `;
        const grid = document.getElementById('res-grid');

        if (reservations.length === 0) {
            grid.innerHTML = '<div class="col-12 text-center text-muted">No active reservations.</div>';
            return;
        }

        reservations.forEach(r => {
            let badgeClass = 'bg-primary';
            if (r.statusReservation === 'CANCELLED') badgeClass = 'bg-danger';
            if (r.statusReservation === 'COMPLETED') badgeClass = 'bg-success';

            let cancelBtn = '';
            if (r.statusReservation !== 'CANCELLED' && r.statusReservation !== 'COMPLETED') {
                cancelBtn = `<button class="btn btn-sm btn-outline-danger fw-bold" onclick="cancelReservation(${r.id})">Cancel</button>`;
            }

            grid.innerHTML += `
                <div class="col-md-6 col-lg-4">
                    <div class="info-card h-100 border-start border-4 border-primary">
                        <div class="d-flex justify-content-between mb-3">
                            <span class="badge ${badgeClass} text-white border">${r.statusReservation}</span>
                            <small class="text-muted"><i class="fa-regular fa-clock"></i> ${formatDate(r.startReservation)}</small>
                        </div>
                        <h5 class="fw-bold mb-1">${r.serviceName || 'Service'}</h5>
                        <p class="text-muted small mb-0"><i class="fa-solid fa-location-dot me-1"></i> ${r.branchName || 'Branch'}</p>
                        <div class="mt-4 text-end">${cancelBtn}</div>
                    </div>
                </div>
            `;
        });
    } catch (e) {
        showError("Failed to load reservations.");
    }
}

async function openReservationModal() {
    const modalEl = document.getElementById('addReservationModal');
    const modal = new bootstrap.Modal(modalEl);
    modal.show();

    const branchSelect = document.getElementById('res-branch');
    const serviceSelect = document.getElementById('res-service');

    branchSelect.innerHTML = '<option disabled selected>Loading...</option>';
    serviceSelect.innerHTML = '<option disabled selected>Loading...</option>';

    try {
        const branchRes = await fetch(`${API_BASE}/branches`);
        const branches = await branchRes.json();
        branchSelect.innerHTML = '<option selected disabled value="">Select Branch</option>';
        branches.forEach(b => { branchSelect.innerHTML += `<option value="${b.bankBranchName}">${b.bankBranchName} (${b.locationDTO.city})</option>`; });

        const serviceRes = await fetch(`${API_BASE}/services`);
        const services = await serviceRes.json();
        serviceSelect.innerHTML = '<option selected disabled value="">Select Service</option>';
        services.forEach(s => { serviceSelect.innerHTML += `<option value="${s.bankServiceName}">${s.bankServiceName}</option>`; });

    } catch (e) {
        branchSelect.innerHTML = '<option disabled>Error</option>';
        serviceSelect.innerHTML = '<option disabled>Error</option>';
    }
}

async function createReservation() {
    const branchName = document.getElementById('res-branch').value;
    const serviceName = document.getElementById('res-service').value;
    const date = document.getElementById('res-date').value;
    const time = document.getElementById('res-time').value;

    if (!branchName || !serviceName || !date || !time) { alert("Please fill all fields"); return; }
    bootstrap.Modal.getInstance(document.getElementById('addReservationModal')).hide();

    try {
        const isoDateTime = `${date}T${time}:00`;
        const params = new URLSearchParams();
        params.append('startReservation', isoDateTime);
        params.append('serviceName', serviceName);
        params.append('branchName', branchName);

        const response = await authFetch(`${API_BASE}/${currentUserId}/reservations?${params.toString()}`, { method: 'POST' });
        if (response && response.ok) { alert("Reservation created!"); loadReservations(); }
        else { alert("Failed to create reservation."); }
    } catch (e) { alert("System Error"); }
}

async function cancelReservation(id) {
    if (!confirm("Cancel reservation?")) return;
    try {
        await authFetch(`${API_BASE}/${currentUserId}/reservations/${id}/cancel`, { method: 'PUT' });
        loadReservations();
    } catch (e) { alert("Error"); }
}

// --- PROFILE & UTILS ---

async function loadProfile() {
    try {
        const response = await authFetch(`${API_BASE}/users/${currentUserId}`);
        if (!response) return;
        const user = await response.json();
        const container = document.getElementById('content-area');

        const changePassButton = user.hasPassword
            ? `<button class="btn btn-outline-secondary" data-bs-toggle="modal" data-bs-target="#changePasswordModal">Change Password</button>`
            : `<button class="btn btn-outline-secondary" onclick="alert('You are signed in via Google. Please manage your password through your provider settings.')">Change Password</button>`;

        container.innerHTML = `
            <div class="col-md-8 offset-md-2 col-lg-6 offset-lg-3">
                <div class="info-card">
                    <h4 class="mb-4 fw-bold text-center">Profile Settings</h4>
                    <div class="text-center mb-4">
                        <div class="rounded-circle bg-primary text-white d-flex justify-content-center align-items-center mx-auto shadow-sm" style="width: 80px; height: 80px; font-size: 2rem;">
                            ${(user.username || 'U').charAt(0).toUpperCase()}
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label-custom">Username</label>
                        <input type="text" id="edit-username" class="form-control form-control-custom" value="${user.username}">
                    </div>
                    <div class="mb-3">
                        <label class="form-label-custom">Email</label>
                        <input type="text" id="edit-email" class="form-control form-control-custom" value="${user.email}" ${!user.hasPassword ? 'disabled' : ''}>
                    </div>
                    
                    <div class="d-flex gap-2 mt-4">
                         <button class="btn btn-primary flex-grow-1" onclick="updateProfile()">Save Changes</button>
                         ${changePassButton} </div>
                    
                    <hr class="my-4">
                    <button class="btn btn-outline-danger w-100 fw-bold" onclick="logout()">
                        <i class="fa-solid fa-arrow-right-from-bracket me-2"></i>Sign Out
                    </button>
                </div>
            </div>

            <div class="modal fade" id="changePasswordModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">Change Password</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">
                            <div class="mb-3">
                                <label class="form-label">Old Password</label>
                                <input type="password" id="cp-old-pass" class="form-control">
                            </div>
                            <div class="mb-3">
                                <label class="form-label">New Password</label>
                                <input type="password" id="cp-new-pass" class="form-control">
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Confirm New Password</label>
                                <input type="password" id="cp-confirm-pass" class="form-control">
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                            <button type="button" class="btn btn-primary" onclick="submitChangePassword()">Update Password</button>
                        </div>
                    </div>
                </div>
            </div>`;
    } catch (e) {
        showError(e);
    }
}

async function updateProfile() {
    const username = document.getElementById('edit-username').value;
    const email = document.getElementById('edit-email').value;
    try {
        const response = await authFetch(`${API_BASE}/users/${currentUserId}`, { method: 'PUT', body: JSON.stringify({ username, email }) });
        if (response && response.ok) { alert('Updated!'); loadUserInfo(); } else { alert('Error'); }
    } catch (e) { alert('Do something!'); }
}

async function submitChangePassword() {
    const oldPassInput = document.getElementById('cp-old-pass');
    const newPassInput = document.getElementById('cp-new-pass');
    const confirmPassInput = document.getElementById('cp-confirm-pass');
    const oldPass = oldPassInput.value;
    const newPass = newPassInput.value;
    const confirmPass = confirmPassInput.value;

    if (!oldPass || !newPass || !confirmPass) {
        alert("Please fill in all password fields.");
        return;
    }
    if (newPass !== confirmPass) {
        alert("New passwords do not match!");
        return;
    }
    try {
        const params = new URLSearchParams({
            oldPassword: oldPass,
            newPassword: newPass
        });
        const response = await authFetch(
            `${API_BASE}/users/${currentUserId}/changePassword?${params.toString()}`,
            {
                method: 'PUT'
            }
        );

        if (response.ok) {
            alert("Password successfully changed!");

            const modalElement = document.getElementById('changePasswordModal');
            const modalInstance = bootstrap.Modal.getInstance(modalElement);
            if (modalInstance) {
                modalInstance.hide();
            }

            oldPassInput.value = '';
            newPassInput.value = '';
            confirmPassInput.value = '';
        } else {
            const errorMsg = await response.text().catch(() => "Unknown error");
            alert("Failed to change password: " + errorMsg);
        }

    } catch (e) {
        console.error(e);
        alert("Failed to change password. Please try again later.");
    }
}

async function sendDashboardSupport() {
    const msg = document.getElementById('supp-msg').value;
    const subElement = document.getElementById('supp-subject');
    const sub = subElement ? subElement.value : "Support Request";

    if (!msg) {
        alert("Enter message");
        return;
    }

    const token = sessionStorage.getItem('accessToken');

    try {
        const headers = { 'Content-Type': 'application/json' };
        if (token) headers['Authorization'] = `Bearer ${token}`;

        await fetch(`/api/v1/support`, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({ userEmail: "", subject: sub, message: msg })
        });

        alert("Message sent!");

        const modalEl = document.getElementById('supportModal');
        if (modalEl) {
            const modal = bootstrap.Modal.getInstance(modalEl);
            if (modal) modal.hide();
        }
        document.getElementById('supp-msg').value = '';

    } catch (e) {
        console.error(e);
        alert("Network Error");
    }
}

// Helpers
function formatDate(dateInput) {
    if (!dateInput) return '';
    let date;
    if (Array.isArray(dateInput)) date = new Date(dateInput[0], dateInput[1] - 1, dateInput[2], dateInput[3] || 0, dateInput[4] || 0);
    else date = new Date(dateInput);
    return date.toLocaleDateString();
}

function showError(msg) {
    document.getElementById('content-area').innerHTML = `<div class="alert alert-danger m-3 shadow-sm border-0">${msg}</div>`;
}

function logout() {
    sessionStorage.removeItem('accessToken');
    window.location.href = 'index.html';
}

async function fetchAccountsForFilter() {
    try {
        const response = await authFetch(`${API_BASE}/${currentUserId}/accounts`);
        if (!response) return;
        const accounts = await response.json();
        const select = document.getElementById('filter-account');
        if (select) {
            select.innerHTML = '';
            accounts.forEach(acc => { select.innerHTML += `<option value="${acc.id}">${acc.accountNumber} (${acc.currency})</option>`; });
        }
    } catch (e) { }
}

function toggleFilterInputs() {
    const type = document.getElementById('filter-type').value;
    document.getElementById('input-date-group').classList.add('d-none');
    document.getElementById('input-amount-group').classList.add('d-none');
    document.getElementById('input-account-group').classList.add('d-none');
    if (type.includes('DATE') || type === 'AFTER' || type === 'BEFORE') document.getElementById('input-date-group').classList.remove('d-none');
    if (type === 'AMOUNT') document.getElementById('input-amount-group').classList.remove('d-none');
    if (type === 'ACCOUNT') document.getElementById('input-account-group').classList.remove('d-none');
}

async function applyFilters() {
    const type = document.getElementById('filter-type').value;
    let url = `${API_BASE}/${currentUserId}/transactions`;
    if (type === 'ACCOUNT') {
        const accId = document.getElementById('filter-account').value;
        url = `${API_BASE}/${currentUserId}/transactions/filter/${accId}`;
    } else if (type === 'AMOUNT') {
        const amount = document.getElementById('filter-amount').value;
        url = `${API_BASE}/${currentUserId}/transactions/filter/amount?amount=${amount}`;
    } else if (['DATE', 'AFTER', 'BEFORE'].includes(type)) {
        const dateInput = document.getElementById('filter-date').value;
        if (!dateInput) return alert('Select date');
        const [year, month, day] = dateInput.split('-');
        const formattedDate = `${day}.${month}.${year}`;
        if (type === 'DATE') url = `${API_BASE}/${currentUserId}/transactions/filter/date?date=${formattedDate}`;
        if (type === 'AFTER') url = `${API_BASE}/${currentUserId}/transactions/filter/afterDate?date=${formattedDate}`;
        if (type === 'BEFORE') url = `${API_BASE}/${currentUserId}/transactions/filter/beforeDate?date=${formattedDate}`;
    }
    loadTransactions(0, url);
}