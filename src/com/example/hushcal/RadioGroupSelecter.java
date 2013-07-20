package com.example.hushcal;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

//Wrapper class to control behavior of radio group on synccal page
public class RadioGroupSelecter extends Activity implements OnClickListener {
	
	private String status;
	
	public RadioGroupSelecter() {
		//this.status = status;
	}

	@Override
	public void onClick(View v) {
		RadioGroup radio_group = (RadioGroup) v;
		
		//id representing the radio button that was clicked
		int radio_button_clicked_id = radio_group.getCheckedRadioButtonId(); 
		
		RadioButton silence_radio_button = (RadioButton) findViewById(R.id.event_silence);
		RadioButton vibrate_radio_button = (RadioButton) findViewById(R.id.event_vibrate);
		
		//what happens when you click the silence radio button
		if (radio_button_clicked_id == silence_radio_button.getId()) {
			//and it is already selected
			if (silence_radio_button.isChecked()) {
				radio_group.clearCheck();
			}
			
			//and it is not already selected
			else if (!silence_radio_button.isChecked()) {
				//clear radio group to make sure two buttons arent selected
				//at the same time
				radio_group.clearCheck();
				silence_radio_button.toggle();
			}
			
		}
		
		//what happens when you click the vibrate radio button
		if (radio_button_clicked_id == vibrate_radio_button.getId()) {
			//and it is already selected
			if (vibrate_radio_button.isChecked()) {
				radio_group.clearCheck();
			}
			
			//and it is not already selected
			else if (!vibrate_radio_button.isChecked()) {
				radio_group.clearCheck();
				vibrate_radio_button.toggle();
			}
		}
		
	}

}
