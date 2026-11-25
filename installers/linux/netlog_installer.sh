#!/bin/bash
# NetLog Installer
# Copyright (c) 2026 John Rokicki KC1VMZ
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#
# http://www.kc1vmz.com
# 
APP_UPGRADE=N
APP_OS=$(uname)
if [ "$APP_OS" = "Linux" ]; then
  echo "Welcome to the NetLog installer for Linux"
  echo "Copyright (c) 2026 John Rokicki KC1VMZ - Licensed under GPL v3"
  echo "https://www.kc1vmz.com"

  if grep -q NETLOGAPP_INSTALL_DIR /etc/environment; then
    echo "A previous version of NetLog was detected."
    APP_UPGRADE=Y
    read -e -i $APP_UPGRADE -p "Do you wish to upgrade NetLog?: " APP_UPGRADE
    if [[ "$APP_UPGRADE" =~ ^[Nn]$ ]]; then
      echo "NetLog installation will now exit.  Uninstall previous version or run this again and choose upgrade."
      exit 1
    fi
  fi

  # default environment variables
  APP_VERSION=2.0.2
  APP_INSTALL_DIR=~/netlog
  APP_DB_DIR=~/netlog/db
  APP_TEMP_DIR=~/netlog/tmp
  APP_PORT=8701
  APP_INSTALL_SERVICES=Y

  if [[ "$APP_INSTALL_SERVICES" =~ ^[Yy]$ ]]; then
    read -e -i $APP_VERSION -p "What version of NetLog?: " APP_VERSION
    if [[ "$APP_UPGRADE" =~ ^[Nn]$ ]]; then
      read -e -i $APP_INSTALL_DIR -p "Where should NetLog be installed?: " APP_INSTALL_DIR
      read -e -i $APP_DB_DIR -p "Where should NetLog database directory be located?: " APP_DB_DIR
      read -e -i $APP_TEMP_DIR -p "Where should NetLog temp space be located?: " APP_TEMP_DIR
      read -e -i $APP_PORT -p "What port should the HTTP service listen on?: " APP_PORT
      read -e -i $APP_INSTALL_SERVICES -p "Do you want NetLog to be configured as services and started at boot time (Y/n)?: " APP_INSTALL_SERVICES
    else
      NETLOGAPP_INSTALL_DIR=$(grep "^NETLOGAPP_INSTALL_DIR=" /etc/environment | sed 's/^NETLOGAPP_INSTALL_DIR=//' | tr -d '"')
      APP_INSTALL_DIR=$NETLOGAPP_INSTALL_DIR
    fi
  fi

  sudo cp /etc/environment /etc/environment.pre_netlog_install

  JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{sub("^$", "0", $2); print $1$2}')
  if [ "$JAVA_VER" -ge 21 ]; then
    # fall through
    echo "Java 21+ verified"
  else
    echo "Java 21 or greater not installed - exiting."
    sudo rm /etc/environment.pre_netlog_install
    exit 1
  fi

  NETLOGAPP_INSTALL_DIR=$APP_INSTALL_DIR
  if [[ "$APP_UPGRADE" =~ ^[Nn]$ ]]; then
    echo "NETLOGAPP_INSTALL_DIR=$NETLOGAPP_INSTALL_DIR" | sudo tee -a /etc/environment >  /dev/null
    echo "NETLOG_DB_DIR=$APP_DB_DIR/" | sudo tee -a /etc/environment >  /dev/null
    echo "NETLOG_TEMP_DIR=$APP_TEMP_DIR" | sudo tee -a /etc/environment >  /dev/null
    echo "NETLOG_HTTP_PORT=$APP_PORT" | sudo tee -a /etc/environment >  /dev/null
    mkdir $APP_INSTALL_DIR
    sudo mkdir $APP_TEMP_DIR
    sudo chown $USER $APP_TEMP_DIR
    sudo mkdir $APP_DB_DIR
    sudo chown $USER $APP_DB_DIR
  fi

  echo "Retrieving NetLog binary"
  cd $APP_INSTALL_DIR
  APP_SRC_URL_ROOT=https://github.com/kc1vmz/net-log/releases/download/v$APP_VERSION
  wget -q -O netlog-$APP_VERSION.jar $APP_SRC_URL_ROOT/netlog-$APP_VERSION.jar
  APP_SRC_URL_ROOT=

  chmod +x netlog-$APP_VERSION.jar

  if [[ "$APP_UPGRADE" =~ ^[Nn]$ ]]; then
    NETLOG_DB_DIR=$APP_DB_DIR
    NETLOG_TEMP_DIR=$APP_TEMP_DIR
    NETLOG_HTTP_PORT=$APP_PORT
  else
    if [ -n "/etc/systemd/system/net-log.service+x" ]; then
      APP_INSTALL_SERVICES=Y
    else
      APP_INSTALL_SERVICES=N
    fi
  fi

  if [[ "$APP_INSTALL_SERVICES" =~ ^[Yy]$ ]]; then
    if [[ "$APP_UPGRADE" =~ ^[Nn]$ ]]; then
      echo '[Unit]' | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo 'Description=NetLog' | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo '[Service]' | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo 'User='$LOGNAME | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo 'WorkingDirectory='$APP_INSTALL_DIR | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo 'Type=simple' | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo 'Restart=always' | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo 'SuccessExitStatus=143' | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo 'RemainAfterExit=yes' | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo 'ExecStart=java -jar '$APP_INSTALL_DIR'/netlog-'$APP_VERSION'.jar' | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo 'Environment=NETLOG_DB_DIR='$NETLOG_DB_DIR | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo 'Environment=NETLOG_TEMP_DIR='$NETLOG_TEMP_DIR | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo 'Environment=SERVER_PORT='$NETLOG_HTTP_PORT | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo 'Environment=NETLOGAPP_INSTALL_DIR='$NETLOGAPP_INSTALL_DIR | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo '[Install]' | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      echo 'WantedBy=multi-user.target' | sudo tee -a /etc/systemd/system/net-log.service >  /dev/null
      sudo systemctl daemon-reload
      sudo systemctl enable net-log
      sudo systemctl start net-log
      echo "NetLog running as background service."
    else
      sudo systemctl stop net-log
      sudo sed -i 's/netlog-.*.jar/netlog-'$APP_VERSION'.jar/g' /etc/systemd/system/net-log.service
      sudo systemctl daemon-reload
      sudo systemctl start net-log
      echo "NetLog restarted."
    fi
  else
    echo "Environment variables have been added to /etc/environment.  Make sure to reboot or run 'source /etc/environment' to add them into any new terminal."
    echo "You can run NetLog using the  'java -jar $APP_INSTALL_DIR/netlog-$APP_VERSION.jar' command"
  fi

  # cleanup environment
  APP_INSTALL_DIR=
  APP_TEMP_DIR=
  APP_VERSION=
  APP_INSTALL_SERVICES=
  APP_DB_DIR=
  APP_PORT=
  APP_UPGRADE=
elif [ "$APP_OS" = "Darwin" ]; then
  echo "This is a Mac Machine - not yet supported"
  exit 1
else
  echo "Unsupported OS -" $APP_OS
  exit 1
fi

echo "NetLog installation complete."