#!/system/bin/sh
BASEDIR=$(dirname "$0")
DEX="$BASEDIR"/shizuku.dex

if [ ! -f "$DEX" ]; then
  echo "Cannot find $DEX, please check post-install.sh"
  exit 1
fi

if [ ! -r "$DEX" ]; then
  echo "Cannot read $DEX, please check post-install.sh"
  exit 1
fi

export SHIZUKU_APPLICATION_ID=""
/system/bin/app_process -Djava.class.path="$DEX" /system/bin --nice-name=sui_wrapper rikka.shizuku.cmd.ShizukuCmd "$@"