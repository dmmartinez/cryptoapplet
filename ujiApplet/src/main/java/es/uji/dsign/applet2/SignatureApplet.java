package es.uji.dsign.applet2;

import es.uji.dsign.applet2.Exceptions.SignatureAppletException;
import es.uji.dsign.crypto.XAdESSignatureVerifier;
import es.uji.dsign.util.i18n.LabelManager;
import es.uji.dsign.applet2.io.FuncOutputParams;
import es.uji.dsign.applet2.io.ParamInputData;
import es.uji.dsign.applet2.io.URLInputParams;
import es.uji.dsign.applet2.io.URLOutputParams;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.swing.JApplet;
import javax.swing.JOptionPane;



/**
 * This is the main applet class, it handles the web-to-java interaction by 
 * exporting some method to JavaScript for being invoked within a web page.
 * 
 * The instantiation inside a web page must be in this way:
 * 
 *  <applet id="CryptoApplet" name="CryptoApplet" code="es.uji.dsign.applet.SignatureApplet" 
 *          width="0" height="0" codebase="path" archive="archive and deps mayscript>
 *  </applet>
 *  
 *  Each method below related to setup or signature forgery can be invoked from JavaScript
 */
public class SignatureApplet extends JApplet
{
	private static final long serialVersionUID = 1L;
	private AppHandler apph;
	private MainWindow window;
	private String _separator= "\\|";
	
	public String recvHash; //compatibility issues
	
	/**
	 * Init method. Installs the applet on client side.
	 * Downloads MicrosoftCryptoApi dll and loads it in case of Internet Explorer 
	 */
	
