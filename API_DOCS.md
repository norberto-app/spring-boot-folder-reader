# These are the available endpoints

- ``POST /process/start`` [http://localhost/process/start]:  
  Starts a new analysis process. The initial state is set to PENDING, but then as soon as it starts running, the status
  is updated to RUNNING

- ``POST /process/stop/{process_id}`` [http://localhost/process/stop/123]:

  If the given process exists and is currently running, then it gets stopped, and the process status is updated accordingly.
  Returns 404 error in case it does not exist, or 400 if the process id format/type is incorrect (Should be greater than zero)

- ``GET /process/status/{process_id}`` [http://localhost/process/status/123]:
  Reads the status of the given process ID.
  Returns 404 error in case it does not exist, or 400 if the process id format/type is incorrect (Should be greater than zero)

- ``GET /process/list`` [http://localhost/process/list]:
  Returns a list containing all the processes, or and empty list if there is nothing.

- ``GET /process/status/{process_id}`` [http://localhost/process/status/123]:
  Gets all the data, process info and analysis results if the given process ID is valid.
  Returns 404 error in case it does not exist, or 400 if the process id format/type is incorrect (Should be greater than zero)