// Register function
async function register(userData) {
    const response = await fetch("http://localhost:8080/register", {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userData)
    });

    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Registration Failed');
    }
    return await response.json();
}

// Login function (already present)
async function login(credentials) {
    const response = await fetch("http://localhost:8080/login", {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(credentials)
    });

    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Login failed');
    }
    const user = await response.json();
    localStorage.setItem('currentUser', JSON.stringify(user));
    return user;
}

document.addEventListener("DOMContentLoaded", function () {
    const loginDisplay = document.getElementById("loginDisplay");
    const registerDisplay = document.getElementById("registerDisplay");

    // Handle form submission for both login and register
    document.getElementById("loginBox").addEventListener("submit", async function (e) {
        e.preventDefault();

        // Determine which mode is active
        if (loginDisplay.style.display !== "none") {
            // --- LOGIN ---
            // Use scoped selectors to get fields inside the visible loginDisplay
            const username = document.querySelector("#loginDisplay #username").value;
            const password = document.querySelector("#loginDisplay #password").value;

            const credentials = { email: username, password: password };

            try {
                const user = await login(credentials);
                sessionStorage.setItem('username', user.name || username);
                window.location.href = '/dashboard.html';
            } catch (err) {
                alert('Login failed: ' + err.message);
            }
        } else {
            // --- REGISTER ---
            const username = document.querySelector("#registerDisplay #username").value;
            const email = document.querySelector("#registerDisplay #email").value;
            const password = document.querySelector("#registerDisplay #password").value;

            const userData = {
                name: username,          // using "username" field as full name
                email: email,
                password: password,
                phone: "",               // add fields if you have them
                licenseNumber: ""
            };

            try {
                const newUser = await register(userData);
                alert('Registration successful! Please log in.');
                // Switch back to login view
                document.querySelector('input[value="login"]').checked = true;
                loginDisplay.style.display = "block";
                registerDisplay.style.display = "none";
            } catch (err) {
                alert('Registration failed: ' + err.message);
            }
        }
    });

    // Radio button toggle (your existing code)
    const radios = document.querySelectorAll('input[name="loginRegister"]');
    radios.forEach(radio => {
        radio.addEventListener("change", function () {
            if (this.value === "login") {
                loginDisplay.style.display = "block";
                registerDisplay.style.display = "none";
            } else {
                loginDisplay.style.display = "none";
                registerDisplay.style.display = "block";
            }
        });
    });

    // Scroll effect (your existing code)
    window.addEventListener("scroll", function () {
        const overlay = document.querySelector(".overlay");
        if (!overlay) return;
        const maxHeight = 400;
        const scrollAmount = Math.min(window.scrollY, maxHeight);
        overlay.style.opacity = 0.7 * scrollAmount / maxHeight;
    });
});