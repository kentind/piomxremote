###Server Side
- send the needed video quality to youtube script
- Send "status" : is pause or not.... and transfer new value when status change.
- secure transfer : send an ID² (time python when first line was send ?).
- change pipe sperator to JSon for transfert  

###Android APP :
- add parametre "youtube video quality" in Setting activity
- transfert the quality needed for youtube video 
- create a separate class for manage all connection (instead of a subclass in each class activity) 
- stop progression if status is pause
- use the ID² 
- make a little remote in bottom on each activity
- show the progress bar on each activity (inside little remote). Use this progress bar to forward.

###In a perfect world :
- share all video/music url and not only youtube url
- make an auto-updater for the server side.
- check in app and server if the server and app vsersion are compatible