#TOPIC=person
TOPIC=person-by-race
ZOOKEEPER=localhost:9092
KAFKA=localhost:9092

up:
	brew services start kafka

topic:
	kafka-topics --create --topic $(TOPIC) --partitions 1 --replication-factor 1 --if-not-exists --bootstrap-server $(ZOOKEEPER)

describe:
	kafka-topics --describe --topic $(TOPIC) --bootstrap-server $(ZOOKEEPER)

offset:
	kafka-run-class kafka.tools.GetOffsetShell --broker-list $(KAFKA) --topic $(TOPIC) --time -1

dump:
	kafka-console-consumer --bootstrap-server $(KAFKA) --topic $(TOPIC) --from-beginning --max-messages 100

delete:
	kafka-topics --bootstrap-server $(KAFKA) --delete --topic $(TOPIC)

kv:
	kafka-console-consumer --bootstrap-server $(KAFKA) --topic $(TOPIC) --property print.key=true --property key.separator="-" --from-beginning

redis:
	ZRANGE redisson__timeout__set:{expiredEventQueue} 0 10

rabbit-purge:
	 rabbitmqctl purge_queue spark-aggregate-event-hourly

rabbit-list:
	rabbitmqctl list_queues