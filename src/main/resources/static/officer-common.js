const OFFICER_PAGES = {
  assigned: {
    title: 'Assigned Tasks',
    subtitle: 'Fresh assignments waiting for ownership and field action.',
    hero: 'Assigned Work Queue',
    heroText: 'Review every grievance routed to you, inspect citizen details, and move work forward without leaving the officer workspace.',
    endpoint: '/api/officer/tasks',
    primaryStat: 'assigned',
    emptyTitle: 'No assigned tasks right now',
    emptySub: 'New assignments from the admin desk will appear here.'
  },
  inprogress: {
    title: 'In Progress',
    subtitle: 'Active cases currently being worked by you.',
    hero: 'Work In Motion',
    heroText: 'Keep active field tasks moving, record remarks, and update statuses as work advances on the ground.',
    endpoint: '/api/officer/tasks/in-progress',
    primaryStat: 'inProgress',
    emptyTitle: 'No tasks are marked in progress',
    emptySub: 'Start one of your assigned cases to see it here.'
  },
  completed: {
    title: 'Completed',
    subtitle: 'Resolved and closed work delivered back to the system.',
    hero: 'Completed Cases',
    heroText: 'Review finished work, verify notes, and keep a clean history of closures and resolutions.',
    endpoint: '/api/officer/tasks/completed',
    primaryStat: 'completed',
    readOnly: true,
    emptyTitle: 'No completed tasks yet',
    emptySub: 'Resolved and closed grievances will appear here.'
  },
  profile: {
    title: 'My Profile',
    subtitle: 'Officer identity, contact details, and workload summary.',
    hero: 'Officer Profile',
    heroText: 'Keep your contact details visible and monitor your current service load from one place.',
    primaryStat: 'assigned'
  }
};

const CATEGORY_ICONS = {
  WATER: '💧',
  STREET_LIGHT: '💡',
  ROAD: '🛣️',
  SANITATION: '🗑️',
  DRAINAGE: '🌊',
  PARK: '🌳',
  ELECTRICITY: '⚡',
  OTHER: '📌'
};

const CATEGORY_COLORS = {
  WATER: '#dbeafe',
  STREET_LIGHT: '#fef3c7',
  ROAD: '#fee2e2',
  SANITATION: '#fef3c7',
  DRAINAGE: '#e0f2fe',
  PARK: '#d1fae5',
  ELECTRICITY: '#fef9c3',
  OTHER: '#f3f4f6'
};

const OFFICER_NAV = [
  { section: 'Work', items: [
    { id: 'overview', label: 'Dashboard', href: '/Dashboard.html', badgeKey: null, icon: homeIcon() },
    { id: 'assigned', label: 'Assigned Tasks', href: '/OfficerAssignedTasks.html', badgeKey: 'assigned', icon: listIcon() },
    { id: 'inprogress', label: 'In Progress', href: '/OfficerInProgress.html', badgeKey: 'inProgress', icon: wrenchIcon() },
    { id: 'completed', label: 'Completed', href: '/OfficerCompleted.html', badgeKey: 'completed', icon: checkIcon() }
  ]},
  { section: 'Account', items: [
    { id: 'profile', label: 'My Profile', href: '/OfficerProfile.html', badgeKey: null, icon: userIcon() }
  ]}
];

const appState = {
  token: localStorage.getItem('civicpulse_token'),
  user: safeParse(localStorage.getItem('civicpulse_user')),
  page: document.body.dataset.page,
  tasks: [],
  stats: null,
  profile: null,
  search: '',
  statusFilter: 'ALL',
  selectedTask: null
};

function safeParse(value) {
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

function authGuard() {
  if (!appState.token || !appState.user) {
    window.location.href = '/index.html';
    throw new Error('Not authenticated');
  }
  if (appState.user.role !== 'OFFICER') {
    alert('Access denied. Officers only.');
    window.location.href = '/Dashboard.html';
    throw new Error('Not officer');
  }
}

function authHeaders() {
  return {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + appState.token
  };
}

function initOfficerPage() {
  authGuard();
  document.body.classList.add('role-officer');
  document.getElementById('sidebarRole').textContent = 'OFFICER';
  document.getElementById('sidebarUsername').textContent = '@' + appState.user.username;
  document.getElementById('userAvatar').textContent = appState.user.username.charAt(0).toUpperCase();
  document.getElementById('topbarDate').textContent = new Date().toLocaleDateString('en-IN', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
  });
  buildNav();
  wireModal();
  if (appState.page === 'profile') {
    loadOfficerProfile();
  } else {
    loadOfficerTasks();
  }
}

