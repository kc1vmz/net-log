@echo off
cd /d "%~dp0"
java -DNETLOG_HTTP_PORT=8701 -NETLOG_DB_DIR=D:/ham -jar target/netlog-2.0.2.jar
