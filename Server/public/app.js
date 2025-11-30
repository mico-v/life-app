/**
 * Life App Web Dashboard
 * Material Design 3 Frontend
 */

class LifeAppDashboard {
    constructor() {
        this.currentPage = 'dashboard';
        this.tasks = [];
        this.profiles = [];
        this.timeline = null;
        this.selectedTag = '';
        
        this.init();
    }
    
    init() {
        this.bindNavigation();
        this.loadData();
        
        // Refresh data every 30 seconds
        setInterval(() => this.loadData(), 30000);
    }
    
    bindNavigation() {
        document.querySelectorAll('.nav-item').forEach(item => {
            item.addEventListener('click', () => {
                const page = item.dataset.page;
                this.navigateTo(page);
            });
        });
    }
    
    navigateTo(page) {
        // Update nav items
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.toggle('active', item.dataset.page === page);
        });
        
        // Update pages
        document.querySelectorAll('.page').forEach(p => {
            p.classList.toggle('active', p.id === `${page}-page`);
        });
        
        this.currentPage = page;
        
        // Update page title
        const titles = {
            dashboard: 'Life App',
            tasks: '任务列表',
            timeline: '时间轴',
            profile: '个人中心'
        };
        document.querySelector('.page-title').textContent = titles[page] || 'Life App';
    }
    
    async loadData() {
        this.showLoading(true);
        
        try {
            const response = await fetch('/api/v1/public/dashboard');
            const data = await response.json();
            
            if (data.success) {
                this.tasks = data.publicTasks || [];
                this.profiles = data.profiles || [];
                this.updateDashboard(data.stats);
                this.renderTasks();
                this.renderProfiles();
                this.renderTimeline();
            }
        } catch (error) {
            console.error('Failed to load data:', error);
        }
        
        this.showLoading(false);
    }
    
    showLoading(show) {
        document.getElementById('loading').classList.toggle('active', show);
    }
    
    updateDashboard(stats) {
        document.getElementById('active-count').textContent = stats?.activeTasks || 0;
        document.getElementById('completed-count').textContent = 
            this.tasks.filter(t => t.isCompleted).length;
        document.getElementById('overdue-count').textContent = stats?.overdueCount || 0;
    }
    
    renderTasks() {
        const container = document.getElementById('public-tasks');
        const allTasksContainer = document.getElementById('all-tasks');
        const tagFilters = document.getElementById('tag-filters');
        
        // Get all unique tags
        const allTags = new Set();
        this.tasks.forEach(task => {
            if (task.tags) {
                const tags = task.tags.split(',').map(t => t.trim()).filter(t => t);
                tags.forEach(tag => allTags.add(tag));
            }
        });
        
        // Render tag filters
        tagFilters.innerHTML = `
            <button class="chip ${this.selectedTag === '' ? 'active' : ''}" data-tag="">全部</button>
            ${Array.from(allTags).map(tag => `
                <button class="chip ${this.selectedTag === tag ? 'active' : ''}" data-tag="${tag}">${tag}</button>
            `).join('')}
        `;
        
        // Bind tag filter clicks
        tagFilters.querySelectorAll('.chip').forEach(chip => {
            chip.addEventListener('click', () => {
                this.selectedTag = chip.dataset.tag;
                this.renderTasks();
            });
        });
        
        // Filter tasks
        let filteredTasks = this.tasks;
        if (this.selectedTag) {
            filteredTasks = this.tasks.filter(task => {
                if (!task.tags) return false;
                const tags = task.tags.split(',').map(t => t.trim().toLowerCase());
                return tags.includes(this.selectedTag.toLowerCase());
            });
        }
        
        // Render public tasks (active only)
        const activeTasks = filteredTasks.filter(t => !t.isCompleted);
        if (activeTasks.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <span class="material-icons">inbox</span>
                    <p>暂无公开任务</p>
                </div>
            `;
        } else {
            container.innerHTML = activeTasks.slice(0, 5).map(task => this.renderTaskItem(task)).join('');
        }
        
        // Render all tasks
        if (filteredTasks.length === 0) {
            allTasksContainer.innerHTML = `
                <div class="empty-state">
                    <span class="material-icons">task</span>
                    <p>暂无任务</p>
                </div>
            `;
        } else {
            allTasksContainer.innerHTML = filteredTasks.map(task => this.renderTaskItem(task)).join('');
        }
    }
    
    renderTaskItem(task) {
        const now = Date.now();
        const isOverdue = task.deadline && task.deadline < now && !task.isCompleted;
        const priorityLabels = { 1: 'low', 2: 'medium', 3: 'high' };
        const priorityNames = { 1: '低', 2: '中', 3: '高' };
        
        const tags = task.tags ? task.tags.split(',').map(t => t.trim()).filter(t => t) : [];
        
        return `
            <div class="task-item ${task.isCompleted ? 'completed' : ''} ${isOverdue ? 'overdue' : ''}">
                <div class="task-checkbox ${task.isCompleted ? 'checked' : ''}">
                    ${task.isCompleted ? '<span class="material-icons">check</span>' : ''}
                </div>
                <div class="task-content">
                    <div class="task-title">${this.escapeHtml(task.title)}</div>
                    <div class="task-meta">
                        ${task.deadline ? `
                            <div class="task-deadline">
                                <span class="material-icons">schedule</span>
                                ${this.formatDate(task.deadline)}
                            </div>
                        ` : ''}
                        <span class="priority-badge ${priorityLabels[task.priority] || 'medium'}">
                            ${priorityNames[task.priority] || '中'}
                        </span>
                        ${tags.length > 0 ? `
                            <div class="task-tags">
                                ${tags.map(tag => `<span class="tag">${this.escapeHtml(tag)}</span>`).join('')}
                            </div>
                        ` : ''}
                    </div>
                    ${task.progress > 0 ? `
                        <div class="task-progress">
                            <div class="task-progress-fill" style="width: ${task.progress * 100}%"></div>
                        </div>
                    ` : ''}
                </div>
            </div>
        `;
    }
    
    renderProfiles() {
        const container = document.getElementById('profiles');
        
        if (this.profiles.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <span class="material-icons">people</span>
                    <p>暂无用户</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = this.profiles.map(profile => `
            <div class="profile-list-item">
                <div class="profile-list-avatar">
                    <span class="material-icons">person</span>
                </div>
                <div class="profile-list-info">
                    <div class="profile-list-name">${this.escapeHtml(profile.displayName)}</div>
                    <div class="profile-list-motto">"${this.escapeHtml(profile.motto)}"</div>
                </div>
                <div class="status-badge ${(profile.status || '').toLowerCase()}">
                    <span class="status-dot"></span>
                </div>
            </div>
        `).join('');
        
        // Update profile page
        const firstProfile = this.profiles[0];
        if (firstProfile) {
            document.getElementById('profile-name').textContent = firstProfile.displayName;
            document.getElementById('profile-motto').textContent = `"${firstProfile.motto}"`;
            document.getElementById('profile-status').innerHTML = `
                <span class="status-dot"></span>
                ${this.getStatusLabel(firstProfile.status)}
            `;
            document.getElementById('profile-status').className = 
                `status-badge ${(firstProfile.status || '').toLowerCase()}`;
        }
        
        // Update profile stats
        const activeTasks = this.tasks.filter(t => !t.isCompleted).length;
        const completedTasks = this.tasks.filter(t => t.isCompleted).length;
        document.getElementById('profile-active').textContent = activeTasks;
        document.getElementById('profile-completed').textContent = completedTasks;
        // Calculate today's completed tasks
        const todayStart = new Date();
        todayStart.setHours(0, 0, 0, 0);
        const todayEnd = new Date(todayStart);
        todayEnd.setDate(todayEnd.getDate() + 1);
        const completedToday = this.tasks.filter(t => {
            // Use t.completedAt if available, otherwise fallback to t.dueDate if that's how completion is tracked
            const completedDate = t.completedAt ? new Date(t.completedAt) : (t.isCompleted && t.dueDate ? new Date(t.dueDate) : null);
            return t.isCompleted && completedDate && completedDate >= todayStart && completedDate < todayEnd;
        }).length;
        document.getElementById('profile-today').textContent = completedToday;
    }
    
    renderTimeline() {
        const now = Date.now();
        const todayStart = new Date();
        todayStart.setHours(0, 0, 0, 0);
        const todayEnd = new Date(todayStart);
        todayEnd.setDate(todayEnd.getDate() + 1);
        const tomorrowEnd = new Date(todayStart);
        tomorrowEnd.setDate(tomorrowEnd.getDate() + 2);
        const weekEnd = new Date(todayStart);
        weekEnd.setDate(weekEnd.getDate() + 7);
        
        const timeline = {
            overdue: [],
            today: [],
            tomorrow: [],
            thisWeek: [],
            later: []
        };
        
        this.tasks.filter(t => !t.isCompleted).forEach(task => {
            const targetTime = task.deadline;
            
            if (!targetTime) {
                timeline.later.push(task);
            } else if (targetTime < now) {
                timeline.overdue.push(task);
            } else if (targetTime < todayEnd.getTime()) {
                timeline.today.push(task);
            } else if (targetTime < tomorrowEnd.getTime()) {
                timeline.tomorrow.push(task);
            } else if (targetTime < weekEnd.getTime()) {
                timeline.thisWeek.push(task);
            } else {
                timeline.later.push(task);
            }
        });
        
        const sections = [
            { id: 'overdue-tasks', tasks: timeline.overdue },
            { id: 'today-tasks', tasks: timeline.today },
            { id: 'tomorrow-tasks', tasks: timeline.tomorrow },
            { id: 'week-tasks', tasks: timeline.thisWeek },
            { id: 'later-tasks', tasks: timeline.later }
        ];
        
        sections.forEach(section => {
            const container = document.getElementById(section.id);
            if (section.tasks.length === 0) {
                container.innerHTML = `<p style="color: var(--md-sys-color-on-surface-variant); font-size: 14px;">暂无任务</p>`;
            } else {
                container.innerHTML = section.tasks.map(task => this.renderTaskItem(task)).join('');
            }
        });
    }
    
    formatDate(timestamp) {
        const date = new Date(timestamp);
        const now = new Date();
        const diff = date - now;
        const days = Math.ceil(diff / (1000 * 60 * 60 * 24));
        
        if (days < 0) {
            return `已逾期 ${-days} 天`;
        } else if (days === 0) {
            return '今天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        } else if (days === 1) {
            return '明天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        } else {
            return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' });
        }
    }
    
    getStatusLabel(status) {
        const labels = {
            'Available': '空闲',
            'Busy': '忙碌',
            'Away': '离开'
        };
        return labels[status] || status || '空闲';
    }
    
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    window.app = new LifeAppDashboard();
});
