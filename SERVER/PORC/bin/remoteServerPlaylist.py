#!/usr/bin/python
from ctypes import *
from thread import *
import time
import subprocess
import os
import socket
import re
import shutil
import youtube_dl

def isInt(s):
    try: 
        int(s)
        return True
    except ValueError:
        return False

def clientthreadEXterne():
    global listClient
    setPostion()
    for cle, valeur in listClient.items() :
       print(cle, valeur)
       sendToClient(cle,3236)

def sendToClient(ipClient,portClient):
     global listClient
     global playlist
     global listParam
     global CurrentFileDuration
     global TimeEnSecPlay
     try:
         SockClient = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
         SockClient.connect((ipClient,portClient))
         print 'status|ISPAUSE|'+str(OmxIsPause()).strip()+"\r\n"
         SockClient.sendall('status|ISPAUSE|'+str(OmxIsPause()).strip()+"\r\n")
         # les param :
         for key in listParam.keys() :
            SockClient.sendall('param|'+key+'|'+str(listParam[key]).strip()+"\r\n")
            print (ipClient,key,str(listParam[key]).strip())
         #La playlist :
         tpsEcoule=TimeEnSecPlay
#int(round(time.time()))-TimeEnSecPlay
         SockClient.sendall('encour|'+str(idLectureEnCour)+"|"+str(CurrentFileDuration)+"|"+str(tpsEcoule).strip()+"\r\n")
         print 'encour|'+str(idLectureEnCour)+"|"+str(CurrentFileDuration)+"|"+str(tpsEcoule).strip()+"\r\n"
         for key in sorted(playlist.keys()) :
             print (ipClient,key,playlist[key])
             SockClient.sendall('item|'+str(key)+'|'+playlist[key].strip()+"\r\n")
         SockClient.sendall("OKEND\r\n");
         SockClient.close()
     except Exception, e:
         del listClient[ipClient]
         print e
		 
def threadLireFile(paramFile):
#     global TimeEnSecPlay
#     TimeEnSecPlay=int(round(time.time()))
     p=paramFile.split("|")
     if p[0]=='youtube':
        readYoutube(p[1])
     else :
        readFile(p[0],p[1])
	    # double next ? next()
		
def readFile(leFile,lesparam) :
# SendKill()
 global soundLevel
 ligne = 'xterm -bg black -fg black -fullscreen -e omxplayer '+lesparam+' --vol '+str(soundLevel)+'  "'+leFile+'"'
 print ligne
 subprocess.call(ligne,  shell=True)
 