function buildNav() {
  const nav = document.getElementById('navItems');
  nav.innerHTML = OFFICER_NAV.map(section => `
    <div class="nav-section">
      <div class="nav-section-label">${section.section}</div>
      ${section.items.map(item => `
        <a class="nav-item ${item.id === appState.page ? 'active' : ''}" href="${item.href}">
          ${item.icon}
          ${item.label}
          ${renderNavBadge(item.badgeKey)}
        </a>
      `).join('')}
    </div>
  `).join('');
}

function renderNavBadge(key) {
  if (!key || !appState.stats) return '';
  const value = appState.stats[key] || 0;
  return value ? `<span class="nav-badge">${value}</span>` : '';
}

async function fetchJson(url, options = {}) {
  const response = await fetch(url, options);
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(payload.error || 'Request failed');
  }
  return payload;
}

async function loadOfficerTasks() {
  const page = OFFICER_PAGES[appState.page];
  renderShell(page, true);
  try {
    const [stats, tasks] = await Promise.all([
      fetchJson('/api/officer/stats', { headers: authHeaders() }),
      fetchJson(page.endpoint, { headers: authHeaders() })
    ]);
    appState.stats = stats;
    appState.tasks = tasks;
    buildNav();
    renderTaskPage(page);
  } catch (error) {
    renderError(page, error.message || 'Could not load officer tasks.');
  }
}

async function loadOfficerProfile() {
  const page = OFFICER_PAGES.profile;
  renderShell(page, true);
  try {
    const [stats, profile, tasks] = await Promise.all([
      fetchJson('/api/officer/stats', { headers: authHeaders() }),
      fetchJson('/api/officer/profile', { headers: authHeaders() }),
      fetchJson('/api/officer/tasks', { headers: authHeaders() })
    ]);
    appState.stats = stats;
    appState.profile = profile;
    appState.tasks = tasks;
    buildNav();
    renderProfilePage(page);
  } catch (error) {
    renderError(page, error.message || 'Could not load profile.');
  }
}

function renderShell(page, loading) {
  document.getElementById('topbarTitle').textContent = page.title;
  const content = document.getElementById('pageContent');
  content.innerHTML = `
    <section class="hero">
      <div>
        <div class="hero-kicker">Officer Workspace</div>
        <h2>${page.hero}</h2>
        <p>${page.heroText}</p>
      </div>
      <div class="hero-stat">
        <strong>${loading ? '...' : primaryValue(page.primaryStat)}</strong>
        <span>${labelForPrimary(page.primaryStat)}</span>
      </div>
    </section>
    <div id="pageBody"></div>
  `;
}

function primaryValue(key) {
  if (!key || !appState.stats) return '--';
  return String(appState.stats[key] || 0);
}

function labelForPrimary(key) {
  const map = {
    assigned: 'assigned tasks',
    inProgress: 'active tasks',
    completed: 'completed tasks'
  };
  return map[key] || 'service load';
}

function renderTaskPage(page) {
  renderShell(page, false);
  const filtered = filterTasks(appState.tasks);
  const body = document.getElementById('pageBody');
  body.innerHTML = `
    ${renderTaskStats()}
    <section class="panel">
      <div class="panel-header">
        <div>
          <div class="panel-title">${page.title}</div>
          <div class="panel-subtitle">${page.subtitle}</div>
        </div>
        <div class="panel-tools">
          <input class="search-input" id="taskSearch" type="search" placeholder="Search by title, citizen, department or grievance ID" value="${escapeAttr(appState.search)}" />
          <select class="status-select" id="statusFilter">
            <option value="ALL">All statuses</option>
            <option value="PENDING">Pending</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="RESOLVED">Resolved</option>
            <option value="CLOSED">Closed</option>
          </select>
        </div>
      </div>
      ${renderTaskGrid(filtered, page)}
    </section>
  `;
  document.getElementById('statusFilter').value = appState.statusFilter;
  document.getElementById('taskSearch').addEventListener('input', event => {
    appState.search = event.target.value;
    renderTaskPage(page);
  });
  document.getElementById('statusFilter').addEventListener('change', event => {
    appState.statusFilter = event.target.value;
    renderTaskPage(page);
  });
}

function renderTaskStats() {
  const stats = appState.stats || { assigned: 0, pending: 0, inProgress: 0, completed: 0 };
  return `
    <section class="stats-grid">
      ${renderStatCard('Assigned', stats.assigned)}
      ${renderStatCard('Pending', stats.pending)}
      ${renderStatCard('In Progress', stats.inProgress)}
      ${renderStatCard('Completed', stats.completed)}
    </section>
  `;
}

