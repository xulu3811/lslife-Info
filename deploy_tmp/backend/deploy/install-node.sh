#!/usr/bin/env bash
# 在无 sudo 的用户空间安装 Node.js 20 (使用 npmmirror 国内镜像, 火山云可靠)
set -euo pipefail

NODE_MAJOR="v20.x"
MIRROR="https://npmmirror.com/mirrors/node"
INSTALL_DIR="$HOME/.local/nodejs"

if command -v node >/dev/null 2>&1; then
  echo "Node 已安装: $(node -v)"
  exit 0
fi

echo "查询最新 ${NODE_MAJOR} 版本..."
FILE=$(curl -fsSL "${MIRROR}/latest-${NODE_MAJOR}/SHASUMS256.txt" | grep -o 'node-v[0-9.]*-linux-x64.tar.xz' | head -1)
if [ -z "${FILE}" ]; then
  echo "无法获取 Node 版本文件名" >&2
  exit 1
fi
echo "目标: ${FILE}"

cd "$HOME"
curl -fsSLO "${MIRROR}/latest-${NODE_MAJOR}/${FILE}"

mkdir -p "${INSTALL_DIR}"
tar -xf "${FILE}" -C "${INSTALL_DIR}" --strip-components=1
rm -f "${FILE}"

# 写入 PATH (bashrc 交互式, profile 登录式)
LINE='export PATH="$HOME/.local/nodejs/bin:$PATH"'
grep -qxF "${LINE}" "$HOME/.bashrc" 2>/dev/null || echo "${LINE}" >> "$HOME/.bashrc"
grep -qxF "${LINE}" "$HOME/.profile" 2>/dev/null || echo "${LINE}" >> "$HOME/.profile"

export PATH="$HOME/.local/nodejs/bin:$PATH"
# npm 使用国内镜像
npm config set registry https://registry.npmmirror.com

echo "Node 安装完成: $(node -v), npm: $(npm -v)"
