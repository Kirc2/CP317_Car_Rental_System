document.addEventListener("DOMContentLoaded", function() {

   
    // Fetch rental history from backend
    fetch("http://localhost:8080/history")
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(rentals => {
            const container = document.getElementById("rental-list");
            if (!container) {
                console.error("Container #rental-list not found!");
                return;
            }

            // Clear any loading message or static content
            container.innerHTML = '';

            if (rentals.length === 0) {
                container.innerHTML = '<div class="no-history">No rental history found.</div>';
                return;
            }

            // Loop through each rental and create a card
            rentals.forEach(rental => {
                const card = document.createElement("div");
                card.className = "rental-card";

                // Format date (if needed) – assuming rental.start and rental.end are strings like "2026-01-10"
                const startDate = rental.start || 'N/A';
                const endDate = rental.end || 'N/A';
                const total = rental.total
                

                card.innerHTML = `
                    <div class="car-name">${rental.carName || 'Unknown Car'}</div>
                    <div class="detail">Start: ${startDate}</div>
                    <div class="detail">End: ${endDate}</div>
                    <div class="detail">Total: $${total}</div>
                `;
                container.appendChild(card);
            });
        })
        .catch(error => {
            console.error("Error loading rental history:", error);
            const container = document.getElementById("rental-list");
            if (container) {
                container.innerHTML = '<div class="no-history">Error loading history. Please try again later.</div>';
            }
        });
});