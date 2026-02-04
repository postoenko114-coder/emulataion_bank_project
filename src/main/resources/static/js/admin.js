/**
 * =========================================================
 * ADMIN DASHBOARD CONTROLLER
 * =========================================================
 */
const API_BASE = '/api/v1/admin';
let jwtToken = sessionStorage.getItem('accessToken');
let currentManageUserId = null;
let currentManageUsername = null;
let modalInstance = null;
let cachedBranches = [];
let cachedSupportMessages = [];

// ==========================================
// INITIALIZATION
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    const modalEl = document.getElementById('adminModal');
    if (modalEl) modalInstance = new bootstrap.Modal(modalEl);

    // Initial Load
    loadSection('users');
});

function logout() {
    sessionStorage.removeItem('accessToken');
    window.location.href = 'index.html';
}

// ==========================================
// NETWORK UTILS
// ==========================================
async function adminFetch(url, options = {}) {
    const token = sessionStorage.getItem('accessToken');
    const headers = { 'Content-Type': 'application/json', ...options.headers };

    if (token) headers['Authorization'] = `Bearer ${token}`;

    try {
        const response = await fetch(url, { ...options, headers });

        if (response.status === 401 || response.status === 403) {
            alert("Session expired. Please login again.");
            sessionStorage.removeItem('jwtToken');
            window.location.href = 'login.html';
            return null;
        }

        const text = await response.text();
        let data;
        try { data = JSON.parse(text); } catch (err) { data = text; }

        if (!response.ok) {
            const errorMessage = (typeof data === 'object' && data.message) ? data.message : data;
            alert("Error: " + errorMessage);
            return null;
        }
        return data;

    } catch (e) {
        console.error("Network error:", e);
        alert("Network error occurred");
        return null;
    }
}

// ==========================================
// ROUTER
// ==========================================
function loadSection(section) {
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
    const navItem = document.getElementById(`nav-${section}`);
    if (navItem) navItem.classList.add('active');

    const content = document.getElementById('content-area');
    content.innerHTML = '<div class="text-center mt-5"><div class="spinner-border text-primary"></div></div>';
    const title = document.getElementById('page-title');

    switch (section) {
        case 'users': title.textContent = 'Users Management'; fetchUsers(); break;
        case 'reservations': title.textContent = 'Global Reservations'; loadGlobalReservations(); break;
        case 'branches': title.textContent = 'Branches'; fetchBranches(); break;
        case 'services': title.textContent = 'Services'; fetchServices(); break;
        case 'support': title.textContent = 'Support Center'; loadSupport(); break;
    }
}

// ==========================================
// MODAL & UTILS
// ==========================================
function showModal(title, bodyHtml, saveCallback = null, saveText = "Save", showFooter = true) {
    document.getElementById('modal-title').textContent = title;
    document.getElementById('modal-body').innerHTML = bodyHtml;

    const footer = document.getElementById('modal-footer');
    footer.style.display = showFooter ? 'flex' : 'none';

    let saveBtn = document.getElementById('modal-save-btn');
    // Restore button if deleted
    if (!saveBtn) {
        footer.innerHTML = `
            <button type="button" class="btn btn-light" data-bs-dismiss="modal">Close</button>
            <button type="button" class="btn btn-primary-custom" id="modal-save-btn">Save</button>
        `;
        saveBtn = document.getElementById('modal-save-btn');
    }

    // Clone to remove old listeners
    const newBtn = saveBtn.cloneNode(true);
    saveBtn.parentNode.replaceChild(newBtn, saveBtn);

    if (saveCallback) {
        newBtn.style.display = 'block';
        newBtn.textContent = saveText;
        newBtn.onclick = () => { saveCallback(); };
    } else {
        newBtn.style.display = 'none';
    }
    modalInstance.show();
}

function hideModal() { modalInstance.hide(); }

function formatCardNumber(number) {
    if (!number) return '';
    return number.toString().replace(/\D/g, '').replace(/(\d{4})(?=\d)/g, '$1 ');
}

// ==========================================
// 1. USERS
// ==========================================
async function fetchUsers(query = "") {
    let url = `${API_BASE}/users`;
    if (query) url = `${API_BASE}/users/search?query=${encodeURIComponent(query)}`;

    let data = await adminFetch(url);
    const users = Array.isArray(data) ? data : (data ? [data] : []);

    const toolbar = `
        <div class="toolbar d-flex justify-content-between align-items-center">
            <div class="search-container d-flex align-items-center gap-2">
                <i class="fa-solid fa-magnifying-glass search-icon"></i>
                <input type="text" class="form-control search-input" 
                       placeholder="Search by ID, Username or Email..." 
                       value="${query}" 
                       onkeypress="if(event.key==='Enter') fetchUsers(this.value)">
                
                <button class="btn btn-sm btn-outline-secondary" onclick="fetchUsers(document.querySelector('.search-input').value)">Search</button>
            </div>
            <div>
                <button class="btn btn-primary-custom me-2" onclick="openCreateUserModal()">+ New User</button>
                <button class="btn btn-light border" onclick="fetchUsers()">Refresh</button>
            </div>
        </div>
    `;

    let rows = users.map(u => `
        <tr>
            <td>
                <div class="d-flex align-items-center">
                    <div class="rounded-circle bg-soft-primary d-flex align-items-center justify-content-center me-3 fw-bold" 
                         style="width:40px; height:40px; color:var(--primary)">${(u.username || 'U').charAt(0).toUpperCase()}</div>
                    <div><div class="fw-bold text-dark">${u.username}</div><div class="small text-muted">ID: ${u.id}</div></div>
                </div>
            </td>
            <td>${u.email}</td>
            <td><span class="status-badge bg-soft-primary">${u.roleUser || 'CLIENT'}</span></td>
            <td>${u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '-'}</td>
            <td class="text-end">
                <button class="btn-icon text-warning" onclick="openChangeRoleModal(${u.id}, '${u.roleUser}')" title="Change Role"><i class="fa-solid fa-user-shield"></i></button>
                <button class="btn-icon" onclick="openUserDetails(${u.id}, '${u.username}')"><i class="fa-solid fa-chevron-right"></i></button>
                <button class="btn-icon delete" onclick="deleteUser(${u.id})"><i class="fa-solid fa-trash"></i></button>
            </td>
        </tr>
    `).join('');

    renderTable(toolbar, ['User', 'Email', 'Role', 'Joined', 'Actions'], rows);
}

