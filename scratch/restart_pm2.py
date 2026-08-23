import paramiko

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect('115.191.6.95', username='root', password='Maxence2468;')

s, out, err = client.exec_command('su - lslife -c "export PATH=/home/lslife/.local/nodejs/bin:\\$PATH && cd /home/lslife/lslife-backend && pm2 restart all"')
print('OUT:', out.read().decode(errors='ignore'))
print('ERR:', err.read().decode(errors='ignore'))
