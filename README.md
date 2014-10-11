piomxremote
===========

A powerfull android remote for omx player on raspberry pi

##Beginners :
#####(one step install) 


Download the plug and play image : http://www.kentind.ovh/


##Advanced users :

###Prerequisites:

1 Raspberry Pi
1 android phone with wifi on the same network as the Raspberry Pi

###Server Installation (Raspberry Pi):

Download the 4 files in the folder "SERVER"

Place "youtube" and "dbuscontrol.sh" in /usr/bin/ and give execute permission:
```
    chmod + x /usr/bin/youtube
    chmod + x /usr/bin/dbuscontrol.sh
```

Place py files wherever you want. 
It is always important to run "remoteServerPlaylist.py" BEFORE "remoteServer.py."

You need to install youtube-dl:
```
    sudo apt-get install youtube-dl
```
For comfort;) :
```
    sudo apt-get install xterm
```
Create a PLAYLIST directory in /home/pi :
```
    mkdir /home/pi/PLAYLIST
```
This is where the playlist will be saved. (If you use the "save" in the playlist) This can be configured in remoteServerPlaylist.py file if you like BTW.

Installing the client (android app):
------------------------------------

Go on the PlayStore: https://play.google.com/store/apps/details?id=wtf.omxclient

First use:
----------
Fill in the IP raspberry and server port "remoteServer.py" To control the connection is ok go to the remote if the "ball" is green it works if it is red it does work: / (you can press the ball to update the status) Try the long press on the differente list (playlist and queues) to display other actions.
you can try long press on the shutdown button for reboot.

Other tips :
----------
It can read ".sh" and ".tv" file if it contains "omxplayer".
For example :
```
    #!/bin/sh <br>
    xterm -bg black -fg black -fullscreen -e omxplayer -o hdmi "rtsp://192.168.0.45/fbxdvb/stream?tsid=1&nid=8442&sid=257&frontend=1"
    
    [...] Some other stuff
```
(only) the entire line that contains "omxplayer" will be execute.
You can use it to store your favourite TV chanel. 
				
Enjoy;)

Thank you mrhobbeys for the translation.
