# challenge

CO2 consumption measurer service implemented with Lagom framework.

In order to run the service the **sbt runAll** command.

Things that good to know:
 - The project runs Cassandra embedded itself, so you don't need to run any database server.
 - You should send your measurement data unique per id/time to get correct metrics.