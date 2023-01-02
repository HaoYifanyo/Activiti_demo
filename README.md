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
## Methods
### open


## TaskListener
