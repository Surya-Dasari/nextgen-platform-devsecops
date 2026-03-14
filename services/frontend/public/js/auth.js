const API = "http://localhost:9000";

function login() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;
  const msg = document.getElementById("msg");

  fetch(`${API}/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      username: username,
      password: password
    })
  })
  .then(response => {
    if (!response.ok) {
      throw new Error("Invalid username or password");
    }
    return response.text();
  })
  .then(token => {
    console.log("TOKEN:", token);

    localStorage.setItem("token", token);

    window.location.href = "welcome.html";
  })
  .catch(err => {
    msg.innerText = err.message;
  });
}

function signup() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;
  const msg = document.getElementById("msg");

  fetch(`${API}/signup`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      username: username,
      password: password
    })
  })
  .then(r => r.text())
  .then(t => msg.innerText = t);
}
