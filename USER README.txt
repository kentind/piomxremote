Voici une petite appli pour caster du youtube sur un raspberry.

Pr� requis :
1  raspebrry avec X11 !! 
1 T�l�phone android avec une connexion wifi sur le m�me r�seau que le pi tant qu'� faire !!
Un peu de patience bien sur !!


le fichier "youtube" il faut le mettre dans /usr/bin/ et lui donner les droit d�ex�cution (chmod +x  /usr/bin/youtube)


Il faut installer quelques paquets sur le pi :

Pour connaitre la dur�e des morceaux (hors youtube) :
sudo apt-get install mp3-info 

Pour Streamer les morceau youtube :
sudo apt-get install youtub-dl

Pour simuler des appuie de touche : 
sudo apt-get install libxtst-dev


Je crois que c'est tout...

Sur le tel il y a 3 param�tre a renseigner :
 - l'IP du pi (je pense que �a ne te posera pas de probl�me)
 - Le port (il est d�fini dans le fichier remoteServer.py par defaut : 3237 )
 - Le dossier ( /home/pi par defaut ) c'est pas utile de le modifier.

il faut cr�er un dossier PLAYLIST dans /home/pi (bien respecter la casse)
C'est l� que seront enregistr�e les playlist. (si tu utilise la fonction "enregistrer" dans la playlist) C'est param�trable dans le fichier remoteServerPlaylist.py


Pour l'usage de l'appli c'est pas toujours hyper intuitif, tente toujours l'appuie long pour avoir d'autre fonction, tu peux utiliser les boutons volume physique pour monter ou baisser le son.

Enjoy !! :-)