package omxclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import wtf.omxclient.R;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AddToPlaylist extends Activity {
	
	private Socket socket;

	private  int SERVERPORT = 3234;
	private String SERVER_IP = "192.168.0.31";
	
	private PrintWriter out = null;
    private BufferedReader in = null;
    
    private SocketService mMyService;
    
    public int maxIdPlaylist=0;
    
    public boolean removeAfterRead=false;
    public boolean playLoop=false;
    
    public static final int MENU_FILE = Menu.FIRST + 3;
	public static final int MENU_PARAM = Menu.FIRST + 2;
	public static final int MENU_PARAM_PLAYLIST = Menu.FIRST ;
	public static final int MENU_ARRETE = Menu.FIRST+4 ;
	public static final int MENU_CLEAR = Menu.FIRST+5 ;
	public static final int MENU_SAVE = Menu.FIRST+6 ;
	public static final int MENU_REMOTE = Menu.FIRST+1 ;
   public int myLastUpdate=0;
    ListView playlistView ;
    SimpleAdapter mSchedule;
    checkServiveUpdate chsu;
    ArrayList<HashMap<String, String>>  listItem = new ArrayList<HashMap<String, String>>();
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_PARAM_PLAYLIST, Menu.NONE, getString(R.string.libel_menu_option));
        menu.add(Menu.NONE, MENU_REMOTE, Menu.NONE,getString(R.string.libel_menu_remote));
        menu.add(Menu.NONE, MENU_PARAM, Menu.NONE, getString(R.string.libel_menu_parametre));
        menu.add(Menu.NONE, MENU_FILE, Menu.NONE, getString(R.string.libel_menu_choose_file));
        menu.add(Menu.NONE, MENU_ARRETE, Menu.NONE, getString(R.string.libel_menu_stop));
        menu.add(Menu.NONE, MENU_CLEAR, Menu.NONE, getString(R.string.libel_menu_remove_all));
        menu.add(Menu.NONE, MENU_SAVE, Menu.NONE, getString(R.string.libel_menu_save_playlist));
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
            case MENU_REMOTE:
        		startActivity(new Intent(this, RemoteControl.class));     	
            return true;
            case MENU_FILE:
            	startActivity(new Intent(this, ChooseFile.class)); 	
            return true;
            case MENU_PARAM_PLAYLIST:
            	OptionParam();
            return true;
            case MENU_ARRETE:
            	stopPlaylist();
            return true;
            case MENU_CLEAR:
            	clearPlaylist();
            return true;
            case MENU_SAVE:
            	savePlaylist();
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
    
	public void ifIntent()
    {
    	
    	Bundle extras = getIntent().getExtras();
		String youtubeSharing = null;
		 
		 if(extras!=null && extras.getString(Intent.EXTRA_TEXT)!=null)
		 	youtubeSharing =extras.getString(Intent.EXTRA_TEXT);
		//sharing youtube :
		 
			if(youtubeSharing!=null)
			{
				final String yt=youtubeSharing;
				
                 SendKeyThread skt=new SendKeyThread("");

				skt.setKey("ADDPLAYLIST|youtube|"+yt+"|caSertArien");
				new Thread(skt).start();
				showToast(youtubeSharing);
			
				
			}
    }
	@Override
	protected void onPause()
	{
		super.onPause();
		if(chsu!=null)
			chsu.running=false;
		//doUnbindService();
	}
	@Override
	protected void onStop()
	{
		super.onStop();
		if(chsu!=null)
			chsu.running=false;
		//doUnbindService();
	}
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if(chsu!=null)
			chsu.running=false;
		doUnbindService();
	}
	@Override
	public void onResume() {
	    super.onResume();  // Always call the superclass method first
	    if(chsu==null || !chsu.running)
	    {  chsu= new checkServiveUpdate();
		 	new Thread(chsu).start();	
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
	        }else if((keyCode == KeyEvent.KEYCODE_BACK)){
	        	AddToPlaylist.this.finish();
	        	return true;
	        	
	        }else {
	        	return false;
	        }
	        
	    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_to_playlist);
		doBindService();
		//Intent i = new Intent(AddToPlaylist.this, SocketService.class);
		 //startService(i);
		 
		loadPref();
		
		ifIntent();
		//return;
		
		 playlistView = (ListView) findViewById(R.id.listView1);
		 this.setTitle("Playlist : ");
		// Defined Array values to show in ListView
		 listItem = new ArrayList<HashMap<String, String>>();
		 mSchedule = new SimpleAdapter (AddToPlaylist.this.getBaseContext(), listItem, R.layout.affichageitem,
			        new String[] {"img", "titre", "description"}, new int[] {R.id.img, R.id.titre, R.id.description});
		 playlistView.setAdapter(mSchedule);
		 final Context mm=this;
		 playlistView.setOnItemClickListener(new OnItemClickListener() {			

	 			@Override
	 			public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
	 				// TODO Auto-generated method stub
	 				final HashMap<String, String> map = (HashMap<String, String>) playlistView.getItemAtPosition(position);
	 				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	 				    @Override
	 				    public void onClick(DialogInterface dialog, int which) {
	 				        switch (which){
	 				        case DialogInterface.BUTTON_POSITIVE:
	 			 				String t[] =  map.get("titre").split("-");
	 			 				String action[] =  map.get("description").split("-");
	 			 				SendKeyThread skt=new SendKeyThread("");
	 			 				if(action[0].trim().equals("Stop"))
	 			 				{	
	 			 					skt.setKey("key|q");
	 			 					new Thread(skt).start();
	 			 				}else if(action[0].trim().equals("Remove"))
	 			 				{	
	 			 					skt.setKey("REMOVEPLAYLIST|"+t[0].trim());
	 			 					new Thread(skt).start();
	 			 				}
	 			 				
	 				            break;

	 				        case DialogInterface.BUTTON_NEGATIVE:
	 				            //No button clicked
	 				            break;
	 				        }
	 				    }
	 				};

	 				AlertDialog.Builder builder = new AlertDialog.Builder(mm);
	 				builder.setMessage(getString(R.string.playlist_confirm_remove_message))
	 				.setPositiveButton(getString(R.string.playlist_confirm_remove_postive), dialogClickListener)
	 				    .setNegativeButton(getString(R.string.playlist_confirm_remove_negative), dialogClickListener).show();
	 			}
		 });
			  
		 playlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

	             @Override
	             public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
	            	 final HashMap<String, String> map = (HashMap<String, String>) playlistView.getItemAtPosition(position);
	            	 String t[] =  map.get("titre").split("-");
	            	 String action[] =  map.get("description").split("-");
	         		
	         		if(action[0].trim().equals("Remove"))
		 			{	
	         			SendKeyThread skt=new SendKeyThread("");
		 					skt.setKey("GOTOPLAYLIST|"+t[0].trim());
		 					new Thread(skt).start();
		 			}
	            	 return true;
	             }

	           });
		 		 
		 if(chsu==null || !chsu.running)
		    {  chsu= new checkServiveUpdate();
			 	new Thread(chsu).start();	
		    }		 
	}
	
	
	
	public void OptionParam()
	{
		String[] lesOption={getString(R.string.playlist_libel_option_remove_after_read),getString(R.string.playlist_libel_option_playloop)};
		final boolean[] lesCheck= {removeAfterRead,playLoop};
		//final ArrayList mSelectedItems = new ArrayList();  // Where we track the selected items
		    AlertDialog.Builder builder = new AlertDialog.Builder(AddToPlaylist.this);
		    // Set the dialog title
		    builder.setTitle(getString(R.string.playlist_box_option_title))		    
		           .setMultiChoiceItems(lesOption, lesCheck,
		                      new DialogInterface.OnMultiChoiceClickListener() {
		               @Override
		               public void onClick(DialogInterface dialog, int which,
		                       boolean isChecked) {
		                   if (isChecked) {
		                       // If the user checked the item, add it to the selected items
		                       //mSelectedItems.add(which);
		                	   lesCheck[which]=true;
		                	   //On decoche lire en boucle si "Retirer" est checked :
		                	   if(which==0){
		                		   lesCheck[1]=false;
		                		   ((AlertDialog) dialog).getListView().setItemChecked(1, false);
		                	   }
		                	   //On décodehe "retirer" si lire en boucle est checked
		                	   if(which==1){
		                		   lesCheck[0]=false;
		                		   ((AlertDialog) dialog).getListView().setItemChecked(0, false);
		                	   }
		                   } else {
		                	   lesCheck[which]=false;
		                   }
		               }
		           })
		    // Set the action buttons
		           .setPositiveButton(getString(R.string.playlist_box_option_postive), new DialogInterface.OnClickListener() {
		               @Override
		               public void onClick(DialogInterface dialog, int id) {
		            	   SendKeyThread skt=new SendKeyThread("");
		            	   showToast("SETPARAMPLAYLIST|LIREENBOUCLE="+(lesCheck[1]?"1":"0")+"|REMOVEAFTER="+(lesCheck[0]?"1":"0")); 
		            	   skt.setKey("SETPARAMPLAYLIST|LIREENBOUCLE="+(lesCheck[1]?"1":"0")+"|REMOVEAFTER="+(lesCheck[0]?"1":"0"));
		 					new Thread(skt).start();	
		               }
		           })
		           .setNegativeButton(getString(R.string.playlist_box_option_negative), new DialogInterface.OnClickListener() {
		               @Override
		               public void onClick(DialogInterface dialog, int id) {
		                  // ...
		               }
		           });

		    builder.create().show();

		
	}
	public void clearPlaylist()
	{
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			    		SendKeyThread skt=new SendKeyThread("");
			    		skt.setKey("SETPARAMPLAYLIST|LIREENBOUCLE=0|INUTILE=0");
			    		new Thread(skt).start();
			    		skt.setKey("REMOVEPLAYLIST|ALL");
			    		new Thread(skt).start();
			    		skt.setKey("key|q");
			    		new Thread(skt).start();
			            break;

			        case DialogInterface.BUTTON_NEGATIVE:
			            //No button clicked
			            break;
			        }
			    }
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.playlist_confirm_remove_all_title))
			.setPositiveButton(getString(R.string.playlist_confirm_remove_all_postive), dialogClickListener)
			.setNegativeButton(getString(R.string.playlist_confirm_remove_all_negative), dialogClickListener).show();

	}
	public void stopPlaylist()
	{
		SendKeyThread skt=new SendKeyThread("");
		skt.setKey("SETPARAMPLAYLIST|LIREENBOUCLE=0|INUTILE=0");
		new Thread(skt).start();
		skt.setKey("GOTOPLAYLIST|"+(maxIdPlaylist+10));
		new Thread(skt).start();
		skt.setKey("key|q");
		new Thread(skt).start();
	}
	
	public void savePlaylist()
	{
		final EditText input = new EditText(this);
		input.setText("");
   	 	new AlertDialog.Builder(this)
   	    .setTitle(getString(R.string.playlist_box_save_title))
   	    .setMessage(getString(R.string.playlist_box_save_message))
   	    .setView(input)
   	    .setPositiveButton(getString(R.string.playlist_box_save_postive), new DialogInterface.OnClickListener() {
   	        public void onClick(DialogInterface dialog, int whichButton) {
   	            String value = input.getText().toString(); 
   	            SendKeyThread skt=new SendKeyThread("");
   	            skt.setKey("SAVEPLAYLIST|"+value.replaceAll("/", ""));
   	            new Thread(skt).start();
   	        }
   	    }).setNegativeButton(getString(R.string.playlist_box_save_negative), new DialogInterface.OnClickListener() {
   	        public void onClick(DialogInterface dialog, int whichButton) {
   	        	return;
   	        }
   	    }).show();		
	}
	
	@Override
    public void onNewIntent (Intent intent)
    {
		loadPref();
	//	new Thread(new ClientThread()).start();
    	setIntent(intent);
    	ifIntent();
    }
	
	
	
	public void showToast(final String toast)
	{
	    runOnUiThread(new Runnable() {
	        public void run()
	        {
	            Toast.makeText(AddToPlaylist.this, toast, Toast.LENGTH_SHORT).show();
	        }
	    });
	}

	
	private ServiceConnection mConnection = new ServiceConnection() {
		 @Override
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        mMyService = ((SocketService.ServiceBinder)service).getService();
	    }
	    @Override
	    public void onServiceDisconnected(ComponentName className) {
	        mMyService = null;
	    }
		};


	void doBindService() {
	    bindService(new Intent(this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
	}

	void doUnbindService() {
	    // Detach our existing connection.
		 mMyService = null;
	    unbindService(mConnection);
	}
	
	public void listRefresh2()
	{
		 runOnUiThread(new Runnable() {
		        public void run()
		        {
		        	 mSchedule.notifyDataSetChanged();
		        }
		    });
		 
	}
	
	
	public void listRefresh3( final HashMap<String, String> map)
	{
		 runOnUiThread(new Runnable() {
		        public void run()
		        {
		        	listItem.add(map);		        	
		        }
		    });
		 
	}
class checkServiveUpdate implements Runnable {
	volatile boolean running = true;
	@Override
	public void run() {
		while(running)
		{
			doBindService();
			if(mMyService!=null &&  myLastUpdate!=mMyService.lastUpdate)
			{
				
				listItem.clear();
				//showToast("updating..."+mMyService.lastUpdate);
				myLastUpdate=mMyService.lastUpdate;
				  //	On déclare la HashMap qui contiendra les informations pour un item
				  HashMap<String, String> map;
				 int idebug=0;
				 //plus simple pour le tri :
				 SortedSet<Integer> keysTrie = new TreeSet<Integer>( mMyService.lesMorceau.keySet());
								 
				 for (Integer ikey : keysTrie) {
					    map = new HashMap<String, String>();
						String key=ikey+"";
						String value=(String)mMyService.lesMorceau.get(ikey);
						 map = new HashMap<String, String>();
						 map.put("titre", key+"- "+value);
						    if(key.equals(mMyService.encour))
						    {
						    	map.put("description", "Stop- "+getString(R.string.playlist_list_lecture_en_cour));
						    	map.put("img", String.valueOf(R.drawable.bouleverte));			    	
						    }else{
						    	map.put("description", "Remove- "+getString(R.string.playlist_list_click_to_remove));
						    	map.put("img", String.valueOf(R.drawable.boulejaune));
						    }
					  		//	enfin on ajoute cette hashMap dans la arrayList
						    listRefresh3(map);
						    idebug++;
						    maxIdPlaylist=ikey;	 
					}
				 
		
				 playLoop=mMyService.PARAM_LireEnBouble;
				 removeAfterRead=mMyService.PARAM_RemoveAfter;
				 String param=(mMyService.PARAM_LireEnBouble?"["+getString(R.string.playlist_message_lecture_en_boucle)+"]":
					 "["+getString(R.string.playlist_message_keep_after_read)+""+
						 (mMyService.PARAM_RemoveAfter?getString(R.string.no):getString(R.string.yes))+"]");
				  showToast(idebug+" files in playlist\n"+param);
				  listRefresh2();
			}else{
				try {
					 Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
    			{
    			InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
    			socket = new Socket(serverAddr, SERVERPORT);
		
    			}
    		try {
    			if(out==null)
    				out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
				if(in==null)
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
			} catch (IOException e1) {
				e1.printStackTrace();
				showToast( e1.toString());
			}
    		
    		///new Thread(new ReadReturnThread()).start();
    		
    		if(socket==null)
    			showToast("Pb socket");
    			
    		else{
    			        			
    			out.println(this.key);
    			out.flush();
    			            			
    			new Thread(new ReadReturnThread()).start();
    			//et.setText("key|");
    			//startActivity(new Intent(null, RemoteControl.class));
			}

		} catch (Exception e) {
			showToast( e.toString());
			e.printStackTrace();
  		}

	}

}

}

