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

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class XMLParser {

	
	   public String getValue(Element item, String str) {
		    //
		    NodeList n = item.getElementsByTagName(str);
		    return this.getElementValue(n.item(0));
		    //
	   }
	
	
	   public final String getElementValue( Node elem ) {
			//
		    Node child;
		    if( elem != null){
		       if (elem.hasChildNodes()){
		          for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
		             if( child.getNodeType() == Node.TEXT_NODE  ){
		                  return child.getNodeValue();
		             }
		          }
		       }
		    }
		    return "";
	   } 
	   
	   public Document getDomElement(String xml){
	       Document doc = null;
	       DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	       try {

	           DocumentBuilder db = dbf.newDocumentBuilder();

	           InputSource is = new InputSource();
	               is.setCharacterStream(new StringReader(xml));
	               doc = db.parse(is); 

	           } catch (ParserConfigurationException e) {
	               Log.e("Error: ", e.getMessage());
	               return null;
	           } catch (SAXException e) {
	               Log.e("Error: ", e.getMessage());
	               return null;
	           } catch (IOException e) {
	               Log.e("Error: ", e.getMessage());
	               return null;
	           }
	               // return DOM
	           return doc;
	   }


}
