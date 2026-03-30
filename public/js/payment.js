document.getElementById('cardNumber').addEventListener('input', function () {
    let val = this.value.replace(/\D/g, '').substring(0, 16);
    this.value = val.match(/.{1,4}/g)?.join(' ') || val;
});


document.getElementById('expiry').addEventListener('input', function () {
    let val = this.value.replace(/\D/g, '').substring(0, 4);
    if (val.length >= 3) val = val.substring(0, 2) + '/' + val.substring(2);
    this.value = val;
});

function processPayment() {
    const name = document.getElementById('cardName').value;
    const number = document.getElementById('cardNumber').value;
    const expiry = document.getElementById('expiry').value;
    const cvv = document.getElementById('cvv').value;
    const msg = document.getElementById('message');

    if (!name || !number || !expiry || !cvv) {
        msg.style.display = 'block';
        msg.style.color = 'white';
        msg.textContent = 'Please fill in all fields.';
        return;
    }

    
    const rawNumber = number.replace(/\s/g, '');
    if (rawNumber.length !== 16) {
        msg.style.display = 'block';
        msg.textContent = 'Please enter a valid 16 digit card number.';
        return;
    }

    
    if (cvv.length !== 3) {
        msg.style.display = 'block';
        msg.textContent = 'CVV must be 3 digits.';
        return;
    }

    msg.style.display = 'block';
    msg.style.color = '#90EE90';
    msg.textContent = 'Payment successful! Redirecting...';

    setTimeout(() => {
        window.location.href = 'dashboard.html';
    }, 2000);
}