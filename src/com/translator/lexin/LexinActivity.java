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

import android.app.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Xml;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import android.preference.PreferenceManager;
//import android.provider.Settings;

public class LexinActivity extends Activity implements Runnable{

   private ListView mainListView;  
   private String ReplyStr;
   private ArrayList<HashMap<String, String>> menuItems;
   private String Language; 
   private EditText input_word;
   private ProgressDialog m_ProgressDialog = null;
   
   static final boolean ENABLE_LICENSE = false;
   static final boolean SYSTEM_DEBUG = false;
   static final String APPLICATION_VERSION = "v1.37";
   //
   static final String KEY_WORD = "Word";
   static final String KEY_TRANSLATE = "Translation";
   static final String KEY_XML = "xml";
   static final String KEY_ID = "word_id";
   static final String KEY_MODE = "display_mode";
   //
   private boolean db_search_like = false;
   private boolean db_search_no_internet = false;
   private boolean offline_search = false;
   private SQLiteDatabase db;
   //
   ArrayList<String> Proposal = new ArrayList<String>();
   //   
   
   /*********************************************************************************
   * 
   * Function Name:
   * Description  :
   * 
   * 
   *********************************************************************************/

   @Override
   public void onResume()
   {
	   //
       super.onResume();
       //
       UpdateVariables();
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
   public void onCreate(Bundle savedInstanceState) {
	   //
       super.onCreate(savedInstanceState);
       setContentView(R.layout.main);
       // 
       // Sound Control
       setVolumeControlStream(AudioManager.STREAM_MUSIC);
       //
       // Get saved data
       //
       UpdateVariables();
	   //
       // Find the ListView resource.
       //
       mainListView = (ListView) findViewById( R.id.mainListView );  
       menuItems = new ArrayList<HashMap<String, String>>();
       //
       ListAdapter adapter = new SimpleAdapter(this, menuItems,R.layout.rowlayout,
               new String[] { KEY_WORD, KEY_TRANSLATE }, new int[] {R.id.input_word, R.id.translation });

       // Set the ArrayAdapter as the ListView's adapter.  
       mainListView.setAdapter(adapter);    
       //
       mainListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
       	//
       public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
     	          //
                  // Launching new Activity on selecting single List Item
                  Intent i = new Intent(getApplicationContext(), DisplayWordActivity.class);
                  // sending data to new activity
                  i.putExtra(KEY_XML,  ReplyStr);
                  i.putExtra(KEY_ID,   position);
                  i.putExtra(KEY_MODE, "NORMAL");
                  startActivity(i);
                  
            }   	
       });
       //       
   }

   /*********************************************************************************
    * 
    * Function Name:
    * Description  :
    * 
    * 
    *********************************************************************************/
   
   public void UpdateVariables() {
	   //
       // Check to see if we have a local database
       //
       File extStore = Environment.getExternalStorageDirectory();
       File dbfile = new File(extStore.getPath() + "/Lexin_DB/Database.db");    	   
       //
       SharedPreferences settings = getSharedPreferences("Lexin_DB", 0);
       //
	   if(dbfile.exists() && settings.getBoolean("db_exists", false)) {
		   //
		   if(db == null) db = SQLiteDatabase.openDatabase(dbfile.toString() , null, Context.MODE_PRIVATE);
		   else if(db.isOpen()) {
			   //
			   // Reopen Database
			   //
			   db.close();
			   db = SQLiteDatabase.openDatabase(dbfile.toString() , null, Context.MODE_PRIVATE);
			   //
		   }
		   //
	   } else {
		   //
		   db = null;
	       SharedPreferences.Editor editor = settings.edit();
		   editor.putBoolean("db_exists", false);
		   editor.commit();
		   //		   
	   }
	   //	   
	   SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	   Language = sharedPrefs.getString("language_translation", "swe_alb");
	   db_search_like = sharedPrefs.getBoolean("search_like", false);
	   db_search_no_internet = sharedPrefs.getBoolean("noconnection_db", false);
	   //
	   String packageName = getPackageName();
	   int resId = getResources().getIdentifier(Language, "string", packageName);
	   //
	   this.setTitle("Lexin 2 - (svenska / " + getString(resId) + ")");
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
   
   private void send_query_lexin2(){
       //
	   URLConnection connection;
	   //
	   try{
		  //
		  String content = "7|0|7|http://lexin2.nada.kth.se/lexin/lexin/|FCDCCA88916BAACF8B03FB48D294BA89|"+
		             "se.jojoman.lexin.lexingwt.client.LookUpService|lookUpWord|"+
		             "se.jojoman.lexin.lexingwt.client.LookUpRequest/682723451|" +
		             Language + "|"+ input_word.getText() + "|1|2|3|4|1|5|5|1|6|0|7|\n";
		  //
		  connection = new URL("http://lexin2.nada.kth.se/lexin/lexin/lookupword").openConnection();
		  connection.setConnectTimeout(10000);
		  connection.setReadTimeout(10000);
		  //
		  connection.setDoOutput(true);
		  connection.setRequestProperty("Content-Type", "text/x-gwt-rpc; charset=utf-8");
		  connection.setRequestProperty("X-GWT-Permutation", "94C8E7B4847149EB6873941CE873571F");
		  connection.setRequestProperty("X-GWT-Module-Base", "http://lexin2.nada.kth.se/lexin/lexin/");
          //
		  // Write body
		  OutputStream output = connection.getOutputStream(); 
          output.write(content.getBytes());
		  output.flush();
		  ReplyStr = "";
		  //
		  BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
		  String inputLine;
		  //
          while ((inputLine = in.readLine()) != null) ReplyStr = ReplyStr + inputLine; 
          //
		  output.close();
          in.close();	   
	   } catch (Throwable t) {
			 // TODO Auto-generated catch block
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
   
   private void send_query_lexin(){
       //
	   URLConnection connection;
	   //
	   try{
		  //
		  String content = "7|0|6|http://folkets-lexikon.csc.kth.se/folkets/folkets/|1F6DF5ACEAE7CE88AACB1E5E4208A6EC|" +
		                	   "se.algoritmica.folkets.client.LookUpService|lookUpWord|" +
		                	   "se.algoritmica.folkets.client.LookUpRequest/1089007912|" +
		                	   input_word.getText() +
		                	   "|1|2|3|4|1|5|5|1|0|0|6|\n";
		  //		  		 
		  connection = new URL("http://folkets-lexikon.csc.kth.se/folkets/folkets/lookupword").openConnection();
		  connection.setConnectTimeout(10000);
		  connection.setReadTimeout(10000);
		  //
		  connection.setDoOutput(true);
		  connection.setRequestProperty("Content-Type", "text/x-gwt-rpc; charset=utf-8");
		  connection.setRequestProperty("X-GWT-Permutation", "D88EC72BE0FA10F91FDC911C7757B7F3");
		  connection.setRequestProperty("X-GWT-Module-Base", "http://folkets-lexikon.csc.kth.se/folkets/folkets/");
          //
		  // Write body
		  OutputStream output = connection.getOutputStream(); 
          output.write(content.getBytes());
		  output.flush();
		  ReplyStr = "";
		  //
		  BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
		  String inputLine;
		  //
          while ((inputLine = in.readLine()) != null) ReplyStr = ReplyStr + inputLine; 
          //
		  output.close();
          in.close();	   
	   } catch (Throwable t) {
			 // TODO Auto-generated catch block
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
   private String parse_cursor(Cursor ReplyCur) {
	   //
	   String NewReplyStr = "";
	   //
		   for(int current = 0; current < ReplyCur.getCount(); current++) {
 			   //
 			   ReplyCur.moveToPosition(current);
 			   //
 			   XmlSerializer bls = Xml.newSerializer();
 	 		   StringWriter blw = new StringWriter();
 	 		   XmlSerializer tls = Xml.newSerializer();
 	 		   StringWriter tlw = new StringWriter();
 	 		   //
 	 		   Cursor Translations = db.rawQuery("SELECT * FROM Translations WHERE ID = '" + ReplyCur.getString(0) + "';", null);
 	 		   Cursor Examples = db.rawQuery("SELECT * FROM Examples WHERE ID = '" + ReplyCur.getString(0) + "';", null);
 	 		   Cursor Definitions = db.rawQuery("SELECT * FROM Definitions WHERE ID = '" + ReplyCur.getString(0) + "';", null);
 	 		   Cursor Inflection = db.rawQuery("SELECT * FROM Paradigms WHERE ID = '" + ReplyCur.getString(0) + "';", null);
 	 		   Cursor Compounds = db.rawQuery("SELECT * FROM Compounds WHERE ID = '" + ReplyCur.getString(0) + "';", null);
	 		   Cursor Idioms = db.rawQuery("SELECT * FROM Idioms WHERE ID = '" + ReplyCur.getString(0) + "';", null);
 	 		   //
 	 		   try {
 	 			   //
 	 			   bls.setOutput(blw);
 	 			   bls.startDocument("UTF-8", false);
 	 			   bls.startTag("", "Word");
 	 			   //
 	 			   tls.setOutput(tlw);
 	 			   tls.startDocument("UTF-8", false);
 	 			   tls.startTag("", "TargetLang");
 	 			   //
 	 			   bls.attribute("", "Value", ReplyCur.getString(1));
 	 			   //
 	 			   String word_type = ReplyCur.getString(2);
 	 			   //
 	 	 	 	   if(word_type.equals("nn"))  bls.attribute("", "Type", "subst.");
 	 	 	 	   if(word_type.equals("vb"))  bls.attribute("", "Type", "verb.");
 	 	 	 	   if(word_type.equals("jj"))  bls.attribute("", "Type", "adjektiv.");
 	 	 	 	   if(word_type.equals("in"))  bls.attribute("", "Type", "interj.");
 	 	 	 	   if(word_type.equals("pn"))  bls.attribute("", "Type", "prepos.");
 	 	 	 	   if(word_type.equals("ab"))  bls.attribute("", "Type", "adverb.");
 	 	 	 	   if(word_type.equals("kn"))  bls.attribute("", "Type", "konjun.");
 	 	 	 	   //
 	 			   bls.startTag("", "BaseLang");
 	 			   //
 	 			   if(!ReplyCur.getString(4).equals("")){
 	 				   //
 	 			       bls.startTag("", "Comment");
 	 			       bls.text( ReplyCur.getString(4) );
 	 			       bls.endTag("", "Comment");
 	 			       //
 	 			   }
 	 			   //
 	 			   String word_phonetic = ReplyCur.getString(3);
 	 			   //
 	 	 	 	   if(word_phonetic != null){
 	 	 	 		   //
 	 	 	 	  	   bls.startTag("", "Phonetic"); 	 	 	   	   
 	  	 	 	       bls.text(word_phonetic);
 	  	 	 	       bls.endTag("", "Phonetic");
 	  	 	 	       // 	 	 		   
 	 	 	 	   }
 	 	 	 	   //
 	 	 	 	   for(int i=0; i< Inflection.getCount(); i++){
 	 	 	 		   //
 	 	 	 		   Inflection.moveToPosition(i);
 	 	 	 		   //
 	 	 	 		   if(!Inflection.getString(1).equals(ReplyCur.getString(1))){
 	 	 	 			   //
 	 	 	 			   bls.startTag("", "Inflection"); 	 	 	   	   
  	  	 	 	           bls.text(Inflection.getString(1));
  	  	 	 	           bls.endTag("", "Inflection");
  	  	 	 	           //
 	 	 	 		   } 	 	 	 	  	   
 	  	 	 	       // 	 	 		   
 	 	 	 	   }
 	 	 	 	   //
 	 	 	 	   if(SYSTEM_DEBUG) Log.d("DB", String.format("DB: Got Translation %d results", Translations.getCount()));
 	 	 	 	   //
 	 	 	 	   tls.startTag("", "Translation");
 	 	 	 	   for(int i=0; i< Translations.getCount(); i++){
 	 	 	 		   //
 	 	 	 		   Translations.moveToPosition(i);
 	 	 	 		   //
 	 	 	 		   if (i == 0) tls.text(Translations.getString(1));
 	 	 	 		   else tls.text(", " + Translations.getString(1));
 	 	 	 		   //
 	 	 	 	   }
 	 	 	 	   tls.endTag("", "Translation");
 	 	 	 	   //
 	 	 	 	   tls.startTag("", "Comment");
 	 	 	 	   for(int i=0; i< Translations.getCount(); i++){
 	 	 	 		   //
 	 	 	 		   Translations.moveToPosition(i);
 	 	 	 		   //
 	 	 	 		   if (i == 0) tls.text(Translations.getString(2));
 	 	 	 		   else tls.text(", " + Translations.getString(2));
 	 	 	 		   //
 	 	 	 	   }
 	 	 	 	   tls.endTag("", "Comment");
 	               //
 	 	 	 	   for(int i=0; i< Examples.getCount(); i++){
 	 	 	 		   //
 	 	 	 		   Examples.moveToPosition(i);
 	 	 	 		   //
 	 	 	 		   bls.startTag("", "Example");
 	 	 	 	       bls.text(Examples.getString(1));
 	 	 	 	       bls.endTag("", "Example");
 	 	 	 		   //
 	 	 	 		   tls.startTag("", "Example");
  	 	 	 	       tls.text(Examples.getString(2));
  	 	 	 	       tls.endTag("", "Example");
 	 	 	 		   //
 	 	 	 	   }
 	               //
 	 	 	 	   for(int i=0; i< Definitions.getCount(); i++){
 	 	 	 		   //
 	 	 	 		   Definitions.moveToPosition(i);
 	 	 	 		   //
 	 	 	 		   bls.startTag("", "Meaning");
 	 	 	 	       bls.text(Definitions.getString(1));
 	 	 	 	       bls.endTag("", "Meaning");
 	 	 	 		   //
 	 	 	 		   tls.startTag("", "Synonym");
  	 	 	 	       tls.text(Definitions.getString(2));
  	 	 	 	       tls.endTag("", "Synonym");
  	 	 	 	       //
 	 	 	 	   } 
 	 	 	 	   //
 	 	 	 	   for(int i=0; i< Idioms.getCount(); i++){
 	 	 	 		   //
 	 	 	 		   Idioms.moveToPosition(i);
 	 	 	 		   //
 	 	 	 		   bls.startTag("", "Idiom");
 	 	 	 	       bls.text(Idioms.getString(1));
 	 	 	 	       bls.endTag("", "Idiom");
 	 	 	 		   //
 	 	 	 		   tls.startTag("", "Idiom");
  	 	 	 	       tls.text(Idioms.getString(2));
  	 	 	 	       tls.endTag("", "Idiom");
  	 	 	 	       //
 	 	 	 	   }  
 	 	 	 	   //
 	 	 	 	   for(int i=0; i< Compounds.getCount(); i++){
 	 	 	 		   //
 	 	 	 		Compounds.moveToPosition(i);
 	 	 	 		   //
 	 	 	 		   bls.startTag("", "Compound");
 	 	 	 	       bls.text(Compounds.getString(1));
 	 	 	 	       bls.endTag("", "Compound");
 	 	 	 		   //
 	 	 	 		   tls.startTag("", "Compound");
  	 	 	 	       tls.text(Compounds.getString(2));
  	 	 	 	       tls.endTag("", "Compound");
  	 	 	 	       //
 	 	 	 	   }  	 	 	 	 	   
 	 	 	 	   //
 	 	 	 	   bls.endTag("", "BaseLang");
 	 	 	 	   bls.endTag("", "Word");
 	 	 	 	   bls.endDocument();
 	  			   tls.endTag("", "TargetLang");
 	  			   tls.endDocument();
 	  			   //
 	  			   // Concatenate the two strings
 	  			   //
 	  			   String tmp1 = blw.toString();
 	  			   int t1 = tmp1.indexOf("<Word");
 	  			   String tmp2 = tlw.toString();
 	  			   int t2 = tmp2.indexOf("<TargetLang>");
 	  			   tmp1 = tmp1.substring(t1, tmp1.length() - 7);
 	  			   tmp2 = tmp2.substring(t2, tmp2.length());
 	  			   NewReplyStr = NewReplyStr + tmp1 + tmp2 + "</Word>";
 	  			   //
 	 		   } catch (Exception e) {
 	 			   //
 	 			   e.getStackTrace();
 	 			   NewReplyStr = "";
 	 			   //
 	 		   }  
 			   
 		   }
	   //
	   return NewReplyStr;
   }
   
   /*********************************************************************************
    * 
    * Function Name:
    * Description  :
    * 
    * 
    *********************************************************************************/
   
   public void query_database() {
	   //
 	   String CurReplyStr = "";
 	   Cursor ReplyCur;
 	   //
 	   String expr = input_word.getText().toString();
 	   String query = "SELECT * FROM Words WHERE Native = '" + expr + "';";
 	   String queryP = "SELECT * FROM Paradigms WHERE Inflection = '" + expr + "';";
 	   //
 	   // If we have selected to use Wildcards
 	   //
 	   if(db_search_like) {
 		   //
           if(expr.startsWith("*")) {
    	       //
    	       if(expr.endsWith("*")) {
    	    	   //
    	    	   expr = expr.substring(1, expr.length() - 1);
    	    	   query = "SELECT * FROM Words WHERE Native LIKE '%" + expr + "%';";
    	    	   queryP = "SELECT * FROM Paradigms WHERE Inflection LIKE '%" + expr + "%';";
    	    	   //
    	       } else {
    	    	   //
    	    	   expr = expr.substring(1, expr.length());
    	    	   query = "SELECT * FROM Words WHERE Native LIKE '%" + expr + "';";
    	    	   queryP = "SELECT * FROM Paradigms WHERE Inflection LIKE '%" + expr + "';";
    	    	   //
    	       }
    	       //
           } else if(expr.endsWith("*")){
    	       //
	    	   expr = expr.substring(0, expr.length() - 1);
	    	   query = "SELECT * FROM Words WHERE Native LIKE '" + expr + "%';";
	    	   queryP = "SELECT * FROM Paradigms WHERE Inflection LIKE '" + expr + "%';";
    	       //
           }
           //
 	   } 
 	   //
 	   if(db != null) if(db.isOpen()) {
 		   //
           ReplyCur = db.rawQuery(query, null);
 		   //
 		   if(ReplyCur.getCount() == 0) {
 			   //
 			   Cursor Paradigms = db.rawQuery(queryP, null);
 			   //
 			   if(Paradigms.getCount() > 0) {
 				   //
 				   Paradigms.moveToFirst();
 				   ReplyCur = db.rawQuery("SELECT * FROM Words WHERE ID = '" + Paradigms.getString(0) + "';", null);
 				   //
 			   }
 			   //
 		   }
 		   //
 		   CurReplyStr = parse_cursor(ReplyCur);
 		   //
 	   }
 	   //
 	   // Add the XML values
 	   //
 	   ReplyStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><result>" + CurReplyStr + "</result>";
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
   
   public void run() {
	   //
   	   input_word = (EditText) findViewById( R.id.wordfortranslation); 
       // 
       if(input_word.getText() == null) return;
       //
       if(offline_search || Language.equals("swe_eng_off")) {
    	   //
    	   query_database();
    	   //
       } else {
    	   //
           if(Language.equals("swe_eng")) send_query_lexin();
           else send_query_lexin2();
           //
       }
       //
       // signaling things to the outside world goes like this
       threadHandler.sendEmptyMessage(0);
       //
   }
   
   /*********************************************************************************
    * 
    * Function Name:
    * Description  :
    * 
    * 
    *********************************************************************************/
      
   private void parseXML(String str){
	   //
	   XMLParser parser = new XMLParser();
	   Document doc;
	   NodeList nl;
	   //
	   try {
		   //
		   doc = parser.getDomElement(str);
		   nl = doc.getElementsByTagName(KEY_WORD);
		   //
	   } catch (Throwable t) {
			// 
		    t.printStackTrace();
		    Toast.makeText(LexinActivity.this, "XML Parse error..", Toast.LENGTH_SHORT).show();
		    return;
	   }
	   //
	   // looping through all item nodes <Word>
	   //
	   for (int i = 0; i < nl.getLength(); i++) {
		   //
		   Element e = (Element) nl.item(i);
		   //
	       String translation = parser.getValue(e, KEY_TRANSLATE); // name child value
	       String word_in = e.getAttribute("Value").toString();
	       //
	       if(translation.length() > 30) translation = translation.substring(0, 30) + "...";
	       //
	       HashMap<String, String> map = new HashMap<String, String>();
	       map.put(KEY_WORD, word_in);
	       map.put(KEY_TRANSLATE, translation);
	       menuItems.add(map);
	       //
	   }
	   mainListView.invalidateViews();
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
      
   private String Translate_XML_Data(String str){
	   //
	   XMLParser parser = new XMLParser();
	   Document doc;
	   NodeList nl;
 	   String NewReplyStr = "";
 	   String word_in = "";
	   //
	   try {
 	         doc = parser.getDomElement(str);
 	         nl = doc.getElementsByTagName("word");
 	   //
 	   if( nl != null) for(int current=0; current<nl.getLength(); current++){
 		   //
 		   XmlSerializer bls = Xml.newSerializer();
 		   StringWriter blw = new StringWriter();
 		   XmlSerializer tls = Xml.newSerializer();
 		   StringWriter tlw = new StringWriter();	   
           //
 		   try {
 			   //
 			   bls.setOutput(blw);
 			   bls.startDocument("UTF-8", false);
 			   bls.startTag("", "Word");
 			   //
 			   tls.setOutput(tlw);
 			   tls.startDocument("UTF-8", false);
 			   tls.startTag("", "TargetLang");
 			   //
 	 	       Element e_Word = (Element) nl.item(current);     
 	 	       NodeList word_nodes = e_Word.getChildNodes(); 
 	 	       //
 	 	       word_in = e_Word.getAttribute("value").toString();
 	 	       bls.attribute("", "Value", word_in);
 	 	       //
 	 	 	   String word_type = e_Word.getAttribute("class").toString();
 	 	 	   //
 	 	 	   if(word_type.equals("nn"))  bls.attribute("", "Type", "subst.");
 	 	 	   if(word_type.equals("vb"))  bls.attribute("", "Type", "verb.");
 	 	 	   if(word_type.equals("jj"))  bls.attribute("", "Type", "adjektiv.");
 	 	 	   if(word_type.equals("in"))  bls.attribute("", "Type", "interj.");
 	 	 	   if(word_type.equals("pn"))  bls.attribute("", "Type", "prepos.");
 	 	 	   if(word_type.equals("ab"))  bls.attribute("", "Type", "adverb.");
 	 	 	   if(word_type.equals("kn"))  bls.attribute("", "Type", "konjun.");
 	 	 	   //
 	 	 	   bls.startTag("", "BaseLang");
 	 	 	   //
 	 	 	   // Phonetic
 	 	 	   //
 	 	 	   Element e_phonetic = (Element) (((NodeList) e_Word.getElementsByTagName("phonetic")).item(0));
 	 	 	   //
 	 	 	   if(e_phonetic != null){
 	 	 		   //
 	 	 	  	   bls.startTag("", "Phonetic"); 	 	 	   	   
  	 	 	       String word_phonetic = e_phonetic.getAttribute("value"); 	 	 	   
  	 	 	       String sound_file = e_phonetic.getAttribute("soundFile");
  	 	 	       bls.attribute("", "FILE", sound_file);
  	 	 	       bls.text(word_phonetic);
  	 	 	       bls.endTag("", "Phonetic");
  	 	 	       // 	 	 		   
 	 	 	   }
 	 	 	   //
 	 	 	   String translation = ""; 	 	   
 	 	 	   //
 	 	 	   for(int i=0;i<word_nodes.getLength();i++){
 	 	 		   //
 	 	 		   // Translation
 	 	 		   //
 	 	 		   if(word_nodes.item(i).getNodeName().equals("translation")) {
 	 	 			   //
 	 	 			   Element e_tr = (Element) word_nodes.item(i);
 	 	 			   if(!translation.equals("")) translation = translation + ", ";
 	 	 			   translation = translation + e_tr.getAttribute("value");
 	 	 			   //
 	 	 		   }
 	 	 		   //
 	 	 		   // Paradigm
 	  	 		   //
 	 	 		   if(word_nodes.item(i).getNodeName().equals("paradigm")) {
 	 	 			   //
 	 	 			   NodeList parad = word_nodes.item(i).getChildNodes();
 	 	 			   //
 	 	 			   for(int j = 0; j < parad.getLength(); j++){
 	 	 				   //
 	 	 				   Element e_tr = (Element) parad.item(j);
 	 	 	 			   if(!e_tr.getAttribute("value").equals(word_in)){
 	 	 	 				   //
 	 	 				       bls.startTag("", "Inflection"); 	 	 	 			   
 	 	 	 			       bls.text(e_tr.getAttribute("value"));
 	 	 	 			       bls.endTag("", "Inflection");
 	 	 	 			       //
 	 	 	 			   }
                           //
 	 	 			   }
 	 	 			   //
 	 	 		   }
 	 	 		   //
 	 	 		   // Definition
 	  	 		   //
 	 	 		   if(word_nodes.item(i).getNodeName().equals("definition")) {
 	 	 			   //
 	 	 			   bls.startTag("", "Meaning");
 	 	 			   Element e_tr = (Element) word_nodes.item(i);
 	 	 			   bls.text(e_tr.getAttribute("value"));
 	 	 			   bls.endTag("", "Meaning");
 	 	 			   // 	 	 			   
 	 	 			   Element e_tt = (Element) e_tr.getChildNodes().item(0);
 	 	 			   //
 	 	 			   if(e_tt != null) {
 	 	 				   //
 	 	 				   tls.startTag("", "Synonym");
 	 	 				   tls.text(e_tt.getAttribute("value"));
 	 	 				   tls.endTag("", "Synonym");
 	 	 				   //
 	 	 			   }
 	 	 			   //
 	 	 		   }
 	 	 		   //
 	 	 		   // Compound
 	  	 		   //
 	 	 		   if(word_nodes.item(i).getNodeName().equals("compound")) {
 	 	 			   //
 	 	 			   bls.startTag("", "Compound");
 	 	 			   Element e_tr = (Element) word_nodes.item(i);
 	 	 			   bls.text(e_tr.getAttribute("value"));
 	 	 			   bls.endTag("", "Compound");
 	 	 			   // 	 	 			   
 	 	 			   Element e_tt = (Element) e_tr.getChildNodes().item(0);
 	 	 			   //
 	 	 			   if(e_tt != null) {
 	 	 				   //
 	 	 				   tls.startTag("", "Compound");
 	 	 				   tls.text(e_tt.getAttribute("value"));
 	 	 				   tls.endTag("", "Compound");
 	 	 				   //
 	 	 			   }
 	 	 		   }
 	 	 		   //
 	 	 		   // Example
 	  	 		   //
 	 	 		   if(word_nodes.item(i).getNodeName().equals("example")) {
 	 	 			   //
 	 	 			   bls.startTag("", "Example");
 	 	 			   Element e_tr = (Element) word_nodes.item(i);
 	 	 			   bls.text(e_tr.getAttribute("value"));
 	 	 			   bls.endTag("", "Example");
 	 	 			   // 	 	 			   
 	 	 			   Element e_tt = (Element) e_tr.getChildNodes().item(0);
 	 	 			   //
 	 	 			   if(e_tt != null) {
 	 	 				   //
 	 	 				   tls.startTag("", "Example");
 	 	 				   tls.text(e_tt.getAttribute("value"));
 	 	 				   tls.endTag("", "Example");
 	 	 				   //
 	 	 			   }
 	 	 			   //
 	 		       } 	 	 			   
 	 	 		   //
 	 	 		   // Idiom
 	  	 		   //
 	 	 		   if(word_nodes.item(i).getNodeName().equals("idiom")) {
 	 	 			   //
 	 	 			   bls.startTag("", "Idiom");
 	 	 			   Element e_tr = (Element) word_nodes.item(i);
 	 	 			   bls.text(e_tr.getAttribute("value"));
 	 	 			   bls.endTag("", "Idiom");
 	 	 			   // 	 	 			   
 	 	 			   Element e_tt = (Element) e_tr.getChildNodes().item(0);
 	 	 			   //
 	 	 			   if(e_tt != null) {
 	 	 				   //
 	 	 				   tls.startTag("", "Idiom");
 	 	 				   tls.text(e_tt.getAttribute("value"));
 	 	 				   tls.endTag("", "Idiom");
 	 	 				   //
 	 	 			   }
 	 	 			   //
 	 		       } 	 	 			   
 	 	 		   // 	 	 		   
 	 	 	   }
 	 	 	   //
 	 	 	   tls.startTag("", "Translation");
 	 	 	   tls.text(translation);
 	 	 	   tls.endTag("", "Translation");
               //
 	 	 	   bls.endTag("", "BaseLang");
 	 	 	   bls.endTag("", "Word");
 	 	 	   bls.endDocument();
  			   tls.endTag("", "TargetLang");
  			   tls.endDocument();
  			   //
  			   // Concatenate the two strings
  			   //
  			   String tmp1 = blw.toString();
  			   int t1 = tmp1.indexOf("<Word");
  			   String tmp2 = tlw.toString();
  			   int t2 = tmp2.indexOf("<TargetLang>");
  			   tmp1 = tmp1.substring(t1, tmp1.length() - 7);
  			   tmp2 = tmp2.substring(t2, tmp2.length());
  			   NewReplyStr = NewReplyStr + tmp1 + tmp2 + "</Word>";  			  
  			   //
 		  } catch (Exception e) {
 			  //
 		  }  
 	 	  //		   
 	   }
	   } catch (Exception e) {
		  //
       }   	         
 	   //
 	   // Add the XML values
 	   //
 	   NewReplyStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><result>" + NewReplyStr + "</result>";
 	   //
	   return NewReplyStr;
   }
   
   /*********************************************************************************
    * 
    * Function Name:
    * Description  :
    * 
    * 
    *********************************************************************************/
   
   private boolean checkInternetConnection() {
	   //
	   ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	   // test for connection
	   if (cm.getActiveNetworkInfo() != null
	            && cm.getActiveNetworkInfo().isAvailable()
	            && cm.getActiveNetworkInfo().isConnected()) {
	        return true;
	   } else {
	        return false;
	   }
   }

   /*********************************************************************************
    * 
    * Function Name:
    * Description  :
    * 
    * 
    *********************************************************************************/
   
   private Handler threadHandler = new Handler() {
	   //
       public void handleMessage(android.os.Message msg) {
    	   //
   		   Button pb = (Button) LexinActivity.this.findViewById(R.id.Translate);
    	   pb.setEnabled(true);
           //
    	   m_ProgressDialog.dismiss();
    	   //
    	   if(!Language.equals("swe_eng_off") && (!offline_search)) {
    		   //
    		   // Get the XML Part
    		   //
    	       int substart;
    	       int subend;
    	       //
    	       try {
    	    	   //
        		   if(ReplyStr.contains("//OK[3,")) {
        		  	   Toast.makeText(LexinActivity.this, "Hittade inget ord!", Toast.LENGTH_SHORT).show(); 
        			   return;
        		   }
        		   if(ReplyStr.contains(",2,1,0,1,[")) {
        			   //
        			   substart = ReplyStr.indexOf("2600011424") + 13;
        			   //
        			   if(Language.equals("swe_eng")) {
        			  	   subend = ReplyStr.length() - 10 - input_word.length();
        			   } else {
        				   subend = ReplyStr.length() - 13 - Language.length() - input_word.length();
        			   }
        			   ReplyStr = ReplyStr.substring(substart, subend);
        			   //
        			   String dec_word;
        			   //
        			   while(subend != -1){
        			       //
        			       subend = ReplyStr.indexOf("\",\"");
        			       //
        			       if(subend == -1) {
        			      	  //
        			      	  dec_word = ReplyStr;
        			      	  Proposal.add(dec_word);        			       	
        			       	  //
        			       } else {
        			       	  //
        			          dec_word = ReplyStr.substring(0 , subend);
        			          Proposal.add(dec_word);
        			          //
        			          ReplyStr = ReplyStr.replace(dec_word + "\",\"", "");
        			       }
        			   }
        			   //
        			   View v = findViewById(R.id.Translate);
        		       LexinActivity.this.registerForContextMenu( v );
        			   LexinActivity.this.openContextMenu(v);
        			   //
        			   return;
        		   }
    	    	   //
        	       substart = ReplyStr.indexOf("2600011424") + 13;
        	       //
        	       if(Language.equals("swe_eng")) {
        	    	   //
        	    	   subend = ReplyStr.length() - 10 - input_word.length();
        	    	   //
        	       } else {
        	    	   //
        	    	   subend = ReplyStr.length() - 13 - Language.length() - input_word.length();
        	    	   //
        	       }
        	       //
        	       ReplyStr = ReplyStr.substring(substart, subend);
        	       ReplyStr = ReplyStr.replace("\\n", "");
        	       ReplyStr = ReplyStr.replace("\\\"", "\"");
        	       ReplyStr = ReplyStr.replace(">\",\"<", "><");
          	       ReplyStr = ReplyStr.replace("&#39;","\' ");
          	       ReplyStr = ReplyStr.replace("&quot;","");
                   //
          	       if(Language.equals("swe_eng")) {
          	    	   //
          	    	   ReplyStr = ReplyStr.replace("&amp;quot;","\'");
             	       ReplyStr = ReplyStr.replace("&amp;","");
             	       ReplyStr = ReplyStr.replace("&quot;","");
             	       ReplyStr = ReplyStr.replace("#39;","\' ");
             	       //          	    	 
          	       }
        	       //
        	       // Fix Arabiska / Persiska bug
        	       //
        	       if(Language.equals("swe_ara") || Language.equals("swe_per")) {
        	    	 //
            	     ReplyStr = ReplyStr.replace("\\u200C", String.format("%s", "\u200C"));
            	     ReplyStr = ReplyStr.replace("\\u0640", String.format("%s", "\u0640"));
            	     ReplyStr = ReplyStr.replace("\\u0641", String.format("%s", "\u0641"));
            	     ReplyStr = ReplyStr.replace("\\u0642", String.format("%s", "\u0642"));
            	     ReplyStr = ReplyStr.replace("\\u0643", String.format("%s", "\u0643"));
            	     ReplyStr = ReplyStr.replace("\\u0644", String.format("%s", "\u0644"));
            	     ReplyStr = ReplyStr.replace("\\u0645", String.format("%s", "\u0645"));
            	     ReplyStr = ReplyStr.replace("\\u0646", String.format("%s", "\u0646"));
            	     ReplyStr = ReplyStr.replace("\\u0647", String.format("%s", "\u0647"));
            	     ReplyStr = ReplyStr.replace("\\u0648", String.format("%s", "\u0648"));        	     
            	     ReplyStr = ReplyStr.replace("\\u0649", String.format("%s", "\u0649"));
            	     ReplyStr = ReplyStr.replace("\\u064A", String.format("%s", "\u064A"));
            	     ReplyStr = ReplyStr.replace("\\u064B", String.format("%s", "\u064B"));
            	     ReplyStr = ReplyStr.replace("\\u064C", String.format("%s", "\u064C"));
            	     ReplyStr = ReplyStr.replace("\\u064D", String.format("%s", "\u064D"));
            	     ReplyStr = ReplyStr.replace("\\u064E", String.format("%s", "\u064E"));
            	     ReplyStr = ReplyStr.replace("\\u064F", String.format("%s", "\u064F"));        	    
            	     ReplyStr = ReplyStr.replace("\\u0650", String.format("%s", "\u0650"));
            	     ReplyStr = ReplyStr.replace("\\u0651", String.format("%s", "\u0651"));
            	     ReplyStr = ReplyStr.replace("\\u0652", String.format("%s", "\u0652"));        	     
        	    	 //
        	       }
        	       //
        	       ReplyStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><result>" + ReplyStr;
        	       ReplyStr = ReplyStr + "</result>\n";
        	       //
    	       } catch (Throwable t) {
    	    	   //
    			   Toast.makeText(LexinActivity.this, "Can not connect to server...", Toast.LENGTH_SHORT).show(); 
    			   return;
    	    	   //
    	       }
    	       //
    	       if(Language.equals("swe_eng")) ReplyStr = Translate_XML_Data(ReplyStr);
    	       //
    	  }
    	  //
     	  if(SYSTEM_DEBUG) Log.d("XML_STR", ReplyStr);
    	  //
    	  parseXML(ReplyStr);
    	  //  
    	  return;
       }
   };

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
       menu.setHeaderTitle("Menade du?");  
       //
       for(int i=0; i<Proposal.size();i++){
    	   //
    	   menu.add(0,i,i,Proposal.get(i));
    	   //
       }
       //
       Proposal.clear();
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
   public boolean onContextItemSelected(MenuItem item) {
       
       String str = item.getTitle().toString();
       //
       input_word = (EditText) findViewById( R.id.wordfortranslation); 
       input_word.setText(str);
       //
       onButtonClicked(LexinActivity.this.findViewById(R.id.Translate));
       //
       return true;
   }

   /*********************************************************************************
    * 
    * Function Name:
    * Description  :
    * 
    * 
    *********************************************************************************/
   
   public void onButtonMenu(View v) {
	 //

	 LexinActivity.this.openOptionsMenu();
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
   
   public void onButtonClear(View v) {
	 //
	 EditText et = (EditText) LexinActivity.this.findViewById(R.id.wordfortranslation);
	 //
	 et.setText("");
     //
	 // Show keyboard in case we press the button
	 //
//	 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//	 imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
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
   
   public void onButtonClicked(View v) {
     //
	 // Hide keyboard in case we press the button
	 //
	 try{
		 //
		 InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		 inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
		 //
	 } catch(Throwable t) {}	 
	 //
	 // Check to See if we have internet connection
	 //
	 if(checkInternetConnection() == false) {
		 //
		 if(!db_search_no_internet) {
			 //
             AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
             // 
             alertbox.setMessage("No Internet Connection \n available!");
             alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() { public void onClick(DialogInterface arg0, int arg1) {} });
             // 
             alertbox.show();
		     //
		     return;
		     //
		 } else offline_search = true;
	 }
	 //
	 Button pb = (Button) LexinActivity.this.findViewById(R.id.Translate);
	 pb.setEnabled(false);
	 //
     // initializing and starting a new local Thread object
	 //
     Thread currentThread = new Thread(this);
     currentThread.start();
     //
     m_ProgressDialog = ProgressDialog.show(LexinActivity.this, "Var god vänta...", "Hämtar data ...", true);
     //
     menuItems.clear();
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
    protected void onStop(){
    	//
        super.onStop();
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	//creates a menu inflater
    	MenuInflater inflater = getMenuInflater();
    	//generates a Menu from a menu resource file
    	//R.menu.main_menu represents the ID of the XML resource file
    	inflater.inflate(R.menu.main_menu, menu);
    	//
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
    public boolean onOptionsItemSelected(MenuItem item)
    { 	
    	//check selected menu item
    	//
    	switch(item.getItemId()){
    	case R.id.menuview:
                            // Launching new Activity on selecting single List Item
                            Intent u = new Intent(LexinActivity.this, ViewSaved.class);
                            LexinActivity.this.startActivity(u);
                            //
    						return true;
    	case R.id.menuexit:  
    		                 this.finish(); 
    	                     return true;
    	                     //
    	case R.id.menuabout: 
    		                 //    		                 
    		                 AlertDialog.Builder alertboxbuilder = new AlertDialog.Builder(this);
    		                 alertboxbuilder.setMessage(Html.fromHtml("<b>App:</b> Lexin 2<BR>" +
                            		             "<b>Version:</b> " + APPLICATION_VERSION + "<BR><BR>" +    		                 
                             		             "<b>Editor:</b> Dimitrios Meintanis<BR><BR>" +
                             		             "<a href=\"http://www.meintanis.se\">http://www.meintanis.se/</a><BR><BR>" +
                             		             "<b>Support:</b> " +
                             		             "<a href=\"mailto:s.lexinandroid@yahoo.com?Subject=BugReport\">Report a bug</a><BR>" +
                             		             "s.lexinandroid@yahoo.com"));
                             //                             
    		                 alertboxbuilder.setNeutralButton("Ok", new DialogInterface.OnClickListener() { public void onClick(DialogInterface arg0, int arg1) {} });
                             
                             AlertDialog alertbox = alertboxbuilder.create();
                             alertbox.show();
                             ((TextView)alertbox.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                             //
            		         return true;
            		         //
    	case R.id.menusettings:
                             // Launching new Activity on selecting single List Item
                             Intent i = new Intent(LexinActivity.this, Preferences.class);
                             LexinActivity.this.startActivity(i);
                             //
			                 return true;    		
        default: return super.onOptionsItemSelected(item);    	
    	}
    	
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mainListView.invalidateViews();
    }    
    
}