function renderStatCard(label, value) {
  return `
    <div class="stat-card">
      <div class="stat-label">${label}</div>
      <div class="stat-value">${value || 0}</div>
    </div>
  `;
}

function renderTaskGrid(tasks, page) {
  if (!tasks.length) {
    return `
      <div class="empty-state">
        <strong>${page.emptyTitle}</strong>
        <div>${page.emptySub}</div>
      </div>
    `;
  }
  return `
    <div class="task-grid">
      ${tasks.map(task => renderTaskCard(task, page)).join('')}
    </div>
  `;
}

function renderTaskCard(task, page) {
  const icon = CATEGORY_ICONS[task.category] || '📌';
  const bg = CATEGORY_COLORS[task.category] || '#f3f4f6';
  const canUpdate = !page.readOnly;
  return `
    <article class="task-card">
      <div class="task-top">
        <div style="display:flex;gap:14px;min-width:0;">
          <div class="task-icon" style="background:${bg}">${icon}</div>
          <div style="min-width:0;">
            <div class="task-title">${escapeHtml(task.title)}</div>
            <div class="task-meta">
              <span>#${task.id}</span>
              <span>${formatCategory(task.category)}</span>
              <span>Citizen: ${escapeHtml(task.citizenUsername || 'Unknown')}</span>
              <span>${formatDate(task.submittedAt)}</span>
            </div>
          </div>
        </div>
        <div class="badges">${statusBadge(task.status)}</div>
      </div>
      <div class="task-description">${escapeHtml(trimText(task.description, 200))}</div>
      <div class="badges">
        ${priorityBadge(task.priority)}
        ${task.department ? `<span class="badge" style="background:#ecfeff;color:#0f766e;">${escapeHtml(task.department)}</span>` : ''}
        ${task.location ? `<span class="badge" style="background:#f8fafc;color:#475569;">${escapeHtml(task.location)}</span>` : ''}
      </div>
      ${task.remarks ? `<div class="note"><strong>Latest remarks:</strong> ${escapeHtml(task.remarks)}</div>` : ''}
      <div class="card-actions">
        <span style="color:var(--ink-soft);font-size:0.8rem;">Updated ${task.updatedAt ? formatDate(task.updatedAt) : 'not yet'}</span>
        <div class="btn-row">
          <button class="btn-secondary" onclick="openTaskModal(${task.id})">View details</button>
          ${canUpdate ? `<button class="btn" onclick="openUpdateModal(${task.id})">Update status</button>` : ''}
        </div>
      </div>
    </article>
  `;
}

function renderProfilePage(page) {
  renderShell(page, false);
  const profile = appState.profile;
  const recentTasks = (appState.tasks || []).slice(0, 4);
  document.getElementById('pageBody').innerHTML = `
    <section class="stats-grid">
      ${renderStatCard('Assigned', appState.stats.assigned)}
      ${renderStatCard('Pending', appState.stats.pending)}
      ${renderStatCard('In Progress', appState.stats.inProgress)}
      ${renderStatCard('Completed', appState.stats.completed)}
    </section>
    <section class="profile-grid">
      <div class="profile-card">
        <div class="profile-header">
          <div class="profile-avatar">${escapeHtml(profile.username.charAt(0).toUpperCase())}</div>
          <div>
            <div class="profile-name">${escapeHtml(profile.username)}</div>
            <div class="profile-role">${escapeHtml(profile.role)}</div>
          </div>
        </div>
        <div class="info-list">
          <div class="info-row">
            <div class="info-label">Email</div>
            <div class="info-value">${escapeHtml(profile.email || 'Not provided')}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Phone</div>
            <div class="info-value">${escapeHtml(profile.phone || 'Not provided')}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Officer ID</div>
            <div class="info-value">#${profile.id}</div>
          </div>
          <div class="info-row">
            <div class="info-label">Role Snapshot</div>
            <div class="info-value">Municipal field operations officer</div>
          </div>
        </div>
      </div>
      <div class="summary-card">
        <div class="panel-title" style="margin-bottom:4px;">Current Work Summary</div>
        <div class="panel-subtitle" style="margin-bottom:18px;">Operational workload based on tasks assigned to your account.</div>
        <div class="summary-list">
          <div class="summary-item"><span class="summary-label">Pending assignments</span><span class="summary-value">${appState.stats.pending}</span></div>
          <div class="summary-item"><span class="summary-label">Tasks in progress</span><span class="summary-value">${appState.stats.inProgress}</span></div>
          <div class="summary-item"><span class="summary-label">Completed cases</span><span class="summary-value">${appState.stats.completed}</span></div>
          <div class="summary-item"><span class="summary-label">Total active workload</span><span class="summary-value">${appState.stats.assigned}</span></div>
        </div>
        <div class="timeline">
          ${(recentTasks.length ? recentTasks : [{ title: 'No tasks assigned yet', status: 'Stand by for new work', submittedAt: new Date().toISOString() }]).map(task => `
            <div class="timeline-item">
              <strong>${escapeHtml(task.title)}</strong>
              <span>${task.status ? formatStatus(task.status) : 'Ready'} • ${formatDate(task.submittedAt)}</span>
            </div>
          `).join('')}
        </div>
      </div>
    </section>
  `;
}

