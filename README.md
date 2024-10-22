# GitMazon

An automated deployment system that allows users to deploy their services with a single click (currently for Java Spring Boot).
----

## Features

- Automated deployment of Spring Boot projects.
- Real-time service interaction via web-based terminal.
- Real-time monitoring of service CPU and RAM usage.

## Technique 
- Employed **Shell Script** to create **Docker images** from user repositories and retrieve real-time services’s CPU, RAM usage.
- Deployed user services in **Docker containers** on **AWS EC2**.
- Used **Nginx** as a reverse proxy to route traffic to services.
- Implemented with **Spring Boot** Framework. 
- Utilized **pty4j** with **Xterm.js** to allow users to connect via **SSH** and interact with their services in        real-time through a web-based terminal.
- Employed **shell scripts** to create Docker images from user repositories and retrieve real-time service usage.
- Utilized the **AWS SDK** to dynamically add security group rules, enabling user services to be accessed.
- Received resource usage data via **Kafka** and sends it back to users in real-time through **WebSocket**.
- Implemented a **Webhook** server using **Flask** to trigger deployments when users push new code.

## Architecture

![Architecture](https://gitmazon.s3.ap-northeast-1.amazonaws.com/%E6%88%AA%E5%9C%96+2024-10-22+%E5%87%8C%E6%99%A81.34.57.png)

## How to use

1. Log into the system using GitHub OAuth.
2. Fill out a service registration form, including:
    - Selecting a repository to register.
    - Specifying a service name.
    - Defining resource limitations.
3. Monitor service CPU and RAM usage in real-time on the "My Services" page, with an option to connect to the service via SSH.

## Demo Video

[![GitMazonDemo](https://gitmazon.s3.ap-northeast-1.amazonaws.com/%E6%88%AA%E5%9C%96+2024-10-22+%E4%B8%8B%E5%8D%881.10.01.png)](https://www.youtube.com/watch?v=x0KQoQbaO60)

## Related Repository

- [GitMazonMainService](https://github.com/b22790188/GitMazonMainService) - Provides a frontend interface and real-time SSH connection functionality.

- [GitMazonKafkaConsumer](https://github.com/b22790188/GitMazonKafkaConsumerServer) - Receives real-time CPU and RAM data from Kafka and sends this information back in real-time via WebSocket.

- [GitMazonImageProducer](https://github.com/b22790188/GitMazonImageProducer) - Builds Docker images for users' repositories and notifies the corresponding worker node to start the service.

- [GitMazonWorkerNode](https://github.com/b22790188/GitMazonWorkerNode) - Starts containers for users and continuously streams both the container data and its own data to Kafka.
