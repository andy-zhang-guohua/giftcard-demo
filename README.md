# 2021-12-19
- 改造和包装
    - 参考以下文档改造项目使用 Java 8 (源语言和目标语言均使用 Java 8)
        - [Quick Start](https://github.com/AxonIQ/reference-guide/blob/master/getting-started/quick-start.md)
        - 目的 : 本地其他项目一直在使用 Java 8, 这个项目也统一改为 Java 8 方便IDE中统一调试
        - 命令行也做相应改造   
    - 汉化UI界面
    - 边阅读代码边增加理解注释
- 观察到的现象
    - Axon服务器，应用启动后，
        - 如果删除数据库表中的数据，或者删除数据库表，重启应用/Axon服务器都不会导致数据表中数据重建;
            - 也就是说此时Axon服务器上的事件不会在数据库上重放形成数据
        - 如果删掉整个数据库,重启应用会导致数据表重建，数据表中的数据也会重建;
            - 也就是说此时Axon服务器上的事件会在数据库上重放形成数据        

# 2021-12-18

- 基于 Axon 官网例子项目 Giftcard 研究理解 Axon DDD 工作方式
- Giftcard 官方例子应用项目基本情况
    - 基于 Java 11
    - Axon 框架版本 : 4.5.1
    - Spring Boot 版本 : 2.5.1
    - 前端页面使用了 Vaadin (8.2.0), 类似 GWT 技术，Java 语言实现前端页面和逻辑
    - Command,Query,Event,Response 等对象的定义使用了 kotlin (1.5.10)
    - 持久化机制定义使用了 Spring Data JPA + Hibernate
    - 数据库使用了 H2

- Axon应用启动需要 Axon 服务端
    - 下载地址 : https://axoniq.io/download  -- 登记一些基本信息之后即可下载
        - 下载标准版 (SE) : 4.5.9  (另有EE企业版收费)

- AxonIQ 团队提供了在线创建Axon项目结构的初始化工具 : https://start.axoniq.io/

- Axon 参考手册 : https://docs.axoniq.io/reference-guide/ 
    - 参考手册源代码 : https://github.com/AxonIQ/reference-guide

- 改造和包装
    - 本地额外安装 JDK 17 用以支持例子应用缺省的Java语言版本要求
    - 增加命令行分别启动 Giftcard应用 和 Axon服务端
        - Giftcard应用启动命令行 : startup.bat  (使用 Java 17)
        - Axon服务端启动命令行 : run-axon-server.bat  (使用 Java 17)
    - 数据库从 H2 切换到 MySQL
        - localhost:3306,axon_giftcard, root/Passw0rd,utf-8

- 启动
    1. 启动 Axon Server : run-axon-server.bat
        - 访问位置 : http://localhost:8024/
    2. 启动 Giftcard 应用 : startup.bat
        - 访问位置 : http://localhost:8080/
        
- 创建根管理员用户 Axon Server
    1. root/Passw0rd, 管理员
    2. andy/andy123, 非管理员
    

Getting started with Axon
=========================

This Axon Framework demo application focuses around a simple giftcard domain, designed to show various aspects of the framework. The app can be run in various modes, using [Spring-boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-profiles.html): by selecting a specific profile, only the corresponding parts of the app will be active. Select none, and the default behaviour is activated, which activates everything. This way you can experiment with Axon in a (structured) monolith as well as in micro-services.

Where to find more information:
-------------------------------

* The [Axon Reference Guide](https://docs.axoniq.io/reference-guide/) is definitive guide on the Axon Framework and Axon Server.
* Visit [www.axoniq.io](https://www.axoniq.io) to find out about AxonIQ, the team behind the Axon Framework and Server.
* Subscribe to the [AxonIQ Youtube channel](https://www.youtube.com/AxonIQ) to get the latest Webinars, announcements, and customer stories.
* The latest version of the Giftcard App can be found [on GitHub](https://github.com/AxonIQ/giftcard-demo).
* Docker images for Axon Server are pushed to [Docker Hub](https://hub.docker.com/u/axoniq).

The Giftcard app
----------------

### Background story
See [the wikipedia article](https://en.wikipedia.org/wiki/Gift_card) for a basic definition of gift cards. Essentially, there are just two events in the life cycle of a gift card:
* They get _issued_: a new gift card gets created with some amount of money stored.
* They get _redeemed_: all or part of the monetary value stored on the gift card is used to purchase something.

### Structure of the App
The Giftcard application is split into four parts, using four sub-packages of `io.axoniq.demo.giftcard`:
* The `api` package contains the ([Kotlin](https://kotlinlang.org/)) sourcecode of the messages and entity. They form the API (sic) of the application.
* The `command` package contains the GiftCard Aggregate class, with all command- and associated eventsourcing handlers.
* The `query` package provides the query handlers, with their associated event handlers.
* The `gui` package contains the [Vaadin](https://vaadin.com/)-based Web GUI.

Of these packages, `command`, `query`, and `gui` are also configured as profiles.

### Building the Giftcard app from the sources
To build the demo app, simply run the provided [Maven wrapper](https://www.baeldung.com/maven-wrapper):

```
mvnw clean package
```
Note that for Mac OSX or Linux you probably have to add "`./`" in front of `mvnw`.

Running the Giftcard app
------------------------

The simplest way to run the app is by using the Spring-boot maven plugin:

```
mvnw spring-boot:run
```
However, if you have copied the jar file `giftcard-demo-1.0.jar` from the Maven `target` directory to some other location, you can also start it with:

```
java -jar giftcard-demo-1.0.jar
```
The Web GUI can be found at [`http://localhost:8080`](http://localhost:8080).

If you want to activate only the `command` profile, use:

```
java -Dspring.profiles.active=command giftcard-demo-1.0.jar
```
Idem for `query` and `gui`.

### Running the Giftcard app as micro-services

To run the Giftcard app as if it were three seperate micro-services, use the Spring-boot `spring.profiles.active` option as follows:

```
$ java -Dspring.profiles.active=command -jar giftcard-demo-1.0.jar
```
This will start only the command part. To complete the app, open two other command shells, and start one with profile `query`, and the last one with `gui`. Again you can open the Web GUI at [`http://localhost:8080`](http://localhost:8080). The three parts of the application work together through the running instance of the Axon Server, which distributes the Commands, Queries, and Events.

Running Axon Server
-------------------

By default the Axon Framework is configured to expect a running Axon Server instance, and it will complain if the server is not found. To run Axon Server, you'll need a Java runtime (JRE versions 8 through 10 are currently supported, Java 11 still has Spring-boot related growing-pains).  A copy of the server JAR file has been provided in the demo package. You can run it locally, in a Docker container (including Kubernetes or even Mini-kube), or on a separate server.

### Running Axon Server locally

To run Axon Server locally, all you need to do is put the server JAR file in the directory where you want it to live, and start it using:

```
java -jar axonserver-4.1-6.jar
```

You will see that it creates a subdirectory `data` where it will store its information.

### Running Axon Server in a Docker container

To run Axon Server in Docker you can use the image provided on Docker Hub:

```
$ docker run -d --name my-axon-server -p 8024:8024 -p 8124:8124 axoniq/axonserver
...some container id...
$
```

*WARNING* This is not a supported image for production purposes. Please use with caution.

If you want to run the clients in Docker containers as well, and are not using something like Kubernetes, use the "`--hostname`" option of the `docker` command to set a useful name like "axonserver", and pass the `AXONSERVER_HOSTNAME` environment variable to adjust the properties accordingly:

```
$ docker run -d --name my-axon-server -p 8024:8024 -p 8124:8124 --hostname axonserver -e AXONSERVER_HOSTNAME=axonserver axoniq/axonserver
```

When you start the client containers, you can now use "`--link axonserver`" to provide them with the correct DNS entry. The Axon Server-connector looks at the "`axon.axonserver.servers`" property to determine where Axon Server lives, so don't forget to set it to "`axonserver`".

### Running Axon Server in Kubernetes and Mini-Kube

*WARNING*: Although you can get a pretty functional cluster running locally using Mini-Kube, you can run into trouble when you want to let it serve clients outside of the cluster. Mini-Kube can provide access to HTTP servers running in the cluster, for other protocols you have to run a special protocol-agnostic proxy like you can with "`kubectl port-forward` _&lt;pod-name&gt;_ _&lt;port-number&gt;_". For non-development scenarios, we don't recommend using Mini-Kube.

Deployment requires the use of a YAML descriptor, an working example of which can be found in the "`kubernetes`" directory. To run it, use the following commands in a separate window:

```
$ kubectl apply -f kubernetes/axonserver.yaml
statefulset.apps "axonserver" created
service "axonserver-gui" created
service "axonserver" created
$ kubectl port-forward axonserver-0 8124
Forwarding from 127.0.0.1:8124 -> 8124
Forwarding from [::1]:8124 -> 8124
```

You can now run the Giftcard app, which will connect throught the proxied gRPC port. To see the Axon Server Web GUI, use "`minikube service --url axonserver-gui`" to obtain the URL for your browser. Actually, if you leave out the "`--url`", minikube will open the the GUI in your default browser for you.

To clean up the deployment, use:

```
$ kubectl delete sts axonserver
statefulset.apps "axonserver" deleted
$ kubectl delete svc axonserver
service "axonserver" deleted
$ kubectl delete svc axonserver-gui
service "axonserver-gui" deleted
```

If you're using a 'real' Kubernetes cluster, you'll naturally not want to use "`localhost`" as hostname for Axon Server, so you need to add three lines to the container spec to specify the "`AXONSERVER_HOSTNAME`" setting:

```
...
      containers:
      - name: axonserver
        image: axoniq/axonserver
        imagePullPolicy: Always
        ports:
        - name: grpc
          containerPort: 8124
          protocol: TCP
        - name: gui
          containerPort: 8024
          protocol: TCP
        readinessProbe:
          httpGet:
            port: 8024
            path: /actuator/health
          initialDelaySeconds: 5
          periodSeconds: 5
          timeoutSeconds: 1
        env:
        - name: AXONSERVER_HOSTNAME
          value: axonserver
---
apiVersion: v1
kind: Service
...
```

Use "`axonserver`" (as that is the name of the Kubernetes service) if you're going to deploy the client next to the server in the cluster, which is what you'ld probably want. Running the client outside the cluster, with Axon Server *inside*, entails extra work to enable and secure this, and is definitely beyond the scope of this example.

Configuring Axon Server
-----------------------

Axon Server uses sensible defaults for all of its settings, so it will actually run fine without any further configuration. However, if you want to make some changes, below are the most common options.

### Environment variables for customizing the Docker image of Axon Server

The `axoniq/axonserver` image can be customized at start by using one of the following environment variables. If no default is mentioned, leaving the environement variable unspecified will not add a line to the properties file.

* `AXONSERVER_NAME`

    This is the name the Axon Server uses for itself.
* `AXONSERVER_HOSTNAME`

    This is the hostname Axon Server communicates to the client as its contact point. Default is "`localhost`", because Docker generates a random name that is not resolvable outside of the container.
* `AXONSERVER_DOMAIN`

    This is the domain Axon Server can suffix the hostname with.
* `AXONSERVER_HTTP_PORT`

    This is the port Axon Server uses for its Web GUI and REST API.
* `AXONSERVER_GRPC_PORT`

    This is the gRPC port used by clients to exchange data with the server.
* `AXONSERVER_TOKEN`

    Setting this will enable access control, which means the clients need to pass this token with each request.
* `AXONSERVER_EVENTSTORE`

    This is the directory used for storing the Events.
* `AXONSERVER_CONTROLDB`

    This is where Axon Server stores information of clients and what types of messages they are interested in.

### Axon Server configuration

There are a number of things you can finetune in the server configuration. You can do this using an "`axonserver.properties`" file. All settings have sensible defaults.

* `axoniq.axonserver.name`

    This is the name Axon Server uses for itself. The default is to use the hostname.
* `axoniq.axonserver.hostname`

    This is the hostname clients will use to connect to the server. Note that an IP address can be used if the name cannot be resolved through DNS. The default value is the actual hostname reported by the OS.
* `server.port`

    This is the port where Axon Server will listen for HTTP requests, by default `8024`.
* `axoniq.axonserver.port`

    This is the port where Axon Server will listen for gRPC requests, by default `8124`.
* `axoniq.axonserver.event.storage`

    This setting determines where event messages are stored, so make sure there is enough diskspace here. Losing this data means losing your Events-sourced Aggregates' state! Conversely, if you want a quick way to start from scratch, here's where to clean.
* `axoniq.axonserver.controldb-path`

    This setting determines where the message hub stores its information. Losing this data will affect Axon Server's ability to determine which applications are connected, and what types of messages they are interested in.
* `axoniq.axonserver.accesscontrol.enabled`

    Setting this to `true` will require clients to pass a token.
* `axoniq.axonserver.accesscontrol.token`

    This is the token used for access control.

### The Axon Server HTTP server

Axon Server provides two servers; one serving HTTP requests, the other gRPC. By default these use ports 8024 and 8124 respectively, but you can change these in the settings.

The HTTP server has in its root context a management Web GUI, a health indicator is available at `/actuator/health`, and the REST API at `/v1`. The API's Swagger endpoint finally, is available at `/swagger-ui.html`, and gives the documentation on the REST API.
