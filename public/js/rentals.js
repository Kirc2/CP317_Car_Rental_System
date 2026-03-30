
// testing-------------------------------------------------
const container = document.getElementById("filtered-list");


/*put for loop into it's own function to reuse in testing*/
function rentalsToHtml(rentals) { 
	let i = 0
	let row = document.createElement('tr');
	row.className = 'rentalsRow';
	rentals.forEach(rental => {
		const item = document.createElement('td');
		
		item.innerHTML = `
			<a href="Reservations.html?id=${rental.id}" class="rental-card">
			<img class="image" src="js/car_image_3.jpg" alt="no image"/>
			<div class="name">${rental.make}  ${rental.model}  ${rental.year}</div>
			<div class="type">${rental.type}</div>
			<div class="price">Daily Rate : $${rental.dailyrate}</div>
			<div class="status">Current Status : ${rental.status}</div>
			</a>
		`;
		row.appendChild(item)
		i = i+1;
		if (i >= 3) {
			container.appendChild(row);
			row = document.createElement('tr');
			row.className = 'rentalsRow';
			i=0;
		}
	});

	if (i > 0) {
		container.appendChild(row);
	}
}
async function testFilters() {
	let testingData = [{id: 1, type: "sedan", make: "toyota", model: "y", year: 2015, dailyrate: 150, status: "available"},
		{id: 1, type: "sedan", make: "toyota", model: "y", year: 2015, dailyrate: 150, status: "available"},
		{id: 1, type: "sedan", make: "toyota", model: "y", year: 2015, dailyrate: 150, status: "available"},
		{id: 1, type: "sedan", make: "toyota", model: "y", year: 2015, dailyrate: 150, status: "available"},
		{id: 1, type: "sedan", make: "toyota", model: "y", year: 2015, dailyrate: 150, status: "available"},
		{id: 1, type: "sedan", make: "toyota", model: "y", year: 2015, dailyrate: 150, status: "available"},
		{id: 1, type: "sedan", make: "toyota", model: "y", year: 2015, dailyrate: 150, status: "available"},
		{id: 1, type: "sedan", make: "toyota", model: "y", year: 2015, dailyrate: 150, status: "available"},
		{id: 1, type: "sedan", make: "toyota", model: "y", year: 2015, dailyrate: 150, status: "available"},
		{id: 1, type: "sedan", make: "toyota", model: "y", year: 2015, dailyrate: 150, status: "available"},
		{id: 1, type: "sedan", make: "toyota", model: "y", year: 2015, dailyrate: 150, status: "available"},
		{id: 1, type: "sedan", make: "toyota", model: "y", year: 2015, dailyrate: 150, status: "available"},
		{id: 1, type: "sedan", make: "toyota", model: "y", year: 2015, dailyrate: 150, status: "available"}
	]
	console.log("testing")
	rentalsToHtml(testingData)
} // in place of applyFilters                                                  
// ---------------------------------------------------------

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

	    rentalsToHtml(rentals);
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