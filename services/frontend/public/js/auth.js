const API = window.location.origin + "/api";


function login() {
  fetch(`${API}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      username: username.value,
      password: password.value
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
  fetch(`${API}/signup`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      username: username.value,
      password: password.value
    })
  })
  .then(r => r.text())
  .then(t => msg.innerText = t);
}
