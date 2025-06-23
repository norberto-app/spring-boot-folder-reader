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

When Starting a new process by executing a request against /process/start, a new process is started with the status set to PENDING
and, and this status is updated as soon as the process starts running (reading the configured folder content).

When trying to stop a process by calling ```/process/stop/{process_id}```, the process needs to be in ```Running``` state, otherwise the API will return 404 error. 

Note: 
- All logs can be found under the app logs folder
- There is small delay added to simulate a "long-running task", the delay is 15 seconds (This was added just to test the /stop endpoint)

## Installation and usage
To install/use this app, you need to have java installed on your local machine.
Once you have java installed in your local environment, follow these steps:

- Download this [jar file](https://github.com/norberto-app/spring-boot-folder-reader/raw/refs/heads/main/build/libs/spring-boot-folder-reader-0.0.1-SNAPSHOT.jar)
- Open a command line interface and execute:
  `
  java -jar [PATH-TO-JAR-FILE]/spring-boot-folder-reader-0.0.1-SNAPSHOT.jar
  `

If you need to change any of the default config values, you can add to the above command line the name of the property you want to change, 
for example, to change the default port number, you can run something like this:

  `java -jar [PATH-TO-JAR-FILE]/spring-boot-folder-reader-0.0.1-SNAPSHOT.jar --server.port=9000`

Now you should have your local environment ready to test this sample app.<br>
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
- To start a new process: ```curl -X POST  http://localhost:8080/process/start | jq```
- To stop a given process: ```curl -X POST  http://localhost:8080/process/stop/{process-id} | jq```
- Get the status of a given process Id: ```curl -X GET  http://localhost:8080/process/status/{process-id} | jq```
- Get the process list: ```curl -X GET  http://localhost:8080/process/list | jq```
- Get the results of a given process: ```curl -X GET  http://localhost:8080/process/results/{process-id} | jq```

or you can use the postman collection file called ```Process API.postman_collection.json```

## Final notes
Claude.ai was used to generate the smaple text files.
This was the prompt:
```generate 15 different text files each one containing at least 500 words```