function openCreateUserModal() {
    const html = `
        <div class="mb-3"><label class="form-label-custom">Username *</label><input type="text" id="cu-username" class="form-control"></div>
        <div class="mb-3"><label class="form-label-custom">Email *</label><input type="email" id="cu-email" class="form-control"></div>
        <div class="mb-3"><label class="form-label-custom">Password *</label><input type="password" id="cu-password" class="form-control"></div>
        <div class="mb-3"><label class="form-label-custom">Role</label>
            <select id="cu-role" class="form-select">
                <option value="CLIENT">Client</option>
                <option value="ADMIN">Admin</option>
            </select>
        </div>
    `;
    showModal('Create New User', html, async () => {
        const dto = {
            username: document.getElementById('cu-username').value,
            email: document.getElementById('cu-email').value,
            password: document.getElementById('cu-password').value,
            roleUser: document.getElementById('cu-role').value
        };
        if (!dto.username || !dto.password || !dto.email) return alert("Please fill all required fields");
        const res = await adminFetch(`${API_BASE}/users`, { method: 'POST', body: JSON.stringify(dto) });
        if (res) { hideModal(); fetchUsers(); }
    }, "Create User");
}

async function deleteUser(id) {
    if (confirm("Are you sure you want to delete this user?")) {
        await adminFetch(`${API_BASE}/users/${id}`, { method: 'DELETE' });
        fetchUsers();
    }
}

async function openUserDetails(userId, username) {
    currentManageUserId = userId;
    currentManageUsername = username;
    const content = document.getElementById('content-area');
    document.getElementById('page-title').textContent = username;
    content.innerHTML = `
        <div class="mb-4 d-flex justify-content-between align-items-center">
            <button class="btn btn-link text-muted p-0 text-decoration-none fw-bold" onclick="loadSection('users')"><i class="fa-solid fa-arrow-left me-2"></i>Back to Users</button>
            <button class="btn btn-primary-custom btn-sm" onclick="openEditUserModal(${userId})"><i class="fa-solid fa-pen me-2"></i>Edit Profile</button>
        </div>
        <div class="nav-tabs-custom">
            <div class="tab-item active" onclick="switchUserTab(this, 'accounts')">Accounts</div>
            <div class="tab-item" onclick="switchUserTab(this, 'cards')">Cards</div>
            <div class="tab-item" onclick="switchUserTab(this, 'transactions')">Transactions</div>
            <div class="tab-item" onclick="switchUserTab(this, 'notifications')">Notifications</div>
        </div>
        <div id="user-sub-content"><div class="text-center mt-4"><div class="spinner-border text-primary"></div></div></div>
    `;
    loadUserAccounts(userId);
}

async function openEditUserModal(userId) {
    const user = await adminFetch(`${API_BASE}/users/${userId}`);
    const html = `
        <div class="mb-3"><label class="form-label-custom">Username</label><input type="text" id="eu-name" class="form-control" value="${user.username}"></div>
        <div class="mb-3"><label class="form-label-custom">Email</label><input type="email" id="eu-email" class="form-control" value="${user.email}"></div>
    `;
    showModal('Edit Profile', html, async () => {
        const dto = { username: document.getElementById('eu-name').value, email: document.getElementById('eu-email').value };
        const res = await adminFetch(`${API_BASE}/users/${userId}`, { method: 'PUT', body: JSON.stringify(dto) });
        if (res) {
            hideModal();
            if (user.username !== dto.username) {
                const token = sessionStorage.getItem('accessToken');
                if (token) {
                    try { if (JSON.parse(atob(token.split('.')[1])).sub === user.username) { alert("Username changed. Re-login."); logout(); return; } } catch (e) { }
                }
            }
            alert("User updated!");
            if (typeof fetchUsers === 'function') fetchUsers();
            if (typeof openUserDetails === 'function') openUserDetails(userId, dto.username);
        }
    }, "Update");
}

async function openChangeRoleModal(userId, currentRole) {
    const html = `
        <div class="mb-3">
            <label class="form-label-custom">Select New Role</label>
            <select id="role-select" class="form-select">
                <option value="CLIENT" ${currentRole === 'CLIENT' ? 'selected' : ''}>CLIENT</option>
                <option value="ADMIN" ${currentRole === 'ADMIN' ? 'selected' : ''}>ADMIN</option>
            </select>
            <div class="form-text text-muted mt-2">Changing role may affect permissions immediately.</div>
        </div>
    `;
    showModal('Change User Role', html, async () => {
        const newRole = document.getElementById('role-select').value;
        const res = await adminFetch(`${API_BASE}/users/${userId}/changeRoleUser?role=${newRole}`, { method: 'PUT' });
        if (res) {
            alert(`Role changed to ${newRole}!`);
            hideModal();
            fetchUsers();
        }
    }, "Save Role");
}

function switchUserTab(el, tab) {
    document.querySelectorAll('.tab-item').forEach(t => t.classList.remove('active'));
    el.classList.add('active');
    if (tab === 'accounts') loadUserAccounts(currentManageUserId);
    if (tab === 'cards') loadUserCards(currentManageUserId);
    if (tab === 'transactions') loadUserTransactions(currentManageUserId);
    if (tab === 'notifications') loadUserNotifications(currentManageUserId);
}

// --- ACCOUNTS ---
async function loadUserAccounts(userId, searchNumber = null) {
    let url = `${API_BASE}/${userId}/accounts`;
    if (searchNumber) url = `${API_BASE}/${userId}/accounts/filter/number?accountNumber=${searchNumber}`;
    let accounts = await adminFetch(url);
    if (searchNumber && accounts && !Array.isArray(accounts)) accounts = [accounts];
    const container = document.getElementById('user-sub-content');

    const toolbar = `
        <div class="toolbar">
            <div class="search-container" style="max-width: 400px;">
                <i class="fa-solid fa-magnifying-glass search-icon"></i>
                <input type="text" class="form-control search-input" placeholder="Search account..." value="${searchNumber || ''}" id="acc-search-input" onkeypress="if(event.key==='Enter') loadUserAccounts(${userId}, this.value)">
            </div>
            <button class="btn btn-primary-custom ms-auto" onclick="openCreateAccountModal()">+ New Account</button>
        </div>
    `;

    if (!accounts || accounts.length === 0) {
        container.innerHTML = toolbar + '<div class="text-center text-muted py-5">No accounts found.</div>';
        return;
    }
    let html = '<div class="row g-3">';
    accounts.forEach(acc => {
        html += `
        <div class="col-md-6 col-lg-4">
            <div class="admin-card h-100 p-4 card-hover" onclick="showAccountDetailsModal(${acc.id})">
                <div class="d-flex justify-content-between mb-4">
                    <div class="text-muted small fw-bold">CURRENT</div>
                    <span class="status-badge ${acc.statusAccount === 'ACTIVE' ? 'bg-soft-success' : 'bg-soft-danger'}">${acc.statusAccount}</span>
                </div>
                <div class="mb-3">
                    <span class="big-balance fs-2">${acc.balance}</span>
                    <span class="fs-5 text-muted fw-bold ms-1">${acc.currency}</span>
                </div>
                <div class="text-muted small font-monospace bg-light p-2 rounded text-center">${acc.accountNumber}</div>
            </div>
        </div>`;
    });
    container.innerHTML = toolbar + html + '</div>';
}

