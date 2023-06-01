from confluent_kafka import Consumer, KafkaException , KafkaError

conf = {'bootstrap.servers': 'localhost:19092,localhost:19093', 
        'group.id': 'group_test_1',
        'session.timeout.ms': 6000,
        'auto.offset.reset': 'earliest'}

consumer = Consumer(conf)

consumer.subscribe(['SomeTopic'])

consumer.poll(1)

while True:
    msg = consumer.poll(timeout=1.0)
    if msg is None:
        print('no more message')
        continue
    if msg.error():
        raise KafkaException(msg.error())
    else:
        # Proper message
        print('%% %s [%d] at offset %d with key %s:\n' %
                        (msg.topic(), msg.partition(), msg.offset(),
                        str(msg.key())))
        print(msg.value())


