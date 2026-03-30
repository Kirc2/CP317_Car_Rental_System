document.addEventListener("DOMContentLoaded", function() {
    const username = sessionStorage.getItem('username') || 'Guest';
    document.getElementById('usernameDisplay').textContent = username;

    fetch("http://localhost:8080/history")
        .then(response => {
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            return response.json();
        })
        .then(rentals => {
            const container = document.getElementById("rental-list");
            if (!container) return;

            container.innerHTML = '';

            if (rentals.length === 0) {
                container.innerHTML = '<div class="no-history">No rental history found.</div>';
                return;
            }

            rentals.forEach(rental => {
                const card = document.createElement("div");
                card.className = "rental-card";
                card.innerHTML = `
                    <div class="car-name">${rental.carName || 'Unknown Car'}</div>
                    <div class="detail">Start: ${rental.start || 'N/A'}</div>
                    <div class="detail">End: ${rental.end || 'N/A'}</div>
                    <div class="detail total">Total: $${rental.total || 0}</div>
                    <div class="button-group">
                    <button class="view-details-btn" data-id="${rental.id}">View Details</button>
                    </div>
                `;
                container.appendChild(card);
            });

            document.querySelectorAll('.view-details-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const rentalId = btn.getAttribute('data-id');
                window.location.href = `RentalDetails.html?id=${rentalId}`;
            });
        });
        })
        .catch(error => {
            console.error(error);
            document.getElementById("rental-list").innerHTML = '<div class="error">Error loading history. Please try again later.</div>';
        });
});