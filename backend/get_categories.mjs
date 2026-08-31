import { NodeSSH } from 'node-ssh';

const ssh = new NodeSSH();

async function runCheck() {
    try {
        await ssh.connect({
            host: '115.191.6.95',
            username: 'root',
            password: 'Maxence2468;'
        });

        const q = await ssh.execCommand('docker exec lslife-backend_db_1 psql -U lslife -d lslife -c "SELECT name, \\"iconUrl\\" FROM \\"Category\\" WHERE \\"parentId\\" IS NULL;"');
        console.log(q.stdout || q.stderr);

    } catch (err) {
        console.error(err);
    } finally {
        ssh.dispose();
    }
}

runCheck();
