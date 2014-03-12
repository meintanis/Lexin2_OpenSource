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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;

public class DisplayWordActivity extends Activity  implements Runnable {
	
    static final String KEY_TRANSLATE = "Translation";
	static final String KEY_WORD = "Word";
	static final String KEY_XML = "xml";
	static final String KEY_ID = "word_id";
	static final String KEY_MODE = "display_mode";
	private MediaPlayer mp = new MediaPlayer();
	private String Word_Phonetic =  "";
	//
	private ProgressDialog m_ProgressDialog = null;
	private String xml_data;
	private int word_id;
	private String mode;
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
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.displayword);
 
        Button btnClose = (Button) findViewById(R.id.back_button);
        TextView input_word = (TextView) findViewById(R.id.word_extra);
        
        registerForContextMenu(input_word);  
 
        Intent i = getIntent();
        // Receiving the Data
        xml_data = i.getStringExtra(KEY_XML);
        word_id = i.getIntExtra(KEY_ID, 0);
        mode  = i.getStringExtra(KEY_MODE);
        // 
        // Displaying Received data
        //
        parseXML(xml_data, word_id);
        // 
        // Binding Click event to Button
        btnClose.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View arg0) {
                //Closing SecondScreen Activity
                finish();
            }
        });
    }
    /*
     *********************************************************************************
	 * 
	 * Function Name:
	 * Description  : Called when the activity is first created.
	 * 
	 * 
	 *********************************************************************************	
     *
     */
    private void parseXML(String str, int current){
 	   //
 	   XMLParser parser = new XMLParser();
 	   Document doc = parser.getDomElement(str);
 	   NodeList nl = doc.getElementsByTagName(KEY_WORD);
 	   //
       TextView xml_output = (TextView) findViewById(R.id.word_extra);
       TextView input_word = (TextView) findViewById(R.id.word_text);
       //
       xml_output.setMovementMethod(new ScrollingMovementMethod());
 	   //
       Element e_Word = (Element) nl.item(current);       
       //
 	   String word_in = e_Word.getAttribute("Value").toString();
 	   String word_type = e_Word.getAttribute("Type").toString();
 	   String word_phonetic = parser.getValue(e_Word, "Phonetic"); 	
 	   if(word_phonetic.length() > 1) word_phonetic = "[" + word_phonetic + "]";
 	   //
 	   Element e_phonetic = (Element) (((NodeList) e_Word.getElementsByTagName("Phonetic")).item(0));
 	   Word_Phonetic = ""; 			   
 	   boolean file_exists = false;
 	   //
 	   if(e_phonetic != null){
 		   //
 		   Word_Phonetic = e_phonetic.getAttribute("FILE");
 		   if(Word_Phonetic.length() > 0){
 			   //
  			   Word_Phonetic = "http://lexin2.nada.kth.se/sound/" + Word_Phonetic; 
 	 	 	   Word_Phonetic = Word_Phonetic.replace(".swf", ".mp3");
 	 	 	   Word_Phonetic = Word_Phonetic.replace("ö", "0366");
 	 	 	   Word_Phonetic = Word_Phonetic.replace("å", "0345");
 	 	 	   Word_Phonetic = Word_Phonetic.replace("ä", "0344");
 	 	 	   //
 	 	 	   file_exists = true;
               //
 		   }
 		   // 		   
 	   }
 	   //
       Element e_BaseLang = (Element) (((NodeList) e_Word.getElementsByTagName("BaseLang")).item(0));
       Element e_TargetLang = (Element) (((NodeList) e_Word.getElementsByTagName("TargetLang")).item(0));
       //
       // Setup Inflection String
       //
 	   String word_infection = word_in;
       NodeList nl_Inflection = e_BaseLang.getElementsByTagName("Inflection"); 	   
       //
       if(nl_Inflection != null) for(int i = 0; i < nl_Inflection.getLength(); i++){
    	   //   	
    	   String tmp = nl_Inflection.item(i).getParentNode().getNodeName();
    	   if(tmp.startsWith("BaseLang")) word_infection += ", " + parser.getElementValue(nl_Inflection.item(i));
    	   //
       }
       //
       String svenska_meaning = parser.getValue(e_BaseLang, "Meaning");
       String svenska_comment = parser.getValue(e_BaseLang, "Comment");
       String translation     = parser.getValue(e_TargetLang, "Translation");
       String translation_comment = parser.getValue(e_TargetLang, "Synonym");
       //
       if(svenska_comment.length() > 0) svenska_meaning += " (" + svenska_comment + ")";       
       if(translation_comment.length() > 0) translation += " (" + translation_comment + ")";
       //
       String examples   = create_list(parser, e_BaseLang, e_TargetLang, "Example");
       String compound   = create_list(parser, e_BaseLang, e_TargetLang, "Compound");
       String idiom      = create_list(parser, e_BaseLang, e_TargetLang, "Idiom");
       String derivation = create_list(parser, e_BaseLang, e_TargetLang, "Derivation");       
       //
       // Create Text Edit String
       //
       String secondary_output = "<b>" + word_infection + "</b><br><br>" + 
                                 svenska_meaning + "<br><br><b>" + translation + "</b><br><br>";
       //
       if(examples.length() > 0) secondary_output += "<i>Exempel:</i><br>" + examples + "<br>";
       if(compound.length() > 0) secondary_output += "<i>Sammansättningar:</i><br>" + compound + "<br>";
       if(idiom.length() > 0) secondary_output += "<i>Uttryck:</i><br>" + idiom + "<br>";
       if(derivation.length() > 0) secondary_output += "<i>Avledningar:</i><br>" + derivation + "<br>";       
       //
       ImageView iv = (ImageView) findViewById(R.id.sound_image);
       //
       if(file_exists) {
           iv.setImageResource(R.drawable.has_sound);
       } else {
    	   iv.setImageResource(R.drawable.no_sound);     	   
       }
       //       
       String input_word_text = "<b>" + word_in + "</b><br><small>" + word_phonetic + "</small>&nbsp;<small>" +  word_type + "</small>";
       //
       input_word.setText(Html.fromHtml(input_word_text, new ImageGetter() {                     	    
    	    public Drawable getDrawable(String source) {
    	     Drawable drawFromPath;
    	     int path = getResources().getIdentifier(source, "drawable", "com.translator.lexin"); 
    	     drawFromPath = (Drawable) getResources().getDrawable(path);
    	     drawFromPath.setBounds(0, 0, drawFromPath.getIntrinsicWidth(), drawFromPath.getIntrinsicHeight());
    	     return drawFromPath;
    	    }
    	}, null));
       
       xml_output.setText(Html.fromHtml(secondary_output));
 	   //
 	   return;
    }
    
    /*
     *********************************************************************************
	 * 
	 * Function Name:
	 * Description  : Called when the activity is first created.
	 * 
	 * 
	 *********************************************************************************	
     *
     */
    private void saveXML(String str, int current){
    	//
        int substart;
        int subend;
        int offset = 0;
        //
        for(int i=0; i<current; i++){
     	   subend = str.indexOf("</Word>", offset) + 7;
     	   offset = subend;
        }
        //       
        substart = str.indexOf("<Word ", offset);
 	    subend = str.indexOf("</Word>", offset) + 7;
 	    //
 	    str = str.substring(substart, subend);
 	    //
 	    // Now we have to row that we want to Save
 	    //
    	try {
    		//
    		// If File doesn't exist
    		//
    		File outputFile = new File("/data/data/com.translator.lexin/savedXML.dat");
    		//
    		if(!outputFile.exists()) {
	    		//
	    		// Create Directory if it doesn't exist
	    		//
    			File cache_dir = new File("/data/data/com.translator.lexin");
	    		if(!(cache_dir.exists() && cache_dir.isDirectory())) cache_dir.mkdirs();
	    		//
    		}
    		//
  			// Append Data
    		//
	    	FileOutputStream dos = new FileOutputStream(outputFile, true);
	    	DataOutputStream outFile = new DataOutputStream(dos);
	    	//
	    	outFile.writeUTF(str);
	    	outFile.close();
    	    //
    		Toast.makeText(this, "Ordet Sparats" , Toast.LENGTH_SHORT).show(); 
    		//
    	} catch (Throwable t) {
    	    //
    		Toast.makeText(this, "Error Writting Data..." , Toast.LENGTH_SHORT).show(); 
    		//
    	}
    	//
    	return;
    }
    /*
     *********************************************************************************
	 * 
	 * Function Name:
	 * Description  : 
	 * 
	 * 
	 *********************************************************************************	
     *
     */         
    public void run() {
    	//
   	    try {
		    //
		    mp.reset();
		    mp.setDataSource(Word_Phonetic);
		    mp.prepare();                     
	   	    //
	    }catch (IOException e) {
            //
		    //Toast.makeText(this, "Ingen Fil!"  , Toast.LENGTH_SHORT).show();		    
		    //
        }
    	//
   	    threadHandler.sendEmptyMessage(0);
   	    //
    	return;
    }
    /*
     *********************************************************************************
	 * 
	 * Function Name:
	 * Description  : 
	 * 
	 * 
	 *********************************************************************************	
     *
     */ 
    private Handler threadHandler = new Handler() {
 	   //
        public void handleMessage(android.os.Message msg) {
     	   //
     	   m_ProgressDialog.dismiss();     	   
     	   mp.start();
     	   //
        }
    };
    /*
     *********************************************************************************
	 * 
	 * Function Name:
	 * Description  : Called when the activity is first created.
	 * 
	 * 
	 *********************************************************************************	
     *
     */
    private String create_list(XMLParser parser, Element e_B, Element e_T, String str){
    	//
   	    String tmp_out = "";
   	    String tmp_str = "";
   	    //
        NodeList nl_compound0 = e_B.getElementsByTagName(str); 	   
        NodeList nl_compound1 = e_T.getElementsByTagName(str);
        //
        if(nl_compound0 == null) return null;
        //
        for(int i = 0; i < nl_compound0.getLength(); i++){
    	   //   	
    	   String tmp = nl_compound0.item(i).getParentNode().getNodeName();
    	   tmp_str = parser.getElementValue(nl_compound0.item(i));
    	   tmp_str = tmp_str.replace("(", ""); // Remove ( in case it exists....BUG fix
    	   //
    	   if(tmp.startsWith("BaseLang")) 
    		   tmp_out += "&#8226; " + tmp_str +  " - " + parser.getElementValue(nl_compound1.item(i)) +  "<br>";
    	   //
        }
        //
        return tmp_out;
    	//
    }
    /*
     *********************************************************************************
	 * 
	 * Function Name:
	 * Description  : 
	 * 
	 * 
	 *********************************************************************************	
     *
     */    
	@Override  
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
		//
	    super.onCreateContextMenu(menu, v, menuInfo);  
		menu.setHeaderTitle("Action"); 
        if(!Word_Phonetic.equals("")) menu.add(0, v.getId(), 0, "Lyssna");  
        if(mode.equals("NORMAL")) menu.add(0, v.getId(), 0, "Spara");
        //
    }   
    /*
     *********************************************************************************
	 * 
	 * Function Name:
	 * Description  : 
	 * 
	 * 
	 *********************************************************************************	
     *
     */ 	
	@Override  
	public boolean onContextItemSelected(MenuItem item) {  
		//
	    if(item.getTitle()=="Lyssna"){
	    	//	    	
	        Thread currentThread = new Thread(this);
	        currentThread.start();
	        //
	        m_ProgressDialog = ProgressDialog.show(this, "", "Hämtar data ...", true);
	        //
	    }  
	    else if(item.getTitle()=="Spara"){
	    	//
	    	saveXML(xml_data, word_id);
            //
	    }
	    else {return false;}
	    //
	    return true;  
	}  

}

