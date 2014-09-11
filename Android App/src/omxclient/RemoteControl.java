package omxclient;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import omxclient.ChooseFile.ReadReturnFIleThread;
import omxclient.MainActivity.ClientThread;
import omxclient.MainActivity.ReadReturnThread;

import wtf.omxclient.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RemoteControl extends Activity {
	
	private Socket socket;

	private  int SERVERPORT = 3234;
	private String SERVER_IP = "192.168.0.31";
	
	private PrintWriter out = null;
    private BufferedReader in = null;
    
    ConnectionDetector cd;

    boolean refreshOnResum=false;
    
    public static final int MENU_PLAYLIST = Menu.FIRST + 1;
	public static final int MENU_PARAM = Menu.FIRST + 2;
	public static final int MENU_FILE = Menu.FIRST ;
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(Menu.NONE, MENU_FILE, Menu.NONE, "File...");
        menu.add(Menu.NONE, MENU_PARAM, Menu.NONE, "Parametres");
        menu.add(Menu.NONE, MENU_PLAYLIST, Menu.NONE, "Playlist");
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case MENU_PARAM:
            	startActivity(new Intent(this, SettingActivity.class));    
            return true;
        case MENU_FILE:
        		startActivity(new Intent(this, ChooseFile.class));     	
            return true;
        case MENU_PLAYLIST:
    		startActivity(new Intent(this, AddToPlaylist.class));     	
        return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void loadPref()
    {
    	SharedPreferences sharedPref =this.getSharedPreferences("OMXclient",Context.MODE_PRIVATE);
		this.SERVER_IP=sharedPref.getString("IP", "192.168.0.1");
		this.SERVERPORT=sharedPref.getInt("PORT", 3234);
    }
    
    
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	//on check si les param on changé :
    	String oldIP=this.SERVER_IP;
    	int oldPort=this.SERVERPORT;
    	    	
    	loadPref();
    	
    	this.refreshOnResum=(!oldIP.equals(this.SERVER_IP) || oldPort!=this.SERVERPORT);
    		
    	if(this.refreshOnResum)
    	{
    		this.refreshOnResum=false;
    		new Thread(new ClientThread()).start();
    	}	  
    	
    	
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
        	final SendKeyThread skt=new SendKeyThread("");
        	skt.setKey("key|MOINS");
        	new Thread(skt).start();
        	return true;
        }else if((keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
        	final SendKeyThread skt=new SendKeyThread("");
        	skt.setKey("key|PLUS");
        	new Thread(skt).start();
        	return true;
        }else {
        	return false;
        }
    }
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote);	
		
		
		loadPref();
		
				
		//((Button) findViewById(R.id.butStatut)).setBackgroundColor(Color.RED);
		//((Button) findViewById(R.id.butStatut)).setBackgroundResource(R.drawable.boulerouge);
		cd= new ConnectionDetector(this);
		if(cd.isConnectingToInternet())
			new Thread(new ClientThread()).start();
		
		final Context mm=this;
		final SendKeyThread skt=new SendKeyThread("");
		//Eteindre
				((Button) findViewById(R.id.butShutdown)).setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View view) {
			            	AlertDialog.Builder builder1 = new AlertDialog.Builder(mm);
			                builder1.setMessage("Confirmer l'extinction ?");
			                builder1.setCancelable(true);
			                builder1.setPositiveButton("Now",
			                        new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int id) {
			                    	skt.setKey("shutdown|NOW");
					            	new Thread(skt).start();
			                        dialog.cancel();
			                    }
			                });
			                builder1.setNeutralButton("Cancel",
			                        new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int id) {
			                    	skt.setKey("shutdown|CANCEL");
					            	new Thread(skt).start();
			                        dialog.cancel();
			                    }
			                });
			                builder1.setNegativeButton("Later",
			                        new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int id) {
			                    	final CharSequence[] items = {"2O minutes", "30 minutes", "45 minutes", "1H","1H30","1H45","2H","3H"};
			                    	final int[] temps ={20,30,45,60,90,105,120,180};

			                    	AlertDialog.Builder builder = new AlertDialog.Builder(mm);
			                    	builder.setTitle("Dans combien de temps ?");
			                    	builder.setItems(items, new DialogInterface.OnClickListener() {
			                    	    public void onClick(DialogInterface dialog, int item) {
			                    	         // Do something with the selection
			                    	    	showToast("shutdown|"+temps[item]);
			                    	    	skt.setKey("shutdown|"+temps[item]);
							            	new Thread(skt).start();
					                        dialog.cancel();
			                    	    }
			                    	});
			                    	AlertDialog alert = builder.create();
			                    	alert.show();
			                        dialog.cancel();
			                    }
			                });

			                AlertDialog alert11 = builder1.create();
			                alert11.show();
			            				            	
			            }
			          });
				//Statut
				((Button) findViewById(R.id.butStatut)).setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View view) {
			            		try {
									socket.close();
								} catch (Exception e) {
									e.printStackTrace();
								}
			            		ColorStatut(Color.RED);
			        		new Thread(new ClientThread()).start();
			            }
			          });
				//Files...
				((Button) findViewById(R.id.butGoFile)).setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View view) {
			            	Intent remoteIntent =new Intent(mm, ChooseFile.class); 
		            		startActivity(remoteIntent);		            	
			            }
			          });
		//volume moins
		((Button) findViewById(R.id.butVolMoins)).setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	            	skt.setKey("key|MOINS");
	            	new Thread(skt).start();
	            	
	            }
	          });
		//Kill
				((Button) findViewById(R.id.butKill)).setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View view) {
			            	skt.setKey("kill|now");
			            	new Thread(skt).start();
			            	
			            }
			          });
		//volume plus
				((Button) findViewById(R.id.butVolPlus)).setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View view) {
			            	skt.setKey("key|PLUS");
			            	new Thread(skt).start();
			            	
			            }
			          });
				//paus
				((Button) findViewById(R.id.butPause)).setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View view) {
			            	skt.setKey("key|SPACE");
			            	new Thread(skt).start();
			            	
			            }
			          });
				//rembobine vite
				((Button) findViewById(R.id.butRemb6000)).setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View view) {
			            	skt.setKey("key|DOWN");
			            	new Thread(skt).start();
			            	
			            }
			          });
				//rembobine 
				((Button) findViewById(R.id.butRemb10)).setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View view) {
			            	skt.setKey("key|LEFT");
			            	new Thread(skt).start();
			            	
			            }
			          });
				//avance
				((Button) findViewById(R.id.butNext10)).setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View view) {
			            	skt.setKey("key|RIGHT");
			            	new Thread(skt).start();
			            	
			            }
			          });
				//avance vite
				((Button) findViewById(R.id.butNext6000)).setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View view) {
			            	skt.setKey("key|UP");
			            	new Thread(skt).start();
			            	
			            }
			          });
				//Stop
				((Button) findViewById(R.id.butStop)).setOnClickListener(new View.OnClickListener() {
			            @Override
			            public void onClick(View view) {
			            	skt.setKey("key|q");
			            	new Thread(skt).start();
			            	
			            }
			          });
				
				//ifIntent();
	}
	
	public void showToast(final String toast)
	{
	    runOnUiThread(new Runnable() {
	        public void run()
	        {
	            Toast.makeText(RemoteControl.this, toast, Toast.LENGTH_SHORT).show();
	        }
	    });
	}
	
	public void ColorStatut(final int laColor)
	{
	    runOnUiThread(new Runnable() {
	        public void run()
	        {
	        	//((Button) findViewById(R.id.butStatut)).setBackgroundColor(laColor);
	        	switch (laColor)
	        	{
	        	case Color.GREEN :
	        		((Button) findViewById(R.id.butStatut)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.bouleverte, 0, 0, 0); 
	        	
	        		break;
	        	case Color.RED :
	        		((Button) findViewById(R.id.butStatut)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.boulerouge, 0, 0, 0); 
	        	
	        		break;
	        		default :
	        			((Button) findViewById(R.id.butStatut)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.boulejaune, 0, 0, 0); 
		        	break;
	        	}
	        	
	        }
	    });
	}

	class ClientThread implements Runnable {

		@Override
		public void run() {

			try {

				InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

				socket = new Socket(serverAddr, SERVERPORT);
				ColorStatut(Color.BLUE);
				try {
					out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					ColorStatut(Color.GREEN);
				} catch (IOException e1) {
					e1.printStackTrace();
					showToast( e1.toString());
				}
				
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
				showToast( e1.toString());
			} catch (IOException e1) {
				e1.printStackTrace();
				showToast( e1.toString());
			} catch (Exception e) {
    			e.printStackTrace();
    			showToast( e.toString());
    		}

		}

	}

	class ReadReturnThread implements Runnable {
		String retour="NO";
		@Override
		public void run() {

			
				try {
					String r;
					while((r= in.readLine())!=null)
					{	retour+=" "+r;
						if (r.trim().equals("OKEND"))
							break;
					}
				} catch (IOException e1) {
					retour="ERREUR";
					e1.printStackTrace();
					showToast(e1.toString());
				}
				
				showToast(retour);

		}

	}
	
	class SendKeyThread implements Runnable {
	
		String key="";
		public SendKeyThread(String pkey)
		{
			this.key=pkey;
		}
		
		public void setKey(String laKey)
		{
			
			this.key=laKey;
		}
		@Override
		public void run() {

			try {
        		if(socket==null)
        			new Thread(new ClientThread()).start();
        		
        		if(socket==null)
        			showToast("Pb socket");
        			
        		else{
        			        			
        			out.println(this.key);
        			out.flush();
        			            			
        			new Thread(new ReadReturnThread()).start();
        			//et.setText("key|");
    			}
    
    		} catch (Exception e) {
    			showToast( e.toString());
    			e.printStackTrace();
      		}

		}

	}

}
