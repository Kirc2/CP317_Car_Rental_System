const customerId = sessionStorage.getItem("customerId") || 1; // replace with actual logic

fetch(`/history?customerId=${customerId}`)
    .then(response => {
        if (!response.ok) {
            throw new Error("Failed to fetch history");
        }
        return response.json();
    })
    .then(rentals => {
        const container = document.getElementById("rental-list");
        if (rentals.length === 0) {
            container.innerHTML = '<div class="no-history">No rental history found.</div>';
            return;
        }
        rentals.forEach(rental => {
            const card = document.createElement("div");
            card.className = "rental-card";
            card.innerHTML = `
                <div class="car-name">${rental.carName}</div>
                <div class="detail">Start: ${rental.start}</div>
                <div class="detail">End: ${rental.end}</div>
                <div class="detail">Total: $${rental.total.toFixed(2)}</div>
                <span class="status ${rental.status}">${rental.status.charAt(0).toUpperCase() + rental.status.slice(1)}</span>
            `;
            container.appendChild(card);
        });
    })
    .catch(error => {
        console.error("Error:", error);
        document.getElementById("rental-list").innerHTML = '<div class="no-history">Error loading history.</div>';
    });