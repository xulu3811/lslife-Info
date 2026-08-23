#!/bin/bash
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
apt-get install -y docker-compose
usermod -aG docker lslife
