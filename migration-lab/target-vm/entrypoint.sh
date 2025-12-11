#!/usr/bin/env bash
set -e

service docker start
exec /usr/sbin/sshd -D -e