function openCreateAccountModal() {
    const html = `
        <div class="mb-3">
            <label class="form-label-custom">Currency</label>
            <select id="na-currency" class="form-select">
                <option value="USD">USD ($)</option>
                <option value="EUR">EUR (€)</option>
                <option value="CZK">CZK (Kč)</option>
            </select>
        </div>
    `;
    showModal('New Account', html, async () => {
        const currency = document.getElementById('na-currency').value;
        const res = await adminFetch(`${API_BASE}/${currentManageUserId}/accounts?currency=${currency}`, { method: 'POST' });
        if (res) { hideModal(); loadUserAccounts(currentManageUserId); }
    }, "Create Account");
}

async function showAccountDetailsModal(accId) {
    const acc = await adminFetch(`${API_BASE}/${currentManageUserId}/accounts/${accId}`);
    const statusBtn = acc.statusAccount === 'ACTIVE'
        ? `<button class="btn btn-warning flex-fill fw-bold" onclick="accountAction(${acc.id}, 'block')"><i class="fa-solid fa-lock me-2"></i>Block</button>`
        : `<button class="btn btn-success flex-fill fw-bold" onclick="accountAction(${acc.id}, 'activate')"><i class="fa-solid fa-unlock me-2"></i>Activate</button>`;

    const html = `
        <div class="text-center mb-4">
            <div class="big-balance display-5">${acc.balance} ${acc.currency}</div>
            <div class="text-muted small">Current Balance</div>
        </div>
        <div class="p-3 bg-light rounded mb-3">
            <div class="d-flex justify-content-between mb-2 border-bottom pb-2"><span>Number:</span> <strong class="text-dark font-monospace">${acc.accountNumber}</strong></div>
            <div class="d-flex justify-content-between"><span>Status:</span> <strong class="${acc.statusAccount === 'ACTIVE' ? 'text-success' : 'text-danger'}">${acc.statusAccount}</strong></div>
        </div>
        <div class="d-grid gap-2">
            <div class="d-flex gap-2">
                ${statusBtn}
                <button class="btn btn-secondary flex-fill fw-bold" onclick="accountAction(${acc.id}, 'close')"><i class="fa-solid fa-ban me-2"></i>Close</button>
            </div>
            <button class="btn btn-outline-danger fw-bold" onclick="confirmDeleteAccount(${acc.id})"><i class="fa-solid fa-trash me-2"></i>Delete Account</button>
        </div>
    `;
    showModal('Account Details', html, null, null, false);
}

function confirmDeleteAccount(id) {
    if (confirm("Are you sure you want to permanently delete this account?")) { accountAction(id, 'delete'); }
}

async function accountAction(id, action) {
    let url = '', method = 'PUT';
    if (action === 'delete') {
        url = `${API_BASE}/${currentManageUserId}/accounts/${id}`;
        method = 'DELETE';
    } else {
        let endpoint = action === 'block' ? 'blockAccount' : (action === 'activate' ? 'activateAccount' : 'closeAccount');
        url = `${API_BASE}/${currentManageUserId}/accounts/${id}/${endpoint}`;
    }
    await adminFetch(url, { method: method });
    if (action === 'close' || action === 'delete') {
        hideModal(); loadUserAccounts(currentManageUserId);
    } else {
        showAccountDetailsModal(id); loadUserAccounts(currentManageUserId);
    }
}

// --- CARDS ---
async function loadUserCards(userId, searchNumber = null) {
    let url = `${API_BASE}/${userId}/cards`;
    if (searchNumber) url = `${API_BASE}/${userId}/cards/filter/number?cardNumber=${searchNumber}`;
    let cards = await adminFetch(url);
    if (searchNumber && cards && !Array.isArray(cards)) cards = [cards];

    const container = document.getElementById('user-sub-content');
    const toolbar = `
        <div class="toolbar">
            <div class="search-container" style="max-width: 400px;">
                <i class="fa-solid fa-magnifying-glass search-icon"></i>
                <input type="text" class="form-control search-input" placeholder="Search card..." value="${searchNumber || ''}" id="card-search-input" onkeypress="if(event.key==='Enter') loadUserCards(${userId}, this.value)">
            </div>
            <button class="btn btn-primary-custom ms-auto" onclick="openCreateCardModal()">+ Issue Card</button>
        </div>
    `;

    if (!cards || cards.length === 0) {
        container.innerHTML = toolbar + '<div class="text-center text-muted py-5">No cards found.</div>';
        return;
    }

    let html = '<div class="row g-4">';
    cards.forEach(c => {
        const formattedNum = formatCardNumber(c.cardNumber);
        // Visual Logic
        const bgClass = c.typeCard === 'CREDIT' ? 'bg-credit' : '';
        const opacity = c.statusCard === 'CLOSED' ? 'opacity-50' : '';

        html += `
            <div class="col-md-6 col-lg-4">
                <div class="bank-card-visual ${bgClass} ${opacity}" onclick="showCardDetailsModal(${c.id})">
                    <div class="card-status-pill">${c.statusCard}</div>
                    <div class="card-chip"></div>
                    <div>
                        <div class="small opacity-75 fw-bold">${c.typeCard}</div>
                        <div class="card-number-display">${formattedNum}</div>
                    </div>
                    <div class="card-meta"><span>${c.cardHolderName}</span><span>Exp: ${c.expiryDate}</span></div>
                </div>
            </div>`;
    });
    container.innerHTML = toolbar + html + '</div>';
}

async function showCardDetailsModal(cardId) {
    const c = await adminFetch(`${API_BASE}/${currentManageUserId}/cards/${cardId}`);
    const prettyNum = formatCardNumber(c.cardNumber);
    const statusBtn = c.statusCard === 'ACTIVE'
        ? `<button class="btn btn-warning flex-fill fw-bold" onclick="cardAction(${c.id}, 'block')"><i class="fa-solid fa-lock me-2"></i>Block</button>`
        : `<button class="btn btn-success flex-fill fw-bold" onclick="cardAction(${c.id}, 'activate')"><i class="fa-solid fa-unlock me-2"></i>Activate</button>`;

    const html = `
        <div class="text-center mb-4">
            <div class="fs-4 font-monospace mb-2 fw-bold">${prettyNum}</div>
            <span class="badge bg-light text-dark border fs-6">${c.typeCard}</span>
        </div>
        <div class="p-3 bg-light rounded mb-3">
            <div class="d-flex justify-content-between border-bottom pb-2 mb-2"><span class="text-muted">Status</span><strong class="${c.statusCard === 'ACTIVE' ? 'text-success' : 'text-danger'}">${c.statusCard}</strong></div>
            <div class="d-flex justify-content-between"><span class="text-muted">Expiry</span><strong>${c.expiryDate}</strong></div>
        </div>
        <div class="d-grid gap-2">
            <div class="d-flex gap-2">
                ${statusBtn}
                <button class="btn btn-info text-white flex-fill fw-bold" onclick="cardAction(${c.id}, 'changeType')"><i class="fa-solid fa-repeat me-2"></i>Change Type</button>
            </div>
            <button class="btn btn-outline-danger fw-bold" onclick="deleteCard(${c.id})"><i class="fa-solid fa-trash me-2"></i>Delete Card</button>
        </div>
    `;
    showModal('Card Management', html, null, null, false);
}

