#!/bin/sh

java -jar -D@appId=pdxazure -D@environment=local $* -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 build/libs/*.jar