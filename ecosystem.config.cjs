module.exports = {
  apps: [{
    name: "lslife-api",
    script: "./dist/index.js",
    instances: "max", // 利用所有可用的 CPU 核心
    exec_mode: "cluster",
    env: {
      NODE_ENV: "production",
    }
  }]
}