def setPostion() :
   global TimeEnSecPlay
   TimeEnSecPlay=0
   try:
       s = subprocess.Popen('dbuscontrol.sh getposition', shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
       du=0
       for line in s.stdout.readlines():
          du=line.strip()
       if(isInt(du)):
          TimeEnSecPlay=int(round(int(du)/1000000))
   except Exception, e:
          print e
 
def OmxIsPause():
   try:
       s = subprocess.Popen('dbuscontrol.sh ispause', shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
       du=0
       for line in s.stdout.readlines():
          du=line.strip()
       if(isInt(du)):
          return du
       else :
          return 0
   except Exception, e:
          print e
 
def setDurationBus():
    global CurrentFileDuration
    CurrentFileDuration=0
    nbTentative=0
    while nbTentative<20 and CurrentFileDuration==0:
       try:
          s = subprocess.Popen('dbuscontrol.sh getduration', shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
          du=0
          for line in s.stdout.readlines():
             du=line.strip()
          if(isInt(du)):
             CurrentFileDuration=int(round(int(du)/1000000))
          else:
             #print str(du)+" n'est pas un int"
             CurrentFileDuration=0
       except Exception, e:
          print e
       nbTentative+=1   
       time.sleep(2)
    if CurrentFileDuration!=0 :
       start_new_thread(clientthreadEXterne,())


def readAll(lePath,lesParam) :
 global lastId
 global playlist
 print 'READ ALL '+lePath+"#"+lesParam
 if os.path.isdir(lePath) :
    for filename in os.listdir(lePath):
      if filename[0]!="." :
          if (os.path.isfile(lePath+"/"+filename)) :
              ext = filename.split(".")
              if(len(ext)>1 and (ext[-1].upper()=="AVI" or ext[-1].upper()=="MPEG" or  ext[-1].upper()=="MKV" or ext[-1].upper()=="MPG" or ext[-1].upper()=="MP3" or ext[-1].upper()=="MP4"  or ext[-1].upper()=="FLAC")) :
                 lastId+=1
                 print 'ajout list : ',lastId
                 playlist[lastId]=lePath+"/"+filename+"|"+lesParam
 elif os.path.isfile(lePath):
   ext = lePath.split(".")
   if(len(ext)>1 and ext[-1].upper()=="PLAY"):
      fichier = open(lePath, "r")
      for ligne in fichier:
         if ligne.strip()!='':
            lastId+=1
            print 'ajout list : ',lastId
            playlist[lastId]=ligne.strip()
      fichier.close()
		

def savePlayList(leNomDuFichier):
   global PlayListSaveDir
   fichier = open(PlayListSaveDir+leNomDuFichier+".PLAY", "w")
   for key in sorted(playlist.keys()) :
      fichier.write(playlist[key].replace("\n"," ").replace("\r","")+"\r\n") 
   fichier.close()

def next():
     global isPlaying
     global idLectureEnCour
     global playlist
     global listParam
	 
	 #on degage celui qu'on a deja lu
     if listParam['REMOVEAFTER']=='1' and idLectureEnCour!=0:
        try:
           del playlist[idLectureEnCour]
        except Exception, e:
           print 'liste vide...'
      
     endOfList=1
     for key in sorted(playlist.keys()):
       if  endOfList==1 and key>idLectureEnCour :
            isPlaying=1
            endOfList=0
            idLectureEnCour=key

	# lire le fichier
     if endOfList==1 and listParam['LIREENBOUCLE']!='1':
	 #Si pas de lecture en boucle :
        isPlaying=0
        idLectureEnCour=0
     
	 #on met a jour la dure du morceau eu cour de lecture :
     if(idLectureEnCour!=0):
          start_new_thread(setDurationBus,())
#          setDuration(playlist[idLectureEnCour])

     start_new_thread(clientthreadEXterne,())
     if  endOfList==0:
	 #si on est pas arrive a la fin de la liste on next tranquilou..
        threadLireFile(playlist[idLectureEnCour])
        next()
     elif endOfList==1 and listParam['LIREENBOUCLE']=='1':
	 #Sinon si on est en en fin de liste avec l'option "lecture en boucle" on repart au debut
        idLectureEnCour=0
        next()

def updateParam(param1,param2,param3):
     global listParam
     p = param1.split("=")
     listParam[p[0]]=p[1].strip()
     p = param2.split("=")
     listParam[p[0]]=p[1].strip()
     p = param3.split("=")
     listParam[p[0]]=p[1].strip()
		
def getYouTubeURL(url) :
    newUrl = url
    if url.find("://") != -1:
        ydl = youtube_dl.YoutubeDL({
        'format': listParam['YOUTUBEQUALITY']+'/best',
           "outtmpl" : "%(id)s%(ext)s",
           "quiet" : False,
           "verbose" : True,
           "debug_printtraffic" : True
              })
        with ydl:
            result = ydl.extract_info(url,download = False)
            if 'entries' in result:
            # Can be a playlist or a list of videos
               video = result['entries'][0]
            else:
               # Just a video
               video = result
               newUrl = video['url']
        p = newUrl.split(" ")
        newUrl=p[0]
    return newUrl
   
def readYoutube(leFile) :
# SendKill()
 global soundLevel
 global listParam
 lf=leFile.split(":")
 print " WOOOOOOOOOO__"+str(len(lf))+"__________!!!!"
 posURL=-1
 http="http"
 for i in range(len(lf)):
    if lf[i].endswith("http") :
       posURL=i
    if lf[i].endswith("https") :
       posURL=i
       http="https"
 if posURL!=-1 :
   if len(lf)>=posURL+1 : 
      if lf[posURL+1].strip().startswith("//"):
#         ligne = 'xterm -bg black  -fg black -fullscreen -e youtube "'+http+':'+lf[posURL+1].strip()+'" '+str(soundLevel)+' '+listParam['YOUTUBEQUALITY']
         ligne= 'xterm -fullscreen -bg black -fg black -e omxplayer -o both --vol '+str(soundLevel)+' "'+getYouTubeURL(http+':'+lf[posURL+1].strip())+'"'
         print ligne  
         subprocess.call(ligne,  shell=True)  
          
 
def isDoubleAdd(leFile) :
    global lastId
    global playlist
    
    try:
       if playlist[lastId] == leFile :
         return True
    except Exception, e:
       print 'allready removed or other...'
    return False
 
#Function for handling connections. This will be used to create threads
def doAction(action):
    global idLectureEnCour
    global listParam
    global lastId
    global isPlaying
    global playlist
    global listClient
    global soundLevel
    #Sending message to connected client
    #conn.send('Welcome to the server. Type something and hit enter\n') #send only takes string
     
    #infinite loop so that function do not terminate and thread do not end.
    if len(action)>0 :
       print action         # affiche les donnees envoyees
       param = action.split("|")
       if param[0]=='SETPARAM' :
             updateParam(param[1].strip(),param[2].strip(),param[3].strip())
             #todo : si isPlaying==0 est que lecture en boucle alor next()...
             start_new_thread(clientthreadEXterne,())
       elif param[0]=='ADD' :
            if isDoubleAdd(param[1].strip()+"|"+param[2].strip())==False :
               lastId+=1
               print 'ajout list : ',lastId
               playlist[lastId]=param[1].strip()+"|"+param[2].strip()
               start_new_thread(clientthreadEXterne,())
               if isPlaying==0 :
                   start_new_thread(next,())
       elif param[0]=='REMOVE' :
			  #On s'attend a ce que param[1] soit l'id...
          if param[1].strip()=='ALL':
             listParam['LIREENBOUCLE']='0'
             playlist.clear()
             start_new_thread(clientthreadEXterne,())
          else:
             try:
                del playlist[int(param[1].strip())]                   
                start_new_thread(clientthreadEXterne,())
             except Exception, e:
                print 'allready removed or other...'
       elif param[0]=='GOTO' :
             idLectureEnCour=int(param[1].strip())-1
             if isPlaying==0 :
                start_new_thread(next,())
       elif param[0]=='PLAYALL' :
             readAll(param[1].strip(),param[2].strip())
             start_new_thread(clientthreadEXterne,())
             if isPlaying==0 :
                start_new_thread(next,())
       elif param[0]=='SETSOUNDLEVEL' :
             soundLevel=param[1].strip()
       elif param[0]=='SAVEPLAYLIST' :
             savePlayList(param[1].strip())
       elif param[0]=='FORCEREFRESH' :
             start_new_thread(clientthreadEXterne,())
       elif param[0]=='ADDCLIENT' :
            if param[1].strip() not in listClient :
               listClient[param[1].strip()]=param[1].strip()
            start_new_thread(sendToClient,(param[1].strip(),3236))


isPlaying=0
playlist={}
lastId=0
idLectureEnCour=0
CurrentFileDuration=0
TimeEnSecPlay=0
soundLevel=-2000
listClient={}
listParam = {'LIREENBOUCLE': '0', 'REMOVEAFTER': '1', 'YOUTUBEQUALITY' :'best'}
PlayListSaveDir='/home/pi/PLAYLIST/'
