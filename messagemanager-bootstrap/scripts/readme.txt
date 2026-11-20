MESSAGE MANAGER

The scripts in this folder will be replaced on each update. You can add JVM options by creating a setenv.cmd/sh file.

Please use launch.cmd/sh to start message manager.

JAVA:

Message Manager downloads its own current Java after its first starts (about 100 MB) and switches to it once it has
passed its checks. Until then, and whenever it cannot be used, it runs on the Java it found before, which must be
Java 17 or later. To keep another Java, set LAUNCHER_JAVA in setenv.cmd/sh (see setenv.*.template).

LAUNCHER ONLY:

In case you only downloaded the launcher it is important to have a Java 17 JRE/JDK set as JAVA_HOME.
Or you have to rename setenv.*.template to setenv.cmd/sh and set the path to the JVM there
The remaining JAR files will be downloaded automatically.