import paramiko
import os

host = '115.191.6.95'
user = 'root'
password = 'Maxence2468;'

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(host, username=user, password=password)

# Restart PM2
print("Restarting pm2...")
stdin, stdout, stderr = client.exec_command('su - lslife -c "cd /home/lslife/backend && pm2 restart all"')
print(stdout.read().decode())
print(stderr.read().decode())

print("Deployment complete!")
