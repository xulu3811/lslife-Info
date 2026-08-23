const { Client } = require('pg');

const client = new Client({
  host: '115.191.6.95',
  port: 5433,
  user: 'lslife',
  password: 'af4a98b163543c58c46bf827bdd546a8',
  database: 'lslife',
});

async function run() {
  await client.connect();
  console.log("Connected to remote PostgreSQL.");

  try {
    // Check posts in 'cat_1_idle' or 'cat_1_idle_3c' or containing 'S24'
    const res = await client.query(`SELECT id, title, category, "postType", "status" FROM "Post" WHERE title ILIKE '%24%' OR category LIKE '%idle%'`);
    console.log("Posts matching S24 or idle:");
    console.table(res.rows);

    const res2 = await client.query(`SELECT id, title, category, "postType", "status" FROM "Post" ORDER BY "createdAt" DESC LIMIT 10`);
    console.log("Recent posts:");
    console.table(res2.rows);

  } catch (err) {
    console.error("DB Query Error:", err);
  } finally {
    await client.end();
  }
}

run();
