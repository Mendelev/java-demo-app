# Migration Lab: Phase 1 (Jenkins + Target VM)

This docker-compose stack now only starts the target Ubuntu VM on the `migration-lab` network. Jenkins runs on your separate VM.

## Start the lab

```bash
cd migration-lab
docker compose up -d --build
```

## Target VM

- SSH: `ssh deploy@<host-ip> -p 2222` (password: `password`)
- Docker-in-Docker is started automatically so Jenkins can run `docker run` via SSH.
- Port 80 is mapped to the host, allowing the deployed app to be reachable at http://<host-ip>:80 once the Jenkins pipeline runs `docker run -d -p 80:8080 <image>`.

## Jenkins (running on your VM)

- Ensure Docker is installed on the Jenkins VM so the pipeline can `docker build`/`push`.
- Install plugins: Suggested, plus **Docker Pipeline** and **SSH Agent**.
- Add credentials:
  - Docker Hub username/password.
  - SSH username/password (deploy/password) pointing to `<host-ip>:2222` for the target VM container.
