package omxclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import omxclient.AddToPlaylist.checkServiveUpdate;

import wtf.omxclient.R;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.widget.Toast;

public class SocketService extends Service {
	  NotificationManager mNM;
	  Builder notifBuilder;
	 private ServerSocket  socket;
	 BufferedReader in;
	 public int lastUpdate=0;
	 public HashMap<Integer , String> lesMorceau=new HashMap<Integer , String>();
	 Notification notification;
	 ShowProgressInNotif shoProgress=null;
	 ClientThread ct;
public String encour="0";
	 
	 public  int LISTENERPORT = 3236;
		public String SERVER_IP = "192.168.0.31";
		
		public boolean PARAM_LireEnBouble=false;
		public boolean PARAM_RemoveAfter=true;
	    // Unique Identification Number for the Notification.
	    // We use it on Notification start, and to cancel it.
	    private int NOTIFICATION = 667576576;//R.string.socket_service_started;

	    public void loadPref()
	    {
	    	SharedPreferences sharedPref =this.getSharedPreferences("OMXclient",Context.MODE_PRIVATE);
			this.LISTENERPORT=sharedPref.getInt("PLAYLISTPORT", 3236);		
			this.SERVER_IP=sharedPref.getString("IP", "192.168.0.31");
	    }
	    
	    /**
	     * Class for clients to access.  Because we know this service always
	     * runs in the same process as its clients, we don't need to deal with
	     * IPC.
	     */
	    public class LocalBinder extends Binder {
	    	SocketService getService() {
	            return SocketService.this;
	        }
	    }

	    @Override
	    public void onCreate() {
	        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	        loadPref();
	        // Display a notification about us starting.  We put an icon in the status bar.
	        showNotification();
	        ct= new ClientThread();
		 	new Thread(ct).start();	
	    }

	    @Override
	    public int onStartCommand(Intent intent, int flags, int startId) {
	        Log.i("LocalService", "Received start id " + startId + ": " + intent);
	        // We want this service to continue running until it is explicitly
	        // stopped, so return sticky.
	        return START_STICKY;
	    }

	    @Override
	    public void onDestroy() {
	        // Cancel the persistent notification.
	        mNM.cancel(NOTIFICATION);

	        // Tell the user we stopped.
	        Toast.makeText(this, "OMX servive stoped", Toast.LENGTH_SHORT).show();
	    }

	    @Override
	    public IBinder onBind(Intent intent) {
	        return mBinder;
	    }

	    @Override
	    public boolean onUnbind(Intent intent) {
	        // All clients have unbound with unbindService()
	        return false;
	    }
	    // This is the object that receives interactions from clients.  See
	    // RemoteService for a more complete example.
	    private final IBinder mBinder = new ServiceBinder();

	    /**
	     * Show a notification while this service is running.
	     */
	    private void showNotification() {
	        // In this sample, we'll use the same text for the ticker and the expanded notification
	        CharSequence text ="En attente...";

	        // Set the icon, scrolling text and timestamp
	        // notification = new Notification(R.drawable.bouleverte, text, System.currentTimeMillis()); // new Notification(R.drawable.stat_sample, text, System.currentTimeMillis());

	        notifBuilder = new NotificationCompat.Builder(this);
	        notifBuilder.setContentTitle("OMX playlist")
	         .setContentText(text)
	         .setSmallIcon(R.drawable.boulejaune);

	         
	        // The PendingIntent to launch our activity if the user selects this notification
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	                new Intent(this,AddToPlaylist.class), 0);

