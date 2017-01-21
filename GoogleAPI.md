
###TODO
- Get google API key.
AIzaSyCXWBHckGfNWwlrymhKdU5VuPkMWaVwbmg **SECRET**

**Place Search API.**
https://developers.google.com/places/web-service/search

 I/p : 
 
  - lat,long
  - radius
  - rankby (distance, keyword, name, type)

  **Optional**
  - type (e.g: art_gallery, bowling_alley, restaurant ) https://developers.google.com/places/web-service/supported_types
 
 ## OR DO a radar search.
 
 Radar search gives only a list of placeIds  - (but type may be sacrificed?) .
 
 ** Place details API **
 https://developers.google.com/places/web-service/details
 
    opening_hours
     open_now
     periods[]
 
 **Distance Matrix API**
 I/p:
  - locations from Place Search API
 O/p: 
  - route, time, distance for given points

 

