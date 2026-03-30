async function updateEmail() {
    const oldEmail = document.getElementById('oldEmail').value;
    const newEmail = document.getElementById('newEmail').value;
    const msg = document.getElementById('message');

    if (!oldEmail || !newEmail) {
        msg.style.display = 'block';
        msg.style.color = 'white';
        msg.textContent = 'Please fill in both fields.';
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/updateEmail', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ oldEmail: oldEmail, newEmail: newEmail })
        });

        if (response.ok) {
            msg.style.display = 'block';
            msg.style.color = '#90EE90';
            msg.textContent = 'Update Completed';
        } else {
            const data = await response.json();
            msg.style.display = 'block';
            msg.style.color = 'white';
            msg.textContent = data.error || 'Old email not found.';
        }
    } catch (err) {
        msg.style.display = 'block';
        msg.style.color = 'white';
        msg.textContent = 'Something is wrong. Try again.';
    }
}