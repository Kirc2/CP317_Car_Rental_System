
// Wait for the DOM to be fully loaded before running initialisation code.
document.addEventListener('DOMContentLoaded', function() {
    const username = sessionStorage.getItem('username') || 'Guest';
    document.getElementById('usernameDisplay').textContent = username;
});

async function applyFilters() {
	const carType = document.getElementById('carType').value;
	const startDate = document.getElementById('startDate').value;
	const endDate = document.getElementById('endDate').value;
	const pricelimit = document.getElementById('price').value; // rename to match backend
	const year = document.getElementById('Year').value;
	const colour = document.getElementById('colour').value;

	// Build the request body object (only include non-empty fields)
	const body = {};
	if (carType) body.carType = carType;
	if (startDate) body.startDate = startDate;
	if (endDate) body.endDate = endDate;
	if (pricelimit) body.pricelimit = pricelimit; // backend expects this field name
	if (year) body.year = year;
	if (colour) body.colour = colour;

	fetch("http://localhost:8080/rentals", {
	    method: 'POST',
	    headers: {
	        'Content-Type': 'application/json'
	    },
	    body: JSON.stringify(body)
	})
	.then(response => {
	    if (!response.ok) {
	        throw new Error(`HTTP error! status: ${response.status}`);
	    }
	    return response.json();
	})
	.then(rentals => {
	    const container = document.getElementById("filtered-list");
	    if (!container) {
	        console.error("Container #rental-list not found!");
	        return;
	    }

	    // Clear any loading message or static content
	    container.innerHTML = '';

	    if (rentals.length === 0) {
	        container.innerHTML = '<div class=".listings-placeholder">No Cars Found</div>';
	        return;
	    }

	    // Loop through each rental and create a card
		rentals.forEach(rental => {
		    const link = document.createElement('a');
		    link.href = `/Reservations.html?id=${rental.id}`;
		    link.className = 'rental-card';

		    link.innerHTML = `
		        <div class="ID">Vehicle ID: ${rental.id}</div>
		        <div class="type">Type : ${rental.type}</div>
		        <div class="name">Name : ${rental.make}  ${rental.model}  ${rental.year}</div>
		        <div class="price">Daily Rate : ${rental.dailyrate}</div>
		        <div class="status">Current Status : ${rental.status}</div>
		    `;
		    container.appendChild(link);
		});
	})
	.catch(error => {
	    console.error('Error:', error);
	    alert('Failed to apply filters.');
	});
}

/**
 * Handle the Cancel Booking action.
 * Displays a confirmation message and redirects to the dashboard after a short delay.
 */
async function cancelBooking() {
    const confirmMsg = document.getElementById('confirmMsg');
    confirmMsg.style.display = 'block'; 

    setTimeout(() => {
        window.location.href = 'dashboard.html';
    }, 2000);
}

/**
 * Navigate to the payment page.
 * In a real implementation, you might first validate that a car is selected,
 * then pass the selected car details via query parameters or session storage.
 */
async function goToPayment() {

    window.location.href = 'payment.html';
}