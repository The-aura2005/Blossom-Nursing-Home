(function () {
    const dashboard = document.querySelector('.analytics-dashboard');
    if (!dashboard) return;

    const residentId = dashboard.dataset.residentId;
    const range = document.getElementById('analytics-range');
    const start = document.getElementById('analytics-start');
    const end = document.getElementById('analytics-end');
    const loading = document.getElementById('analytics-loading');
    const content = document.getElementById('analytics-content');
    const error = document.getElementById('analytics-error');
    const charts = {};
    const today = new Date();

    function iso(date) { return date.toISOString().slice(0, 10); }
    function setDefaultDates(days) {
        const finish = new Date();
        const beginning = new Date(finish);
        beginning.setDate(finish.getDate() - days + 1);
        start.value = iso(beginning);
        end.value = iso(finish);
    }
    function setCustomVisibility() {
        const custom = range.value === 'custom';
        start.hidden = !custom;
        end.hidden = !custom;
    }
    function number(value, suffix) { return value == null ? 'No data' : Number(value).toFixed(1) + suffix; }
    function chartMessage(canvas, visible) { canvas.hidden = visible; canvas.nextElementSibling.hidden = !visible; }

    function renderCharts(data) {
        Object.values(charts).forEach(chart => chart.destroy());
        const temperatureCanvas = document.getElementById('temperature-chart');
        const activityCanvas = document.getElementById('activity-chart');
        const incidentCanvas = document.getElementById('incident-chart');
        chartMessage(temperatureCanvas, !data.vital.chartData.length);
        chartMessage(activityCanvas, data.activity.scheduledActivities === 0);
        chartMessage(incidentCanvas, !data.incident.chartData.some(point => point.count > 0));
        if (data.vital.chartData.length) charts.temperature = new Chart(temperatureCanvas, { type: 'line', data: { labels: data.vital.chartData.map(point => point.date), datasets: [{ label: 'Temperature (°C)', data: data.vital.chartData.map(point => point.temperature), borderColor: '#126e64', backgroundColor: 'rgba(18,110,100,.12)', fill: true, tension: .25 }] }, options: { responsive: true, maintainAspectRatio: false } });
        if (data.activity.scheduledActivities) charts.activity = new Chart(activityCanvas, { type: 'doughnut', data: { labels: ['Completed', 'Not recorded'], datasets: [{ data: [data.activity.completedActivities, data.activity.incompleteActivities], backgroundColor: ['#126e64', '#e6ded0'] }] }, options: { responsive: true, maintainAspectRatio: false } });
        if (data.incident.chartData.some(point => point.count > 0)) charts.incident = new Chart(incidentCanvas, { type: 'bar', data: { labels: data.incident.chartData.map(point => point.date), datasets: [{ label: 'Incidents', data: data.incident.chartData.map(point => point.count), backgroundColor: '#c76b4b' }] }, options: { responsive: true, maintainAspectRatio: false } });
    }

    function render(data) {
        const vital = data.vital;
        document.getElementById('latest-temperature').textContent = number(vital.latestTemperature, ' °C');
        document.getElementById('average-temperature').textContent = number(vital.averageTemperature, ' °C');
        document.getElementById('temperature-trend').textContent = vital.readingCount ? vital.trend : '';
        document.getElementById('temperature-range').textContent = vital.readingCount ? number(vital.minimumTemperature, ' - ') + number(vital.maximumTemperature, ' °C') : '';
        document.getElementById('reading-count').textContent = vital.readingCount + ' reading' + (vital.readingCount === 1 ? '' : 's');
        document.getElementById('activity-percentage').textContent = Number(data.activity.completionPercentage).toFixed(0) + '%';
        document.getElementById('activity-count').textContent = data.activity.completedActivities + ' of ' + data.activity.scheduledActivities + ' recorded';
        document.getElementById('medication-percentage').textContent = Number(data.medication.administrationPercentage).toFixed(0) + '%';
        document.getElementById('medication-count').textContent = data.medication.recordedAdministration + ' administration records';
        document.getElementById('incident-count').textContent = data.incident.chartData.reduce((sum, point) => sum + point.count, 0);
        document.getElementById('incident-trend').textContent = data.incident.trend + ' (' + Number(data.incident.percentageChange).toFixed(0) + '% vs previous)';
        const alerts = document.getElementById('analytics-alerts');
        alerts.replaceChildren();
        (data.alerts.length ? data.alerts : [{ message: 'No alerts for this period.', severity: 'INFO' }]).forEach(alert => { const item = document.createElement('p'); item.className = 'analytics-alert ' + alert.severity.toLowerCase(); item.textContent = alert.message; alerts.appendChild(item); });
        renderCharts(data);
    }

    async function load() {
        loading.hidden = false; content.hidden = true; error.hidden = true;
        const response = await fetch('/api/residents/' + residentId + '/analytics?startDate=' + start.value + '&endDate=' + end.value);
        if (!response.ok) throw new Error(response.status === 404 ? 'Resident not found.' : 'Analytics could not be loaded.');
        render(await response.json()); loading.hidden = true; content.hidden = false;
    }
    async function refresh() { try { await load(); } catch (exception) { loading.hidden = true; error.textContent = exception.message; error.hidden = false; } }

    range.addEventListener('change', function () { if (range.value === 'today') setDefaultDates(1); else if (range.value === '7' || range.value === '30') setDefaultDates(Number(range.value)); setCustomVisibility(); if (range.value !== 'custom') refresh(); });
    document.getElementById('analytics-refresh').addEventListener('click', refresh);
    setDefaultDates(7); setCustomVisibility(); refresh();
})();