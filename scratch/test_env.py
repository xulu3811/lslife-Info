import paramiko

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect('115.191.6.95', username='root', password='Maxence2468;')

s, out, err = client.exec_command('su - lslife -c "ls -la /home/lslife/.local/nodejs/bin && node -v && npx -v && pm2 -v"')
print('OUT:', out.read().decode())
print('ERR:', err.read().decode())
