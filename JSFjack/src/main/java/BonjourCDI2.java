import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;


@RequestScoped
@Named
public class BonjourCDI2 {
private String hello="Hello ....";
private String login;
public String getLogin() {
	return login;
}

public void setLogin(String login) {
	this.login = login;
}

public String getPwd() {
	return pwd;
}

public void setPwd(String pwd) {
	this.pwd = pwd;
}

private String pwd;

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
