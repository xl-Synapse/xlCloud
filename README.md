# xlCloud

## Introduction
A cloud disk server developed through java which includes,
- **SpringBoot**: Help developers build web efficiently.
- **Mybatis**: To efficiently manage the mapping between databases and entity classes.
- **Redis**: To save the token and necessary cache.
- **Mysql**: Currently, only user data is stored.

## Preparation
For deployment, you need to install the following dependencies.
- **Jdk** >= 1.8
- **Mysql** >= 8.0.31 for Linux
- **Redis** >= 7.0.5

## Deployment
When you complete the dependent installation and the corresponding database configuration, you can start the server with a single command.
```shell
java -jar xlCloud.jar
```

## Start
Note that you need to use the corresponding client like **xlCloudWeb** to access the server.