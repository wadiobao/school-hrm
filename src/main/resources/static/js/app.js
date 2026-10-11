// API helper function
async function fetchApi(url, options = {}) {
    try {
        const response = await fetch(url, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            }
        });

        // Handle empty body responses (e.g. 204 or void success)
        const text = await response.text();
        const data = text ? JSON.parse(text) : {};

        if (!response.ok) {
            const errorMsg = data.message || (data.errors ? Object.values(data.errors).join(', ') : 'Thao tác không thành công');
            throw new Error(errorMsg);
        }

        return data;
    } catch (error) {
        console.error('API Error:', error);
        showAlert(error.message, 'danger');
        throw error;
    }
}

// Bootstrap Toast/Alert Notifications
function showAlert(message, type = 'success') {
    const alertContainer = document.getElementById('alert-container');
    if (!alertContainer) return;

    const iconMap = {
        success: 'bi-check-circle-fill',
        danger: 'bi-exclamation-triangle-fill',
        warning: 'bi-exclamation-circle-fill',
        info: 'bi-info-circle-fill'
    };

    const icon = iconMap[type] || 'bi-info-circle-fill';

    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show shadow-sm d-flex align-items-center mb-3" role="alert">
            <i class="bi ${icon} me-2 fs-5"></i>
            <div>${message}</div>
            <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;

    alertContainer.innerHTML = alertHtml;

    setTimeout(() => {
        const alertNode = alertContainer.querySelector('.alert');
        if (alertNode) {
            const bsAlert = new bootstrap.Alert(alertNode);
            bsAlert.close();
        }
    }, 4500);
}

// Format Date Utility
function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    try {
        const d = new Date(dateStr);
        if (isNaN(d.getTime())) return dateStr;
        return d.toLocaleDateString('vi-VN');
    } catch (e) {
        return dateStr;
    }
}

// Format Time Utility
function formatTime(timeStr) {
    if (!timeStr) return '--:--';
    return timeStr.substring(0, 5);
}

// Global modal helpers
function openModal(modalId) {
    const modalEl = document.getElementById(modalId);
    if (modalEl) {
        const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
        modal.show();
    }
}

function closeModal(modalId) {
    const modalEl = document.getElementById(modalId);
    if (modalEl) {
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();
    }
}

// ─── Authentication & Session Helpers ───────────────────────────────────────
function getCurrentUser() {
    try {
        const raw = localStorage.getItem('currentUser');
        return raw ? JSON.parse(raw) : null;
    } catch (e) {
        return null;
    }
}

function checkAuth() {
    const user = getCurrentUser();
    // Nếu đang ở trang login thì không redirect
    if (window.location.pathname === '/login') {
        if (user) {
            window.location.href = '/';
        }
        return;
    }

    if (!user) {
        window.location.href = '/login';
        return;
    }

    // Render thông tin người dùng lên topbar
    const avatarEl = document.getElementById('topbar-avatar');
    const fullnameEl = document.getElementById('topbar-fullname');
    const roleEl = document.getElementById('topbar-role');

    if (fullnameEl && user.fullName) {
        fullnameEl.textContent = user.fullName;
    }
    if (roleEl && (user.roleName || user.roleCode)) {
        roleEl.textContent = `${user.roleName || user.roleCode} (${user.email || user.username})`;
    }
    if (avatarEl && user.fullName) {
        const names = user.fullName.trim().split(' ');
        const initials = names.length > 1
            ? (names[0][0] + names[names.length - 1][0]).toUpperCase()
            : user.fullName.substring(0, 2).toUpperCase();
        avatarEl.textContent = initials;
    }

    // Áp dụng phân quyền UI (Sidebar menus & Action buttons)
    applyRoleBasedUI(user);
}

