import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.inject.Named;



@Named
public class BonjourCDI {
private String hello="Hello ....";

public String getHello() {
	return hello;
}

public void setHello(String hello) {
	this.hello = hello;
}

public String getMessage() {
	return hello + "  " + new SimpleDateFormat("HH:mm:ss").format(new Date());
	
}
}
