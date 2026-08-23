import paramiko

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect('115.191.6.95', username='root', password='Maxence2468;')

s, out, err = client.exec_command('su - lslife -c "export PATH=/home/lslife/.local/nodejs/bin:\\$PATH && cd /home/lslife/lslife-backend && npx prisma studio" & sleep 2 && kill $!')
s, out, err = client.exec_command('su - lslife -c "psql -U lslife -d lslife -p 5433 -h localhost -c \\"SELECT id, nickname, role FROM \\\\\\"User\\\\\\" LIMIT 5;\\""')
print(out.read().decode('utf-8', errors='ignore'))
print(err.read().decode('utf-8', errors='ignore'))
