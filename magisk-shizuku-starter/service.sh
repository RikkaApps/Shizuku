#!/system/bin/sh

# maybe it's better to start after emulated storage is setup
while [ ! -d "/storage/emulated/0/Android" ]; do
  sleep 3
done

if [ -f "/data/user_de/0/moe.shizuku.privileged.api/start.sh" ]; then
  (sh "/data/user_de/0/moe.shizuku.privileged.api/start.sh")&
  exit 0
fi

if [ -f "/data/user/0/moe.shizuku.privileged.api/start.sh" ]; then
  (sh "/data/user/0/moe.shizuku.privileged.api/start.sh")&
  exit 0
fi