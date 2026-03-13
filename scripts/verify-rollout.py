import subprocess
import sys

NAMESPACE = "nextgen"

deployments = [
    "apiservice",
    "authservice",
    "userservice",
    "nextgen-ui"
]

for d in deployments:
    print(f"⏳ Waiting for rollout: {d}")
    result = subprocess.run(
        [
            "kubectl",
            "rollout",
            "status",
            f"deployment/{d}",
            "-n",
            NAMESPACE,
            "--timeout=180s"
        ]
    )

    if result.returncode != 0:
        print(f"❌ Rollout failed: {d}")
        sys.exit(1)

print("✅ All deployments rolled out successfully")
