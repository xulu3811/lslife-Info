const { NodeSSH } = require('node-ssh');
const backupSsh = new NodeSSH();

async function run() {
  try {
    await backupSsh.connect({
      host: '47.107.179.201',
      username: 'root',
      password: 'Maxence2468;'
    });
    
    const result = await backupSsh.execCommand('ls -la /root/backups/lslife_prod');
    console.log('Remote Files:', result.stdout);
    
    // Also check the source code zip
    const result2 = await backupSsh.execCommand('ls -lh /root/backups/LsLife_V2.01_SourceCode.tar.gz');
    console.log('Source Code Backup:', result2.stdout);
    
    backupSsh.dispose();
  } catch (err) {
    console.error(err);
  }
}

run();
