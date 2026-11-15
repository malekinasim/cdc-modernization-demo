ARG CONFLUENT_VERSION=8.4.0
FROM confluentinc/cp-kafka-connect:${CONFLUENT_VERSION}

RUN confluent-hub install --no-prompt debezium/debezium-connector-postgresql:3.1.2