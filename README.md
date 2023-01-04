# Activiti_demo
The project is based on [Activiti](https://www.activiti.org/) 6.0. 
## Background
Activiti is an open-source engine designed to help businesses solve automation challenges in distributed, highly-scalable and cost effective infrastructures. 
The most important task of it is to provide flexible and customizable processes. With the necessary configuration, Activiti can determine the following two things:
* What is the process to go through (by bpmn file)
* Who is authorized to operate at a certain point (by [taskListener](#TaskListener))

In this project, I created some useful wrapper [methods](#Methods) to make it easier for other modules to operate the processes. 
The code has been used in large real software projects. I removed the parts of them that were associated with business code and improved them.

## Usage
Usually, the module can be run as a separate service. For example, you can use Spring Cloud and have other modules call methods in this module via HTTP requests. 
However, it is possible to use it without complex architecture. You can include the code directly in the project. The calls within the service will be easier and faster.

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
* There are many different toy production lines in the factory, each with a different process. (**ProcessDefinition**)
* Many of the same toys can be produced on one production line. (**ProcessInstance**, There can be many instances of one definition.)
* There can be N carriages in a toy train. (**Task**, there can be N task nodes in an instance.)
* There are goods in the carriages. (candidates and other parameters, such as time and comments)

## Methods
### open


## TaskListener
