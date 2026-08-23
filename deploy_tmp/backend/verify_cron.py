import paramiko

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
c.connect('115.191.6.95', 22, 'lslife', key_filename=r'C:\Users\xl246\.ssh\id_lslife')

def r(cmd, title):
    print(f"=== {title} ===")
    _, o, e = c.exec_command(cmd)
    print(o.read().decode())

r('crontab -l', 'CRONTAB')
r('ls -lh /home/lslife/backups/daily/', 'DAILY BACKUPS')
r('cat /home/lslife/backups/logs/*.log | tail -n 15', 'LAST LOGS')

c.close()
