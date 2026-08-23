import paramiko
import os
import tarfile

host = '115.191.6.95'
user = 'root'
password = 'Maxence2468;'

print("Creating archive...")
with tarfile.open('d:/LsLife/scratch/update.tar.gz', 'w:gz') as tar:
    tar.add('d:/LsLife/backend/dist', arcname='dist')
    tar.add('d:/LsLife/backend/prisma/schema.prisma', arcname='schema.prisma')

print("Connecting...")
client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(host, username=user, password=password)

print("Uploading...")
sftp = client.open_sftp()
sftp.put('d:/LsLife/scratch/update.tar.gz', '/home/lslife/lslife-backend/update.tar.gz')
sftp.close()

print("Extracting and applying...")
commands = [
    'su - lslife -c "cd /home/lslife/lslife-backend && tar -xzf update.tar.gz"',
    'su - lslife -c "cd /home/lslife/lslife-backend && mv schema.prisma prisma/schema.prisma"',
    'su - lslife -c "export PATH=/home/lslife/.local/nodejs/bin:\\$PATH && cd /home/lslife/lslife-backend && npx prisma generate && npx prisma db push --accept-data-loss"',
    'su - lslife -c "export PATH=/home/lslife/.local/nodejs/bin:\\$PATH && cd /home/lslife/lslife-backend && pm2 restart all"'
]

for cmd in commands:
    print(f"Running: {cmd}")
    stdin, stdout, stderr = client.exec_command(cmd)
    out = stdout.read().decode()
    err = stderr.read().decode()
    if out: print('OUT:', out)
    if err: print('ERR:', err)

client.close()
print("Done!")
