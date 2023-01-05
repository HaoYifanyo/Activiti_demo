# Activiti_demo
The project is based on [Activiti](https://www.activiti.org/) 6.0 and SpringBoot.
## Background
Activiti is an open-source engine designed to help businesses solve automation challenges in distributed, highly-scalable and cost effective infrastructures. 
The most important task of it is to provide flexible and customizable processes. With the necessary configuration, Activiti can determine the following two things:
* What is the process to go through (by bpmn file)
* Who are authorized to operate at a certain task node (by [taskListener](#TaskListener))

In this project, I created some useful wrapper [methods](#Methods) to make it easier for other modules to operate the processes. 
To reduce the call time consumption, I also created [batch methods](#BatchMethods) that use **multiple threads**.

## Usage
Usually, the **workflow** module can be run as a separate service. For example, you can use **Spring Cloud** and have other modules call methods in the workflow module via **HTTP** requests. There are corresponding interfaces which use **RESTful API** in `ProcessController`.

However, it is possible to use it without complex architecture. You can include the workflow module directly in the project and call methods in `IProcessService`. The calls within the service will be easier and faster.

## Principle
### Services
The essence of Activiti is to manipulate data through the following services. 
![image](https://user-images.githubusercontent.com/41005474/210655024-6f814c6d-8b7d-4ae2-8ed9-9b942035424a.png)

### Databese table names explained
* ACT_RE_*: RE stands for repository. Tables with this prefix contain static information such as process definitions and process resources (images, rules, etc.).

* ACT_RU_*: RU stands for runtime. These are the runtime tables that contain the runtime data of process instances, user tasks, variables, jobs, etc. Activiti only stores the runtime data during process instance execution, and removes the records when a process instance ends. This keeps the runtime tables small and fast.

* ACT_ID_*: ID stands for identity. These tables contain identity information, such as users, groups, etc.

* ACT_HI_*: HI stands for history. These are the tables that contain historic data, such as past process instances, variables, tasks, etc.

* ACT_GE_*: general data, which is used in various use cases.

### A metaphor
![activiti structure](https://user-images.githubusercontent.com/41005474/210773264-ea43e2b1-88c1-4cc9-be81-c1d4d0d37515.png)

* There are many different toy production lines in the factory, each with a different process. (**ProcessDefinition**)
* Many of the same toys can be produced on one production line. (**ProcessInstance**, There can be many instances of one definition.)
* There can be N carriages in a toy train. (**Task**, there can be N task nodes in an instance.)
* There are goods in the carriages. (candidates and other variables, such as time and comments)

## Methods
### open

`ProcessDTO openProcess(String processDefinitionKey, String businessKey, String userId, Map<String, Object> variableMap);`
Create a process instance by `processDefinitionKey`. After running this method, the process will reach the first node.

`ProcessDTO openAuditProcess(String processDefinitionKey, String businessKey, String userId, Map<String, Object> variableMap);`
When the first node is the submitter (to make it easier to reject the process from other nodes to the submitter), we generally open the process and audit the first node at the same time.

### BatchMethods
The two multithreading methods `openAuditBatch` and `auditBatch` are implemented with `Future` and `Callable`.

## TaskListener

## Configuration
### In the yml file
```yml
spring:
  activiti:
    # whether to enable automatic deployment, the default is true.
    check-process-definitions: false
    
    # the location of bpmn files, the default is /processes in the resource folder.
    process-definition-location-predix: classpath:/processes
    
    # check if the identity table or history table exists, the default is true.
    db-identity-used:
    db-history-used:
    
    # false(default): Check if the version of the database table and the version of the dependency library match, if not, throw an exception.
    # true: Check when activiti starts and create or update tables if needed.
    # create_drop: Create tables when activiti starts, and drop tables when activiti stops.
    # drop_create: When activiti starts, first drop old tables, then create new tables.
    database-schema-update: true
```
    
### In the configuration class
About the deployment mode, see `activitiConfig`.
    
