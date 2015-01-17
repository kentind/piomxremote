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

Download the folder "SERVER"

Place "youtube" and "dbuscontrol.sh" in /usr/bin/ and give execute permission:
```
    chmod + x /usr/bin/youtube
    chmod + x /usr/bin/dbuscontrol.sh
```

Place PORC folder in /home/pi/ 

You need to install youtube-dl:
```
    sudo apt-get install youtube-dl
```
For comfort;) (but inevitably required) :
```
    sudo apt-get install xterm
```
Create a PLAYLIST directory in /home/pi :
```
    mkdir /home/pi/PLAYLIST
```
This is where the playlist will be saved. (If you use the "save" in the playlist) This can be configured in remoteServerPlaylist.py file if you like BTW.

You can now run PORC :
```
    sudo /home/pi/PORC/bin/remoteServer.py
```

*OPTIONAL :*

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;For a welcome sound : `mkdir /home/pi/PORC/WelcomeSound/` Place any music song here.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;For a ring on incoming call : `mkdir /home/pi/PORC/RING/` Place a file named "ring.mp3" here.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;To run PORC server on start :

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;add the line : `@xterm -e sudo /home/pi/PORC/bin/remoteServer.py` 

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;at the end of this file : `sudo nano /etc/xdg/lxsession/LXDE/autostart`



Installing the client (android app):
------------------------------------

Go on the PlayStore: https://play.google.com/store/apps/details?id=wtf.omxclient

First use:
----------
Fill in the IP raspberry and server port "remoteServer.py" To control the connection is ok go to the remote if the "PORC" icon is pink it works if it is grey it does work :/ (you can press the icon to update the status) Try the long press on the different list (file list and queue) to display other actions.

you can try long press on the shutdown button for reboot.

The "cancel" option in shutdown box mean "cancel a previously programmed shutdown"  

Other tips :
----------
It can read ".sh" and ".tv" file if it contains "omxplayer".
For example :
```
    #!/bin/sh 
    xterm -bg black -fg black -fullscreen -e omxplayer -o hdmi "rtsp://192.168.0.45/fbxdvb/stream?tsid=1&nid=8442&sid=257&frontend=1"
    
    [...] Some other stuff
```
(only) the entire line that contains "omxplayer" will be execute.

You can use it to store your favourite TV chanel. 

Enjoy;)

Thank you mrhobbeys for the translation.
