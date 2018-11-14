#!/bin/bash
mvn clean package -DskipTests
mkdir -p bin && cp target/multi-filedownloader-1.0-SNAPSHOT.jar bin/multi-filedownloader.jar
