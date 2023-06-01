from confluent_kafka import Producer
import threading
import argparse
import time

# Arguments:
# -t : number of threads (int)
# -n : number of messages (int)
# -m : mode 'sync', 'async' or 'forget' (string)
# print the time of all threads producers computing 


parser = argparse.ArgumentParser()

parser.add_argument('-t','--th', )
parser.add_argument('-n','--nbrmsg', )
parser.add_argument('-m','--mode', )


args = parser.parse_args()

if args.th:
    th = int(args.th)
else :
    th = 1

if args.nbrmsg:
    nbrmsg = int(args.nbrmsg)
else :
    nbrmsg=1

if args.mode:
    mode = int(args.mode)
else : 
    mode='sync'

def acked(err, msg):
    if err is not None:
        print("Failed to deliver message: %s: %s" % (str(msg), str(err)))
    else:
        print(f"Message produced: %s" %  (str(msg.value())))

conf = {'bootstrap.servers': 'localhost:19092,localhost:19093'}


def produce(nb_msg=1, mode='async'):
    
    for i in range(0,nb_msg):

        producer = Producer(conf)
        
        i = str(i)

        if mode == 'forget' :        
            producer.produce("SomeTopic", key="key1"+i, value="Hello"+i) 
        else : 
            producer.produce("SomeTopic", key="key1"+i, value="Hello"+i, callback=acked) 


        if mode == 'sync':
            producer.poll(1) 
            producer.flush()
        else:
            producer.poll(1) 


if __name__ == '__main__':
    
    threads = list()
    
    start = time.time()
    for index in range(0,th):
        print("Main    : create and start thread %d.", index)
        
        x = threading.Thread(target=produce, args=(nbrmsg, mode,))
        threads.append(x)
        x.start()
    
    for x in threads:
     x.join()
    
    end = time.time()
    delay = end - start 
    
    print('time:',  delay, 's')
    
        

    
    