	        // Set the info for the views that show in the notification panel.
	        //notification.setLatestEventInfo(this, "OMX playlist", text, contentIntent);
	        notifBuilder.setContentIntent(contentIntent);
	        notification=notifBuilder.build();
	        notification.flags = notification.FLAG_AUTO_CANCEL;
	        // Send the notification.
	        mNM.notify(NOTIFICATION, notification);
	    }
	    
	    public class ServiceBinder extends Binder {
	        public SocketService getService() {
	            return SocketService.this;
	        }
	    }
	    
	    class ClientThread implements Runnable {

			@Override
			public void run() {
				String r;
				
				try {
					//InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
					InetAddress serverAddr = InetAddress.getByName("0.0.0.0");
					
					socket = new ServerSocket(LISTENERPORT, 0, null);
					//String debug="";
					
					while(true){
						// debug="";
						Socket socketclient = socket.accept();
						try {
							in = new BufferedReader(new InputStreamReader(socketclient.getInputStream()));
							lesMorceau.clear();
							
							int dureEnCour=0;
							int dejaLu=0;
							
							String MSGparam="";
							while((r= in.readLine())!=null)
							{	
							//	debug+=r+"++";
								String ret[]=r.split("\\|");
								if(ret[0].equals("encour"))
								{	encour=ret[1];
									if(ret[2]!=null && ret[3]!=null)
									{
										dureEnCour=Integer.parseInt(ret[2]);
										dejaLu=Integer.parseInt(ret[3]);
									}
								}
								else if(ret[0].equals("item") && ret[1].equals(encour))
								{
									String titreEncour="";
									if(!ret[2].matches(".*http.*") && !ret[2].matches(".*youtub.*"))
									{
										String[] titre=ret[2].split("/");
										titreEncour=titre[titre.length-1];
									}else{
										String[] titre=ret[3].split(":");
										titreEncour=titre[0];
									}
									String g="";
									if(ret[2].matches(".*youtub.*"))
									{
										String[] titre=ret[3].split(":");
										g=" - "+titre[0];
									}
									 notifBuilder.setContentTitle(titreEncour)
									 .setTicker(titreEncour)
							         .setContentText("Chargement...")
							         .setSmallIcon(R.drawable.bouleverte)
							         .setNumber(Integer.parseInt(ret[1]))
							         .setContentInfo((PARAM_LireEnBouble?"[LECTURE EN BOUCLE]":"[Keep after read : "+(PARAM_RemoveAfter?"NON":"OUI")+"]")) ;
							         //.setSubText((PARAM_LireEnBouble?"[LECTURE EN BOUCLE]":"[Keep after read : "+(PARAM_RemoveAfter?"NON":"OUI")+"]")) ;
									/* notifBuilder.setStyle( new NotificationCompat.InboxStyle()
									 .setBigContentTitle(titre[titre.length-1])
									 .addLine((PARAM_LireEnBouble?"[LECTURE EN BOUCLE]":"[Keep after read : "+(PARAM_RemoveAfter?"NON":"OUI")+"]"))
									 .addLine( ret[2]));*/
									 notification=notifBuilder.build();
									 notification.flags = notification.FLAG_ONGOING_EVENT;
									 mNM.notify(NOTIFICATION, notification);		
									 lesMorceau.put(Integer.parseInt(ret[1]), ret[2]+g);
									 
								}else if(ret[0].equals("item")){
									String g="";
									if(ret[2].matches(".*youtub.*"))
									{
										String[] titre=ret[3].split(":");
										g=" - "+titre[0];
									}
									lesMorceau.put(Integer.parseInt(ret[1]), ret[2]+g);
								}else if(ret[0].equals("param")){
									if(ret[1].equals("LIREENBOUCLE"))
										PARAM_LireEnBouble=ret[2].trim().equals("1");
									else if(ret[1].equals("REMOVEAFTER"))
										PARAM_RemoveAfter=ret[2].trim().equals("1");
									
								}else{
									if (r.trim().equals("OKEND"))
										break;
								}
							}
							if(encour.equals("0"))
							{
								if(shoProgress!=null)
									shoProgress.stop();
							//	PendingIntent contentIntent = PendingIntent.getActivity(SocketService.this, 0, new Intent(SocketService.this,AddToPlaylist.class), 0);
								// notification.setLatestEventInfo(SocketService.this, "OMX playlist", "Lecture terminé... "+(PARAM_LireEnBouble?"[LECTURE EN BOUCLE]":"[Keep after read : "+(PARAM_RemoveAfter?"NON":"OUI")+"]"), contentIntent);
								 notifBuilder.setContentTitle("OMX playlist")
								 .setTicker("Lecture terminé...")
						         .setContentText( "Lecture terminé... ")
						         .setContentInfo((PARAM_LireEnBouble?"[LECTURE EN BOUCLE]":"[Keep after read : "+(PARAM_RemoveAfter?"NON":"OUI")+"]")) 
						         //.setSubText((PARAM_LireEnBouble?"[LECTURE EN BOUCLE]":"[Keep after read : "+(PARAM_RemoveAfter?"NON":"OUI")+"]")) 
						         .setSmallIcon(R.drawable.boulejaune).setProgress(0,0,false);

								 notification=notifBuilder.build();		
								 //notifBuilder.setVibrate(new long[] {1000,1000,1000,1000,1000});
								 //notifBuilder.setLights(Color., 3000,3000);
								 notification.flags = notification.FLAG_AUTO_CANCEL;
								 //notification.flags |= Notification.FLAG_SHOW_LIGHTS;
								 mNM.notify(NOTIFICATION, notification);
							}else{
								if(shoProgress!=null)
									shoProgress.stop();
								if(dureEnCour!=0)
								{
									shoProgress= new ShowProgressInNotif(dureEnCour, dejaLu);
									new Thread(shoProgress).start();
								}	
							}
							lastUpdate++;
							//in.close();
							//socketclient.close();
						} catch (IOException e1) {
							e1.printStackTrace();
							//showToast(e1.toString());
						}
					}
					
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				//	showToast(e1.toString());
				} catch (IOException e1) {
					e1.printStackTrace();
					//showToast(e1.toString());
				} catch (Exception e) {
	    			e.printStackTrace();
	    		//	showToast(e.toString());
	    		}

			}

		}
	    
	    class ShowProgressInNotif implements Runnable {
	    	public int dureTotal=0;
	   
	    	public int ecoule=0;
	    	public boolean continu=true;
	    	
	    	private int realTimeStart_second=0;
	    	
	    	public ShowProgressInNotif(int dureTotal,int startAs)
	    	{
	    		this.dureTotal=dureTotal;
	    		this.ecoule=startAs;
	    		this.realTimeStart_second= Math.round(TimeUnit.MILLISECONDS.toSeconds(SystemClock.elapsedRealtime()))-startAs;
	    	}
	    	public void stop()
	    	{
	    		this.continu=false;
	    	}
	    	
	    	public String Seconde_tohms(int nbseconde)
	    	{
	    		int hours = nbseconde / 3600;
	    		int minutes = (nbseconde % 3600) / 60;
	    		int seconds = nbseconde % 60;

	    		return (hours!=0?hours + "h":"") + (minutes!=0?minutes + "m":"") + ((hours==0 && seconds!=0)?seconds+"s":"");
	    	}
			@Override
			public void run() {
				while(this.continu && this.ecoule<=this.dureTotal)
				{
					 this.ecoule=Math.round(TimeUnit.MILLISECONDS.toSeconds( SystemClock.elapsedRealtime()))-this.realTimeStart_second;
					 this.ecoule=this.ecoule>this.dureTotal?this.dureTotal:this.ecoule;
					 
					 notifBuilder.setContentText("Ecoulé : "+Seconde_tohms(this.ecoule)+" / "+Seconde_tohms(this.dureTotal))
					 .setProgress(this.dureTotal,  this.ecoule, false);
	                    // Displays the progress bar for the first time.
					 notification=notifBuilder.build();
					 notification.flags = notification.FLAG_ONGOING_EVENT;
					 mNM.notify(NOTIFICATION, notification);		
					 
					 try {
                         Thread.sleep(1000);
                  } catch (InterruptedException e) {
                     e.printStackTrace();
                  }
				}
				// notifBuilder.setContentTitle("Fin :"+this.ecoule+"*"+this.realTimeStart_second);
				// notification=notifBuilder.build();
				// mNM.notify(NOTIFICATION, notification);	
			}
			
	    }
	}