async function cardAction(id, action) {
    let endpoint = action === 'block' ? 'blockCard' : (action === 'activate' ? 'activateCard' : 'changeTypeCard');
    await adminFetch(`${API_BASE}/${currentManageUserId}/cards/${id}/${endpoint}`, { method: 'PUT' });
    loadUserCards(currentManageUserId);
    showCardDetailsModal(id);
}

async function deleteCard(id) {
    if (confirm('Permanently delete card?')) {
        await adminFetch(`${API_BASE}/${currentManageUserId}/cards/${id}`, { method: 'DELETE' });
        hideModal(); loadUserCards(currentManageUserId);
    }
}

async function openCreateCardModal() {
    const accounts = await adminFetch(`${API_BASE}/${currentManageUserId}/accounts`);
    if (!accounts || accounts.length === 0) return alert("User needs an account first");
    const opts = accounts.map(a => `<option value="${a.accountNumber}">${a.accountNumber} (${a.currency})</option>`).join('');
    const html = `<div class="mb-3"><label class="form-label-custom">Account</label><select id="nc-acc" class="form-select">${opts}</select></div><div class="mb-3"><label class="form-label-custom">Type</label><select id="nc-type" class="form-select"><option value="DEBIT">Debit</option><option value="CREDIT">Credit</option></select></div>`;
    showModal('New Card', html, async () => {
        await adminFetch(`${API_BASE}/${currentManageUserId}/cards?accountNumber=${document.getElementById('nc-acc').value}&typeCard=${document.getElementById('nc-type').value}`, { method: 'POST' });
        loadUserCards(currentManageUserId); hideModal();
    }, "Issue");
}

// --- TRANSACTIONS ---
async function loadUserTransactions(userId, filters = {}) {
    let url = `${API_BASE}/${userId}/transactions`;
    if (filters.type && filters.val) {
        let val = filters.val;
        if (['date', 'before', 'after'].includes(filters.type) && val.includes('-')) {
            const [y, m, d] = val.split('-');
            val = `${d}.${m}.${y}`;
        }
        const map = { 'amount': 'amount', 'date': 'date', 'before': 'beforeDate', 'after': 'afterDate' };
        if (map[filters.type]) url = `${API_BASE}/${userId}/transactions/filter/${map[filters.type]}?${filters.type === 'amount' ? 'amount' : 'date'}=${val}`;
    }
    const txs = await adminFetch(url);
    const container = document.getElementById('user-sub-content');
    const toolbar = `<div class="toolbar align-items-end"><div><label class="small fw-bold text-muted d-block mb-1">Filter Type</label><select id="tx-type" class="form-select" onchange="updateTxInput()"><option value="">Show All</option><option value="date">Exact Date</option><option value="before">Before Date</option><option value="after">After Date</option><option value="amount">Amount</option></select></div><div id="tx-val-container" style="display:none;"><label class="small fw-bold text-muted d-block mb-1">Value</label><input type="text" id="tx-val" class="form-control" placeholder="Enter value..."></div><button class="btn btn-primary-custom ms-2 mb-0" style="height: 42px;" onclick="applyTxFilter()">Apply</button></div>`;
    if (!txs || txs.length === 0) {
        container.innerHTML = toolbar + '<div class="text-center text-muted py-5">No transactions found.</div>';
    } else {
        let rows = txs.map(t => `<tr style="cursor:pointer"><td><span class="fw-bold">${t.typeTransaction}</span></td><td class="${t.typeTransaction === 'DEPOSIT' ? 'text-success fw-bold' : 'text-danger fw-bold'}">${t.amount}</td><td class="small text-muted font-monospace">${t.accountFrom} <i class="fa-solid fa-arrow-right mx-1"></i> ${t.accountTo}</td><td><span class="status-badge bg-soft-primary">${t.statusTransaction}</span></td><td>${new Date(t.createdAt).toLocaleDateString()} ${new Date(t.createdAt).toLocaleTimeString()}</td></tr>`).join('');
        renderTable(toolbar, ['Type', 'Amount', 'Flow', 'Status', 'Date'], rows, container);
    }
    if (filters.type) {
        document.getElementById('tx-type').value = filters.type; updateTxInput();
        if (['date', 'before', 'after'].includes(filters.type)) document.getElementById('tx-val').value = filters.val.split('.').reverse().join('-');
        else document.getElementById('tx-val').value = filters.val;
    }
}

window.updateTxInput = function () {
    const type = document.getElementById('tx-type').value;
    const c = document.getElementById('tx-val-container');
    const i = document.getElementById('tx-val');
    if (!type) { c.style.display = 'none'; return; }
    c.style.display = 'block';
    i.type = ['date', 'before', 'after'].includes(type) ? 'date' : 'number';
};
window.applyTxFilter = function () {
    loadUserTransactions(currentManageUserId, { type: document.getElementById('tx-type').value, val: document.getElementById('tx-val').value });
};

// --- NOTIFICATIONS ---
async function loadUserNotifications(userId) {
    const notifs = await adminFetch(`${API_BASE}/${userId}/notifications`);
    const container = document.getElementById('user-sub-content');
    const form = `<div class="admin-card p-4 mb-4"><h6 class="fw-bold mb-3">Send Notification</h6><div class="row g-2"><div class="col-md-3"><select class="form-select" id="notif-type"><option value="PERSONAL">Personal</option><option value="ADVERTISING">Advertising</option><option value="SUPPORT">Support</option></select></div><div class="col-md-7"><input type="text" class="form-control" id="notif-msg" placeholder="Message..."></div><div class="col-md-2"><button class="btn btn-primary-custom w-100" onclick="sendNotification()">Send</button></div></div></div>`;
    let list = notifs && notifs.length ? notifs.map(n => `<div class="p-3 border-bottom"><strong>${n.typeNotification}</strong>: ${n.message}</div>`).join('') : '<div class="text-center text-muted">No history</div>';
    container.innerHTML = form + `<div class="admin-card p-0">${list}</div>`;
}

async function sendNotification() {
    const msg = document.getElementById('notif-msg').value;
    if (!msg) return;
    await adminFetch(`${API_BASE}/${currentManageUserId}/notifications/sendNotification?message=${encodeURIComponent(msg)}&typeNotification=${document.getElementById('notif-type').value}`, { method: 'POST' });
    loadUserNotifications(currentManageUserId);
}

// ==========================================
// 2. BRANCHES
// ==========================================
async function fetchBranches() {
    const branches = await adminFetch(`${API_BASE}/branches`);
    cachedBranches = branches || [];
    renderBranchesTable(cachedBranches);
}