function renderError(page, message) {
  renderShell(page, false);
  document.getElementById('pageBody').innerHTML = `
    <section class="panel">
      <div class="error-state">
        <strong>Unable to load officer workspace</strong>
        <div>${escapeHtml(message)}</div>
      </div>
    </section>
  `;
}

function filterTasks(tasks) {
  return tasks.filter(task => {
    const matchesStatus = appState.statusFilter === 'ALL' || task.status === appState.statusFilter;
    if (!matchesStatus) return false;
    const search = appState.search.trim().toLowerCase();
    if (!search) return true;
    return [
      task.title,
      task.description,
      task.citizenUsername,
      task.department,
      task.location,
      String(task.id)
    ].some(value => String(value || '').toLowerCase().includes(search));
  });
}

function openTaskModal(id) {
  appState.selectedTask = appState.tasks.find(task => task.id === id) || null;
  if (!appState.selectedTask) return;
  document.getElementById('modalTitle').textContent = `Task #${appState.selectedTask.id}`;
  document.getElementById('modalBody').innerHTML = `
    <div class="modal-grid">
      <div class="modal-field full"><div class="modal-label">Title</div><div class="modal-value">${escapeHtml(appState.selectedTask.title)}</div></div>
      <div class="modal-field"><div class="modal-label">Status</div><div class="modal-value">${formatStatus(appState.selectedTask.status)}</div></div>
      <div class="modal-field"><div class="modal-label">Priority</div><div class="modal-value">${escapeHtml(appState.selectedTask.priority || 'MEDIUM')}</div></div>
      <div class="modal-field"><div class="modal-label">Citizen</div><div class="modal-value">${escapeHtml(appState.selectedTask.citizenUsername || 'Unknown')}</div></div>
      <div class="modal-field"><div class="modal-label">Department</div><div class="modal-value">${escapeHtml(appState.selectedTask.department || 'Not assigned')}</div></div>
      <div class="modal-field"><div class="modal-label">Location</div><div class="modal-value">${escapeHtml(appState.selectedTask.location || 'Not provided')}</div></div>
      <div class="modal-field full"><div class="modal-label">Description</div><div class="modal-value">${escapeHtml(appState.selectedTask.description || '')}</div></div>
      ${appState.selectedTask.adminNotes ? `<div class="modal-field full"><div class="modal-label">Admin Notes</div><div class="modal-value">${escapeHtml(appState.selectedTask.adminNotes)}</div></div>` : ''}
      ${appState.selectedTask.remarks ? `<div class="modal-field full"><div class="modal-label">Officer Remarks</div><div class="modal-value">${escapeHtml(appState.selectedTask.remarks)}</div></div>` : ''}
    </div>
    <div class="modal-actions">
      <button class="btn-secondary" onclick="closeModal()">Close</button>
      ${OFFICER_PAGES[appState.page].readOnly ? '' : `<button class="btn" onclick="openUpdateModal(${appState.selectedTask.id}, true)">Update status</button>`}
    </div>
  `;
  showModal();
}

