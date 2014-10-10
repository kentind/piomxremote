package omxclient;

import wtf.omxclient.R;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingActivity extends Activity {
	
	
	public static final int MENU_REMOTE = Menu.FIRST;
	public static final int MENU_FILE = Menu.FIRST  + 1;
	public static final int MENU_PLAYLIST = Menu.FIRST + 2;
	
	private  int SERVERPORT = 3237;
	private String SERVER_IP = "192.168.0.31";
	String docAlister="";
	
	boolean refreshOnResum=false;
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(Menu.NONE, MENU_FILE, Menu.NONE, getString(R.string.libel_menu_choose_file));
        menu.add(Menu.NONE, MENU_REMOTE, Menu.NONE, getString(R.string.libel_menu_remote));
        menu.add(Menu.NONE, MENU_PLAYLIST, Menu.NONE, getString(R.string.libel_menu_playlist));
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case MENU_REMOTE:
            	startActivity(new Intent(this, RemoteControl.class));    
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
	
	public void saveParam(String leParam,String laValue)
    {
    	//sauvegarde du dossier en cour :
    	SharedPreferences sharedPref = this.getSharedPreferences("OMXclient",Context.MODE_PRIVATE);
		  SharedPreferences.Editor editor = sharedPref.edit();
		  editor.putString(leParam, laValue);
		  editor.commit();
    }
	
	public void saveParam(String leParam,int laValue)
    {
    	//sauvegarde du dossier en cour :
    	SharedPreferences sharedPref = this.getSharedPreferences("OMXclient",Context.MODE_PRIVATE);
		  SharedPreferences.Editor editor = sharedPref.edit();
		  editor.putInt(leParam, laValue);
		  editor.commit();
    }
	
	public String getParam(SharedPreferences sharedPref,String leParam,String laValueDefaut)
	{
		
		return sharedPref.getString(leParam, laValueDefaut);
	}

	public void loadPref()
    {
    	SharedPreferences sharedPref =this.getSharedPreferences("OMXclient",Context.MODE_PRIVATE);
		this.SERVER_IP=sharedPref.getString("IP", "192.168.0.1");
		this.SERVERPORT=sharedPref.getInt("PORT", 3237);
		this.docAlister= sharedPref.getString("savedTree", "/home");
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
    		setParamToInput();
    	}	  
    }
    
    public void setParamToInput()
    {
    	EditText txtIP = (EditText) findViewById(R.id.txtIP);
		txtIP.setText(this.SERVER_IP);
		
		EditText txtPort = (EditText) findViewById(R.id.txtPort);
		txtPort.setText(this.SERVERPORT+"");
		
		EditText txtDir = (EditText) findViewById(R.id.txtDir);
		txtDir.setText(this.docAlister);    	
    }
	public void saveAll()
	{
		
		EditText txtIP = (EditText) findViewById(R.id.txtIP);
    	saveParam("IP",txtIP.getText().toString());      
    	
    	EditText txtPort = (EditText) findViewById(R.id.txtPort);
    	saveParam("PORT",Integer.parseInt(txtPort.getText().toString()));   
    	
    	EditText txtDir = (EditText) findViewById(R.id.txtDir);
    	saveParam("savedTree",txtDir.getText().toString());   
	}
	@Override
	protected void onStop() {
		super.onStop();
		saveAll();		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		loadPref();
		final Context mm=this;
		
	//	saveParam("PORT",1);
		setParamToInput();
		
		((Button) findViewById(R.id.butSave)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	saveAll();
            	
        		startActivity(new Intent(mm, RemoteControl.class));
            }
          });
		
		((Button) findViewById(R.id.butSave2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	saveAll();
          
        		startActivity(new Intent(mm, ChooseFile.class));
            }
          });
		((Button) findViewById(R.id.butNoteTheApp)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	saveAll();
          
            	Intent intent =new Intent(Intent.ACTION_VIEW);
            	intent.setData(Uri.parse("market://details?id=wtf.omxclient"));
            	startActivity(intent);
            }
          });
	}

	

}
