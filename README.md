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

You can either use the pre-built Java JAR file for version 1.0.3 of the application, or download / clone source code from Github.

## Prerequisites 

You must have Java 21 installed on the system running NETLOG.

## Configuration information

Information is stored in a simple database found at the environment variable NETLOG_DATABASE_FILE.

The environment variable NETLOG_HTTP_PORT will determine the HTTP port used for the application.

## Build from Source

### Recommended IDE Setup

[VSCode](https://code.visualstudio.com/)

### Project Setup

```sh
mvn clean install
```

## Running the software


```sh
java -DNETLOG_HTTP_PORT=8701 -DNETLOG_DATABASE_FILE=D:/ham/netlog.db -jar netlog-1.0.3.jar
```


## Versions

1.0.3 - Added monthly and quarterly non-participation reports

1.0.2 - Added a list of previous event operators to make check-in easier

1.0.1 - Initial release