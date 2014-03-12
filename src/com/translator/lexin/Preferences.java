/*
* Copyright 2014 - Dimitrios Meintanis 
* Webpage: http://www.meintanis.se/
* 
* 
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
* * Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimer.
* * Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimer in the
* documentation and/or other materials provided with the distribution.
* * Neither the name of ARM Limited nor the
* names of its contributors may be used to endorse or promote products
* derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL ARM LIMITED BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.translator.lexin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;

public class Preferences extends PreferenceActivity {
	//
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;
    private AsyncTask<String, String, String> DownloadAsyncTask;
    private boolean is_language_setup = true;
    private int download_data = 0;
    //
    /*
     *********************************************************************************
	 * 
	 * Function Name: onCreate
	 * Description  : Called when the activity is first created. This is the main
	 *                function of our application
	 * 
	 *********************************************************************************	
     *
     */
	@Override
    public void onCreate(Bundle savedInstanceState) {
		//
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.prefers);
        //
        Preference dialogPreference = (Preference) getPreferenceScreen().findPreference("dialog_preference");
        //
        File sf = new File("/data/data/com.translator.lexin/savedXML.dat");
		//
        if(!sf.exists()) dialogPreference.setEnabled(false);
        else dialogPreference.setEnabled(true);
        //
        dialogPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                   // dialog code here
               	   AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(Preferences.this);
              	   myAlertDialog.setTitle("Radera sparade orden");
              	   myAlertDialog.setMessage("Är du säker?");
              	   myAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   //
              	   public void onClick(DialogInterface arg0, int arg1) {
            		   //
            		   File inputFile = new File("/data/data/com.translator.lexin/savedXML.dat");
            		   //
                       inputFile.delete();
              		   //
                       Preference dp = (Preference) getPreferenceScreen().findPreference("dialog_preference");
                       dp.setEnabled(false);
                       //
              	    }});
              	   //
              	   myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
              	       //
              		   public void onClick(DialogInterface arg0, int arg1) {
              			   //      		   
              			   return; 
              		   }});
              	   //
              	   myAlertDialog.show();
              	   //              	   
                   return true;
                }
            });
        //
        //
        // Check saved variables if we need to disable some functions
        //
        SharedPreferences settings = getSharedPreferences("Lexin_DB", 0);
        boolean db_exists = settings.getBoolean("db_exists", false);
        //
        // DOWNLOAD the Database
        //
        dialogPreference = (Preference) getPreferenceScreen().findPreference("download_db");
        PreferenceGroup pg = (PreferenceGroup) findPreference("offline_parameters");
        //
        if(db_exists) {
        	//
        	dialogPreference.setEnabled(false);
        	pg.setEnabled(true);
        	//
        }
        else {
        	//
        	dialogPreference.setEnabled(true);
        	pg.setEnabled(false);
        	//
        }
        //
        dialogPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                   // 
                   is_language_setup = false;
      		       registerForContextMenu( getListView() );
      			   openContextMenu(getListView());
                   //
                   return true;
                }
            });
        //
        // DELETE the Database
        //
        dialogPreference = (Preference) getPreferenceScreen().findPreference("delete_db");
        //
        if(db_exists) dialogPreference.setEnabled(true);
        else dialogPreference.setEnabled(false);        
        //
        dialogPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                   // dialog code here
               	   AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(Preferences.this);
              	   myAlertDialog.setTitle("Radera Offline Engelsa DB");
              	   myAlertDialog.setMessage("Är du säker?");
              	   myAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   //
              	   public void onClick(DialogInterface arg0, int arg1) {
            		   //
            		   File extStore = Environment.getExternalStorageDirectory();
        			   File dbfile = new File(extStore.getPath() + "/Lexin_DB/Database.db"); 
            		   //
            		   dbfile.delete();
            		   //
            		   Preference pr = (Preference) getPreferenceScreen().findPreference("delete_db");
            		   pr.setEnabled(false);
            		   pr = (Preference) getPreferenceScreen().findPreference("download_db");
            		   pr.setEnabled(true);   
            		   //
            		   PreferenceGroup pg = (PreferenceGroup) findPreference("offline_parameters");
           			   pg.setEnabled(false);
              		   //
            	       SharedPreferences settings = getSharedPreferences("Lexin_DB", 0);
            	       SharedPreferences.Editor editor = settings.edit();
            	       editor.putBoolean("db_exists", false);
            	       editor.commit();
            	       //
            	       setupLanguageSummary();
            	       //
              	    }});
              	   //
              	   myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
              	       //
              		   public void onClick(DialogInterface arg0, int arg1) {
              			   //      		   
              			   return; 
              		   }});
              	   //
              	   myAlertDialog.show();
              	   //              	   
                   return true;
                }
            });
        //
        // Create Manualy the List
        // 
        dialogPreference = (Preference) getPreferenceScreen().findPreference("language_translation");
        dialogPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
               // 
               is_language_setup = true;
 		       registerForContextMenu( getListView() );
 			   openContextMenu(getListView());
               //
               return true;
            }
        });
        //
        setupLanguageSummary();
        //
        return; 
	}

	
	   /*********************************************************************************
	    * 
	    * Function Name:
	    * Description  :
	    * 
	    * 
	    *********************************************************************************/
	   private void setupLanguageSummary() {
		   //
		   CharSequence[] ValuesOL = getResources().getTextArray(R.array.languageValuesOL);
		   CharSequence[] NamesOL = getResources().getTextArray(R.array.languageNamesOL);
		   //
		   SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
           String lan = sharedPrefs.getString("language_translation", "swe_alb");
		   //
	       SharedPreferences settings = getSharedPreferences("Lexin_DB", 0);
	       boolean db_exists = settings.getBoolean("db_exists", false);
	       //
	       // If we have deleted the database and we had selected it before that,
	       // then setup the language on default value
	       //
	       if((!db_exists) && (lan.equals("swe_eng_off"))) {
	    	   //
	    	   SharedPreferences.Editor editor = sharedPrefs.edit();
	    	   editor.putString("language_translation", "swe_alb");
	    	   lan = "swe_alb";
	    	   editor.commit();
	    	   //
	       }
           //
           int lanID = 0;
           for( ; lanID < ValuesOL.length; lanID++) {
        	   //
        	   if(ValuesOL[lanID].toString().equals(lan)) break;
        	   //
           }
           //
    	   Preference dialog = (Preference) getPreferenceScreen().findPreference("language_translation");
    	   dialog.setSummary(NamesOL[lanID]);
           //
		   return;
	   }
	
	   /*********************************************************************************
	    * 
	    * Function Name:
	    * Description  :
	    * 
	    * 
	    *********************************************************************************/
	   
	   @Override  
	   public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
	       //
	       super.onCreateContextMenu(menu, v, menuInfo);  
	       //
	       if( is_language_setup == true ) {
	    	   //
	    	   menu.setHeaderTitle("Välj Språk");  
		       //
	     	   CharSequence[] NamesOL = getResources().getTextArray(R.array.languageNamesOL);
			   CharSequence[] Names = getResources().getTextArray(R.array.languageNames);
	           //
		       SharedPreferences settings = getSharedPreferences("Lexin_DB", 0);
		       boolean db_exists = settings.getBoolean("db_exists", false);
		       //
	    	   if(db_exists) {
	    		   //
	    		   for(int i=0; i< NamesOL.length ;i++){
	    	    	   //
	    	    	   menu.add(0,i,i, NamesOL[i].toString());
	    	    	   //
	    	       }
	    		   //
	    	   } else {
	    		   //
	    		   for(int i=0; i< Names.length ;i++){
	    	    	   //
	    	    	   menu.add(0,i,i, Names[i].toString());
	    	    	   //
	    	       }
	    		   //
	    	   }
	       } else {
	    	   //
	    	   menu.setHeaderTitle("Välj Databas");
	    	   menu.add(0,0,0, "Sv -> En (5.8Mb)");
	    	   menu.add(0,1,1, "Sv -> En:Lite (3.2Mb)");
	    	   //
	       }
    	   //
    	   return;
	   }  
	   
	   /*********************************************************************************
	    * 
	    * Function Name:
	    * Description  :
	    * 
	    * 
	    *********************************************************************************/
	   
	   @Override
	   public boolean onContextItemSelected(MenuItem item) {
	       //
		   if( is_language_setup == true ) {
			   //
	     	   CharSequence[] NamesOL = getResources().getTextArray(R.array.languageNamesOL);
			   CharSequence[] Names = getResources().getTextArray(R.array.languageNames);
	     	   CharSequence[] ValuesOL = getResources().getTextArray(R.array.languageValuesOL);
			   CharSequence[] Values = getResources().getTextArray(R.array.languageValues);
			   //
		       String str = item.getTitle().toString();
	           //
		       SharedPreferences settings = getSharedPreferences("Lexin_DB", 0);
		       boolean db_exists = settings.getBoolean("db_exists", false);
		       //
			   SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			   SharedPreferences.Editor editor = sharedPrefs.edit();
			   //
	    	   if(db_exists) {
	    		   //
	    		   for(int i=0; i< NamesOL.length ;i++){
	    	    	   //
	                   if(str.equals(NamesOL[i].toString())) {
	                	   //
	            		   editor.putString("language_translation", ValuesOL[i].toString());
	            		   //
	                   }
	    	    	   //
	    	       }
	    		   //
	    	   } else {
	    		   //
	    		   for(int i=0; i< Names.length ;i++){
	    	    	   //
	                   if(str.equals(Names[i].toString())) {
	                	   //
	            		   editor.putString("language_translation", Values[i].toString());
	            		   //
	                   }    			   
	    			   //
	    	       }
	    		   //
	    	   }
	    	   //
	    	   editor.commit();
		       //
	    	   setupLanguageSummary();
		       //
		   } else {
			   //
			   int i = item.getItemId();
			   String url = "";
			   //
			   switch(i){
			      //
			      case 0: url = "http://www.meintanis.se/files/Lexin_SVEN.db"; break;
			      case 1: url = "http://www.meintanis.se/files/Lexin_SVEN_Lite.db"; break;
			   }
			   //
			   startDownload(url);
			   //
		   }
	       return true;
	   }	
	
   /*********************************************************************************
	* 
	* Function Name:
	* Description  :
	* 
	* 
    *********************************************************************************/
	
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
		case DIALOG_DOWNLOAD_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("Hämtar DataBase fil...\nVar god vänta...");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setProgress(0);
			//
			mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			     public void onClick(DialogInterface dialog, int which) {
			        //
			     	DownloadAsyncTask.cancel(true);
			    	//
			        File extStore = Environment.getExternalStorageDirectory();
				    File dbfile = new File(extStore.getPath() + "/Lexin_DB/Database.db"); 
					//
					dbfile.delete();
			    	//
			    	mProgressDialog.dismiss(); 
			    	//
			    }
			});
			//
			mProgressDialog.show();
			//
			return mProgressDialog;
		default:
			return null;
        }
    }	
    
   /*********************************************************************************
	* 
	* Function Name:
	* Description  :
	* 
	* 
    *********************************************************************************/
	  	
    private boolean startDownload( String url ) {
    	//
    	String state = Environment.getExternalStorageState();
    	//
    	if( state.equals(Environment.MEDIA_UNMOUNTED)) {
    		//
            AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
            // 
            alertbox.setMessage("Ingen SD kort!");
            alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() { public void onClick(DialogInterface arg0, int arg1) {} });
            // 
            alertbox.show();
    		//
    		return false;
    	}
        //
    	download_data = 0;
        DownloadAsyncTask = new DownloadFileAsync().execute(url);
        //
        return true;
    }
	
	   
    /*********************************************************************************
 	* 
 	* Class Name:
 	* Description  :
 	* 
 	* 
     *********************************************************************************/
 	  	
	class DownloadFileAsync extends AsyncTask<String, String, String> {
		   
		//
        private boolean running = true;
        //
		/*********************************************************************************
		 * 
		 * Function Name:
		 * Description  :
		 * 
		 * 
		 *********************************************************************************/
			  	
	    @Override
	    protected void onCancelled() {
	    	//
	        running = false;
	        //
	    }
	    
		/*********************************************************************************
		 * 
		 * Function Name:
		 * Description  :
		 * 
		 * 
		 *********************************************************************************/
			  	
		@Override
		protected void onPreExecute() {
			//
			super.onPreExecute();
			//
			showDialog(DIALOG_DOWNLOAD_PROGRESS);
			//
		}
		   
		/*********************************************************************************
		 * 
		 * Function Name:
		 * Description  :
		 * 
		 * 
		 *********************************************************************************/
			
		@Override
		protected String doInBackground(String... aurl) {
			//
			int count;
			//
			File extStore = Environment.getExternalStorageDirectory();
			File folder = new File(Environment.getExternalStorageDirectory() + "/Lexin_DB");
			//
			if (!folder.exists()) folder.mkdir();
			//
			File dbfile = new File(extStore.getPath() + "/Lexin_DB/Database.db");
			//
			// prepare for a progress bar dialog
            //
			try {

				URL url = new URL(aurl[0]);
				URLConnection conexion = url.openConnection();
				conexion.connect();

				int lenghtOfFile = conexion.getContentLength();

				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(dbfile.toString());

				byte data[] = new byte[1024];

				long total = 0;

				while (((count = input.read(data)) != -1) && ( running == true )) {
					total += count;
					publishProgress(""+(int)((total*100)/lenghtOfFile));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
				
			} catch (Exception e) {}
			//
			return null;
		}
		   
		/*********************************************************************************
		 * 
		 * Function Name:
		 * Description  :
		 * 
		 * 
		 *********************************************************************************/
						
		protected void onProgressUpdate(String... progress) {
			//
			download_data = Integer.parseInt(progress[0]);
			mProgressDialog.setProgress(download_data);
			//
		}
		   
		/*********************************************************************************
		 * 
		 * Function Name:
		 * Description  :
		 * 
		 * 
		 *********************************************************************************/
			
		@Override
		protected void onPostExecute(String unused) {
			//
			if(download_data > 0) {
				//
				Preference pr = (Preference) getPreferenceScreen().findPreference("delete_db");
				pr.setEnabled(true);
				pr = (Preference) getPreferenceScreen().findPreference("download_db");
				pr.setEnabled(false);   
				//
				PreferenceGroup pg = (PreferenceGroup) findPreference("offline_parameters");
				pg.setEnabled(true);
				//
	 	        SharedPreferences settings = getSharedPreferences("Lexin_DB", 0);
	 	        SharedPreferences.Editor editor = settings.edit();
	 	        editor.putBoolean("db_exists", true);
	  	        editor.commit();
	  	        //
			}
			//
			dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
			//
		}
	}


	   
}
