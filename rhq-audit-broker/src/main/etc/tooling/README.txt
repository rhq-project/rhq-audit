This is how to test the prototype code.

At the root node, first build everything:
    mvn clean install

Then:

rhq-audit-broker:
    mvn org.apache.activemq.tooling:maven-activemq-plugin:run

rhq-audit-producer:
    mvn exec:java -Dexec.mainClass=org.rhq.audit.producer.App

rhq-audit-consumer:
    mvn exec:java -Dexec.mainClass=org.rhq.audit.consumer.App

