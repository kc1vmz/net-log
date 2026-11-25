#!/bin/bash
APP_OS=$(uname)
if [ "$APP_OS" = "Linux" ]; then
  echo "Welcome to the NetLog UNinstaller for Linux"

  # default environment variables
  APP_CONFIRM_UNINSTALL=Y

  read -e -i $APP_CONFIRM_UNINSTALL -p "Do you want to uninstall NetLog (Y/n)?: " APP_CONFIRM_UNINSTALL

  if [[ "$APP_CONFIRM_UNINSTALL" =~ ^[Yy]$ ]]; then
    # fall through
    echo Starting uninstall of NetLog
  else
    #any other answer - exit
    exit 1
  fi

  echo Backing up /etc/environment
  sudo cp /etc/environment /etc/environment.pre_netlog_uninstall

  echo Removing environment variables from /etc/environment
  sudo sed -i '/NETLOGAPP_INSTALL_DIR/d' /etc/environment
  sudo sed -i '/NETLOG_DB_DIR/d' /etc/environment
  sudo sed -i '/NETLOG_TEMP_DIR/d' /etc/environment
  sudo sed -i '/NETLOG_HTTP_PORT/d' /etc/environment

  echo Removing services
  sudo systemctl stop net-log
  sudo systemctl disable net-log
  sudo rm /etc/systemd/system/net-log.service
  sudo systemctl daemon-reload

  if [ -n "${NETLOGAPP_INSTALL_DIR+x}" ]; then
    echo Deleting NetLog application
    sudo rm netlog*.jar
    echo Not deleting ${NETLOGAPP_INSTALL_DIR} - delete independently.
  else
    echo NetLog installation directory unknown - delete independently.
  fi

  if [ -n "${NETLOG_DB_DIR+x}" ]; then
    echo Not deleting ${NETLOG_DB_DIR} - delete independently.
  else
    echo NetLog database directory unknown - delete independently.
  fi

  if [ -n "${NETLOG_TEMP_DIR+x}" ]; then
    echo Not deleting ${NETLOG_TEMP_DIR} - delete independently.
  else
    echo NetLog temporary directory unknown - delete independently.
  fi

  APP_CONFIRM_UNINSTALL=

elif [ "$APP_OS" = "Darwin" ]; then
  echo "This is a Mac Machine - not yet supported"
  exit 1
else
  echo "Unsupported OS -" $APP_OS
  exit 1
fi

echo "NetLog uninstallation complete."