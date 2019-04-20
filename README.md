# challenge

CO2 consumption measurer service implemented with Lagom framework.

In order to run the service type **sbt runAll** command.

Things that are good to know:
 - The project runs Cassandra database embedded itself, so you don't need to run any server.
 - You should send your measurement data unique per id/time to get correct metrics.
