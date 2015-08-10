
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

import com.opencsv.CSVWriter;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;




public class DataSampler  {

	public  JComboBox<String> comboBoxPorts;
	public  JButton btnIndicator;
	public  JTextArea txtLogs;
	public JButton btnConnect;
	public JTextField txtFilePath;
	public JButton btnBrowse;
	public JButton btnStartSampling;
	public JButton btnDisplaySamples;
	public JButton btnRefresh;
	public JCheckBox chkBox1;
	public JCheckBox chkBox2;
	public JCheckBox chkBox3;
	public JCheckBox chkBox4;
	
	private JFrame frame;
	private JLabel lblSerialPort;
	private JLabel lblFileLocation;
	private JLabel lblStartTime;
	private JLabel lblTime;
	private JLabel lblDuration;
	private JLabel lblperiod;
	private JLabel lblStopTime;
	private JLabel lblStop;
	//private JScrollPane jScrollPane1;
	
	serialCommunication serialcommunication = null;
	buttonEnabler buttonenabler = null;
	long startTime = 0, stopTime = 0;
	
	List<String[]> data = new ArrayList<String[]>();
	
  	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DataSampler window = new DataSampler();
					window.frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public DataSampler() {
		serialcommunication = new serialCommunication(this);
		buttonenabler = new buttonEnabler(this);
		initialize();
		buttonenabler.onDisconnect();
	}

