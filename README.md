# netlog

NETLOG will document nets and other events.

Reports can be generated to help fill out monthly ARRL ARES Form 2 and Form 4 sheets.

You can generate an ICS 309 report for your net, or monthly and quarterly participation reports.

Start the app and use your browser to enter data.

Add your members, your group/section, then your recurring events.
Start an event with its start time and check in your participants.
When complete, secure the net.

This can be done in real time when a net is started, or after the fact.

## Download 

You can either use the pre-built Java JAR file for version 2.0.3 of the application, or download / clone source code from Github.

## Prerequisites 

You must have Java 21 installed on the system running NETLOG.

## Installation

On Linux, download and run the netlog_installer.sh file.  Answer the questions and it will download the necessary JAR files and configure the system to run the software properly.

On Windows, download and run the netlog_installer.bat file.  Answer the questions and it will download the necessary JAR files and create a .BAT file to run the software properly.

## Build from Source

### Recommended IDE Setup

[VSCode](https://code.visualstudio.com/)

### Project Setup

```sh
mvn clean install
```


## Logging

NETLOG writes INFO, warning, and error messages to the console and to
`netlog.log` in the directory specified by `NETLOG_TEMP_DIR`. The directory is
created if needed. If the variable is unset, logs use the `netlog` subdirectory
of Java's temporary directory.

Logs roll over daily or at 10 MB. Archived logs are dated, numbered, and
gzip-compressed, with 30 days of history and a 1 GB total archive limit. The
current log is additional to that limit. Old archives are also cleaned at startup.
Restarting appends to the current log. File entries include a full timestamp,
thread, level, logger, and exception details without console color codes.

If file logging fails, a message is written to standard error and console logging
continues. Temporary-directory cleanup may independently remove these files.

## Versions

2.0.3 - add file logging

2.0.2 - add Windows and Linux installers, potential participant refresh, reactivate secured net

2.0.1 - fix bug on check in page (null pointer reference)

2.0.0 - rewrite using Spring and Thymeleaf. Prettier and new features.

1.0.9 - Add communication band and platform for participation, fix summary reporting for leadership

1.0.8 - Add location management, district management

1.0.7 - Added operator RACES affiliation; additional section-based reports

1.0.6 - Housekeeping snafu

1.0.5 - Added UI to set transmit power, primary and backup power sources during participation

1.0.4 - Added scheduled events, cancelled events

1.0.3 - Added monthly and quarterly non-participation reports

1.0.2 - Added a list of previous event operators to make check-in easier

1.0.1 - Initial release
