#!/bin/bash
java  -Xms256m -Xmx512m -Xss1m -XX:MaxMetaspaceSize=384m -XX:+CMSClassUnloadingEnabled -jar `dirname $0`/sbt-launch-0.13.9.jar "$@"