	/**
	 * Initialise the contents of the frame.
	 */
	private void initialize() {
				
		frame = new JFrame();
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(100, 100, 380, 600);
		frame.getContentPane().setLayout(null);
		
		btnIndicator = new JButton("");
		btnIndicator.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnIndicator.setEnabled(false);
		btnIndicator.setBackground(Color.RED);
		btnIndicator.setBounds(361, 37, 3, 23);
		frame.getContentPane().add(btnIndicator);
		
		lblSerialPort = new JLabel("Serial Port:");
		lblSerialPort.setFont(new Font("Calibri", Font.PLAIN, 16));
		lblSerialPort.setBounds(10, 38, 70, 21);
		frame.getContentPane().add(lblSerialPort);
		
		comboBoxPorts = new JComboBox<String>();
		comboBoxPorts.setBounds(104, 38, 76, 21);
		serialcommunication.populateComboBox();
		frame.getContentPane().add(comboBoxPorts);
		
		lblFileLocation = new JLabel("File Location:");
		lblFileLocation.setFont(new Font("Calibri", Font.PLAIN, 16));
		lblFileLocation.setBounds(10, 85, 92, 26);
		frame.getContentPane().add(lblFileLocation);
		
		txtFilePath = new JTextField();
		txtFilePath.setBounds(104, 88, 163, 23);
		frame.getContentPane().add(txtFilePath);
		txtFilePath.setColumns(10);
		
		lblStartTime = new JLabel("Start time:");
		lblStartTime.setFont(new Font("Calibri", Font.PLAIN, 16));
		lblStartTime.setBounds(10, 205, 76, 14);
		frame.getContentPane().add(lblStartTime);
		
		lblTime = new JLabel("");
		lblTime.setFont(lblTime.getFont().deriveFont(lblTime.getFont().getStyle() | Font.BOLD));
		lblTime.setBounds(135, 205, 229, 14);
		frame.getContentPane().add(lblTime);
		
		lblDuration = new JLabel("Duration: ");
		lblDuration.setFont(new Font("Calibri", Font.PLAIN, 16));
		lblDuration.setBounds(10, 283, 70, 14);
		frame.getContentPane().add(lblDuration);
		
		lblperiod = new JLabel("");
		lblperiod.setBounds(135, 281, 229, 14);
		frame.getContentPane().add(lblperiod);
		
		lblStopTime = new JLabel("Stop Time:");
		lblStopTime.setFont(new Font("Calibri", Font.PLAIN, 16));
		lblStopTime.setBounds(10, 240, 76, 21);
		frame.getContentPane().add(lblStopTime);
		
		lblStop = new JLabel("");
		lblStop.setBounds(135, 240, 229, 14);
		frame.getContentPane().add(lblStop);
		
		
		txtLogs = new JTextArea();
		txtLogs.setLineWrap(true);
		txtLogs.setWrapStyleWord(true);
		txtLogs.setEditable(false);
		txtLogs.setFont(new Font("Monospaced", Font.PLAIN, 11));
		txtLogs.setBounds(10, 327, 354, 233);
		DefaultCaret caret = (DefaultCaret)txtLogs.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		//frame.getContentPane().add(txtLogs);
		
		JScrollPane scrollPane = new JScrollPane(txtLogs);
		scrollPane.setBounds(10, 315, 354, 245);
		frame.getContentPane().add(scrollPane);
		
		/*
		 * button settings and code
		 */
		btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) { // get the directory path and display it on the textbox
				getFolderLocation(); // browse folder location
			}
		});
		btnBrowse.setBounds(277, 87, 84, 23);
		frame.getContentPane().add(btnBrowse);
		
		btnStartSampling = new JButton("Start Sampling");
		btnStartSampling.addActionListener(new ActionListener() {
			String date = null, start = null, stop = null;
			
			public void actionPerformed(ActionEvent e) {
				if (btnStartSampling.getText() == "Start Sampling"){
					if (connected()){
						txtLogs.setText(""); // clear logs
						txtLogs.append(">> Start sampling \n"); // indicate start of sampling
						buttonenabler.onSampling(); // disable all other buttons
						serialcommunication.sampling = true; // start sampling
						serialcommunication.displayOnSerial = false; // dont display incoming data on log
						btnDisplaySamples.setText("Display Samples");
							
						lblperiod.setText(""); // clear previous time interval
						lblStop.setText(""); // clear previous stop time
							
						//set the start time
						String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
						Calendar cal = Calendar.getInstance();
						SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
						String time = sdf.format(cal.getTime());
						date = new SimpleDateFormat("dd-MM-yyyy").format(cal.getTime());
						start = new SimpleDateFormat("HH-mm-ss").format(cal.getTime());
						lblTime.setText(time);
						    
						startTime = System.currentTimeMillis(); // get start time in milliseconds
						btnStartSampling.setText("Stop Sampling"); // change button text 
						sendChannelData(); // send selected channels to teensy
						serialcommunication.writeData(1); // send data to serial monitor to start sampling
					}
					else 
					{
						JOptionPane.showMessageDialog( null,"Select Channel","Input Error",JOptionPane.OK_CANCEL_OPTION);
						chkBox1.grabFocus();
					}
					
				}
				else if (btnStartSampling.getText() == "Stop Sampling"){
					serialcommunication.writeData(0); // send command to serial monitor to stop teensy sampling
					serialcommunication.sampling = false; // stop sampling
					txtLogs.append(">> Stopped Sampling \n"); // indicate end of sampling
					buttonenabler.onSamplingEnd(); //disable other buttons					
					btnStartSampling.setText("Start Sampling"); // change button text
				}
				else if (btnStartSampling.getText() == "Save Data"){
					serialcommunication.sampling = false; // stop sampling
					txtLogs.append(">> Saving sampled data \n"); // indicate end of sampling
					buttonenabler.onSamplingEnd(); //disable other buttons
					
					//set the stop time
					String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
					Calendar cal = Calendar.getInstance();
				    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
				    String time = sdf.format(cal.getTime());
				    stop = new SimpleDateFormat("HH-mm-ss").format(cal.getTime());
				    lblStop.setText(time);
					
					stopTime = System.currentTimeMillis(); // get stop time in milliseconds
					long interval = stopTime - startTime; // get time interval
					if (interval < 60000)lblperiod.setText(interval/1000 + " sec"); // display interval in seconds
					else if (interval >= 60000){
						DecimalFormat df = new DecimalFormat("#.##");
						lblperiod.setText(df.format((double)interval/60000) + " mins"); // display interval in minutes
					}
					
					
					String csv = "";
					// save values of sensor 1
					if (chkBox1.isSelected()){
						txtLogs.append("\nSaving sensor 1 data...\n");
						csv = txtFilePath.getText()+ "\\" + "S1"+ date + " From " + start + " to " + stop +".csv";
						parseData(serialcommunication.dataSensor1);
						saveData(csv, data);
					}
					
					
					// save values of sensor 2
					if (chkBox2.isSelected()){
						txtLogs.append("Saving sensor 2 data...\n");
						csv = txtFilePath.getText()+ "\\" + "S2"+ date + " From " + start + " to " + stop +".csv";
						parseData(serialcommunication.dataSensor2);
						saveData(csv, data);
					}
					
					
					// save values of sensor 3
					if (chkBox3.isSelected()){
						txtLogs.append("Saving sensor 3 data...\n");
						csv = txtFilePath.getText()+ "\\" + "S3"+ date + " From " + start + " to " + stop +".csv";
						parseData(serialcommunication.dataSensor3);
						saveData(csv, data);
					}
					
					
					// save values of sensor 4
					if (chkBox4.isSelected()){
						txtLogs.append("Saving sensor 4 data...\n");
						csv = txtFilePath.getText()+ "\\" + "S4"+ date + " From " + start + " to " + stop +".csv";
						parseData(serialcommunication.dataSensor4);
						saveData(csv, data);
					}
					
					
					txtLogs.append("\n>>Data saved successfully\n");
					
					btnStartSampling.setText("Start Sampling"); // change button text
				}
			}
		});
		btnStartSampling.setBounds(35, 162, 120, 23);
		frame.getContentPane().add(btnStartSampling);
		
		btnDisplaySamples = new JButton("Display Samples");
		btnDisplaySamples.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (btnDisplaySamples.getText() == "Display Samples"){
					sendChannelData(); // send selected channels to teensy
					serialcommunication.writeData(2); // send data to serial monitor
					serialcommunication.displayOnSerial = true;
					serialcommunication.sampling = true; // start sampling
					txtLogs.setText("");
					btnDisplaySamples.setText("Stop Display");
				}
				else if (btnDisplaySamples.getText() == "Stop Display"){
					serialcommunication.writeData(0); // send data to serial monitor to stop sampling
					serialcommunication.displayOnSerial = false; // stop displaying data on the log
					serialcommunication.sampling = false; // stop sampling
					btnDisplaySamples.setText("Display Samples");
				}
			}
		});
		btnDisplaySamples.setBounds(199, 162, 131, 21);
		frame.getContentPane().add(btnDisplaySamples);
		
		
		
		btnRefresh = new JButton("Refresh");
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				serialcommunication.populateComboBox(); // lists com ports in the combo box
				
			}
		});
		btnRefresh.setBounds(183, 37, 84, 23);
		frame.getContentPane().add(btnRefresh);
		
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (btnConnect.getText() == "Connect"){ 
					txtLogs.setText("");
					serialcommunication.connect(); // connect to serial port
					 if (serialcommunication.connected == true){
						 if (serialcommunication.initIOStream() == true){ // serial port initialised?
							 serialcommunication.initListener(); // listen to serial port
						 }					 
					 }					 				
				}
				else if (btnConnect.getText() == "Disconnect"){ 
					serialcommunication.disconnect();	
				}
			}
		});
		btnConnect.setBounds(272, 37, 89, 23);
		frame.getContentPane().add(btnConnect);		
		
		chkBox1 = new JCheckBox("1");
		chkBox1.setBounds(104, 132, 37, 23);
		frame.getContentPane().add(chkBox1);
		
		chkBox2 = new JCheckBox("2");
		chkBox2.setBounds(149, 132, 40, 23);
		frame.getContentPane().add(chkBox2);
		
		chkBox3 = new JCheckBox("3");
		chkBox3.setBounds(191, 132, 43, 23);
		frame.getContentPane().add(chkBox3);
		
		chkBox4 = new JCheckBox("4");
		chkBox4.setBounds(236, 132, 37, 23);
		frame.getContentPane().add(chkBox4);
		
		JLabel lblChannel = new JLabel("Channel:");
		lblChannel.setFont(new Font("Calibri", Font.PLAIN, 16));
		lblChannel.setBounds(10, 137, 92, 14);
		frame.getContentPane().add(lblChannel);
		
		
	}
		
	
	/*
	 * open folder browser to select the location where the data will be stored
	 */
	public void getFolderLocation(){
		JFileChooser chooser = new JFileChooser();
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);	    
	    chooser.setCurrentDirectory(new File("C:\\"));
	    int option = chooser.showSaveDialog(null);
	    
	    if (option == JFileChooser.APPROVE_OPTION)
	    {
	        File buffer = chooser.getSelectedFile();
	    	txtFilePath.setText(buffer.getAbsolutePath());
	    }
	}
	
	/*
	 * copy data to list string
	 */
	public void parseData(byte sensor[]){
		data = new ArrayList<String[]>();
		String dataBuffer = "";
		
		for(int i = 0; i < sensor.length; i++){
			String text = new String(new byte[] {sensor[i]});
			//txtLogs.append(text);
			if (text.equals("\n")){
				data.add(new String[] {dataBuffer});
				dataBuffer = "";
			}
			else{
				dataBuffer = dataBuffer + text;
			}
		}
	}
	
	/*
	 *  save data in csv file
	 */
	public void saveData(String csv, List<String[]> data){
		CSVWriter writer = null;
	
		try {
			writer = new CSVWriter(new FileWriter(csv));
		} catch (IOException e1) {
			txtLogs.append(">>Error: Data not saved!! Please define the correct Filepath!! \n");
        	txtFilePath.grabFocus();
		}
		 
		writer.writeAll(data);
		 
		try {
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/*
	 * clear the data arrays
	 */
	public void clearArrays(){
		serialcommunication.dataSensor1 = new byte[16000];
		serialcommunication.dataSensor2 = new byte[1600];
		serialcommunication.dataSensor3 = new byte[1600];
		serialcommunication.dataSensor4 = new byte[1600];
	}
	
	/*
	 * check if at least one check box is selected
	 */
	public boolean connected(){
		boolean status = false;
		if(!chkBox1.isSelected() && !chkBox2.isSelected() && !chkBox3.isSelected() && !chkBox4.isSelected()){
			
		}
		else status = true;
		
		return status;
	}
	
	/*
	 * send channel data
	 */
	public void sendChannelData(){
		if(chkBox1.isSelected()) serialcommunication.writeData(3); // send command to serial monitor to select first channle for samping
		if(chkBox2.isSelected()) serialcommunication.writeData(4); // send command to serial monitor to select first channle for samping
		if(chkBox3.isSelected()) serialcommunication.writeData(5); // send command to serial monitor to select first channle for samping
		if(chkBox4.isSelected()) serialcommunication.writeData(6); // send command to serial monitor to select first channle for samping
	}
}

