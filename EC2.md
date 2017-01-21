### How to Connect ?

    ssh -i "GatorKnuckle.pem" ubuntu@ec2-54-191-106-184.us-west-2.compute.amazonaws.com

### How to RUN ?

Run the tunnel : 

    beame-insta-ssl tunnel 9070 http
    
[Optional] Remote debug
    
    export MAVEN_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
    
Run the java project : 
      
    mvn jetty:run 


### Details :

Bot hook : https://ec2-54-191-106-184.us-west-2.compute.amazonaws.com:8443/bot/webhook

OUR CA Certificate url.

[2017-01-21 09:08:36] [BeameStore] INFO: Credential metadata for jej7wf6rlqa7t1wl.v1.p.beameio.net updated successfully...

Certificate created! Certificate FQDN is jej7wf6rlqa7t1wl.v1.p.beameio.net

Congratulations!
