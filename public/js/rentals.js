
// Wait for the DOM to be fully loaded before running initialisation code.
document.addEventListener('DOMContentLoaded', function() {
    const username = sessionStorage.getItem('username') || 'Guest';
    document.getElementById('usernameDisplay').textContent = username;
});

function applyFilters() {
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
	    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
	    return response.json();
	})
	.then(data => {
	    console.log('Filtered rentals:', data);

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
function cancelBooking() {
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
function goToPayment() {

    window.location.href = 'payment.html';
}