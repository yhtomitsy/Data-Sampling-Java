
import gnu.io.*;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;

import javax.swing.JOptionPane;

public class serialCommunication implements SerialPortEventListener
{
	// passed from main gui
	DataSampler window = null;
	
    //map the port names to CommPortIdentifiers
    private HashMap<String, Object> portMap = new HashMap<String, Object>();

    //this is the object that contains the opened port
    private CommPortIdentifier serialPortId = null;
    private SerialPort serialPort = null;
    
    //input and output streams for sending and receiving data
    private InputStream input = null;
    private OutputStream output = null;
    
    //some ascii values for for certain things
    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    final static int NEW_LINE_ASCII = 10;
  
    //boolean to determine whether the serial port is connected
    boolean connected = false;
    
    String logText = "";
    String dataBuffer = null;
    boolean displayOnSerial = false;
    boolean sampling = false;
    byte dataSensor1[] = new byte[16000];
    byte dataSensor2[] = new byte[1600];
    byte dataSensor3[] = new byte[1600];
    byte dataSensor4[] = new byte[1600];
    int count = 0;
    int sensor = 1;

    public serialCommunication(DataSampler window)
    {
        this.window = window;
    }
    
    /*
     * Find all serial ports and list them in a combo box in the gui
     */
    public void populateComboBox() {
		window.comboBoxPorts.removeAllItems(); // clear combo box
	    Enumeration<?> enumComm;

	    enumComm = CommPortIdentifier.getPortIdentifiers(); // get list of all serial ports
	    while (enumComm.hasMoreElements()) {
	    	CommPortIdentifier PortId = (CommPortIdentifier) enumComm.nextElement();
	     	if(PortId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
	    		window.comboBoxPorts.addItem(PortId.getName()); // add serial port name to Combo box
	    		portMap.put(PortId.getName(), PortId);
	    	}
	    }
	}
    
    /*
     * Connect to the selected COM port
     */
    public void connect(){
    	String selectedPort = (String)window.comboBoxPorts.getSelectedItem();
        serialPortId = (CommPortIdentifier)portMap.get(selectedPort);

        CommPort commPort = null;

        try
        {
        	//the method below returns an object of type CommPort
            commPort = serialPortId.open("window", 2000);
            //the CommPort object can be casted to a SerialPort object
            serialPort = (SerialPort)commPort;
            connected = true;
            window.txtLogs.setForeground(Color.blue);
            window.txtLogs.append(">> "+selectedPort + " connected successfully. \n");
        }
        catch (PortInUseException e){
        	// display error message
        	JOptionPane.showMessageDialog( null, selectedPort + " is busy.","Serial Port Error",JOptionPane.OK_CANCEL_OPTION); 
        	window.txtLogs.setForeground(Color.blue);
            window.txtLogs.append(">>ERROR: "+selectedPort + " is busy. \n");
        }
        catch (Exception e){
        	// display error message
        	JOptionPane.showMessageDialog( null, selectedPort + " failed to open.","Serial Port Error",JOptionPane.OK_CANCEL_OPTION);
        	window.txtLogs.setForeground(Color.blue);
            window.txtLogs.append(">>ERROR: "+selectedPort + " failed to open. \n");
        }
    }
    
    /*
     * Initialise the input and out put streams of the open port
     */
    public boolean initIOStream()
    {
        //return value for whether opening the streams is successful or not
        boolean successful = false;

        try {
            //
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            writeData(0);
            
            window.txtLogs.setForeground(Color.blue);
            window.txtLogs.append(">> Input and output streams initialized \n");
            successful = true;
            return successful;
        }
        catch (IOException e) {
        	window.txtLogs.setForeground(Color.blue);
            window.txtLogs.append(">>ERROR: I/O Streams failed to open. (" + e.toString() + ")\n");
            return successful;
        }
    }
    
