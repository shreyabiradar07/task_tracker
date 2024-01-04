# Task Tracker
The Task Tracker is a servlet-based task tracking application. This application provides a set of APIs for creating, retrieving, updating, and deleting tasks using HTTP methods.

## Technologies Used
- Java Servlets
- Jetty Server
- Docker
- Kubernetes
- Minikube
- Google Gson Library

## API Endpoints

### Get All Tasks
- **Method**: GET
- **Endpoint**: `/tasks`
- **Description**: Retrieves all tasks in JSON format.

### Create a Task
- **Method**: POST
- **Endpoint**: `/tasks`
- **Description**: Adds a new task to the task list.

### Update a Task
- **Method**: PUT
- **Endpoint**: `/tasks/{taskId}`
- **Description**: Updates an existing task specified by `taskId`.

### Delete a Task
- **Method**: DELETE
- **Endpoint**: `/tasks/{taskId}`
- **Description**: Deletes the task corresponding to the provided `taskId`.