function openUpdateModal(id, fromDetail = false) {
  if (fromDetail) closeModal();
  appState.selectedTask = appState.tasks.find(task => task.id === id) || null;
  if (!appState.selectedTask) return;
  document.getElementById('modalTitle').textContent = `Update Task #${appState.selectedTask.id}`;
  document.getElementById('modalBody').innerHTML = `
    <div class="modal-grid">
      <div class="modal-field full"><div class="modal-label">Task</div><div class="modal-value">${escapeHtml(appState.selectedTask.title)}</div></div>
      <div class="modal-field"><div class="modal-label">Current Status</div><div class="modal-value">${formatStatus(appState.selectedTask.status)}</div></div>
      <div class="modal-field"><div class="modal-label">Citizen</div><div class="modal-value">${escapeHtml(appState.selectedTask.citizenUsername || 'Unknown')}</div></div>
      <div class="modal-field full">
        <div class="modal-label">New Status</div>
        <select id="statusUpdateSelect" class="status-update-select">
          <option value="PENDING">Pending</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="RESOLVED">Resolved</option>
          <option value="CLOSED">Closed</option>
        </select>
      </div>
      <div class="modal-field full">
        <div class="modal-label">Remarks</div>
        <textarea id="remarksInput" class="textarea" rows="5" placeholder="Add field update, site note, completion summary, or blocker.">${escapeHtml(appState.selectedTask.remarks || '')}</textarea>
      </div>
    </div>
    <div class="modal-actions">
      <button class="btn-secondary" onclick="closeModal()">Cancel</button>
      <button class="btn" onclick="submitStatusUpdate()">Save Update</button>
    </div>
  `;
  document.getElementById('statusUpdateSelect').value = appState.selectedTask.status || 'PENDING';
  showModal();
}

async function submitStatusUpdate() {
  const status = document.getElementById('statusUpdateSelect').value;
  const remarks = document.getElementById('remarksInput').value.trim();
  try {
    const updated = await fetchJson(`/api/officer/tasks/${appState.selectedTask.id}/status`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify({ status, remarks })
    });
    appState.tasks = appState.tasks.map(task => task.id === updated.id ? updated : task);
    appState.stats = await fetchJson('/api/officer/stats', { headers: authHeaders() });
    buildNav();
    closeModal();
    showToast('Task updated successfully.', 'success');
    if (appState.page === 'profile') {
      renderProfilePage(OFFICER_PAGES.profile);
    } else {
      const page = OFFICER_PAGES[appState.page];
      if (page.endpoint !== '/api/officer/tasks') {
        appState.tasks = await fetchJson(page.endpoint, { headers: authHeaders() });
      }
      renderTaskPage(page);
    }
  } catch (error) {
    showToast(error.message || 'Could not update task.', 'error');
  }
}

function wireModal() {
  document.getElementById('modalOverlay').addEventListener('click', event => {
    if (event.target.id === 'modalOverlay') {
      closeModal();
    }
  });
}

function showModal() {
  document.body.classList.add('modal-open');
  document.getElementById('modalOverlay').classList.add('show');
}

function closeModal() {
  document.body.classList.remove('modal-open');
  document.getElementById('modalOverlay').classList.remove('show');
}

function showToast(message, type) {
  const toast = document.getElementById('toast');
  toast.textContent = message;
  toast.className = `toast ${type || ''} show`;
  setTimeout(() => {
    toast.className = 'toast';
  }, 2600);
}

function logout() {
  localStorage.removeItem('civicpulse_token');
  localStorage.removeItem('civicpulse_user');
  window.location.href = '/index.html';
}

function formatDate(input) {
  if (!input) return 'Not available';
  return new Date(input).toLocaleDateString('en-IN', {
    day: 'numeric', month: 'short', year: 'numeric'
  });
}

function formatCategory(category) {
  return String(category || 'OTHER').replace(/_/g, ' ');
}

function formatStatus(status) {
  return String(status || '').replace(/_/g, ' ');
}

function trimText(text, limit) {
  const value = String(text || '');
  return value.length > limit ? value.slice(0, limit - 1) + '…' : value;
}

function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function escapeAttr(value) {
  return escapeHtml(value).replace(/'/g, '&#39;');
}

function statusBadge(status) {
  const map = {
    PENDING: 'badge-pending',
    IN_PROGRESS: 'badge-progress',
    RESOLVED: 'badge-resolved',
    CLOSED: 'badge-closed'
  };
  return `<span class="badge ${map[status] || 'badge-closed'}">${formatStatus(status)}</span>`;
}

function priorityBadge(priority) {
  const value = String(priority || 'MEDIUM').toUpperCase();
  return `<span class="badge badge-priority-${value.toLowerCase()}">${value}</span>`;
}

function homeIcon() { return `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>`; }
function listIcon() { return `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z"/></svg>`; }
function wrenchIcon() { return `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M22.7 19.3l-6.4-6.4a6 6 0 01-7.8-7.8l3.1 3.1 2.8-.7.7-2.8-3.1-3.1a6 6 0 017.8 7.8l6.4 6.4-3.5 3.5zM4.5 18A2.5 2.5 0 107 20.5 2.5 2.5 0 004.5 18z"/></svg>`; }
function checkIcon() { return `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>`; }
function userIcon() { return `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>`; }

document.addEventListener('DOMContentLoaded', initOfficerPage);
