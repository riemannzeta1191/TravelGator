### How to Connect ?

Update hostname accordingly.

    ssh ubuntu@ec2-54-191-106-184.us-west-2.compute.amazonaws.com


### How to RUN ?

Run the tunnel (to avoid self signed certificate): 

    beame-insta-ssl tunnel 9070 http
    
[Optional] Remote debug
    
    export MAVEN_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
    
Run the java project : 
      
    mvn jetty:run 

### Details :

Bot webhook : https://ec2-54-191-106-184.us-west-2.compute.amazonaws.com:8443/bot/webhook
