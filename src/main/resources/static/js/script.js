document.getElementById('loginForm').addEventListener('submit', function (e) {
    e.preventDefault(); // Prevent the form from submitting the traditional way

    let name = document.getElementById('name').value;
    let password = document.getElementById('password').value;

    fetch('/user/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `name=${encodeURIComponent(name)}&password=${encodeURIComponent(password)}`
    })
        .then(response => response.text())
        .then(data => {
            document.getElementById('result').innerText = data;
        })
        .catch(error => console.error('Error:', error));
});