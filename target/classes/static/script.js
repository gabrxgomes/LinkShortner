const API_BASE = window.location.origin;
let qrCodeInstance = null;

// Load statistics on startup
loadStats();

// Update statistics every 10 seconds
setInterval(loadStats, 10000);

async function loadStats() {
    try {
        const response = await fetch(`${API_BASE}/api/system-stats`);

        if (response.ok) {
            const data = await response.json();

            // Animate numbers
            animateValue('totalLinks', parseInt(document.getElementById('totalLinks').textContent) || 0, data.totalLinks, 1000);
            animateValue('totalClicks', parseInt(document.getElementById('totalClicks').textContent) || 0, data.totalClicks, 1000);
            animateValue('activeLinks', parseInt(document.getElementById('activeLinks').textContent) || 0, data.activeLinks, 1000);
        }
    } catch (error) {
        console.error('Error loading stats:', error);
        // Keep previous values if request fails
    }
}

// Animate number changes
function animateValue(id, start, end, duration) {
    const element = document.getElementById(id);
    const range = end - start;
    const increment = range / (duration / 16); // 60fps
    let current = start;

    const timer = setInterval(() => {
        current += increment;
        if ((increment > 0 && current >= end) || (increment < 0 && current <= end)) {
            element.textContent = end;
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(current);
        }
    }, 16);
}

document.getElementById('shortenForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const urlInput = document.getElementById('urlInput');
    const expirationInput = document.getElementById('expirationInput');
    const resultDiv = document.getElementById('result');
    const errorDiv = document.getElementById('error');
    const loadingDiv = document.getElementById('loading');

    // Hide previous results
    resultDiv.classList.add('hidden');
    errorDiv.classList.add('hidden');
    loadingDiv.classList.remove('hidden');

    try {
        const response = await fetch(`${API_BASE}/api/shorten`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                url: urlInput.value,
                expirationHours: parseInt(expirationInput.value) || 24
            })
        });

        const data = await response.json();

        loadingDiv.classList.add('hidden');

        if (response.ok) {
            // Show success result
            document.getElementById('shortUrl').value = data.shortUrl;
            document.getElementById('originalUrl').textContent = data.originalUrl;
            document.getElementById('shortCode').textContent = data.shortCode;
            document.getElementById('expiresAt').textContent = new Date(data.expiresAt).toLocaleString('en-US');
            document.getElementById('clickCount').textContent = data.clickCount;

            // Generate QR Code
            generateQRCode(data.shortUrl);

            resultDiv.classList.remove('hidden');

            // Store short code for stats
            resultDiv.dataset.shortCode = data.shortCode;

            // Update statistics
            loadStats();
        } else {
            // Show error
            errorDiv.textContent = '❌ ' + (data.error || 'Error shortening the link');
            errorDiv.classList.remove('hidden');
        }
    } catch (error) {
        loadingDiv.classList.add('hidden');
        errorDiv.textContent = '❌ Error connecting to server';
        errorDiv.classList.remove('hidden');
    }
});

function generateQRCode(url) {
    const qrcodeDiv = document.getElementById('qrcode');

    // Clear previous QR Code
    qrcodeDiv.innerHTML = '';

    // Generate new QR Code
    qrCodeInstance = new QRCode(qrcodeDiv, {
        text: url,
        width: 160,
        height: 160,
        colorDark: '#0a0a0a',
        colorLight: '#ffffff',
        correctLevel: QRCode.CorrectLevel.H
    });
}

document.getElementById('copyBtn').addEventListener('click', () => {
    const shortUrl = document.getElementById('shortUrl');
    shortUrl.select();
    document.execCommand('copy');

    const copyBtn = document.getElementById('copyBtn');
    const originalText = copyBtn.textContent;
    copyBtn.textContent = '✅ Copied!';

    setTimeout(() => {
        copyBtn.textContent = originalText;
    }, 2000);
});

document.getElementById('statsBtn').addEventListener('click', async () => {
    const resultDiv = document.getElementById('result');
    const shortCode = resultDiv.dataset.shortCode;

    if (!shortCode) return;

    try {
        const response = await fetch(`${API_BASE}/api/stats/${shortCode}`);
        const data = await response.json();

        if (response.ok) {
            document.getElementById('clickCount').textContent = data.clickCount;
            document.getElementById('expiresAt').textContent = new Date(data.expiresAt).toLocaleString('en-US');

            const statsBtn = document.getElementById('statsBtn');
            const originalText = statsBtn.textContent;
            statsBtn.textContent = '✅ Updated!';

            setTimeout(() => {
                statsBtn.textContent = originalText;
            }, 2000);
        }
    } catch (error) {
        console.error('Error fetching stats:', error);
    }
});

document.getElementById('downloadQR').addEventListener('click', () => {
    const qrcodeDiv = document.getElementById('qrcode');
    const canvas = qrcodeDiv.querySelector('canvas');

    if (canvas) {
        const url = canvas.toDataURL('image/png');
        const link = document.createElement('a');
        link.download = 'qrcode.png';
        link.href = url;
        link.click();
    }
});
