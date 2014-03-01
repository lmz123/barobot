package com.barobot.i2c;

import java.util.ArrayList;
import java.util.List;

import com.barobot.isp.Hardware;
import com.barobot.isp.IspSettings;
import com.barobot.isp.Main;
import com.barobot.isp.Wizard;
import com.barobot.parser.Parser;

public class Upanel extends I2C_Device_Imp {
	public Upanel can_reset_me_dev	= null;
	public I2C_Device have_reset_to	= null;

	public static List<Upanel> list	= new ArrayList<Upanel>();
	public static int findByI2c(int device_add) {
		for (I2C_Device s : list){
			if(s.getAddress() == device_add ){
				return Upanel.list.indexOf(s);
			}
		}
		return -1;
	}
	public Upanel(){
		this.cpuname	= "atmega8";
		this.lfuse		= "0xA4";
		this.hfuse		= "0xC7";
		this.lock		= "0x3F";
		this.efuse		= "";
	}
	public Upanel(int index, int address ){
		this();	// call default constructor
		this.setAddress(address);
		this.setIndex(index);
	}
	public Upanel(int index, int address, Upanel parent ){
		this();	// call default constructor
		this.setAddress(address);
		this.setIndex(index);
		this.can_reset_me_dev	= parent;
		parent.hasResetTo(this);
	}
	private void hasResetTo(I2C_Device child) {
		this.have_reset_to	= child;
	}
	public void canResetMe( Upanel current_dev){
		this.can_reset_me_dev = current_dev;
	}

	public void hasNext(Hardware hw){
		hw.send("h" + getAddress() );
	}

	public void reset(Hardware hw) {
		if(getIndex() > 0 ){
			hw.send("RESET"+ this.myindex );
		}else if( can_reset_me_dev == null ){
			hw.send("RESET_NEXT"+ can_reset_me_dev.getAddress() );
		}
	}
	public void reset_next(Hardware hw) {
		if( getAddress() > 0 ){
			hw.send("RESET_NEXT"+ getAddress() );
		}
	}
	public void isp(Hardware hw) {		// mnie
		if(getIndex() > 0 ){
			hw.send("P"+ this.myindex );
		}else if( can_reset_me_dev == null ){
			hw.send("p"+ can_reset_me_dev.getAddress() );
		}
	}
	public void isp_next(Hardware hw) {	// podłączony do mnie
		hw.send( "p"+ getAddress() );
	}

	public int resetNextAndReadI2c(Hardware hw) {
		int reset_tries = IspSettings.reset_tries;
		while( reset_tries-- > 0 ){
			this.reset_next( hw );
			int wait_tries = IspSettings.wait_tries;
			while( Parser.last_found_device <= 1 && (wait_tries-- > 0 ) ){
				Wizard.wait(IspSettings.wait_time);
			}
			if( Parser.last_found_device > 1 ){		// tylko plytka glowna ma 1
				break;
			}
			System.out.println("Reset try " + IspSettings.reset_tries );
		}
		int ret = Parser.last_found_device;
		Parser.last_found_device = 0;	// resetuj
		return ret;
	}

	public int readHasNext(Hardware hw) {
		int reset_tries = IspSettings.reset_tries;
		while( reset_tries-- > 0 ){
			this.hasNext( hw );
			int wait_tries = IspSettings.wait_tries;
			while( Parser.last_has_next == -1 && (wait_tries-- > 0 ) ){
				Wizard.wait(IspSettings.wait_time);
			}
			if( Parser.last_has_next > -1 ){
				break;
			}
			System.out.println("Check try " + IspSettings.reset_tries );
		}
		int ret = Parser.last_has_next;
		Parser.last_has_next = -1;	// resetuj
		return ret;
	}

	public String getHexFile() {
		return IspSettings.upHexPath;
	}
}