    /*
     * event listener that knows when there is data available in the serial port
     */
    public void initListener()
    {
        try
        {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            
            window.txtLogs.setForeground(Color.blue);
            window.txtLogs.append(">> Listening to port \n");
            window.btnIndicator.setBackground(Color.GREEN); // change the indicator button to green
	        
	        connected = true;
	        window.buttonenabler.onConnect(); //enable buttons
	        
	        window.btnConnect.setText("Disconnect"); // change connect button to disconnect
        }
        catch (TooManyListenersException e)
        {
        	connected = false;
        	window.txtLogs.setForeground(Color.blue);
            window.txtLogs.append(">>ERROR: Too many listeners. (" + e.toString() + ")\n");
        }
    }
    
    /*
     * Disconnect serial port
     */
    public void disconnect()
    {
        //close the serial port
        try
        {
            writeData(0);

            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
            
            // print on log text area
            window.txtLogs.setForeground(Color.blue);
            window.txtLogs.append("\n>> Serial Port Disconnected \n");
            window.btnIndicator.setBackground(Color.RED); // change the indicator button to red
            
            connected = false;
            
			window.buttonenabler.onDisconnect();// disable command buttons
			window.btnConnect.setText("Connect"); // change disconnect button to connect
			
        }
        catch (Exception e)
        {
        	// print on log text area
            window.txtLogs.setForeground(Color.blue);
            window.txtLogs.append(">>ERROR: Failed to close " + serialPort.getName() + "(" + e.toString() + ")\n");
        }
    }
    
    /*
     * Receiving sent data
     */
    public void serialEvent(SerialPortEvent evt) {
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            try
            {            	 
            	 if (sampling){
            		 byte singleData = (byte)input.read();
                	 logText = new String(new byte[] {singleData});
                	 if ((System.currentTimeMillis() - window.startTime) >= 20000  && window.btnStartSampling.getText() == "Stop Sampling"){
                		 window.btnStartSampling.setEnabled(false); // disable sampling button
                	 }
                	 if (logText.equals("#")){
                		 displayOnSerial = true; // indicate that sampling is done
                	 }
                	 
            		 if (!displayOnSerial){ 
            			// check the sensor that is being sampled
	            		if (logText.equals("$")){
	            			 sensor++;
	            			 
	            			 if (sensor == 5){
	            				 sensor = 1; // sensor goes back to 1
	            				 sampling = false; // stop sampling
	            				 window.txtLogs.append("\n>> All data parsed. Ready for saving!!\n");
	            				 window.btnStartSampling.setText("Save Data"); // change sampling button text
	            				 window.btnStartSampling.setEnabled(true); // enable sampling button
	            			 }
	            			 
	            			 count = 0;
	            		 }
	            		 else if (logText.equals("#")){
	            			 // do nothing
            			 }
	            		 else if (logText.equals("*")){
	            			 window.clearArrays(); // clear all data arrays
	            			 count = 0;
	            		 }
            			 else{
            				// store data in respective sensor array
		            		 if (sensor == 1){
		            			 dataSensor1[count] = singleData;
		                		 count++;
		            		 }
		            		 else if (sensor == 2){
		            			 dataSensor2[count] = singleData;
		                		 count++;
		            		 }
		            		 else if (sensor == 3){
		            			 dataSensor3[count] = singleData;
		                		 count++;
		            		 }
		            		 else if (sensor == 4){
		            			 dataSensor4[count] = singleData;
		                		 count++;
		            		 }
            			 }
	            	 }
	            	 else if(displayOnSerial){
	            		 if (logText.equals("$") || logText.equals("#")){
	            			 //do nothing
	            		 }
	            		 else window.txtLogs.append(logText); // display received data on status log 
	            		 
	            		 if (logText.equals("$")){
	            			 displayOnSerial = false; // stop displaying received data on status log 
	            		 }
	            	 }
            	 }
            }
            catch (Exception e)
            {
                window.txtLogs.setForeground(Color.blue);
                window.txtLogs.append(">>ERROR: Failed to read data. (" + e.toString() + ") \n");
            }
        }
    }
    
    /*
     * write data to serial port
     */
    public void writeData(int leftThrottle)
    {
        try
        {
            output.write(leftThrottle);
            output.flush();            
        }
        catch (Exception e)
        {
            window.txtLogs.setForeground(Color.blue);            
            window.txtLogs.append(">>ERROR: Failed to write data. (" + e.toString() + ") \n");
        }
    }

}