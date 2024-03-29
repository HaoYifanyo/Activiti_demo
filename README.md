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

### An analogy
![activiti structure](https://user-images.githubusercontent.com/41005474/210773264-ea43e2b1-88c1-4cc9-be81-c1d4d0d37515.png)

* There are many different toy train production lines in the factory, each with a different process. (**ProcessDefinition**)
* Many of the same toy trains can be produced on one production line. (**ProcessInstance**, There can be many instances of one definition.)
* There can be N carriages in a toy train. (**Task**, there can be N task nodes in an instance.)
* There are goods in the carriages. (candidates and other variables, such as time and comments)

## Methods
### open

`ProcessDTO openProcess(String processDefinitionKey, String businessKey, String userId, Map<String, Object> variableMap);`

Create a process instance by `processDefinitionKey`. After running this method, the process will reach the first node.

`ProcessDTO openAuditProcess(String processDefinitionKey, String businessKey, String userId, Map<String, Object> variableMap);`

When the first node is the submitter (to make it easier to reject the process from other nodes to the submitter), we generally open the process and audit the first node at the same time.

### audit

`TaskDTO audit(String businessKey, String userId, Map<String, Object> variableMap, Map<String, Object> transientVariableMap);`

Audit means looking up the task and making a decision. What the next task node is depends on the variables in `transientVariableMap`, corresponding to conditionExpression of sequence flow in the bpmn file.

### back
`List<TaskDTO> back(String businessKey, String userId, Map<String, Object> variableMap);`

Go back to the previous node. The default is to assign the task to the person who last handled the task. The assignment strategy can be changed in `CustomTaskListener`.

### discard
`void discard(String businessKey, String userId, Map<String, Object> variableMap);`

If there are no special restrictions, the process can be discarded at any node.
The implementation is to delete the process instance by `runtimeService.deleteProcessInstance`. This method will delete the data in ACT_RU_VARIBLE, ACT_RU_IDENTITYLINK, ACT_RU_TASK, ACT_RU_EXECUTION, and update data in ACT_HI_ACTINST, ACT_HI_TASKINST, ACT_HI_PROCINST.

### BatchMethods
The two multithreading methods `openAuditBatch` and `auditBatch` are implemented with `Future` and `Callable`.

## TaskListener
A task listener is used to execute custom Java logic or an expression upon the occurrence of a certain task-related event.

`CustomTaskListener` is an example. The implementation idea is to get the configuration information from candidate groups and parse it, and call the method of the user module to get the **candidate users**.

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
About the deployment mode, see `ActivitiConfig`.
    
