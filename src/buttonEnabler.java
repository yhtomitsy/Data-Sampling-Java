
public class buttonEnabler {

	DataSampler window = null;
	
	public buttonEnabler(DataSampler window)
    {
        this.window = window;
    }
	
	public void onConnect(){
		if (window.serialcommunication.connected == true){
			window.btnBrowse.setEnabled(true);
			window.btnDisplaySamples.setEnabled(true);
			window.btnStartSampling.setEnabled(true);
			window.txtFilePath.setEnabled(true);
			checkBoxEnable();
		}
	}
	
	public void onDisconnect(){
		if (window.serialcommunication.connected == false){
			window.btnBrowse.setEnabled(false);
			window.btnDisplaySamples.setEnabled(false);
			window.btnStartSampling.setEnabled(false);
			window.txtFilePath.setEnabled(false);
			checkBoxDisable();
		}
	}
	
	public void onSampling(){
			window.btnBrowse.setEnabled(false);
			window.btnDisplaySamples.setEnabled(false);
			window.btnConnect.setEnabled(false);
			window.txtFilePath.setEnabled(false);
			window.btnBrowse.setEnabled(false);
			window.comboBoxPorts.setEnabled(false);
			window.btnRefresh.setEnabled(false);
			checkBoxDisable();
	}
	
	public void onSamplingEnd(){
			window.btnBrowse.setEnabled(true);
			window.btnDisplaySamples.setEnabled(true);
			window.btnConnect.setEnabled(true);
			window.txtFilePath.setEnabled(true);
			window.btnBrowse.setEnabled(true);
			window.comboBoxPorts.setEnabled(true);
			window.btnRefresh.setEnabled(true);
			checkBoxEnable();
	}
	
	public void checkBoxEnable(){
		window.chkBox1.setEnabled(true);
		window.chkBox2.setEnabled(true);
		window.chkBox3.setEnabled(true);
		window.chkBox4.setEnabled(true);		
	}
	
	public void checkBoxDisable(){
		window.chkBox1.setEnabled(false);
		window.chkBox2.setEnabled(false);
		window.chkBox3.setEnabled(false);
		window.chkBox4.setEnabled(false);		
	}
}
