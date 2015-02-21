#!/usr/bin/python
from ctypes import *
from thread import *
import time
import subprocess
import os
import socket
import re
import shutil
import remoteServerPlaylist

def sendKeyCode(keyArg,conn):
    global soundLevel
    global SockPlaylist
    keyCode=str(keyArg.strip())
    print "**",keyCode,"**"
    code="o"
    if keyCode=='LEFT' :
    #ACTION_SEEK_BACK_SMALL
        code = 19
    elif keyCode=='RIGHT' :
    #ACTION_SEEK_FORWARD_SMALL
        code = 20
    elif keyCode=='DOWN' :
    #ACTION_SEEK_BACK_LARGE
        code = 21
    elif keyCode=='UP' :
    # ACTION_SEEK_FORWARD_LARGE
        code = 22
    elif keyCode=='PLUS' :
    #ACTION_INCREASE_VOLUME
        code = 18
        soundLevel+=300;
        remoteServerPlaylist.doAction("SETSOUNDLEVEL|"+str(soundLevel)+"\r\n")
        conn.sendall("SAY|Vol : "+str(soundLevel)+" done\r\n")
    elif keyCode=='MOINS' :
    #ACTION_DECREASE_VOLUME
        code = 17
        soundLevel-=300
        remoteServerPlaylist.doAction("SETSOUNDLEVEL|"+str(soundLevel)+"\r\n")
        conn.sendall("SAY|Vol : "+str(soundLevel)+" done\r\n")
    elif keyCode=='SPACE' :
    #ACTION_PAUSE
        code = 16
    elif keyCode=='q' :
    #ACTION_EXIT
        code = 15
    try :
      s=subprocess.Popen("dbuscontrol.sh action "+str(code), shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
      for line in s.stdout.readlines():
         print str(line)
      remoteServerPlaylist.doAction("FORCEREFRESH|ALL\r\n")
    except :
       print "DBus ErrOr" 

def SendShutDown(argument) :
        arg=str(argument.strip())
        if arg=='NOW' :
           cde="shutdown now -h";
        elif arg=='CANCEL' :
           cde="shutdown -c"; 
        elif arg=='REBOOT' :
           cde="shutdown now -r"; 
        else :
           cde="shutdown +"+arg+" -h";
        subprocess.Popen(cde,  shell=True)

def SendKill() :
        subprocess.Popen("killall youtube",  shell=True)
        subprocess.Popen("killall omxplayer",  shell=True)
		
def lisdirectory(lePath) :
  f=[]
  dossier=[]
  fichier=[]
  if os.path.isdir(lePath) :
    for filename in os.listdir(lePath):
      if filename[0]!="." :
          if (os.path.isfile(lePath+"/"+filename)) :
              ext = filename.split(".")
              if(len(ext)>1 and (
              ext[-1].upper()=="AVI" or ext[-1].upper()=="MPEG" or  ext[-1].upper()=="MKV" or ext[-1].upper()=="MPG" or ext[-1].upper()=="MP3" or ext[-1].upper()=="MP4" or ext[-1].upper()=="FLAC" )) :
                 typeF='AVI'
              elif (len(ext)>1 and (ext[-1].upper()=="TV" or ext[-1].upper()=="SH")) : 
                 typeF='TV'
              elif  (len(ext)>1 and (ext[-1].upper()=="PLAY")) :
                 typeF='PL'
              else : 
                 typeF='FIL'
              fichier.append(typeF+'|'+ lePath+"/"+filename+"|"+filename[0].upper()+""+filename[1:])
          else : dossier.append('DIR|'+ lePath+"/"+filename+"|"+filename[0].upper()+""+filename[1:])
  else : dossier.append("ERR|Click to modify curent dir|DOSSIER INEXISANT : "+ lePath)
  dossier.sort(key=lambda y: y.lower())
  fichier.sort(key=lambda y: y.lower())        
  f.append(dossier)
  f.append(fichier)  
  return f

def deleteFile(leFile) :
 if os.path.isfile(leFile) :
    os.remove(leFile)

def readsh(leFile) :
 SendKill()
 p = re.compile('.*omxplayer.*')
 fichier = open(leFile, "r")
 for ligne in fichier:
   if p.match(ligne):
      print ligne
      subprocess.Popen(ligne,  shell=True)
 fichier.close()

def readFile_single(leFile,lesParam) :
 SendKill()
 ligne = 'xterm -bg black  -fg black -fullscreen -e omxplayer '+lesParam+' "'+leFile+'"'
 print ligne
 subprocess.Popen(ligne,  shell=True)

def readYoutube_single(leFile) :
 SendKill()
 ligne = 'xterm -bg black  -fg black -fullscreen -e youtube "'+leFile+'"'
 print ligne
 subprocess.Popen(ligne,  shell=True)
 
def doRing(ringFile) :
 if (os.path.isfile(ringFile)) :
    ligne = 'omxplayer -o both --vol '+str(soundLevel+100)+' "'+ringFile+'"'
 print ligne
 subprocess.Popen(ligne,  shell=True)
      
 

#Function for handling connections. This will be used to create threads
def clientthread(conn):
    #Sending message to connected client
    #conn.send('Welcome to the server. Type something and hit enter\n') #send only takes string
    conn.sendall('SAY|Welcome\r\n')
    
    #infinite loop so that function do not terminate and thread do not end.
    while True:
        RequeteDuClient = conn.recv(99999) # on recoit 255 caracteres grand max
        if len(RequeteDuClient)>0 :
              print RequeteDuClient         # affiche les donnees envoyees
              param = RequeteDuClient.split("|")
              if param[0]=='key' :
                   sendKeyCode(param[1].strip(),conn)
              elif param[0]=='shutdown' :
                   SendShutDown(param[1].strip())
              elif param[0]=='ringing' :
                   doRing("/home/pi/PORC/RING/ring.mp3")
              elif param[0]=='readsh' :
                   readsh(param[1].strip())
              elif param[0]=='readfile' :
                   readFile_single(param[1].strip(),param[2].strip())
              elif param[0]=='readall' :
                   remoteServerPlaylist.doAction("PLAYALL|"+param[1].strip()+'|'+param[2].strip()+"\r\n")
              elif param[0]=='kill' :
                   SendKill()
              elif param[0]=='youtube' :
                   readYoutube_single(param[1].strip())
              elif param[0]=='ADDPLAYLIST' :
                   remoteServerPlaylist.doAction("ADD|"+param[1].strip()+'|'+param[2].strip()+"\r\n")
              elif param[0]=='REMOVEPLAYLIST' :
                   remoteServerPlaylist.doAction("REMOVE|"+param[1].strip()+"\r\n")
                   if param[1].strip()=='ALL' :
                      SendKill()
              elif param[0]=='GOTOPLAYLIST' :
                   SendKill()
                   remoteServerPlaylist.doAction("GOTO|"+param[1].strip()+"\r\n")
                   #sendKeyCode("q")
              elif param[0]=='DELETEFILE' :
                   deleteFile(param[1].strip())
                   #sendKeyCode("q")
              elif param[0]=='SETPARAMPLAYLIST' :
                   remoteServerPlaylist.doAction("SETPARAM|"+param[1].strip()+"|"+param[2].strip()+"|"+param[3].strip()+"\r\n")
              elif param[0]=='SAVEPLAYLIST' :
                   remoteServerPlaylist.doAction("SAVEPLAYLIST|"+param[1].strip().replace("/", ".")+"\r\n")
              elif param[0]=='list' :
                   list0= lisdirectory(param[1].strip())
                   conn.sendall('STARTFILELIST|'+param[1].strip()+'\r\n')
                   for i in range(len(list0)):
                      for y in range(len(list0[i])):
                        conn.sendall("FILE|"+list0[i][y]+"\r\n")
                   conn.sendall('ENDFILELIST\r\n')
#        conn.sendall('SAY|'+param[0]+' done\r\n')
        else :
           break
    #came out of loop
    conn.close()


Sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
Host = '0.0.0.0' # l'ip locale de l'ordinateur
Port = 3237         # choix d'un port

# on bind notre socket :
Sock.bind((Host,Port))

#sound level by defaut :
soundLevel=-2000
 
# On est a l'ecoute d'une seule et unique connexion :
Sock.listen(10)

#Welcome sound :
try :
   if os.path.isdir("/home/pi/PORC/WelcomeSound/") :
      remoteServerPlaylist.doAction("PLAYALL|/home/pi/PORC/WelcomeSound/|-o both\r\n")
except Exception, e:
   print e
   
while 1:
   # Le script se stoppe ici jusqu'a ce qu'il y ait connexion :
   client, adresse = Sock.accept() # accepte les connexions de l'exterieur
   print "L'adresse",adresse,"vient de se connecter au serveur !"
   remoteServerPlaylist.doAction('ADDCLIENT|'+adresse[0]+"\r\n")
   start_new_thread(clientthread ,(client,))
 
Sock.close()
 
        
        






