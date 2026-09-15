async function getGreeting() {
    const name = document.getElementById('nameInput').value;
    const box = document.getElementById('responseBox');
    box.textContent = "Cargando...";
    try {
        const response = await fetch(`/hello?name=${encodeURIComponent(name)}`);
        const message = await response.text();
        box.textContent = message;
    } catch (err) {
        box.textContent = "Error: " + err.message;
    }
}

async function getPi() {
    const box = document.getElementById('responseBox');
    box.textContent = "Cargando...";
    try {
        const response = await fetch('/pi');
        const message = await response.text();
        box.textContent = message;
    } catch (err) {
        box.textContent = "Error: " + err.message;
    }
}
