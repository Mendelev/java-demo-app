FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      openssh-server \
      openjdk-17-jre \
      ca-certificates \
      curl \
      gnupg \
      lsb-release \
      docker.io && \
    rm -rf /var/lib/apt/lists/*

RUN useradd -m -s /bin/bash deploy && echo "deploy:password" | chpasswd && \
    mkdir -p /var/run/sshd && \
    mkdir -p /home/deploy/.ssh && chmod 700 /home/deploy/.ssh && \
    chown -R deploy:deploy /home/deploy && \
    usermod -aG docker deploy

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 22

CMD ["/entrypoint.sh"]
