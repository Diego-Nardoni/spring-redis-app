// Modern Dashboard JavaScript

// Utility functions
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    const toastBody = document.getElementById('toastBody');
    const toastHeader = toast.querySelector('.toast-header');
    
    // Set message
    toastBody.textContent = message;
    
    // Set type styling
    toastHeader.className = `toast-header bg-${type} text-white`;
    
    // Show toast
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
}

function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        showToast('Copied to clipboard!', 'success');
    }).catch(() => {
        showToast('Failed to copy to clipboard', 'danger');
    });
}

function addLoadingSpinner(button) {
    const originalText = button.innerHTML;
    button.innerHTML = '<span class="loading"></span> Loading...';
    button.disabled = true;
    return originalText;
}

function removeLoadingSpinner(button, originalText) {
    button.innerHTML = originalText;
    button.disabled = false;
}

// API functions
async function testRedisHealth() {
    const button = event.target;
    const originalText = addLoadingSpinner(button);
    
    try {
        const response = await fetch('/api/health/redis');
        const health = await response.json();
        
        if (health.connected) {
            showToast(`Redis is healthy! Response time: ${health.responseTimeMs}ms`, 'success');
        } else {
            showToast(`Redis is unhealthy: ${health.status}`, 'warning');
        }
        
        // Update UI
        setTimeout(() => location.reload(), 1000);
        
    } catch (error) {
        showToast('Failed to test Redis health', 'danger');
        console.error('Redis health test error:', error);
    } finally {
        removeLoadingSpinner(button, originalText);
    }
}

async function runPerformanceTest() {
    const button = event.target;
    const originalText = addLoadingSpinner(button);
    
    try {
        const response = await fetch('/api/redis/performance-test', {
            method: 'POST'
        });
        const result = await response.json();
        
        if (result.success) {
            showToast('Performance test passed!', 'success');
        } else {
            showToast('Performance test failed', 'warning');
        }
        
        // Update UI
        setTimeout(() => location.reload(), 1000);
        
    } catch (error) {
        showToast('Failed to run performance test', 'danger');
        console.error('Performance test error:', error);
    } finally {
        removeLoadingSpinner(button, originalText);
    }
}

async function setSessionAttribute() {
    const key = document.getElementById('attrKey').value.trim();
    const value = document.getElementById('attrValue').value.trim();
    
    if (!key || !value) {
        showToast('Please enter both key and value', 'warning');
        return;
    }
    
    try {
        const response = await fetch('/api/session/attribute', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `key=${encodeURIComponent(key)}&value=${encodeURIComponent(value)}`
        });
        
        const result = await response.json();
        
        if (result.success) {
            showToast(`Attribute '${key}' set successfully!`, 'success');
            
            // Add to UI
            const attributesList = document.getElementById('attributesList');
            const newAttr = document.createElement('div');
            newAttr.className = 'attribute-item';
            newAttr.innerHTML = `<strong>${key}</strong>: <span>${value}</span>`;
            attributesList.appendChild(newAttr);
            
            // Clear inputs
            document.getElementById('attrKey').value = '';
            document.getElementById('attrValue').value = '';
            
        } else {
            showToast('Failed to set attribute', 'danger');
        }
        
    } catch (error) {
        showToast('Error setting session attribute', 'danger');
        console.error('Set attribute error:', error);
    }
}

async function getSessionAnalytics() {
    const sessionId = document.querySelector('code').textContent;
    
    try {
        const response = await fetch(`/api/session/analytics/${sessionId}`);
        const analytics = await response.json();
        
        if (analytics.error) {
            showToast('No analytics data available', 'info');
        } else {
            // Create modal or new window with analytics
            const analyticsWindow = window.open('', '_blank', 'width=800,height=600');
            analyticsWindow.document.write(`
                <html>
                <head>
                    <title>Session Analytics</title>
                    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
                </head>
                <body class="p-4">
                    <h3>Session Analytics</h3>
                    <pre class="bg-light p-3 rounded">${JSON.stringify(analytics, null, 2)}</pre>
                </body>
                </html>
            `);
            showToast('Analytics opened in new window', 'info');
        }
        
    } catch (error) {
        showToast('Failed to get session analytics', 'danger');
        console.error('Analytics error:', error);
    }
}

function exportSessionData() {
    const sessionData = {
        sessionId: document.querySelector('code').textContent,
        timestamp: new Date().toISOString(),
        url: window.location.href,
        userAgent: navigator.userAgent
    };
    
    const dataStr = JSON.stringify(sessionData, null, 2);
    const dataBlob = new Blob([dataStr], {type: 'application/json'});
    
    const link = document.createElement('a');
    link.href = URL.createObjectURL(dataBlob);
    link.download = `session-data-${sessionData.sessionId.substring(0, 8)}.json`;
    link.click();
    
    showToast('Session data exported!', 'success');
}

function refreshPage() {
    showToast('Refreshing session data...', 'info');
    setTimeout(() => location.reload(), 500);
}

// Auto-refresh functionality
let autoRefreshInterval;

function startAutoRefresh(intervalSeconds = 30) {
    if (autoRefreshInterval) {
        clearInterval(autoRefreshInterval);
    }
    
    autoRefreshInterval = setInterval(() => {
        console.log('Auto-refreshing session data...');
        location.reload();
    }, intervalSeconds * 1000);
    
    showToast(`Auto-refresh enabled (${intervalSeconds}s)`, 'info');
}

function stopAutoRefresh() {
    if (autoRefreshInterval) {
        clearInterval(autoRefreshInterval);
        autoRefreshInterval = null;
        showToast('Auto-refresh disabled', 'info');
    }
}

// Keyboard shortcuts
document.addEventListener('keydown', (e) => {
    if (e.ctrlKey || e.metaKey) {
        switch (e.key) {
            case 'r':
                e.preventDefault();
                refreshPage();
                break;
            case 'h':
                e.preventDefault();
                testRedisHealth();
                break;
            case 'p':
                e.preventDefault();
                runPerformanceTest();
                break;
        }
    }
});

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    console.log('Modern Redis Dashboard loaded');
    
    // Add keyboard shortcut hints
    const shortcuts = document.createElement('div');
    shortcuts.className = 'position-fixed bottom-0 start-0 p-2 text-muted small';
    shortcuts.innerHTML = `
        <div>Shortcuts: Ctrl+R (Refresh) | Ctrl+H (Health) | Ctrl+P (Performance)</div>
    `;
    document.body.appendChild(shortcuts);
    
    // Optional: Start auto-refresh (uncomment if desired)
    // startAutoRefresh(60); // 60 seconds
});

// Real-time updates using Server-Sent Events (if implemented)
function initializeSSE() {
    if (typeof EventSource !== "undefined") {
        const eventSource = new EventSource('/api/events');
        
        eventSource.onmessage = function(event) {
            const data = JSON.parse(event.data);
            console.log('SSE update:', data);
            
            // Update UI based on real-time data
            if (data.type === 'health_update') {
                showToast(`Redis health: ${data.status}`, data.connected ? 'success' : 'warning');
            }
        };
        
        eventSource.onerror = function(event) {
            console.log('SSE error:', event);
            eventSource.close();
        };
    }
}
