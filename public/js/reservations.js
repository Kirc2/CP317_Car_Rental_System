// reservations.js – handles the reservation page

// Wait for the DOM to be ready
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const vehicleId = urlParams.get('id');

    if (!vehicleId) {
        showError('No vehicle specified.');
        return;
    }

    fetchVehicleDetails(vehicleId);
});

async function fetchVehicleDetails(id) {
    try {
        const response = await fetch(`http://localhost:8080/getvehicleinfo?id=${id}`);

        if (!response.ok) {
            if (response.status === 404) {
                throw new Error('Vehicle not found.');
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }
		
		const text = await response.text();
		console.log("Raw response text:", text);
		const vehicle = JSON.parse(text);
		
		const container = document.getElementById('details-container');
		console.log("Full vehicle object:", vehicle);
		console.log("Keys:", Object.keys(vehicle));

    
		if (!container) return;

		    // Build the details HTML
		    container.innerHTML = `
		        <div class="detail-row">
		            <span class="detail-label">Vehicle ID:</span>
		            <span>${vehicle.id}</span>
		        </div>
		        <div class="detail-row">
		            <span class="detail-label">Type:</span>
		            <span>${vehicle.type}</span>
		        </div>
		        <div class="detail-row">
		            <span class="detail-label">Make:</span>
		            <span>${vehicle.make}</span>
		        </div>
		        <div class="detail-row">
		            <span class="detail-label">Model:</span>
		            <span>${vehicle.model}</span>
		        </div>
		        <div class="detail-row">
		            <span class="detail-label">Year:</span>
		            <span>${vehicle.year}</span>
		        </div>
		        <div class="detail-row">
		            <span class="detail-label">Daily Rate:</span>
		            <span>$${vehicle.dailyrate}</span>
		        </div>
		        <div class="detail-row">
		            <span class="detail-label">Status:</span>
		            <span class="status">${vehicle.status}</span>
		        </div>
		    `;

		    // Show reserve button only if vehicle is available
		    const reserveBtn = document.getElementById('reserveBtn');
		    if (vehicle.status && vehicle.status.toUpperCase() === 'AVAILABLE') {
		        reserveBtn.style.display = 'block';
		        reserveBtn.onclick = () => reserveVehicle(vehicle);
		    } else {
		        // Optionally show a message that vehicle is not available
		        const msg = document.createElement('div');
		        msg.className = 'error';
		        msg.innerText = 'This vehicle is not available for reservation.';
		        container.appendChild(msg);
		    }
    } catch (err) {
        console.error(err);
        showError(err.message || 'Failed to load vehicle details.');
    }
}


function reserveVehicle(vehicle) {
    // Store the selected vehicle in sessionStorage for the payment page
    sessionStorage.setItem('selectedVehicle', JSON.stringify(vehicle));
	const startDate = document.getElementById('rentalStartDate').value;
	const endDate = document.getElementById('rentalEndDate').value;
	if (!startDate || !endDate) {
	    alert('Please select both start and end dates.');
	    return;
	}

	// Get user ID from sessionStorage (make sure you store it after login)
	const userId = sessionStorage.getItem('userId');
	if (!userId) {
	    alert('User not logged in. Please log in again.');
	    window.location.href = 'login.html'; 
	    return;
	}

	// Build reservation payload
	const reservation = {
	    vehicleId: vehicle.id,
	    userId: parseInt(userId),
	    startDate: startDate,
	    endDate: endDate
	};

	try {
	    const response = await fetch('http://localhost:8080/reserve', {
	        method: 'POST',
	        headers: { 'Content-Type': 'application/json' },
	        body: JSON.stringify(reservation)
	    });

	    if (!response.ok) {
	        const errorText = await response.text();
	        throw new Error(errorText || 'Reservation failed');
	    }

	    const reservationResult = await response.json();
	    // Store reservation details for payment page
	    sessionStorage.setItem('currentReservation', JSON.stringify(reservationResult));
	    // Redirect to payment page
		alert("Reservation was successfull");
		window.location.href = 'dashboard.html'
		} catch (err) {
	    console.error(err);
	    alert('Error: ' + err.message);
	}

    // Redirect to the payment page
    //window.location.href = 'Payment.html';
}

function showError(message) {
    const container = document.getElementById('details-container');
    if (container) {
        container.innerHTML = `<div class="error">${message}</div>`;
    } else {
        alert(message);
    }
}