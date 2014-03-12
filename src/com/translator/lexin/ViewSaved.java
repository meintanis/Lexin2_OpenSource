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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ViewSaved extends Activity{
	//
	private ListView savedListView;
    private ArrayList<HashMap<String, String>> menuItems;
    private String mode_str;
    private String xmlData;
	//
    static final String KEY_WORD = "Word";
    static final String KEY_TRANSLATE = "Translation";
    static final String KEY_ID = "word_id";    
    static final String KEY_XML = "xml";
    static final String KEY_MODE = "display_mode";
	//
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
        this.setContentView(R.layout.viewsaved);
        //
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mode_str = sharedPrefs.getString("saved_words_setup", "value2");
        //
        savedListView = (ListView) findViewById( R.id.savedListView );  
        menuItems = new ArrayList<HashMap<String, String>>();
        //
        ListAdapter adapter = new SimpleAdapter(this, menuItems,R.layout.rowlayout,
                new String[] { KEY_WORD, KEY_TRANSLATE }, new int[] {R.id.input_word, R.id.translation });

        // Set the ArrayAdapter as the ListView's adapter.  
        savedListView.setAdapter(adapter);
		//
		// Read the file first in order to see how many saved words we have
		//
        ReadXMLSavedFile();
	    //
	    registerForContextMenu(savedListView);
        //
        savedListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
        //
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
     	           //
                   // Launching new Activity on selecting single List Item
                   Intent i = new Intent(getApplicationContext(), DisplayWordActivity.class);
                   // sending data to new activity
        	       i.putExtra(KEY_XML,  	xmlData);
                   i.putExtra(KEY_ID,       position);
                   i.putExtra(KEY_MODE, "SAVED");
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
	
	private void ReadXMLSavedFile(){
		//
        String line = "";
        xmlData = "";
        DataInputStream in = null;
        //
        try {
        	//
            File Test = new File("/data/data/com.translator.lexin/savedXML.dat");
            //                   
            FileInputStream inputS = new FileInputStream(Test);
        	//
            in = new DataInputStream(inputS);
        	//
        	xmlData = "";
            //        	
            while (( line = in.readUTF()) != null) {
            	xmlData += line;
            }
            //
        } catch (Throwable t) {
            //
            xmlData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><result>" + xmlData + "\n</result>";
            //            
        }
        //
        parseXML(xmlData);
        //
        try{
        	//
        	in.close();
        	//
        } catch (Throwable t) {
        	//
        	Toast.makeText(this, "Inga sparade ord" , Toast.LENGTH_SHORT).show();
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
	public void onCreateContextMenu(ContextMenu menu, View v,  ContextMenuInfo menuInfo) {
	   //
	   super.onCreateContextMenu(menu, v, menuInfo);
	   //
	   if (v.getId()==R.id.savedListView) {
	      //
		  menu.setHeaderTitle("Action");
	      menu.add(0, v.getId(), 0, "Radera");  
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
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	    int current = info.position;
	    //
	    String str = ""; 
    	//
        int substart;
        int subend;
        int offset = 0;
        //
        for(int i=0; i< current; i++){
        	//
     	    subend = xmlData.indexOf("</Word>", offset) + 7;
     	    offset = subend;
     	    //
        }
        //       
        substart = xmlData.indexOf("<Word", offset);
 	    subend = xmlData.indexOf("</Word>", offset) + 7;
 	    //
 	    if((substart < 0) || (subend < 0)) {
 	    	//
 	    	Toast.makeText(this, "Error Deleting Data..." , Toast.LENGTH_SHORT).show();
 	    	ReadXMLSavedFile();
 		    //
 		    return true; 	    	
 	    }
 	    // 	    
 	    str = xmlData.replace(xmlData.substring(substart, subend), "");
 	    str = str.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?><result>", "");
 	    str = str.replace("\n</result>", "");
 	    //
 	    // Now we have to row that we want to Save
 	    //
    	try {
    		//
 		    File inputFile = new File("/data/data/com.translator.lexin/savedXML.dat");
            inputFile.delete();
            //
  			// Append Data
    		//
    		File outputFile = new File("/data/data/com.translator.lexin/savedXML.dat");
            FileOutputStream dos = new FileOutputStream(outputFile, true);
	    	DataOutputStream outFile = new DataOutputStream(dos);
	    	//
	    	outFile.writeUTF(str);
	    	outFile.close();
    		//
    	} catch (Throwable t) {
    	    //
    		Toast.makeText(this, "Error Deleting Data..." , Toast.LENGTH_SHORT).show(); 
    		//
    	}
    	//
    	ReadXMLSavedFile();
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
			    Toast.makeText(this, "XML Parse error..", Toast.LENGTH_SHORT).show();
			    //
			    
			    //
			    return;
		   }
		   //
	       menuItems.clear();
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
	    	   if((mode_str.equals("value0")) || (mode_str.equals("value2"))) map.put(KEY_WORD, word_in); else map.put(KEY_WORD, "---");             	    	  
	    	   if((mode_str.equals("value1")) || (mode_str.equals("value2"))) map.put(KEY_TRANSLATE, translation); else map.put(KEY_TRANSLATE, "---");
		       menuItems.add(map);
		       //
		   }
		   savedListView.invalidateViews();
		   //
		   return;
	   }

	
	
}

