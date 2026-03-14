const API = "http://localhost:9000";

function login() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;
  const msg = document.getElementById("msg");

  fetch(`${API}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      username: username,
      password: password
    })
  })
  .then(r => r.text())
  .then(t => {
    if (t.startsWith("ey")) {
      localStorage.setItem("token", t);
      window.location.href = "welcome.html";
    } else {
      msg.innerText = t;
    }
  });
}

function signup() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;
  const msg = document.getElementById("msg");

  fetch(`${API}/signup`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      username: username,
      password: password
    })
  })
  .then(r => r.text())
  .then(t => msg.innerText = t);
}
