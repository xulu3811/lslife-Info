import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function run() {
    try {
        await ssh.connect({
            host: '115.191.6.95',
            username: 'root',
            password: 'Maxence2468;'
        });
        
        const result = await ssh.execCommand('rm -f /home/lslife/.admin_pass_once && echo "File removed successfully"');
        console.log('STDOUT: ' + result.stdout);
        console.log('STDERR: ' + result.stderr);
        ssh.dispose();
    } catch (error) {
        console.error('Error connecting via SSH:', error);
        process.exit(1);
    }
}

run();
