Voici une petite appli pour caster du youtube sur un raspberry.

Pré requis :
1  raspebrry avec X11 !! 
1 Téléphone android avec une connexion wifi sur le même réseau que le pi tant qu'à faire !!
Un peu de patience bien sur !!


le fichier "youtube" il faut le mettre dans /usr/bin/ et lui donner les droit d’exécution (chmod +x  /usr/bin/youtube)


Il faut installer quelques paquets sur le pi :

Pour connaitre la durée des morceaux (hors youtube) :
sudo apt-get install mp3-info 

Pour Streamer les morceau youtube :
sudo apt-get install youtub-dl

Pour simuler des appuie de touche : 
sudo apt-get install libxtst-dev


Je crois que c'est tout...

Sur le tel il y a 3 paramètre a renseigner :
 - l'IP du pi (je pense que ça ne te posera pas de problème)
 - Le port (il est défini dans le fichier remoteServer.py par defaut : 3237 )
 - Le dossier ( /home/pi par defaut ) c'est pas utile de le modifier.

il faut créer un dossier PLAYLIST dans /home/pi (bien respecter la casse)
C'est là que seront enregistrée les playlist. (si tu utilise la fonction "enregistrer" dans la playlist) C'est paramétrable dans le fichier remoteServerPlaylist.py


Pour l'usage de l'appli c'est pas toujours hyper intuitif, tente toujours l'appuie long pour avoir d'autre fonction, tu peux utiliser les boutons volume physique pour monter ou baisser le son.

Enjoy !! :-)