function renderBranchesTable(branches) {
    const container = document.getElementById('content-area');
    const toolbar = `<div class="toolbar"><div class="search-container"><i class="fa-solid fa-magnifying-glass search-icon"></i><input type="text" class="form-control search-input" placeholder="Search branch..." onkeyup="filterBranches(this.value)"></div><button class="btn btn-primary-custom" onclick="openCreateBranchModal()">+ New Branch</button></div>`;
    if (!branches || branches.length === 0) {
        container.innerHTML = toolbar + '<div class="text-center text-muted py-5">No branches found.</div>';
        return;
    }
    let rows = branches.map(b => `<tr><td><div class="fw-bold text-dark">${b.bankBranchName}</div><div class="small text-muted">ID: ${b.id}</div></td><td><div class="fw-bold">${b.locationDTO ? b.locationDTO.country : '-'}</div><div class="fw-bold">${b.locationDTO ? b.locationDTO.city : '-'}</div><div class="small text-muted">${b.locationDTO ? b.locationDTO.address : ''}</div></td><td>${b.locationDTO ? b.locationDTO.postCode : ''}</td><td class="text-end"><button class="btn-icon" onclick="openEditBranchModal(${b.id})"><i class="fa-solid fa-pen"></i></button><button class="btn-icon delete" onclick="deleteBranch(${b.id})"><i class="fa-solid fa-trash"></i></button></td></tr>`).join('');
    renderTable(toolbar, ['Branch Name', 'Location', 'Post Code', 'Actions'], rows);
}

function filterBranches(query) {
    if (!query) return renderBranchesTable(cachedBranches);
    const filtered = cachedBranches.filter(b => b.bankBranchName.toLowerCase().includes(query.toLowerCase()));
    renderBranchesTable(filtered);
    const input = document.querySelector('.search-input');
    if (input) { input.value = query; input.focus(); }
}

async function deleteBranch(id) {
    if (confirm('Are you sure you want to delete this branch?')) {
        await adminFetch(`${API_BASE}/branches/${id}`, { method: 'DELETE' });
        fetchBranches();
    }
}

function openCreateBranchModal() {
    const html = `
        <div class="mb-3"><label class="form-label-custom">Branch Name *</label><input type="text" id="cb-name" class="form-control"></div>
        <div class="mb-3"><label class="form-label-custom">Country *</label><input type="text" id="cb-country" class="form-control"></div>
        <div class="mb-3"><label class="form-label-custom">City *</label><input type="text" id="cb-city" class="form-control"></div>
        <div class="mb-3"><label class="form-label-custom">Address *</label><input type="text" id="cb-address" class="form-control"></div>
        <div class="mb-3"><label class="form-label-custom">Post Code *</label><input type="text" id="cb-postcode" class="form-control"></div>
    `;
    showModal('New Branch', html, async () => {
        const name = document.getElementById('cb-name').value;
        const country = document.getElementById('cb-country').value;
        const city = document.getElementById('cb-city').value;
        const addr = document.getElementById('cb-address').value;
        const postCode = document.getElementById('cb-postcode').value;
        if (!name || !city || !addr || !postCode ) return alert("Please fill in all required fields");

        const dto = { bankBranchName: name, locationDTO: { country, city, address: addr, postCode }, schedule: [], bankServices: [] };
        const res = await adminFetch(`${API_BASE}/branches`, { method: 'POST', body: JSON.stringify(dto) });
        if (res) { hideModal(); fetchBranches(); }
    }, "Create");
}

let tempBranchServices = [];
let cachedAllServices = [];

async function openEditBranchModal(branchId) {
    const b = await adminFetch(`${API_BASE}/branches/${branchId}`);
    const allServicesData = await adminFetch(`${API_BASE}/services`);
    cachedAllServices = allServicesData || [];
    tempBranchServices = b.services || b.bankServices || [];

    const days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
    let scheduleHtml = '';
    const branchSchedule = b.schedule || [];

    days.forEach(day => {
        const existing = branchSchedule.find(s => s.dayOfWeek === day);
        const open = existing ? existing.openTime : '';
        const close = existing ? existing.closeTime : '';
        scheduleHtml += `<div class="schedule-grid mb-2" style="display: grid; grid-template-columns: 100px 1fr 1fr; gap: 10px; align-items: center;"><div class="day-label fw-bold">${day}</div><input type="time" class="form-control form-control-sm schedule-open" data-day="${day}" value="${open}"><input type="time" class="form-control form-control-sm schedule-close" data-day="${day}" value="${close}"></div>`;
    });

    const bodyHtml = `
        <ul class="nav nav-pills mb-3" id="branchTabs">
            <li class="nav-item"><a class="nav-link active" href="javascript:void(0)" onclick="switchTab('tab-info')">Info</a></li>
            <li class="nav-item"><a class="nav-link" href="javascript:void(0)" onclick="switchTab('tab-schedule')">Schedule</a></li>
            <li class="nav-item"><a class="nav-link" href="javascript:void(0)" onclick="switchTab('tab-services')">Services</a></li>
        </ul>
        <div id="tab-info" class="tab-content-block" style="display:block;">
            <div class="mb-3"><label class="form-label-custom">Name</label><input type="text" id="eb-name" class="form-control" value="${b.bankBranchName}"></div>
            <div class="mb-3"><label class="form-label-custom">Country</label><input type="text" id="eb-country" class="form-control" value="${b.locationDTO?.country || ''}"></div>
            <div class="row">
                <div class="col-md-6 mb-3"><label class="form-label-custom">City</label><input type="text" id="eb-city" class="form-control" value="${b.locationDTO?.city || ''}"></div>
                <div class="col-md-6 mb-3"><label class="form-label-custom">Post Code</label><input type="text" id="eb-postcode" class="form-control" value="${b.locationDTO?.postCode || ''}"></div>
            </div>
            <div class="mb-3"><label class="form-label-custom">Address</label><input type="text" id="eb-address" class="form-control" value="${b.locationDTO?.address || ''}"></div>
            <div class="row">
                <div class="col-md-6 mb-3"><label class="form-label-custom">Latitude</label><input type="text" id="eb-latitude" class="form-control" value="${b.locationDTO?.latitude || ''}"></div>
                <div class="col-md-6 mb-3"><label class="form-label-custom">Longitude</label><input type="text" id="eb-longitude" class="form-control" value="${b.locationDTO?.longitude || ''}"></div>
            </div>
        </div>
        <div id="tab-schedule" class="tab-content-block" style="display:none;">${scheduleHtml}</div>
        <div id="tab-services" class="tab-content-block" style="display:none;">
            <div class="d-flex gap-2 mb-3">
                <select id="eb-service-select" class="form-select">${cachedAllServices.map(s => `<option value="${s.id}">${s.bankServiceName}</option>`).join('')}</select>
                <button class="btn btn-success fw-bold" type="button" onclick="addServiceToLocalList()">Add</button>
            </div>
            <div class="admin-card p-0"><div class="card-header small fw-bold bg-light p-2">Selected Services</div><ul class="list-group list-group-flush" id="services-list-container"></ul></div>
        </div>
    `;

    showModal(`Edit ${b.bankBranchName}`, bodyHtml, async () => {
        const newSchedule = [];
        document.querySelectorAll('.schedule-open').forEach((inp, i) => {
            if (inp.value && document.querySelectorAll('.schedule-close')[i].value) {
                newSchedule.push({ dayOfWeek: inp.getAttribute('data-day'), openTime: inp.value, closeTime: document.querySelectorAll('.schedule-close')[i].value });
            }
        });

        const dto = {
            id: b.id,
            bankBranchName: document.getElementById('eb-name').value,
            locationDTO: {
                country: document.getElementById('eb-country').value,
                city: document.getElementById('eb-city').value,
                address: document.getElementById('eb-address').value,
                postCode: document.getElementById('eb-postcode').value,
                latitude: document.getElementById('eb-latitude').value,
                longitude: document.getElementById('eb-longitude').value,
            },
            schedule: newSchedule,
            bankServices: tempBranchServices
        };

        const res = await adminFetch(`${API_BASE}/branches/${branchId}`, { method: 'PUT', body: JSON.stringify(dto) });
        if (res) { hideModal(); fetchBranches(); }
    }, "Save All Changes");

    renderServicesList();
}

