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

import wtf.omxclient.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity  {

	private Socket socket;
	
	
	
	private  int SERVERPORT = 3237;
	private   String SERVER_IP = "192.168.0.31";
	
	private PrintWriter out = null;
    private BufferedReader in = null;
    
    ConnectionDetector cd;
    
   


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		 Intent i = new Intent(MainActivity.this, SocketService.class);
		 startService(i);
		final Context mm=this;
		
		
		SharedPreferences sharedPref =this.getSharedPreferences("OMXclient",Context.MODE_PRIVATE);
		this.SERVER_IP= sharedPref.getString("IP", "192.168.0.1");
		this.SERVERPORT=  sharedPref.getInt("PORT", 3237);
		
		cd= new ConnectionDetector(this);
		if(cd.isConnectingToInternet())
		{
			String docAlister= sharedPref.getString("savedTree", "FIRSTSTART");	
			if(docAlister.equals("FIRSTSTART"))
			    startActivity(new Intent(mm, SettingActivity.class));
			else startActivity(new Intent(mm, RemoteControl.class));
		}
			
		
		Button b=(Button) findViewById(R.id.myButton);
		b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	try {
            		if(!cd.isConnectingToInternet())
            			return;
            		if(socket==null)
            			new Thread(new ClientThread()).start();
            		if(socket==null)
            			Toast.makeText(MainActivity.this, "Socket null...", 10).show();
            			
            		else{
            			EditText et = (EditText) findViewById(R.id.EditText01);
            			String str = et.getText().toString();
            			
            			out.println(str);
            			out.flush();
            			            			
            			new Thread(new ReadReturnThread()).start();
            			/*TReadReturn tr = new TReadReturn(MainActivity.this, in);
            			String retour=tr.retour;
            			Toast.makeText(MainActivity.this,retour, 10).show();
            			*/
            			//et.setText("key|");
        			}
        
        		} catch (Exception e) {
        			e.printStackTrace();
          			Toast.makeText(MainActivity.this, e.toString(), 10).show();
        		}
            }
          });
		
		Button c=(Button) findViewById(R.id.butremote);
        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	try {
            		if(socket!=null)
            			socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	Intent remoteIntent =new Intent(mm, RemoteControl.class); 
            		startActivity(remoteIntent);
            }
          });
        Button d=(Button) findViewById(R.id.butfile);
        d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	try {
            		if(socket!=null)
            			socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	Intent remoteIntent =new Intent(mm, ChooseFile.class); 
            		startActivity(remoteIntent);
            		
            }
          });
        
        Button e=(Button) findViewById(R.id.butparam);
        e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	try {
            		if(socket!=null)
            			socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	Intent paramIntent =new Intent(mm, SettingActivity.class); 
            		startActivity(paramIntent);
            }
          });
	}

	
	public void showToast(final String toast)
	{
	    runOnUiThread(new Runnable() {
	        public void run()
	        {
	            Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
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

	class ReadReturnThread implements Runnable {
		String retour="NO";
		@Override
		public void run() {

			
				try {
					String r;
					showToast("Reading..");
					while((r= in.readLine())!=null)
					{	retour+=" "+r;
					showToast("#"+r);
						if (r.trim().equals("OKEND"))
							break;
					}
				} catch (IOException e1) {
					retour="ERREUR";
					e1.printStackTrace();
					showToast(e1.toString());
				}
				
				showToast(retour);
				showToast("END Reading..");

		}

	}

	
}