const { Client } = require('pg');

const client = new Client({
  connectionString: 'postgresql://lslife:af4a98b163543c58c46bf827bdd546a8@115.191.6.95:5433/lslife'
});

async function run() {
  await client.connect();
  console.log("Connected to remote PostgreSQL.");

  try {
    const res = await client.query(`SELECT category, COUNT(*) FROM "Post" GROUP BY category`);
    console.log("Post counts by category:");
    console.table(res.rows);
  } catch (err) {
    console.error("DB Query Error:", err);
  } finally {
    await client.end();
  }
}

run();
