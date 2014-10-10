﻿piomxremote
===========

A powerfull android remote for omx player on raspberry pi

##Debutants :
#####(one step install) 


Telecharger une image plug and play pour votre carte sd  :
http://www.kentind.ovh/


##Utilisateurs avancé :

###Prerequis :


1 Raspberry !! 
1 Téléphone android avec une connexion wifi sur le même réseau que le pi tant qu'à faire !!

###Installation serveur :
Télècharger les 4 fichiers présents dans le dossier "SERVER
Placer les fichiers "youtube" et dbuscontrol.sh dans /usr/bin/
Leur donner les droits d’exécution :
```
	chmod +x  /usr/bin/youtube
	chmod +x  /usr/bin/dbuscontrol.sh
```
Placer les fichiers .py où bon vous semble, il en revanche important de toujours lancer "remoteServerPlaylist.py" AVANT "remoteServer.py".


Il faut installer youtube-dl sur le pi Pour Streamer les morceaux youtube :

```
	sudo apt-get install youtube-dl
```

Pour plus de confort ;) :
```
	sudo apt-get install xterm
```

il faut créer un dossier PLAYLIST dans /home/pi :
```
	mkdir /home/pi/PLAYLIST
```
C'est là que seront enregistré les playlist. (si tu utilise la fonction "enregistrer" dans la playlist) C'est paramétrable dans le fichier remoteServerPlaylist.py si tu préféres les mettre ailleur.

Installation du client (l'appli android):
-----------------------------------------
Rendez vous sur le PlayStore : https://play.google.com/store/apps/details?id=wtf.omxclient

First use :
-----------
Renseignez l'IP de votre raspberry et le port du serveur "remoteServer.py"
Pour controler que la connexion est ok allez sur la remote, si la "boule" est verte ca marche si elle est rouge ca ne marche :/ (vous pouvez appuyer sur la boule pour actualiser l'état)
Essayer les appuies long sur les differente liste (playliste et files) pour afficher d'autres actions.

Enjoy ;) 





 
