@echo off
cd /d "%~dp0"
java -DNETLOG_HTTP_PORT=8701 -DNETLOG_DATABASE_FILE=D:/ham/netlog.db -jar target/netlog-2.0.1.jar
