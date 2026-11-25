@echo off
rd /s/q target
mkdir target
echo Gathering files for upload to Github

copy ..\README.md target
copy ..\installers\linux\netlog_installer.sh target
copy ..\installers\linux\netlog_uninstaller.sh target
copy ..\installers\windows\netlog_installer.bat target
copy ..\target\netlog-2.0.3.jar target
