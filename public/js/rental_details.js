        document.addEventListener("DOMContentLoaded", function() {
            const username = sessionStorage.getItem('username') || 'Guest';
            document.getElementById('usernameDisplay').textContent = username;

            const urlParams = new URLSearchParams(window.location.search);
            const rentalId = urlParams.get('id');
            if (!rentalId) {
                document.getElementById('details-container').innerHTML = '<div class="error">No rental specified.</div>';
                return;
            }

            fetchRentalDetails(rentalId);
        });

        async function fetchRentalDetails(id) {
            try {
                const response = await fetch(`http://localhost:8080/rentalhistory?id=${id}`);
                if (!response.ok) {
                    if (response.status === 404) throw new Error('Rental not found.');
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const rental = await response.json();
                displayRental(rental);
            } catch (err) {
                console.error(err);
                document.getElementById('details-container').innerHTML = `<div class="error">${err.message || 'Failed to load rental details.'}</div>`;
            }
        }

        function displayRental(rental) {
            const container = document.getElementById('details-container');
            if (!container) return;

            // Determine status class
            let statusClass = 'status';
			if (rental.status === 'ACTIVE' || rental.status === 'RESERVED') statusClass += ' status-active';
            else if (rental.status === 'CANCELLED') statusClass += ' status-cancelled';
            else if (rental.status === 'COMPLETED') statusClass += ' status-completed';

            container.innerHTML = `
                <div class="detail-row">
                    <span class="detail-label">Rental ID:</span>
                    <span>${rental.id}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Vehicle:</span>
                    <span>${rental.carName || 'N/A'}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Start Date:</span>
                    <span>${rental.start || 'N/A'}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">End Date:</span>
                    <span>${rental.end || 'N/A'}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Total Cost:</span>
                    <span>$${rental.total || 0}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Status:</span>
                    <span class="${statusClass}">${rental.status || 'UNKNOWN'}</span>
                </div>
            `;

            const cancelBtn = document.getElementById('cancelBtn');
            // Show cancel button only if rental is active and start date is not in the past (optional)
            if (rental.status === 'RESERVED' || rental.status == 'ACTIVE') {
                cancelBtn.style.display = 'block';
                cancelBtn.onclick = () => cancelRental(rental.id);
            }
        }

        async function cancelRental(rentalId) {
            if (!confirm('Are you sure you want to cancel this reservation?')) return;
            try {
				const data = {
					id: rentalId,
				}
                const response = await fetch(`http://localhost:8080/cancel`,{
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)});

                if (!response.ok) throw new Error(`HTTP ${response.status}`);
                alert('Reservation cancelled successfully.');
                // Reload page to reflect updated status
                window.location.href = "History.html";
            } catch (err) {
                console.error(err);
                alert('Failed to cancel reservation, contact support or try again later');
            }
        }