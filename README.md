The goal of this project was to learn commons configuration to do some Spring Boot like overrides when it came to receiving properties in a non Spring environment (hence why I'm using commons config).  This POC shows an in memory POC of doing just that, the resolution order is

1. properties added at runtime
2. System properties (e.g. -DmyProperty=foo)
3. Environment variables (e.g. $JAVA_HOME)
4. Read from properties file on the classpath (System.getResourceAsStream("/myFile.properties")
