// main.js - theme toggle, simple toast, mobile nav, sample chart

// Get CSRF token from meta tag (Spring Security)
function getCsrfToken() {
    const token = document.querySelector('meta[name="_csrf"]');
    return token ? token.getAttribute('content') : '';
}

// Get CSRF header name from meta tag (Spring Security)
function getCsrfHeaderName() {
    const header = document.querySelector('meta[name="_csrf_header"]');
    return header ? header.getAttribute('content') : 'X-CSRF-TOKEN';
}

document.addEventListener('DOMContentLoaded', () => {
    // theme toggle
    const themeToggle = document.getElementById('themeToggle');
    const body = document.body;
    const saved = localStorage.getItem('theme-mode');
    if (saved === 'dark') body.classList.add('dark');

    if (themeToggle) {
        themeToggle.addEventListener('click', () => {
            body.classList.toggle('dark');
            localStorage.setItem('theme-mode', body.classList.contains('dark') ? 'dark' : 'light');
            themeToggle.innerText = body.classList.contains('dark') ? '☀️' : '🌙';
        });
        themeToggle.innerText = body.classList.contains('dark') ? '☀️' : '🌙';
    }

    // mobile nav toggle
    const navBtn = document.getElementById('navToggle');
    const navLinks = document.querySelector('.nav-links');
    if (navBtn && navLinks) {
        navBtn.addEventListener('click', () => {
            navLinks.classList.toggle('open');
            navBtn.classList.toggle('open');
        });
    }

    // toast helper
    window.showToast = (msg) => {
        const t = document.createElement('div');
        t.className = 'toast';
        t.innerText = msg;
        document.body.appendChild(t);
        setTimeout(() => t.classList.add('visible'), 20);
        setTimeout(() => t.classList.remove('visible'), 2400);
        setTimeout(() => t.remove(), 2800);
    };

    // copy-to-clipboard handler (delegated)
    document.addEventListener('click', (ev) => {
        const btn = ev.target.closest && ev.target.closest('.copy-btn');
        if (!btn) return;
        const id = btn.getAttribute('data-id');
        if (!id) {
            showToast('No id');
            return;
        }
        fetch(`/api/password/${id}`)
            .then(r => r.json())
            .then(data => {
                if (data && data.password) {
                    navigator.clipboard.writeText(data.password).then(() => {
                        showToast('Password copied to clipboard');
                    }).catch(() => showToast('Clipboard write failed'));
                } else {
                    showToast('Unable to retrieve password');
                }
            }).catch(() => showToast('Network error'));
    });

    // simple edit handler triggered by Edit buttons
    window.openEdit = async (el) => {
        const id = el.getAttribute('data-id');
        if (!id) return showToast('No id');
        try {
            const r = await fetch(`/api/entry/${id}`);
            if (!r.ok) return showToast('Entry not found');
            const entry = await r.json();
            const website = prompt('Website', entry.website || '');
            if (website === null) return;
            if (!website.trim()) return showToast('Website is required');
            const username = prompt('Username', entry.username || '') || '';
            const password = prompt('Password (leave blank to keep current)', '') || '';
            const category = prompt('Category', entry.category || '') || '';

            const form = new URLSearchParams();
            form.set('website', website);
            form.set('username', username);
            form.set('password', password);
            form.set('category', category);

            const u = await fetch(`/api/entry/${id}`, {
                method: 'POST',
                body: form,
                headers: {
                    [getCsrfHeaderName()]: getCsrfToken()
                }
            });
            if (u.ok) {
                showToast('Password updated successfully');
                setTimeout(() => location.reload(), 600);
            } else {
                showToast('Update failed');
            }
        } catch (e) {
            console.warn(e);
            showToast('Error updating entry');
        }
    };

    // logout handler
    window.logout = (ev) => {
        ev.preventDefault();
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/logout';

        // Add CSRF token
        const csrfToken = document.createElement('input');
        csrfToken.type = 'hidden';
        csrfToken.name = '_csrf';
        csrfToken.value = getCsrfToken();
        form.appendChild(csrfToken);

        document.body.appendChild(form);
        form.submit();
    };

    // dynamic profile chart: fetch /api/stats and poll for updates (robust)
    if (document.getElementById('profileChart')) {
        try {
            const ctx = document.getElementById('profileChart').getContext('2d');
            let chart = null;

            async function fetchStatsAndUpdate() {
                try {
                    const res = await fetch('/api/stats', { cache: 'no-store' });
                    if (!res.ok) return;
                    const json = await res.json();

                    // derive labels (exclude Total)
                    const labels = Object.keys(json).filter(k => k !== 'Total');
                    const data = labels.map(l => json[l] || 0);

                    if (!chart) {
                        chart = new Chart(ctx, {
                            type: 'doughnut',
                            data: {
                                labels: labels,
                                datasets: [{
                                    data: data,
                                    backgroundColor: [
                                        'rgba(11,99,211,0.9)', 'rgba(255,159,67,0.92)', 'rgba(25,169,116,0.9)', 'rgba(155,155,155,0.12)'
                                    ].slice(0, labels.length)
                                }]
                            },
                            options: { plugins: { legend: { position: 'bottom' } }, cutout: '60%' }
                        });
                    } else {
                        // keep labels in-sync (in case categories change)
                        chart.data.labels = labels;
                        chart.data.datasets[0].data = data;
                        chart.update();
                    }

                    // update count widgets if present
                    const entryEls = document.querySelectorAll('.entry-count');
                    const catEls = document.querySelectorAll('.category-count');
                    if (json.Total !== undefined) entryEls.forEach(el => el.innerText = json.Total);
                    // categories with count > 0
                    const categoryCount = labels.filter(l => (json[l] || 0) > 0).length;
                    catEls.forEach(el => el.innerText = categoryCount);

                } catch (e) {
                    console.warn('Failed to update stats', e);
                }
            }

            // initial fetch + polling
            fetchStatsAndUpdate();
            setInterval(fetchStatsAndUpdate, 5000);

        } catch (e) {
            console.warn(e);
        }
    }
});
