#!/usr/bin/env bash
mvn clean package
mkdir -p bin && cp target/multi-filedownloader-1.0-SNAPSHOT.jar bin/multi-filedownloader.jar