// ─── Role-Based Access Control (RBAC) Helpers ──────────────────────────────
/**
 * Kiểm tra xem user hiện tại có thuộc danh sách roles cho phép hay không.
 * Tuân thủ theo permission.md: SUPER_ADMIN chỉ có quyền quản trị kỹ thuật & danh mục,
 * KHÔNG tự động có quyền sửa lương, chấm công, duyệt phép của nghiệp vụ.
 *
 * @param  {...string} allowedRoles - Danh sách RoleCode (ví dụ: 'HR_MANAGER', 'BOARD_OF_DIRECTORS')
 * @returns {boolean}
 */
function hasRole(...allowedRoles) {
    const user = getCurrentUser();
    if (!user || !user.roleCode) return false;
    return allowedRoles.includes(user.roleCode);
}

/**
 * Ẩn hoặc hiện các phần tử có thuộc tính `data-roles` trên toàn bộ trang:
 * - Nếu user có role khớp với danh sách data-roles: hiển thị.
 * - Nếu không có role phù hợp: ẩn khỏi DOM.
 */
function applyRoleBasedUI(user) {
    if (!user) return;
    const currentRole = user.roleCode || '';

    // 1. Áp dụng cho các phần tử có thuộc tính [data-roles]
    document.querySelectorAll('[data-roles]').forEach(el => {
        const rolesAttr = el.getAttribute('data-roles');
        if (!rolesAttr) return;

        const allowedList = rolesAttr.split(',').map(r => r.trim());
        if (allowedList.includes(currentRole)) {
            el.style.removeProperty('display');
            el.classList.remove('d-none');
        } else {
            el.style.setProperty('display', 'none', 'important');
            el.classList.add('d-none');
        }
    });

    // 2. Chặn truy cập trực tiếp nếu người dùng vào URL ngoài quyền hạn (theo ma trận permission.md)
    enforcePageAccess(currentRole);
}

/**
 * Kiểm tra quyền truy cập đường dẫn hiện tại của trang theo Ma trận quyền permission.md:
 * - /employees: SUPER_ADMIN (kỹ thuật), BOARD_OF_DIRECTORS (view), HR_MANAGER (CRUD), HEAD_OF_DEPARTMENT (unit)
 * - /departments, /positions: SUPER_ADMIN (danh mục), BOARD_OF_DIRECTORS (view), HR_MANAGER (CRUD), HEAD_OF_DEPARTMENT (dept)
 * - /payroll: BOARD_OF_DIRECTORS (approve/view), HR_MANAGER (quản lý/đề xuất), TEACHER & STAFF (view own)
 * - /teaching: BOARD_OF_DIRECTORS (view), HEAD_OF_DEPARTMENT (quản lý/duyệt), TEACHER (giảng dạy/nhật ký)
 * - /attendance, /leaves: Tất cả nhân viên đều có quyền truy cập (xem cá nhân hoặc quản lý)
 */
function enforcePageAccess(roleCode) {
    const path = window.location.pathname;

    const pagePermissions = {
        '/employees': ['SUPER_ADMIN', 'HR_MANAGER', 'BOARD_OF_DIRECTORS', 'HEAD_OF_DEPARTMENT'],
        '/departments': ['SUPER_ADMIN', 'HR_MANAGER', 'BOARD_OF_DIRECTORS', 'HEAD_OF_DEPARTMENT'],
        '/positions': ['SUPER_ADMIN', 'HR_MANAGER', 'BOARD_OF_DIRECTORS'],
        '/payroll': ['SUPER_ADMIN', 'HR_MANAGER', 'BOARD_OF_DIRECTORS', 'TEACHER', 'STAFF'],
        '/teaching': ['SUPER_ADMIN', 'BOARD_OF_DIRECTORS', 'HEAD_OF_DEPARTMENT', 'TEACHER']
    };

    for (const [route, roles] of Object.entries(pagePermissions)) {
        if (path === route || path.startsWith(route + '/')) {
            if (!roles.includes(roleCode)) {
                alert(`Bạn không có quyền truy cập chức năng này (Vai trò: ${roleCode}).`);
                window.location.href = '/';
                break;
            }
        }
    }
}

function logout() {
    localStorage.removeItem('currentUser');
    window.location.href = '/login';
}

// Tự động kiểm tra phiên đăng nhập khi trang tải xong
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
});


