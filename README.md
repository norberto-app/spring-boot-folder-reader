# Sample file reader using spring boot

This is a Document Processing System that allows loading, processing, and analyzing text files
asynchronously, with a REST API to control and monitor the process.

This sample app was developed using spring boot framework and kotlin programming language.

## Approach and design decisions
This is a simple spring boot app, written in kotlin.
The key components are:
- com.example.napp.filereader.controller.ProcessController: The REST controller in charge of handling all requests
- com.example.napp.filereader.controller.FileProcessorService: Contains all the logic for reading the files in the given folder and calculate the stats/results for the all the files.
- com.example.napp.filereader.data.model: here are all the data classes used across the app.
- com.example.napp.filereader.repository: This is the data access layer.
- com.example.napp.filereader.exception: Exception handlers in charge of handling API parameter values, etc

Note: all logs can be found under the app logs folder
## Installation and usage
To install/use this app, you need to have java installed on your local machine.
Once you have java installed in your local environment, follow these steps:

- Download this [jar file](https://github.com/norberto-app/spring-boot-folder-reader/raw/refs/heads/main/build/libs/spring-boot-folder-reader-0.0.1-SNAPSHOT.jar)
- open a command line interface and execute:
  `
  java -jar [PATH-TO-JAR-FILE]/spring-boot-folder-reader-0.0.1-SNAPSHOT.jar
  `
- Now you should have your local environment ready to test this sample app

Try some of these endpoints (from your command line):
- To start a new process: ```curl -X POST  http://localhost:8080/process/start```
- To stop a given process: ```curl -X POST  http://localhost:8080/process/stop/{process-id}```
- Get the status of a given process Id: ```curl -X GET  http://localhost:8080/process/status/{process-id}```
- Get the process list: ```curl -X GET  http://localhost:8080/process/list```
- Get the results of a given process: ```curl -X GET  http://localhost:8080/process/results/{process-id}```


### The list of available endpoints are:

- POST /process/start - Start a new analysis process
- POST /process/stop/{process_id} - Stop a specific process
- GET /process/status/{process_id} - Query the status of a process
- GET /process/list - List all processes and their states
- GET /process/results/{process_id} - Get analysis results

### App Configuration
The configuration file is under src/resources/application.properties

In this file, the main config params are:
- server.port: Default value is 8080, but it can be changed if needed.
- logging.file.name: The file where the app logs are stored (logs/app.log)
- com.example.napp.filereader.folder.name: The source folder that will be used for reading the files (data/ by default)
- com.example.napp.filereader.folder.fileExtensions: File extensions that will be read (txt, log, etc)

## How to test
You can test the endpoints via the command line, using curl:
- To start a new process: ```curl -X POST  http://localhost:8080/process/start```
- To stop a given process: ```curl -X POST  http://localhost:8080/process/stop/{process-id}```
- Get the status of a given process Id: ```curl -X GET  http://localhost:8080/process/status/{process-id}```
- Get the process list: ```curl -X GET  http://localhost:8080/process/list```
- Get the results of a given process: ```curl -X GET  http://localhost:8080/process/results/{process-id}```

## Final notes
Claude.ai was used to generate the smaple text files.
This was the prompt:
```generate 15 different text files each one containing at least 500 words```
