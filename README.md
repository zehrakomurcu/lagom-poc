# challenge

CO2 consumption measurer service implemented with Lagom framework.

In order to run the service, please type **sbt runAll** command. 
The service will be running at localhost:9000

Things that are good to know:
 - The project runs Cassandra database embedded itself, so you don't need to run any server.
 - You should send your measurement data unique per id/time to get correct metrics.
