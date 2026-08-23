import paramiko

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect('115.191.6.95', username='root', password='Maxence2468;')

stdin, stdout, stderr = client.exec_command('cat /var/www/lslife/backend/src/modules/upload.ts')
content = stdout.read().decode()
if "'/audio'" in content:
    print("YES")
else:
    print("NO")
