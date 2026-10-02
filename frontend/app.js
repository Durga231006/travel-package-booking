// Backend address. Change the port here if your Spring Boot app runs on a different one.
const API_BASE = 'http://localhost:8080/api';

// ---------- small helpers ----------
const $ = id => document.getElementById(id);
const money = value => Number(value).toLocaleString('en-IN', { style: 'currency', currency: 'INR' });
const esc = text => String(text ?? '').replace(/[&<>"']/g, ch =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch]));

let customers = [];
let destinations = [];
let packages = [];

// Calls the REST API and returns the JSON. Throws an Error with the server message on failure.
async function api(path, method = 'GET', body) {
    const response = await fetch(API_BASE + path, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: body ? JSON.stringify(body) : undefined
    });
    const data = response.status === 204 ? null : await response.json().catch(() => null);
    if (!response.ok) {
        throw new Error(data && data.message ? data.message : 'Request failed');
    }
    return data;
}

function toast(message, isError = false) {
    const box = $('toast');
    box.textContent = message;
    box.className = 'toast show' + (isError ? ' error' : '');
    clearTimeout(toast.timer);
    toast.timer = setTimeout(() => { box.className = 'toast'; }, 4000);
}

// Runs an action and shows any error as a toast
async function run(action) {
    try {
        await action();
    } catch (error) {
        toast(error.message, true);
    }
}

function destinationName(id) {
    const found = destinations.find(d => d.destinationId === id);
    return found ? found.name : 'Unknown';
}

function statusBadge(status) {
    const css = status === 'CONFIRMED' || status === 'AVAILABLE' ? '' : (status === 'SOLD_OUT' ? 'warn' : 'bad');
    return `<span class="badge ${css}">${esc(status.replace('_', ' '))}</span>`;
}

function emptyRow(columns, text) {
    return `<tr><td class="empty" colspan="${columns}">${text}</td></tr>`;
}

// ---------- tabs ----------
document.querySelectorAll('.tab').forEach(button => {
    button.addEventListener('click', () => {
        document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
        document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
        button.classList.add('active');
        $('tab-' + button.dataset.tab).classList.add('active');
    });
});

// ---------- loading data ----------
async function loadAll() {
    [customers, destinations, packages] = await Promise.all([
        api('/customers'), api('/destinations'), api('/packages')
    ]);
    renderCustomers();
    renderDestinations();
    renderPackages();
    fillSelects();
    await Promise.all([loadBookings(), loadPopular()]);
}

async function loadBookings() {
    const bookings = await api('/bookings');
    $('bookings-body').innerHTML = bookings.length === 0
        ? emptyRow(9, 'No bookings yet. Use "Book a package" to create the first one.')
        : bookings.map(b => `
            <tr>
                <td>#${b.bookingId}</td>
                <td>${esc(b.customerName)}<br><small>${esc(b.customerEmail)}</small></td>
                <td>${esc(b.destinationName)}, ${esc(b.country)}</td>
                <td>${esc(b.packageName)}</td>
                <td>${b.travelers}</td>
                <td>${money(b.totalCost)}</td>
                <td>${new Date(b.bookingDate).toLocaleDateString('en-IN')}</td>
                <td>${statusBadge(b.status)}</td>
                <td>${b.status === 'CONFIRMED'
                    ? `<button class="link" data-cancel="${b.bookingId}">Cancel booking</button>` : ''}</td>
            </tr>`).join('');
}

async function loadPopular() {
    const popular = await api('/packages/above-average-bookings');
    $('popular-body').innerHTML = popular.length === 0
        ? emptyRow(3, 'No package is above the average yet.')
        : popular.map(p => `
            <tr><td>${esc(p.packageName)}</td><td>${esc(p.destinationName)}</td><td>${p.totalBookings}</td></tr>`).join('');
}

// ---------- rendering tables ----------
function renderPackages() {
    $('packages-body').innerHTML = packages.length === 0
        ? emptyRow(8, 'No packages yet. Add one below.')
        : packages.map(p => `
            <tr>
                <td>${p.packageId}</td>
                <td>${esc(p.packageName)}</td>
                <td>${esc(destinationName(p.destinationId))}</td>
                <td>${money(p.pricePerPerson)}</td>
                <td>${p.durationDays} days</td>
                <td>${p.availableSeats} / ${p.totalSeats}</td>
                <td>${statusBadge(p.status)}</td>
                <td><button class="link" data-delete-package="${p.packageId}">Delete</button></td>
            </tr>`).join('');
}

function renderCustomers() {
    $('customers-body').innerHTML = customers.length === 0
        ? emptyRow(6, 'No customers yet. Add one below.')
        : customers.map(c => `
            <tr>
                <td>${c.customerId}</td><td>${esc(c.name)}</td><td>${esc(c.email)}</td>
                <td>${esc(c.phone)}</td><td>${esc(c.city)}</td>
                <td><button class="link" data-delete-customer="${c.customerId}">Delete</button></td>
            </tr>`).join('');
}

function renderDestinations() {
    $('destinations-body').innerHTML = destinations.length === 0
        ? emptyRow(5, 'No destinations yet. Add one below.')
        : destinations.map(d => `
            <tr>
                <td>${d.destinationId}</td><td>${esc(d.name)}</td><td>${esc(d.country)}</td>
                <td>${esc(d.description)}</td>
                <td><button class="link" data-delete-destination="${d.destinationId}">Delete</button></td>
            </tr>`).join('');
}

function fillSelects() {
    $('book-customer').innerHTML = customers
        .map(c => `<option value="${c.customerId}">${esc(c.name)} (${esc(c.email)})</option>`).join('');

    // Only packages that can still be booked are listed
    $('book-package').innerHTML = packages
        .filter(p => p.status === 'AVAILABLE')
        .map(p => `<option value="${p.packageId}">${esc(p.packageName)} - ${esc(destinationName(p.destinationId))}
            (${money(p.pricePerPerson)} per person, ${p.availableSeats} seats left)</option>`).join('');

    $('package-destination').innerHTML = destinations
        .map(d => `<option value="${d.destinationId}">${esc(d.name)}, ${esc(d.country)}</option>`).join('');

    updateCostPreview();
}

// ---------- cost preview (calls the SQL function through the API) ----------
async function updateCostPreview() {
    const packageId = $('book-package').value;
    const travelers = Number($('book-travelers').value);
    const preview = $('cost-preview');

    if (!packageId || !travelers || travelers < 1) {
        preview.textContent = '';
        return;
    }
    try {
        const result = await api(`/packages/${packageId}/cost?travelers=${travelers}`);
        preview.textContent = 'Total cost: ' + money(result.totalCost)
            + (travelers >= 5 ? ' (10% group discount applied)' : '');
    } catch (error) {
        preview.textContent = '';
    }
}
$('book-package').addEventListener('change', updateCostPreview);
$('book-travelers').addEventListener('input', updateCostPreview);

// ---------- forms ----------
$('book-form').addEventListener('submit', event => {
    event.preventDefault();
    run(async () => {
        const result = await api('/bookings', 'POST', {
            customerId: Number($('book-customer').value),
            packageId: Number($('book-package').value),
            travelers: Number($('book-travelers').value)
        });
        toast(`Booking #${result.bookingId} confirmed. Total ${money(result.totalCost)}`);
        $('book-travelers').value = 1;
        await loadAll();
    });
});

$('customer-form').addEventListener('submit', event => {
    event.preventDefault();
    run(async () => {
        await api('/customers', 'POST', Object.fromEntries(new FormData(event.target)));
        event.target.reset();
        toast('Customer added');
        await loadAll();
    });
});

$('destination-form').addEventListener('submit', event => {
    event.preventDefault();
    run(async () => {
        await api('/destinations', 'POST', Object.fromEntries(new FormData(event.target)));
        event.target.reset();
        toast('Destination added');
        await loadAll();
    });
});

$('package-form').addEventListener('submit', event => {
    event.preventDefault();
    run(async () => {
        const form = Object.fromEntries(new FormData(event.target));
        await api('/packages', 'POST', {
            packageName: form.packageName,
            destinationId: Number(form.destinationId),
            pricePerPerson: Number(form.pricePerPerson),
            durationDays: Number(form.durationDays),
            totalSeats: Number(form.totalSeats)
        });
        event.target.reset();
        toast('Package added');
        await loadAll();
    });
});

// ---------- cancel and delete buttons (event delegation) ----------
document.addEventListener('click', event => {
    const button = event.target.closest('button[data-cancel], button[data-delete-package], '
        + 'button[data-delete-customer], button[data-delete-destination]');
    if (!button) return;

    run(async () => {
        if (button.dataset.cancel) {
            if (!confirm('Cancel this booking? The seats will be released.')) return;
            await api(`/bookings/${button.dataset.cancel}/cancel`, 'PUT');
            toast('Booking cancelled');
        } else if (button.dataset.deletePackage) {
            if (!confirm('Delete this package?')) return;
            await api(`/packages/${button.dataset.deletePackage}`, 'DELETE');
            toast('Package deleted');
        } else if (button.dataset.deleteCustomer) {
            if (!confirm('Delete this customer?')) return;
            await api(`/customers/${button.dataset.deleteCustomer}`, 'DELETE');
            toast('Customer deleted');
        } else if (button.dataset.deleteDestination) {
            if (!confirm('Delete this destination?')) return;
            await api(`/destinations/${button.dataset.deleteDestination}`, 'DELETE');
            toast('Destination deleted');
        }
        await loadAll();
    });
});

run(loadAll);
