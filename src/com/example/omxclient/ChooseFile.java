package com.example.omxclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.omxclient.MainActivity.ReadReturnThread;
import com.example.omxclient.RemoteControl.ClientThread;
import com.example.omxclient.RemoteControl.SendKeyThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ChooseFile  extends Activity {
	
	 public static final int MENU_PLAYALL = Menu.FIRST + 2;
	public static final int MENU_PARAM = Menu.FIRST + 1;
	public static final int MENU_REMOTE = Menu.FIRST ;

	private Socket socket;

	private  int SERVERPORT = 3234;
	private String SERVER_IP = "192.168.0.31";//"192.168.0.31";
	
	String OMXOPTION="-o hdmi -r ";
	
	private boolean lockList=false;
	
	private PrintWriter out = null;
    private BufferedReader in = null;
    
    private boolean refreshOnResum=false;
    
    ListView  maListViewPerso;
    SimpleAdapter mSchedule;
    ArrayList<HashMap<String, String>>  listItem = new ArrayList<HashMap<String, String>>();
    String docAlister="";
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(Menu.NONE, MENU_REMOTE, Menu.NONE, "Remote");
        menu.add(Menu.NONE, MENU_PARAM, Menu.NONE, "Parametres");
        menu.add(Menu.NONE, MENU_PLAYALL, Menu.NONE, "Play all");
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
        case MENU_PLAYALL:
        	PlayFile pf=new PlayFile(docAlister,"ALL",OMXOPTION);
				new Thread(pf).start();  	
        return true;
        default:
            return super.onOptionsItemSelected(item);
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
        	String d="";
			String a[]=docAlister.split("/");
			for(int i=0;i<a.length-1;i++) 
				d+=(a[i].equals("")?"":"/"+a[i]);
			
			docAlister = d;
			saveTree(d);	
			//showToast(d);
			new Thread(new ReadReturnFIleThread()).start();
        	return true;
        	
        }else {
        	return false;
        }
        
    }
    
    public void saveTree(String ledoc)
    {
    	//sauvegarde du dossier en cour :
    	SharedPreferences sharedPref = this.getSharedPreferences("OMXclient",Context.MODE_PRIVATE);
		  SharedPreferences.Editor editor = sharedPref.edit();
		  editor.putString("savedTree", ledoc);
		  editor.commit();
    }
    
    public void saveOMXOPTION(String option)
    {
    	this.OMXOPTION=option;
    	SharedPreferences sharedPref = this.getSharedPreferences("OMXclient",Context.MODE_PRIVATE);
		  SharedPreferences.Editor editor = sharedPref.edit();
		  editor.putString("OMXOPTION", option);
		  editor.commit();    	
    }
    
    public void loadPref()
    {
    	SharedPreferences sharedPref =this.getSharedPreferences("OMXclient",Context.MODE_PRIVATE);
		this.SERVER_IP=sharedPref.getString("IP", "192.168.0.1");
		this.SERVERPORT=sharedPref.getInt("PORT", 3234);
		this.docAlister= sharedPref.getString("savedTree", "/home");
		this.OMXOPTION= sharedPref.getString("OMXOPTION", "-o hdmi -r ");
    }
   
    @Override
    public void onResume()
    {
    	super.onResume();
    	//on check si les param on changé :
    	String oldIP=this.SERVER_IP;
    	String oldDir=this.docAlister;
    	int oldPort=this.SERVERPORT;
    	    	
    	loadPref();
    	
    	this.refreshOnResum=(!oldIP.equals(this.SERVER_IP) || !oldDir.equals(this.docAlister) || oldPort!=this.SERVERPORT);
    		
    	if(this.refreshOnResum)
    	{
    		this.refreshOnResum=false;
    		new Thread(new ReadReturnFIleThread()).start();
    	}	  
    }
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choosefile);	
		
		loadPref();
		
	//	if(socket==null)
	//		new Thread(new ClientThread()).start();
		
		final Context mm=this;
		this.setTitle(docAlister);
		//getExtra le dossier a lister :
	//	if (savedInstanceState == null) {
		    Bundle extras = getIntent().getExtras();
		    if(extras != null) {
		    	docAlister= extras.getString("LeDossier");
		    	saveTree(docAlister);
		    }
	/*	} else {
			docAlister= (String) savedInstanceState.getSerializable("docAlister");
		}*/
		
		 //Récupération de la listview créée dans le fichier main.xml
		  maListViewPerso = (ListView) findViewById(R.id.listviewperso);
		  mSchedule = new SimpleAdapter (ChooseFile.this.getBaseContext(), listItem, R.layout.affichageitem,
			        new String[] {"img", "titre", "description"}, new int[] {R.id.img, R.id.titre, R.id.description});
		        	
			         //On attribut à notre listView l'adapter que l'on vient de créer
			         maListViewPerso.setAdapter(mSchedule);
			        
			         maListViewPerso.setOnItemClickListener(new OnItemClickListener() {			

			 			@Override
			 			public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
			 				// TODO Auto-generated method stub
			 				HashMap<String, String> map = (HashMap<String, String>) maListViewPerso.getItemAtPosition(position);
			 			//	Intent remoteIntent =new Intent(ChooseFile.this, ChooseFile.class); 
			 			//	remoteIntent.putExtra("LeDossier", map.get("description"));
			         	//	startActivity(remoteIntent);
			 				String t[] =  map.get("titre").split("-");
			 				
			 				if(t[0].trim().equals("ERR"))
			 				{	
			 					startActivity(new Intent(mm, SettingActivity.class));  
			 				}else if(t[0].trim().equals("DIR"))
			 				{	docAlister = map.get("description");
			 					saveTree(docAlister);
			 					
			 					new Thread(new ReadReturnFIleThread()).start();
			 				}else if(t[0].trim().equals("TV") || t[0].trim().equals("AVI") || t[0].trim().equals("PL"))
			 				{	PlayFile pf=new PlayFile(map.get("description"),t[0].trim(),OMXOPTION);
		 						new Thread(pf).start();
			 				}
			 			}
			           });
			         maListViewPerso.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			             @Override
			             public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
			            	 final HashMap<String, String> map = (HashMap<String, String>) maListViewPerso.getItemAtPosition(position);
			            	final String t[] =  map.get("titre").split("-");
			            	// showToast("LOOOOng clique");
			            	 if(t[0].trim().equals("AVI"))
			            	 {
			            	 final EditText input = new EditText(mm);
			            	 input.setText(OMXOPTION);
			            	 new AlertDialog.Builder(mm)
			            	    .setTitle("OMX param")
			            	    .setMessage("Custom OMX param :")
			            	    .setView(input)
			            	    .setPositiveButton("set default", new DialogInterface.OnClickListener() {
			            	        public void onClick(DialogInterface dialog, int whichButton) {
			            	            String value = input.getText().toString(); 
			            	            saveOMXOPTION(value);
			            	            PlayFile pf=new PlayFile(map.get("description"),t[0].trim(),value);
				 						new Thread(pf).start();
			            	        }
			            	    }).setNeutralButton("run once", new DialogInterface.OnClickListener() {
			            	        public void onClick(DialogInterface dialog, int whichButton) {
			            	        	 PlayFile pf=new PlayFile(map.get("description"),t[0].trim(),input.getText().toString());
					 						new Thread(pf).start();
			            	        }
			            	    }).setNegativeButton("+Playlist", new DialogInterface.OnClickListener() {
			            	        public void onClick(DialogInterface dialog, int whichButton) {
			            	        	PlayFile pf=new PlayFile(map.get("description"),"ADDPLAYLIST",input.getText().toString());
				 						new Thread(pf).start();
			            	        }
			            	    }).show();
			            	 }
			            	 return true;
			             }

			           });
		
		  if(docAlister.trim().equals(""))
		  {
			  
			  //Création de la ArrayList qui nous permettra de remplire la listView
			  //ArrayList<HashMap<String, String>> 
			  listItem = new ArrayList<HashMap<String, String>>();
			  
			  //	On déclare la HashMap qui contiendra les informations pour un item
			  HashMap<String, String> map;
 
			  //Création d'une HashMap pour insérer les informations du premier item de notre listView
			  map = new HashMap<String, String>();
			  map.put("titre", "DIR- /home");
			  map.put("description", "No dir...");
			  //on insère la référence à l'image (convertit en String car normalement c'est un int) que l'on récupérera dans l'imageView créé dans le fichier affichageitem.xml
			  map.put("img", String.valueOf(R.drawable.folder));
			  //	enfin on ajoute cette hashMap dans la arrayList
			  listItem.add(map);
			 
			  mSchedule.notifyDataSetChanged();
	        
			//  listRefresh( listItem);
			  
		  }else{
				  
				new Thread(new ReadReturnFIleThread()).start();	  
		  }
           
        
   	}
	public void listRefresh2()
	{
		 runOnUiThread(new Runnable() {
		        public void run()
		        {
		        	 mSchedule.notifyDataSetChanged();
		        	 ChooseFile.this.setTitle(docAlister);
		        }
		    });
		 
	}
	
	
	public void listRefresh3( final HashMap<String, String> map)
	{
		 runOnUiThread(new Runnable() {
		        public void run()
		        {
		        	listItem.add(map);
		        	// mSchedule.notifyDataSetChanged();
		        	ChooseFile.this.setTitle(docAlister);
		        }
		    });
		 
	}
	public void showToast(final String toast)
	{
	    runOnUiThread(new Runnable() {
	        public void run()
	        {
	            Toast.makeText(ChooseFile.this, toast, Toast.LENGTH_SHORT).show();
	        }
	    });
	}
	
	

	class ClientThread implements Runnable {

		@Override
		public void run() {

			try {
				InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

				socket = new Socket(serverAddr, SERVERPORT);
				
				try {
					out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				} catch (IOException e1) {
					e1.printStackTrace();
					showToast(e1.toString());
				}
				
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
				showToast(e1.toString());
			} catch (IOException e1) {
				e1.printStackTrace();
				showToast(e1.toString());
			} catch (Exception e) {
    			e.printStackTrace();
    			showToast(e.toString());
    		}

		}

	}
	
	class PlayFile implements Runnable {
		String leFile="";
		String leType="";
		String option="";
		public PlayFile (String f,String t,String option)
		{
			this.leFile=f;			
			this.leType=t;
			this.option=option;
		}
		
		@Override
		public void run() {

			
				try {
					if(socket==null)
					{
						InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

						socket = new Socket(serverAddr, SERVERPORT);
						
						try {
							out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
							in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						} catch (IOException e1) {
							e1.printStackTrace();
							showToast(e1.toString());
						}
						
					}
					
					if(this.leType.equals("TV"))
					{out.println("readsh|"+leFile);
					out.flush();
					}else if(this.leType.equals("AVI"))
					{
						out.println("readfile|"+leFile+"|"+this.option);
						out.flush();
					}else if(this.leType.equals("ALL"))
					{
						out.println("readall|"+leFile+"|"+this.option);
						out.flush();
					}else if(this.leType.equals("ADDPLAYLIST"))
					{
						out.println("ADDPLAYLIST|"+leFile+"|"+this.option);
						out.flush();
					}else if(this.leType.equals("PL"))
					{
						out.println("readall|"+leFile+"|"+this.option);
						out.flush();
					}
					//else OMX player...
					
				} catch (IOException e1) {
				
					e1.printStackTrace();
					 
				}
				
			//	showToast(retour);
			//	showToast("END Reading..");

		}
	}

	class ReadReturnFIleThread implements Runnable {
		String fullRetour="";
		
		@Override
		public void run() {

			
				try {
					if(socket==null)
					{
						InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

						socket = new Socket(serverAddr, SERVERPORT);
						
						try {
							out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
							in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						} catch (IOException e1) {
							e1.printStackTrace();
							showToast(e1.toString());
						}
						
					}
					
					while(lockList)
						try {
							this.wait(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					lockList=true;
					out.println("list|"+docAlister);
					out.flush();
					
					String r;
					showToast(docAlister);
					
					listItem.clear();
					//Creation de la liste :
					 //Création de la ArrayList qui nous permettra de remplire la listView ArrayList<HashMap<String, String>>
			       //  listItem = new ArrayList<HashMap<String, String>>();
			 
			        //On déclare la HashMap qui contiendra les informations pour un item
			        HashMap<String, String> map;
			        
			        //LE dossier parent :
			        map = new HashMap<String, String>();
					map.put("titre", "DIR- ../ ");
					String d="";
					String a[]=docAlister.split("/");
					for(int i=0;i<a.length-1;i++) 
						d+=(a[i].equals("")?"":"/"+a[i]);
					map.put("description", d);
					map.put("img", String.valueOf(R.drawable.folder));
					
					if(!d.equals(""))
						listItem.add(map);
			        
			 
			        //Création d'une HashMap pour insérer les informations du premier item de notre listView
			       			        
			        while((r= in.readLine())!=null)
					{	
			        	String retour[]=r.split("\\|");
						if(retour.length>=2)
						{
							 map = new HashMap<String, String>();
							map.put("titre", retour[0]+"- "+retour[2]);
							map.put("description", retour[1]);
							if(retour[0].equals("DIR"))
								map.put("img", String.valueOf(R.drawable.folder));
							else if(retour[0].equals("TV"))
								map.put("img", String.valueOf(R.drawable.tv));
							else if(retour[0].equals("AVI") || retour[0].equals("PL"))
								map.put("img", String.valueOf(R.drawable.video));
							else if(retour[0].equals("ERR")){
								map.put("img", String.valueOf(R.drawable.exclamation));
								refreshOnResum=true;
							}else map.put("img", String.valueOf(R.drawable.files));
							//listItem.add(map);
							listRefresh3(map);
						}
						if (r.trim().equals("OKEND"))
							break;
					}
			        
			       
			       // showToast(map.size()+" Elements");
			        //enfin on ajoute cette hashMap dans la arrayList
			        
			        listRefresh2();
			       // mSchedule.notifyDataSetChanged();
			       // listRefresh( listItem);
			       
					
					
				} catch (IOException e1) {
					fullRetour="ERREUR";
					e1.printStackTrace();
					showToast(e1.toString());
				}
				lockList=false;
			//	showToast(retour);
			//	showToast("END Reading..");

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
	    			            			
	    			try {
	    				String r;
	    				while((r= in.readLine())!=null)
	    				{	
	    					if (r.trim().equals("OKEND"))
	    						break;
	    				}
	    			} catch (IOException e1) {
	    				
	    				e1.printStackTrace();
	    				showToast(e1.toString());
	    			}
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