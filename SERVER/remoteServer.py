#!/usr/bin/python
from ctypes import *
from thread import *
import time
import subprocess
import os
import socket
import re
import shutil

Xtst = CDLL("libXtst.so.6")
Xlib = CDLL("libX11.so.6")
dpy = Xtst.XOpenDisplay(0)

def sendKeyCode(keyArg):
    global soundLevel
    global SockPlaylist
    keyCode=str(keyArg.strip())
    print "**",keyCode,"**"
    code="o"
    if keyCode=='LEFT' :
        code = Xlib.XKeysymToKeycode(dpy, Xlib.XStringToKeysym("Left"))
    elif keyCode=='RIGHT' :
        code = Xlib.XKeysymToKeycode(dpy, Xlib.XStringToKeysym("Right"))
    elif keyCode=='DOWN' :
        code = Xlib.XKeysymToKeycode(dpy, Xlib.XStringToKeysym("Down"))
    elif keyCode=='UP' :
        code = Xlib.XKeysymToKeycode(dpy, Xlib.XStringToKeysym("Up"))
    elif keyCode=='PLUS' :
        Xtst.XTestFakeKeyEvent(dpy,Xlib.XKeysymToKeycode (dpy,  Xlib.XStringToKeysym("Shift_L")),True,0)
        code = Xlib.XKeysymToKeycode(dpy, Xlib.XStringToKeysym("plus"))
        soundLevel+=300;
        SockPlaylist.sendall("SETSOUNDLEVEL|"+str(soundLevel)+"\r\n")
    elif keyCode=='MOINS' :
        code = Xlib.XKeysymToKeycode(dpy, Xlib.XStringToKeysym("minus"))
        soundLevel-=300
        SockPlaylist.sendall("SETSOUNDLEVEL|"+str(soundLevel)+"\r\n")
    elif keyCode=='SPACE' :
        code = Xlib.XKeysymToKeycode(dpy, Xlib.XStringToKeysym("space"))
    else : 
        code = Xlib.XKeysymToKeycode(dpy, Xlib.XStringToKeysym(keyCode))
    print code
    Xtst.XTestFakeKeyEvent(dpy, code, True, 0)
    Xtst.XTestFakeKeyEvent(dpy, code, False, 0)
    if keyCode=='PLUS' :
        Xtst.XTestFakeKeyEvent(dpy,Xlib.XKeysymToKeycode (dpy,  Xlib.XStringToKeysym("Shift_L")),False,0)
    Xlib.XFlush(dpy)


def SendInput( txt ):
        for c in txt:
            sym = Xlib.XStringToKeysym(c)
            code = Xlib.XKeysymToKeycode(dpy, sym)
            Xtst.XTestFakeKeyEvent(dpy, code, True, 0)
            Xtst.XTestFakeKeyEvent(dpy, code, False, 0)
        Xlib.XFlush(dpy)

def SendKeyPress(key):
        sym = Xlib.XStringToKeysym(str(key))
        code = Xlib.XKeysymToKeycode(dpy, sym)
        Xtst.XTestFakeKeyEvent(dpy, code, True, 0)
        Xlib.XFlush(dpy)

def SendKeyRelease(key):
        sym = Xlib.XStringToKeysym(str(key))
        code = Xlib.XKeysymToKeycode(dpy, sym)
        Xtst.XTestFakeKeyEvent(dpy, code, False, 0)
        Xlib.XFlush(dpy)

def SendShutDown(argument) :
        arg=str(argument.strip())
        if arg=='NOW' :
           cde="shutdown now -h";
        elif arg=='CANCEL' :
           cde="shutdown -c"; 
        else :
           cde="shutdown +"+arg+" -h";
        subprocess.Popen(cde,  shell=True)

def SendKill() :
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
              if(len(ext)>1 and (ext[-1].upper()=="AVI" or ext[-1].upper()=="MPEG" or  ext[-1].upper()=="MKV" or ext[-1].upper()=="MPG" or ext[-1].upper()=="MP3")) :
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

def readsh(leFile) :
 SendKill()
 p = re.compile('.*omxplayer.*')
 fichier = open(leFile, "r")
 for ligne in fichier:
   if p.match(ligne):
      print ligne
      subprocess.Popen(ligne,  shell=True)
 fichier.close()