	public void init()		
	{
		try
		{
			Logger.getRootLogger().setLevel(Level.OFF);
			apph = AppHandler.getInstance(this);	
		}
		catch (SignatureAppletException ex)
		{	
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, ex.getMessage(), "", JOptionPane.ERROR_MESSAGE);
			netscape.javascript.JSObject.getWindow(this).call("onSignError", new String[] {});
		}
		catch (Exception ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, ex.getMessage(), "", JOptionPane.ERROR_MESSAGE);
			netscape.javascript.JSObject.getWindow(this).call("onSignError", new String[] {});
		}
		
		try{
			netscape.javascript.JSObject.getWindow(this).call("onInitOk", new String[] {});	
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	
	private void initializeWindow()
	{	
		try{
			if (window==null){ 
				apph.initDnie();
				window = new MainWindow(apph);
			}
			else{
				window.getPasswordTextField().setText("");
				window.getGlobalProgressBar().setValue(0);
				window.getInformationLabelField().setText(LabelManager.get("SELECT_A_CERTIFICATE"));
				window.getAppHandler().flushKeyStoresTable();
				window.getAppHandler().initKeyStoresTable();
				window.reloadCertificateJTree();
				window.getMainFrame().setVisible(true);
				window.getShowSignatureCheckBox().setVisible(true);
			}
		}
		catch (SignatureAppletException ex)
		{	
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, ex.getMessage(), "", JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, ex.getMessage(), "", JOptionPane.ERROR_MESSAGE);
		}
		
		try{
		
			netscape.javascript.JSObject.getWindow(this).call(apph.getJsWindowShow(), new String[] { "" });
		}
		catch(netscape.javascript.JSException e ){
			//Perhaps the function does not exists
		}	
	}
	
	/**
	 * This method sets up the applet language, possible values are:
	 *   ES_es for spanish 
	 *   CA_ca for 
	 *   EN_en
	 * 
	 * @param lang true means ask for it, false means keep the last correct config.
	 */
	public void setLanguage(final String lang)
	{
		/* We grant to JavaScript the same privileges as the core applet. */
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run()
			{
				apph.setLanguage(lang);
				return null;
			}
		});
	}
	
			
	/**
	 * Allow override the default three JavaScript functions OnSignOk, OnSignCancel and 
	 * OnSignError those functions are called when the process was success, user cancelled
	 * or an error happened respectively
	 * 
	 * @param ok     The name of the JavaSript function will be called on success
	 * @param cancel The name of the JavaSript function will be called on user cancel
	 * @param error  The name of the JavaSript function will be called on error
	 */
	public void setJavaScriptCallbackFunctions(final String ok, final String error, final String cancel, final String windowShow)
	{
		AccessController.doPrivileged(new PrivilegedAction<Object>(){
			public Object run()
			{
				apph.setJavaScriptCallbackFunctions(ok, error, cancel, windowShow);
				return null;
			}
		});
	}
	
	/**
	 * The invocation of this function is mandatory, the client must set up with this 
	 * function the type of signature format he want
	 * 
	 * @param format Possible values are the Strings: RAW, CMS, XADES or PDF
	 */
	
	public void setSignatureOutputFormat(final String format)
	{
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run()
			{
				apph.setSignatureOutputFormat(format);
				return null;
			}
		});
	}
		
	/**
	 * Sets the input encoding, if the input is different that plain, the 
	 * applet will decodify the input before computing the signature 
	 * 
	 * @param encoding Possible values PLAIN, HEX, BASE64
	 */
	
	public void setInputDataEncoding(final String encoding)
	{	
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run()
			{
				apph.setInputDataEncoding(encoding);
				return null;
			}
		});
	}	
	
	/**
	 * Sets the input encoding, if the input is different that plain, the 
	 * applet will decodify the input before computing the signature 
	 * 
	 * @param encoding Possible values PLAIN, HEX, BASE64
	 */
	
	public void setSSLServerCertificateVerification(final String value)
	{	
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run()
			{
				apph.setSSLCertificateVerfication(!(value.trim().equals("false")));
				return null;
			}
		});
	}	
	
	/**
	 * JS interface to setting the XAdES signer role from Navigator's javascript
	 *  
	 * @param signerrole
	 */
	public void setXadesSignerRole(final String signerrole)
	{
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run()
			{
				apph.setSignerRole(signerrole);
				return null;
			}
		});
	}
	
	
	/* SIGNATURE COMPUTATION FUNCTIONS */ 
	
	/**
	 * Computes the signature with the given toSign input data, if everything
	 * is correct, the applet invokes funcOut with the resulting signature object 
	 * as the function parameter.
	 * 
	 * @param toSign   the data to be signed  
	 * @param funcOut  the JavaScript function that must be called on success with the 
	 *                 signature object as a result. 
	 */
	
	public void signDataParamToFunc(final String toSign,final  String funcOut)
	{
		final SignatureApplet sa= this;
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run()
			{				
				/* We construct the hash table in order to pass it to 
				 * the applet handler for signature porpouses  
				 * */
				
				ParamInputData input= new ParamInputData(toSign);
				FuncOutputParams output= new FuncOutputParams(sa, funcOut);
			
				apph.setInput(input);
				apph.setOutput(output);
				
				initializeWindow();	
				
				return null;
			}
		});
	}
		
	/**
	 * Computes the signature with the given toSign input data, if everything
	 * is correct, the applet make a POST to the outputURL with the resulting 
	 * signature object in the "content" variable, it also send to that 
	 * URL the cookies for the current website.
	 * 
	 * @param toSign    the data to be signed  
	 * @param outputURL the URL where the data must be POSTed 
	 */
	
	public void signDataParamToURL(final String toSign, final String outputURL)
	{
		final SignatureApplet sa= this;
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run()
			{
				ParamInputData input= new ParamInputData(toSign);
				URLOutputParams output= new URLOutputParams(sa,outputURL);
				
				apph.setInput(input);
				apph.setOutput(output);
				
				initializeWindow();	

				return null;
			}
		});
	}
		
	/**
	 * Computes the signature with the given toSign input data, if everything
	 * is correct, the applet make a POST to the outputURL with the resulting 
	 * signature object in the postVariableName, it also send to that 
	 * URL the cookies for the current website.
	 * 
	 * @param toSign            the data to be signed  
	 * @param outputURL         the URL where the data must be POSTed 
	 * @param postVariableName  the name of the post variable to use, content by default.
	 */
	
	public void signDataParamToURL(final String toSign, final String outputURL, final String postVariableName)
	{
		final SignatureApplet sa= this;
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run()
			{
				ParamInputData input= new ParamInputData(toSign);
				URLOutputParams output= new URLOutputParams(sa, outputURL, postVariableName);

				apph.setInput(input);
				apph.setOutput(output);
				
				initializeWindow();	

				return null;
			}
		});
	}
		
	
	/**
	 * Computes the signature getting the data from the given inputURL,
	 * and it does a POST to the outputURL with the signature object in the 
	 * "content" variable. An item variable is passed by the post method if 
	 * inputURLs was a comma separated list of URL sources. That item 
	 * variable is incremented from 1 to n so the first one is item=1 the second
	 * item=2 and so on.
	 * 
	 * @param inputURL          the URL where the data must be retrieved.  
	 * @param outputURL         the URL where the data must be POSTed 
	 */
	
	public void signDataUrlToUrl(final String inputURLs, final String outputURL)
	{
		final SignatureApplet sa= this;
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run()
			{
				String[] in= inputURLs.split(_separator);
				URLInputParams input= new URLInputParams(inputURLs.split(_separator));
				URLOutputParams output= new URLOutputParams(sa, outputURL);

				output.setOutputCount(in.length);
		
				System.out.println("LONGITUD: " + in.length);
				
				apph.setInput(input);
				apph.setOutput(output);

				initializeWindow();	
				return null;
			}
		});
	}	
		
	/**
	 * Computes the signature getting the data from the given inputURL,
	 * and it does a POST to the outputURL with the signature object in the 
	 * postVariableName variable. An item variable is passed by the post method
	 * if inputURLs was a comma separated list of URL sources. That item 
	 * variable is incremented from 0 to n. 
	 * 
	 * @param inputURL          the URL where the data must be retrieved.  
	 * @param outputURL         the URL where the data must be POSTed 
	 * @param postVariable      the name of the post variable to use, content by default.
	 */
	
	public void signDataUrlToUrl(final String inputURLs, final String outputURL, final String postVariableName)
	{
		final SignatureApplet sa= this;
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run()
			{
				String[] in= inputURLs.split(_separator);
				URLInputParams input= new URLInputParams(inputURLs.split(_separator));
				URLOutputParams output= new URLOutputParams(sa, outputURL, postVariableName);
			
				output.setOutputCount(in.length);
		
				apph.setInput(input);
				apph.setOutput(output);

				initializeWindow();	
				return null;
			}
		});
	}
		
	/**
	 * Computes the signature getting the data from the given inputURL,
	 * and it invokes JavaScript funcOut with the signature object as a
	 * parameter.
	 * 
	 * @param inputURL       the URL where the data must be retrieved.  
	 * @param funcOut        the output JavaScript function name.    
	 */
	
	public void signDataUrlToFunc(final String inputURLs, final String funcOut)
	{
		final SignatureApplet sa= this;
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run()
			{
				String[] in= inputURLs.split(_separator);
				URLInputParams input= new URLInputParams(inputURLs.split(_separator));
				FuncOutputParams output= new FuncOutputParams(sa, funcOut);
				
				apph.setInput(input);
				apph.setOutput(output);

				initializeWindow();	
				return null;
			}
		});
	}	
	
	/**
	 * Computes the signature getting the hash of the data as a parameter,
	 * and giving the result as a parameter for funcOut JavaScript function.
	 * The hash must be encoded as an hex string or as 
	 * 
	 * This method only works with CMS output format signature.
	 * 
	 * @param toSign      the hash to be signed.  
	 * @param funcOut     the output JavaScript function name.    
	 * 
	 */
	
	public void signHashParamToFunc(String toSign, String funcOut)
	{
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run()
			{
				//TODO: CALL FUNC			 
				return null;
			}
		});
	}	
	
	/**
	 * Computes the signature getting the hash of the data as a parameter,
	 * and giving the result as a POST to the "outputURL" URL with the 
	 * content in the variable named "content".
	 * The hash must be encoded as an hex string or as 
	 * 
	 * This method only works with CMS output format signature.
	 * 
	 * @param toSign        the hash to be signed.  
	 * @param outputURL     the output JavaScript function name.    
	 */
	
	public void signHashParamlToUrl(String toSign, String outputURL)
	{
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run()
			{
				//TODO: CALL FUNC
				return null;
			}
		});
	}

	/**
	 * Computes the signature getting the hash of the data as a parameter,
	 * and giving the result as a POST to the "outputURL" URL with the 
	 * content in the variable postVariableName (content by default).
	 * The hash must be encoded as an hex string or as 
	 * 
	 * This method only works with CMS output format signature.
	 * 
	 * @param toSign            the hash to be signed.  
	 * @param outputURL         the output JavaScript function name.    
	 * @param postVariableName  the variable name for the post.
	 */
	
	public void signHashParamlToUrl(String toSign, String outputURL, String[] postVariableName)
	{
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run()
			{
				//TODO: CALL FUNC
				return null;
			}
		});
	}
	
	
	/* VERIFICATION FUNCTIONS */
	
	/**
	 * Allow signature verification for Xades types
	 * 
	 * @param input The URL string where de data must be got.
	 * 
	 * @return res an String[] vector indicating the causes of the signature, null return shows correct signature
	 */
	public String[] verifyXAdESDataUrl(final String input)
	{
		String[] res= AccessController.doPrivileged(new PrivilegedAction<String[]>() {
			public String[] run() {
				XAdESSignatureVerifier sv= new XAdESSignatureVerifier();
				return sv.verifyUrl(input);	
			}});
		return res;
	}
	
	public void destroy()
	{
		super.destroy();
		Runtime.getRuntime().gc();
	}
	
	public String getAppletInfo()
	{
		return "Universitat Jaume I: CryptoApplet for multiformat digital signature generation";
	}
	
	public String getAppletVersion()
	{
		return "2.0.1";
	}

	public String getJavaVersion()
	{
		return System.getProperty("java.version");
	}
}