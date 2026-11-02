@echo off
rd /s/q target
mkdir target
echo Gathering files for upload to Github

copy ..\README.md target
copy ..\target\netlog-1.0.9.jar target
