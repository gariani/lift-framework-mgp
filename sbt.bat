set SCRIPT_DIR=%~dp0
java -XX:+CMSClassUnloadingEnabled -Xss2M -jar "%SCRIPT_DIR%\sbt-launch-0.13.9.jar" %*
