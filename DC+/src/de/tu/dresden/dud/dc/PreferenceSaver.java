/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * This class is responsible for saving user / participant preferences, so a
 * participant can use them the next time the program starts.
 * 
 * This class implements the singleton pattern, so each running DC instance only
 * has one PreferenceSaver.
 * 
 * This class is currently under development, and not ready for usage, yet.
 * 
 * @author klobs
 * 
 */
public class PreferenceSaver {

	private static 	PreferenceSaver ps;

	private 		DocumentBuilder docBuilder = null;
	private 		String 		prefFile;
	private 		Document	doc	     = null;
	
	/**
	 * This Constructor sets the default filename (dcrc.xml) for the preference
	 * file, and tries to create it, if not already present.
	 */
	private PreferenceSaver() {
		this.setPathToPrefFile("dcrc.xml");

		File f = new File(prefFile);
		if(!f.exists())
			try {
				f.createNewFile();
				
				XMLOutputFactory a = XMLOutputFactory.newInstance();
				
				XMLStreamWriter w = a.createXMLStreamWriter(new FileWriter(prefFile));
				
				w.writeStartDocument("1.0");
				
				w.writeStartElement("userlist");
				w.writeEndDocument();
				
				w.flush();
				w.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		
		try {

			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			docBuilder = docFactory.newDocumentBuilder();
			
						
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
	}
	
	/**
	 * PreferenceSaver is implemented as singleton. 
	 * @return The instance of the PreferenceSaver.
	 */
	public static PreferenceSaver getInstance(){
		if (ps == null){
			ps = new PreferenceSaver();
		}	
		return ps;
	}
	
	/**
	 * Standard getter.
	 * @return The filename of the preferences file.
	 */
	public String getPathToPrefFile(){
		return prefFile;
	}
	
	
	private Node prepareUserElement(String id, String username, byte [] publicKey, byte [] privateKey){
		
		Element eleUser 	= doc.createElement("user");
		
		Element eleID 		= doc.createElement("id");
		Text	textID		= doc.createTextNode(id);
		eleID.appendChild(textID);
		eleUser.appendChild(eleID);
		
		Element eleUName 	= doc.createElement("username");
		Text	textUname	= doc.createTextNode(username);
		eleUName.appendChild(textUname);
		eleUser.appendChild(eleUName);
		
		String publicKeyHex = Util.convertToHex(publicKey);
		
		Element elePubKey 	= doc.createElement("publicKey");
		Text	textPubKey	= doc.createTextNode(publicKeyHex);
		elePubKey.appendChild(textPubKey);
		eleUser.appendChild(elePubKey);

		String privateKeyHex = Util.convertToHex(privateKey);
		
		Element elePrivKey 	= doc.createElement("privateKey");
		Text	textPrivKey	= doc.createTextNode(privateKeyHex);
		elePrivKey.appendChild(textPrivKey);
		eleUser.appendChild(elePrivKey);
		
		return eleUser;
	}
	
	/**
	 * Standard setter.
	 * 
	 * Change the name / path of the preference file.
	 * 
	 * @param p
	 */
	public void setPathToPrefFile(String p){
		prefFile = p;
	}
	
	/**
	 * Save preferences for a {@link Participant} p.
	 * Those preferences currently contain p's id, username, public and private key.
	 * @param p the participant p that you want to save.
	 * @return Whether saving was successfull, or not. 
	 */
	public boolean saveParticipant(Participant p){

		if (p.getKeyPair() == null) return false;
		
		try {
			doc = docBuilder.parse(prefFile);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// TODO do some dupe checking here
		doc.appendChild(prepareUserElement(p.getId(), p.getUsername(), p.getKeyPair().getPublic().getEncoded(), p.getKeyPair().getPrivate().getEncoded()));
		
		savePrefFile();
		
		return true;
	}
	
	private void savePrefFile() {
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer;
		try {
			
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(prefFile));
			transformer.transform(source, result);

		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	
	}
}
