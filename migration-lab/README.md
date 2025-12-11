# Migration Lab: Phase 1 (Jenkins + Target VM)

This docker-compose stack starts Jenkins and a target Ubuntu VM on the `migration-lab` network.

## Start the lab

```bash
cd migration-lab
docker compose up -d --build
```

## Jenkins

- URL: http://localhost:8080
- Initial admin password: `docker logs migration-jenkins | grep -m1 'Please use the following password' -A 1`
- Plugins to install: Suggested plugins, plus **Docker Pipeline** and **SSH Agent**.

## Target VM

- SSH: `ssh deploy@localhost -p 2222` (password: `password`)
- Docker-in-Docker is started automatically so Jenkins can run `docker run` via SSH.
- Port 80 is mapped to the host, allowing the deployed app to be reachable at http://localhost:80 once the Jenkins pipeline runs `docker run -d -p 80:8080 <image>`.
