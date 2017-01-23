# Inspiration

The motivation for us was to develop something that could be accessible easily by anyone and can plan and assist in the best possible way to spend a few hours of one's time. When it comes to something that can be easily accessible to anyone the first thing that came across our mind was a personal assistant or a chatbot.

# What & How.

## What it does
This FB messenger chatbot interacts with the user whenever it is prompted to by the user and asks for the user for his current location. Based on this it gives a suggestion of 10 possible places to visit based on the ratings of the places. To add a personal touch the bot uses the information of the user's friends who visited and liked these places in order provide a concise list of places matching the user's tastes and interests. These could restaurants or sight-seeing places, etc. It shows the places and the user can select what to be on the list and what not and based on this final list it generates a google map showing the optimal route and order in which the places need to be visited enhancing the user's experience.

## Flow of the Bot:

The user finds goes to our [Bot page](https://www.facebook.com/gatorknuckle/) and says hi to our bot. Bot will ask for location and user sends the location.

| Hello World | Get Location |
| --- | --- | --- |
| ![User Login](/screenshots/howdy.jpg)  | ![Restaurant Search](/screenshots/location.jpg) |

Bot receives the location and calls Google places API and crunches few locations to make a best recommendation for the user. 

| Recommendation 1 | Recommendation 2 | ... | Recommendation n |
| --- | --- | --- |--- |
| ![Recommendation 1](/screenshots/reco1.jpg)  | ![Recommendation 2](/screenshots/reco2.jpg) | ... |  ![Recommendation n](/screenshots/recon.jpg) |

After user accepts a certain number of locations, a summary is displayed to the user. Route Map shows the optimal route to cover all the destinations.

| Summary 1 | Summary 2 | Route Map
| --- | --- | --- |
| ![Summary 1](/screenshots/summ1.jpg)  | ![Summary 2](/screenshots/summ2.jpg) | ![Route](/screenshots/map.jpg) | 


## How we built it
We used various APIs made available by Facebook and Google. We built the bot using Java, Javascript, JSON and RESTful APIs, viz., google-maps-api, google-places-api, google-directions-api and facebook-graph-api.

### Challenges we ran into
The main challenge was to make the interaction with bot friendly and enhance the user experience with images and human-like interaction. Other challenges were to figure out what APIs to use, how to use them, hosting it on AWS and integrating all these things with the bot.

### Accomplishments that we're proud of
The fact that realizing this idea and having working prototype is something we take pride in. Other personal accomplishments include learning to use APIs available out there to build something useful and completing the task with a great team spirit.

### What we learned
Using the various APIs, learning their functionality and working by reading the documentations and integrating them with already known technologies like Java, JSON and Javascript.

### What's next ?
The bot is still just a prototype and as of now it can deal with only one type of input data like restaurants or tourist places, etc. We would like to expand its capabilities so that it can take various types of input types and maybe plan a whole vacation for the user.

## Citations

Some of the code snippets were taken from [Java-FbChatBot](https://github.com/thekosmix/Java-FbChatBot). Thanks [Siddharth Kumar](https://github.com/thekosmix)

# API GUIDE

We have used APIs provided by Google for location services. We have used Facebook APIs for interactive bot chatting.

## Google APIs

Below APIs are used.

### [Radar Search API](https://developers.google.com/places/web-service/search#RadarSearchRequests)

The Google Places API Radar Search Service allows you to search for up to 200 places at once, but with less detail than is typically returned from a Text Search or Nearby Search request. With Radar Search, you can create applications that help users identify specific areas of interest within a geographic area.

### [Place Details API](https://developers.google.com/places/web-service/details)

After the place id is fetched, this API is used to get detailed information about the place. A Place Details request returns more comprehensive information about the indicated place such as its complete address, phone number, user rating and reviews.

### [Travelling Salesman Optimization](https://developers.google.com/optimization/routing/tsp)

To find the best route connection the all finalized locations, this API is used. 

### [Maps API]

This is simply used to plot all the latitudes and longitudes on Google Maps and show the visualization to the user.

## Facebook APIs

### [Quick Reply](https://developers.facebook.com/docs/messenger-platform/send-api-reference/quick-replies)

This is used initially to fetch the location of the user.

### [Sender Actions](https://developers.facebook.com/docs/messenger-platform/send-api-reference/sender-actions)

This is used to set status such as Read, Typing etc.. inside Facebook Messenger.

### [Send API - Generic Template](https://developers.facebook.com/docs/messenger-platform/send-api-reference/generic-template)

To show detailed info of each place.

### [Send API - List Template](https://developers.facebook.com/docs/messenger-platform/send-api-reference/list-template)

To show summary view of selected places.

## Owners
* [Manoj Battula](https://github.com/ManojNVOB)
* [Prashanth Peddabbu](https://github.com/ppeddabbu)
* [Sai Vishnu Teja Vempali](https://github.com/saivempali)
* [Tarun Gupta Akirala](https://github.com/takirala)

Write to us for any feedback or queries.
