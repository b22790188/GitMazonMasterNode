# GitMazon

An automated deployment system that allows users to deploy their services with a single click (currently for Java Spring Boot).
----

## Features

- Automated deployment of Spring Boot projects.
- Real-time service interaction via web-based terminal.
- Real-time monitoring of service CPU and RAM usage.

## Technique used
- Deploys user services in **Docker containers** on **AWS EC2**.
- Uses **Nginx** as a reverse proxy to route traffic to services.
- Utilizes **pty4j** with **Xterm.js** to allow users to connect via **SSH** and interact with their services in        real-time through a web-based terminal.
- Employs **shell scripts** to create Docker images from user repositories and retrieve real-time service usage.
- Utilizes the **AWS SDK** to dynamically add security group rules, enabling user services to be accessed.
- Receives resource usage data via **Kafka** and sends it back to users in real-time through **WebSocket**.
- Implements a **Webhook** server using **Flask** to trigger deployments when users push new code.

## Architecture

![Architecture](https://gitmazon.s3.ap-northeast-1.amazonaws.com/%E6%88%AA%E5%9C%96+2024-10-22+%E5%87%8C%E6%99%A81.34.57.png)

## Demo Video

[![GitMazonDemo](https://gitmazon.s3.ap-northeast-1.amazonaws.com/%E6%88%AA%E5%9C%96+2024-10-22+%E4%B8%8B%E5%8D%881.10.01.png)](https://www.youtube.com/watch?v=x0KQoQbaO60)

## Related Repository

- [GitMazonMainService](https://github.com/b22790188/GitMazonMainService) - Provides a frontend interface and real-time SSH connection functionality.

- [GitMazonKafkaConsumer](https://github.com/b22790188/GitMazonKafkaConsumerServer) - Receives real-time CPU and RAM data from Kafka and sends this information back in real-time via WebSocket.

- [GitMazonImageProducer](https://github.com/b22790188/GitMazonImageProducer) - Builds Docker images for users' repositories and notifies the corresponding worker node to start the service.

- [GitMazonWorkerNode](https://github.com/b22790188/GitMazonWorkerNode) - Starts containers for users and continuously streams both the container data and its own data to Kafka.