function renderServicesList() {
    const container = document.getElementById('services-list-container');
    if (!container) return;
    if (tempBranchServices.length === 0) { container.innerHTML = '<li class="list-group-item text-muted text-center py-3">No services assigned</li>'; return; }
    container.innerHTML = tempBranchServices.map(s => `<li class="list-group-item d-flex justify-content-between align-items-center"><span>${s.bankServiceName}</span><button class="btn btn-sm btn-outline-danger border-0" onclick="removeServiceFromLocalList(${s.id})"><i class="fa-solid fa-trash"></i></button></li>`).join('');
}

window.addServiceToLocalList = function () {
    const select = document.getElementById('eb-service-select');
    const selectedId = parseInt(select.value);
    if (tempBranchServices.some(s => s.id === selectedId)) { alert("This service is already added."); return; }
    const serviceObj = cachedAllServices.find(s => s.id === selectedId);
    if (serviceObj) { tempBranchServices.push(serviceObj); renderServicesList(); }
};

window.removeServiceFromLocalList = function (serviceId) {
    tempBranchServices = tempBranchServices.filter(s => s.id !== serviceId); renderServicesList();
};

// ==========================================
// 3. SERVICES
let cachedServices = [];

async function fetchServices() {
    const data = await adminFetch(`${API_BASE}/services`);
    cachedServices = Array.isArray(data) ? data : (data && data.id ? [data] : []);
    renderServicesTable(cachedServices);
}

function renderServicesTable(services) {
    const container = document.getElementById('content-area');

    const toolbar = `
        <div class="toolbar">
            <div class="search-container">
                <i class="fa-solid fa-magnifying-glass search-icon"></i>
                <input type="text" class="form-control search-input" 
                       placeholder="Search service..." 
                       onkeyup="filterServices(this.value)"> 
            </div>
            <div class="d-flex gap-2">
                <button class="btn btn-info text-white fw-bold" onclick="openAvailabilityModal()">Check Availability</button>
                <button class="btn btn-primary-custom" onclick="openCreateServiceModal()">+ New Service</button>
            </div>
        </div>`;

    if (!services || services.length === 0) {
        container.innerHTML = toolbar + '<div class="text-center text-muted py-5">No services found.</div>';
        return;
    }

    let rows = services.map(s => `
        <tr>
            <td class="fw-bold">${s.bankServiceName}</td>
            <td>${s.duration}</td>
            <td class="text-muted small">${s.description || '-'}</td>
            <td class="text-end">
                <button class="btn-icon" onclick="openEditServiceModal(${s.id})"><i class="fa-solid fa-pen"></i></button>
                <button class="btn-icon delete" onclick="deleteService(${s.id})"><i class="fa-solid fa-trash"></i></button>
            </td>
        </tr>`).join('');

    renderTable(toolbar, ['Service Name', 'Duration', 'Description', 'Actions'], rows);
}

function filterServices(query) {
    if (!query) {
        renderServicesTable(cachedServices);
        return;
    }

    const lowerQuery = query.toLowerCase();
    const filtered = cachedServices.filter(s =>
        s.bankServiceName.toLowerCase().includes(lowerQuery) ||
        (s.description && s.description.toLowerCase().includes(lowerQuery))
    );

    renderServicesTable(filtered);

    const input = document.querySelector('.search-input');
    if (input) {
        input.value = query;
        input.focus();
    }
}

async function deleteService(id) {
    if (confirm('Delete service?')) {
        await adminFetch(`${API_BASE}/services/${id}`, { method: 'DELETE' });
        fetchServices();
    }
}

function openCreateServiceModal() {
    const html = `<div class="mb-3"><label class="form-label-custom">Name</label><input id="ns-name" class="form-control"></div><div class="mb-3"><label class="form-label-custom">Duration</label><input id="ns-dur" class="form-control" placeholder="e.g. 30 minutes"></div><div class="mb-3"><label class="form-label-custom">Description</label><textarea id="ns-desc" class="form-control"></textarea></div>`;
    showModal('New Service', html, async () => {
        const dto = { bankServiceName: document.getElementById('ns-name').value, duration: document.getElementById('ns-dur').value, description: document.getElementById('ns-desc').value };
        await adminFetch(`${API_BASE}/services`, { method: 'POST', body: JSON.stringify(dto) }); fetchServices(); hideModal();
    }, "Create");
}

async function openEditServiceModal(id) {
    const s = await adminFetch(`${API_BASE}/services/${id}`);
    const html = `<div class="mb-3"><label class="form-label-custom">Name</label><input id="es-name" class="form-control" value="${s.bankServiceName}"></div><div class="mb-3"><label class="form-label-custom">Duration</label><input id="es-dur" class="form-control" value="${s.duration}"></div><div class="mb-3"><label class="form-label-custom">Description</label><textarea id="es-desc" class="form-control">${s.description || ''}</textarea></div>`;
    showModal('Edit Service', html, async () => {
        const dto = { id: s.id, bankServiceName: document.getElementById('es-name').value, duration: document.getElementById('es-dur').value, description: document.getElementById('es-desc').value };
        await adminFetch(`${API_BASE}/services/${id}/update`, { method: 'PUT', body: JSON.stringify(dto) }); fetchServices(); hideModal();
    }, "Save");
}

async function openAvailabilityModal() {
    try {
        const branches = await adminFetch(`${API_BASE}/branches`);
        const services = await adminFetch(`${API_BASE}/services`);
        const bOpts = (branches || []).map(b => `<option value="${b.bankBranchName}">${b.bankBranchName}</option>`).join('');
        const sOpts = (services || []).map(s => `<option value="${s.id}">${s.bankServiceName}</option>`).join('');

        const html = `
            <div class="mb-3">
                <label class="form-label-custom">Branch</label>
                <select id="av-branch" class="form-select">${bOpts}</select>
            </div>
            <div class="mb-3">
                <label class="form-label-custom">Service</label>
                <select id="av-service" class="form-select">${sOpts}</select>
            </div>
            <div class="mb-3">
                <label class="form-label-custom">Date</label>
                <input type="date" id="av-date" class="form-control">
            </div>
            <div id="av-result" class="mt-3 text-center fw-bold" style="min-height: 24px;"></div>
        `;

        showModal('Check Availability', html, performAvailabilityCheck, "Check", true);

    } catch (e) {
        alert("Failed to load data for modal.");
    }
}