def readFile(leFile,lesParam) :
 SendKill()
 ligne = 'xterm -bg black -fullscreen -e omxplayer '+lesParam+' "'+leFile+'"'
 print ligne
 subprocess.Popen(ligne,  shell=True)

def readYoutube(leFile) :
 SendKill()
 ligne = 'xterm -bg black -fullscreen -e youtube "'+leFile+'"'
 print ligne
 subprocess.Popen(ligne,  shell=True)
 
#def readAll(lePath,lesParam) :
# if os.path.isdir(lePath) :
#    for filename in os.listdir(lePath):
#      if filename[0]!="." :
#          if (os.path.isfile(lePath+"/"+filename)) :
#              ext = filename.split(".")
#              if(len(ext)>1 and (ext[-1].upper()=="AVI" or ext[-1].upper()=="MPEG" or  ext[-1].upper()=="MKV" or #ext[-1].upper()=="MPG" or ext[-1].upper()=="MP3")) :
                 #SockPlaylist.sendall("ADD|"+lePath+"/"+filename+'|'+lesParam.strip()+"\r\n")
                  
 

#Function for handling connections. This will be used to create threads
def clientthread(conn):
    #Sending message to connected client
    #conn.send('Welcome to the server. Type something and hit enter\n') #send only takes string

    
    #infinite loop so that function do not terminate and thread do not end.
    while True:
        RequeteDuClient = conn.recv(99999) # on recoit 255 caracteres grand max
        if len(RequeteDuClient)>0 :
              print RequeteDuClient         # affiche les donnees envoyees
              param = RequeteDuClient.split("|")
              if param[0]=='key' :
                   sendKeyCode(param[1].strip())
              elif param[0]=='shutdown' :
                   SendShutDown(param[1].strip())
              elif param[0]=='readsh' :
                   readsh(param[1].strip())
              elif param[0]=='readfile' :
                   readFile(param[1].strip(),param[2].strip())
              elif param[0]=='readall' :
                   SockPlaylist.sendall("PLAYALL|"+param[1].strip()+'|'+param[2].strip()+"\r\n")
              elif param[0]=='kill' :
                   SendKill()
              elif param[0]=='youtube' :
                   readYoutube(param[1].strip())
              elif param[0]=='ADDPLAYLIST' :
                   SockPlaylist.sendall("ADD|"+param[1].strip()+'|'+param[2].strip()+"\r\n")
              elif param[0]=='REMOVEPLAYLIST' :
                   SockPlaylist.sendall("REMOVE|"+param[1].strip()+"\r\n")
              elif param[0]=='GOTOPLAYLIST' :
                   SockPlaylist.sendall("GOTO|"+param[1].strip()+"\r\n")
                   sendKeyCode("q")
              elif param[0]=='SETPARAMPLAYLIST' :
                   SockPlaylist.sendall("SETPARAM|"+param[1].strip()+"|"+param[2].strip()+"\r\n")
              elif param[0]=='SAVEPLAYLIST' :
                   SockPlaylist.sendall("SAVEPLAYLIST|"+param[1].strip().replace("/", ".")+"\r\n")
              elif param[0]=='list' :
                   list0= lisdirectory(param[1].strip())
                   for i in range(len(list0)):
                      for y in range(len(list0[i])):
                        conn.sendall(list0[i][y]+"\r\n");
        conn.sendall('OKEND\r\n')
     
    #came out of loop
    conn.close()
def threadConnectionServer():
     global SockPlaylist
     HostPlaylist = '127.0.0.1'
     PortPlaylist = 3235
     SockPlaylist.connect((HostPlaylist,PortPlaylist)) 


Sock = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
Host = '0.0.0.0' # l'ip locale de l'ordinateur
Port = 3237         # choix d'un port

#connexion au serveur de playlist
SockPlaylist = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
start_new_thread(threadConnectionServer ,())
 
# on bind notre socket :
Sock.bind((Host,Port))

#sound level by defaut :
soundLevel=-2000
 
# On est a l'ecoute d'une seule et unique connexion :
Sock.listen(10)

while 1:
   # Le script se stoppe ici jusqu'a ce qu'il y ait connexion :
   client, adresse = Sock.accept() # accepte les connexions de l'exterieur
   print "L'adresse",adresse,"vient de se connecter au serveur !"
   SockPlaylist.sendall('ADDCLIENT|'+adresse[0]+"\r\n")
   start_new_thread(clientthread ,(client,))
 
Sock.close()
 
        
        