async function performAvailabilityCheck() {
    const branchName = document.getElementById('av-branch').value;
    const serviceId = document.getElementById('av-service').value;
    const dateIso = document.getElementById('av-date').value;

    const resDiv = document.getElementById('av-result');
    resDiv.textContent = "";

    if (!dateIso) {
        alert("Select a date");
        return;
    }

    resDiv.innerHTML = '<div class="spinner-border spinner-border-sm text-primary"></div> Checking...';

    try {

        const currentToken = sessionStorage.getItem('accessToken');

        if (!currentToken || currentToken === "null") {
            alert("No authorization token found. Please login.");
            window.location.href = "login.html"; // Редирект на вход
            return;
        }

        const url = `${API_BASE}/services/${serviceId}/availability?branchName=${encodeURIComponent(branchName)}&date=${dateIso}`;

        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${currentToken}` // Теперь здесь точно не "null"
            }
        });

        if (response.ok) {
            const text = await response.text();
            resDiv.innerHTML = `<i class="fa-solid fa-check-circle me-2"></i>Available! <br><span class="small fw-normal">${text}</span>`;
            resDiv.className = "mt-3 text-center text-success fw-bold";
        } else {
            if (response.status === 401 || response.status === 403) {
                alert("Session expired. Please login again.");
                sessionStorage.removeItem('accessToken');
                window.location.href = "login.html";
                return;
            }
            resDiv.innerHTML = `<i class="fa-solid fa-times-circle me-2"></i>Not Available`;
            resDiv.className = "mt-3 text-center text-danger fw-bold";
        }
    } catch (e) {
        console.error(e);
        resDiv.innerHTML = '<span class="text-danger">Connection Error</span>';
    }
}
// ==========================================
// 4. GLOBAL RESERVATIONS
// ==========================================
async function loadGlobalReservations() {
    document.getElementById('content-area').innerHTML = '<div class="text-center mt-5"><div class="spinner-border text-primary"></div></div>';
    const branches = await adminFetch(`${API_BASE}/branches`);
    const services = await adminFetch(`${API_BASE}/services`);
    const bOpts = branches.map(b => `<option value="${b.bankBranchName}">${b.bankBranchName}</option>`).join('');
    const sOpts = services.map(s => `<option value="${s.bankServiceName}">${s.bankServiceName}</option>`).join('');
    const toolbar = `<div class="toolbar"><div class="d-flex align-items-center gap-2 flex-grow-1"><i class="fa-solid fa-filter text-muted me-2"></i><select id="res-filter-branch" class="form-select border-0 bg-transparent fw-bold" style="max-width: 200px;"><option value="">All Branches</option>${bOpts}</select><div class="vr mx-2"></div><select id="res-filter-service" class="form-select border-0 bg-transparent" style="max-width: 200px;"><option value="">All Services</option>${sOpts}</select><div class="vr mx-2"></div><input type="date" id="res-filter-date" class="form-control" style="width: auto;"></div><button class="btn btn-light border ms-2" onclick="fetchGlobalReservations()">Apply Filters</button><button class="btn btn-primary-custom ms-3" onclick="openGlobalCreateReservationModal()">+ New Reservation</button></div><div id="res-table-container"></div>`;
    document.getElementById('content-area').innerHTML = toolbar;
    fetchGlobalReservations();
}

async function fetchGlobalReservations() {
    const container = document.getElementById('res-table-container');
    if (!container) return;
    container.innerHTML = '<div class="text-center py-5"><div class="spinner-border text-primary"></div></div>';

    const branchName = document.getElementById('res-filter-branch')?.value || '';
    const serviceName = document.getElementById('res-filter-service')?.value || '';
    const date = document.getElementById('res-filter-date')?.value || '';

    let url = `${API_BASE}/reservations`;
    if (branchName || serviceName || date) {
        const params = new URLSearchParams();
        if (branchName) params.append('branchName', branchName);
        if (serviceName) params.append('serviceName', serviceName);
        if (date) params.append('date', date);
        url = `${API_BASE}/reservations/search?${params.toString()}`;
    }

    const res = await adminFetch(url);
    if (!res || !Array.isArray(res) || res.length === 0) {
        container.innerHTML = '<div class="text-center text-muted py-5">No reservations found.</div>';
        return ;
    }

    let rows = res.map(r => {
        let act = '-';
        const btnComplete = `<button class="btn-icon text-success" onclick="adminResAction(${r.id}, 'complete')" title="Complete"><i class="fa-solid fa-check"></i></button>`;
        const btnCancel = `<button class="btn-icon text-danger" onclick="adminResAction(${r.id}, 'cancel')" title="Cancel"><i class="fa-solid fa-xmark"></i></button>`;
        const btnActivate = `<button class="btn-icon text-warning" onclick="adminResAction(${r.id}, 'activate')" title="Activate"><i class="fa-solid fa-unlock"></i></button>`;

        if (r.statusReservation === 'PENDING') act = `${btnComplete} ${btnActivate} ${btnCancel}`;
        else if (r.statusReservation === 'ACTIVE') act = `${btnComplete} ${btnCancel}`;

        return `<tr>
                <td><div class="fw-bold text-dark">${new Date(r.startReservation).toLocaleDateString()}</div><div class="small text-muted">${new Date(r.startReservation).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</div></td>
                <td>${r.serviceName}</td>
                <td>${r.branchName || '-'}</td>
                <td><div class="small fw-bold">${r.username || ('ID: ' + r.userId)}</div></td>
                <td><span class="status-badge ${r.statusReservation === 'CANCELED' ? 'bg-soft-danger' : (r.statusReservation === 'COMPLETED' ? 'bg-soft-success' : 'bg-soft-primary')}">${r.statusReservation}</span></td>
                <td class="text-end">${act}</td>
            </tr>`;
    }).join('');

    container.innerHTML = `<div class="admin-card p-0 overflow-hidden border-0"><table class="table-custom"><thead><tr><th>Date</th><th>Service</th><th>Branch</th><th>Client</th><th>Status</th><th class="text-end">Actions</th></tr></thead><tbody>${rows}</tbody></table></div>`;
}

async function adminResAction(id, action) {
    let ep = action === 'cancel' ? 'cancelReservation' : (action === 'activate' ? 'activateReservation' : 'completeReservation');
    await adminFetch(`${API_BASE}/reservations/${id}/${ep}`, { method: 'POST' });
    fetchGlobalReservations();
}

async function openGlobalCreateReservationModal() {
    try {
        const branches = await adminFetch(`${API_BASE}/branches`); // Предполагаю, что adminFetch возвращает JSON
        const services = await adminFetch(`${API_BASE}/services`);
        const bOpts = (branches || []).map(b => `<option value="${b.bankBranchName}">${b.bankBranchName}</option>`).join('');
        const sOpts = (services || []).map(s => `<option value="${s.bankServiceName}">${s.bankServiceName}</option>`).join('');

        const html = `
            <div class="mb-3">
                <label class="form-label-custom">Client Username</label>
                <input type="text" id="new-res-user" class="form-control" placeholder="Enter username">
            </div>
            <div class="row g-2">
                <div class="col-md-6 mb-3">
                    <label class="form-label-custom">Branch</label>
                    <select id="new-res-branch" class="form-select">${bOpts}</select>
                </div>
                <div class="col-md-6 mb-3">
                    <label class="form-label-custom">Service</label>
                    <select id="new-res-service" class="form-select">${sOpts}</select>
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label-custom">Date & Time</label>
                <input type="datetime-local" id="new-res-date" class="form-control">
            </div>`;

        showModal('New Reservation', html, async () => {
            const usernameInput = document.getElementById('new-res-user');
            const dateInput = document.getElementById('new-res-date');
            const branchInput = document.getElementById('new-res-branch');
            const serviceInput = document.getElementById('new-res-service');

            const username = usernameInput.value.trim();
            const dateVal = dateInput.value;
            if (!username) {
                alert("Please enter a Client Username!");
                usernameInput.focus();
                return;
            }
            if (!dateVal) {
                alert("Please select a Date and Time for the reservation!");
                dateInput.focus();
                return;
            }
            try {
                const p = new URLSearchParams({
                    username: username,
                    startReservation: dateVal,
                    serviceName: serviceInput.value,
                    branchName: branchInput.value
                });

                await adminFetch(`${API_BASE}/reservations?${p.toString()}`, { method: 'POST' });

                fetchGlobalReservations();
                hideModal();

            } catch (e) {
                console.error(e);
                alert("Error creating reservation. Check console for details.");
            }

        }, "Create");

    } catch (e) {
        console.error("Error opening modal:", e);
        alert("Failed to load branches or services.");
    }
}

// ==========================================
// 5. SUPPORT
// ==========================================
async function loadSupport() {
    const msgs = await adminFetch(`${API_BASE}/supportMessages`);
    cachedSupportMessages = msgs || [];

    renderSupportUI(cachedSupportMessages, '', '');
}

async function filterSupport() {
    const emailInput = document.getElementById('supp-filter-email');
    const dateInput = document.getElementById('supp-filter-date');

    const currentEmail = emailInput ? emailInput.value.trim() : '';
    const currentDate = dateInput ? dateInput.value : '';

    const params = new URLSearchParams();
    if (currentEmail) params.append('email', currentEmail);

    if (currentDate) params.append('date', currentDate);

    const msgs = await adminFetch(`${API_BASE}/supportMessages/search?${params.toString()}`);
    cachedSupportMessages = msgs || [];

    renderSupportUI(cachedSupportMessages, currentEmail, currentDate);
}

function renderSupportUI(messages, emailVal, dateVal) {
    const container = document.getElementById('content-area');

    const toolbar = `
        <div class="toolbar mb-3">
             <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 w-100">
                 <h5 class="m-0 fw-bold">Support Center</h5>
                 <div class="d-flex gap-2">
                     <input type="text" id="supp-filter-email" class="form-control" 
                            placeholder="Filter by Email" value="${emailVal}" style="width: 200px;">
                     <input type="date" id="supp-filter-date" class="form-control" 
                            value="${dateVal}" style="width: 150px;">
                     
                     <button class="btn btn-primary-custom" onclick="filterSupport()">
                        <i class="fa-solid fa-magnifying-glass"></i>
                     </button>
                     
                     <button class="btn btn-light border" onclick="loadSupport()">Reset</button>
                 </div>
             </div>
        </div>
    `;

    if (!messages || messages.length === 0) {
        container.innerHTML = toolbar + '<div class="text-center text-muted py-5">No messages found.</div>';
        return;
    }

    let rows = messages.map(m => {
        const previewText = m.message.length > 60 ? m.message.substring(0, 60) + '...' : m.message;
        const userDisplay = m.userEmail || '<span class="text-muted fst-italic">Unknown</span>';

        return `<tr>
            <td>
                <div class="fw-bold text-dark">${new Date(m.createdAt).toLocaleDateString()}</div>
                <div class="small text-muted">${new Date(m.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</div>
            </td>
            <td>${userDisplay}</td>
            <td>${previewText}</td>
            <td class="text-end">
                <button class="btn btn-outline-primary btn-sm fw-bold" onclick="openReplyModal(${m.id})">
                    <i class="fa-solid fa-reply me-1"></i> Reply
                </button>
            </td>
        </tr>`;
    }).join('');

    renderTable(toolbar, ['Date', 'From', 'Message', 'Action'], rows);

    if(emailVal) {
        const input = document.getElementById('supp-filter-email');
        if(input) {
            input.focus();
            input.setSelectionRange(input.value.length, input.value.length);
        }
    }
}

function openReplyModal(msgId) {
    const msg = cachedSupportMessages.find(m => m.id === msgId);
    if (!msg) return;
    const html = `
        <div class="alert alert-secondary mb-3">
            <div class="d-flex justify-content-between mb-1"><small class="fw-bold">${msg.userEmail}</small><small>${new Date(msg.createdAt).toLocaleString()}</small></div>
            <div class="fst-italic small text-muted">"${msg.message}"</div>
        </div>
        <div class="mb-3"><label class="form-label-custom">Your Answer</label><textarea id="support-reply-text" class="form-control" rows="6" placeholder="Type your reply here..."></textarea></div>
    `;
    showModal('Reply to User', html, async () => {
        const text = document.getElementById('support-reply-text').value;
        if (!text || text.trim() === "") return alert("Please enter a message.");
        const res = await adminFetch(`${API_BASE}/supportMessages/${msgId}/reply`, { method: 'POST', body: JSON.stringify({ messageId: msgId, replyText: text }) });
        if (res) { alert("Reply sent successfully!"); hideModal(); loadSupport(); }
    }, "Send Reply");
}

// ==========================================
// CORE HELPERS
// ==========================================
function renderTable(topHtml, headers, rows, target = null) {
    const content = target || document.getElementById('content-area');
    content.innerHTML = `${topHtml}<div class="admin-card p-0 overflow-hidden border-0"><table class="table-custom"><thead><tr>${headers.map(h => `<th>${h}</th>`).join('')}</tr></thead><tbody>${rows}</tbody></table></div>`;
}

window.switchTab = function (tabId) {
    document.querySelectorAll('.tab-content-block').forEach(el => { el.style.display = 'none'; });
    document.querySelectorAll('#branchTabs .nav-link').forEach(el => { el.classList.remove('active'); });
    const activeBlock = document.getElementById(tabId);
    if (activeBlock) activeBlock.style.display = 'block';
    if (event && event.target) event.target.classList.add('active');
}