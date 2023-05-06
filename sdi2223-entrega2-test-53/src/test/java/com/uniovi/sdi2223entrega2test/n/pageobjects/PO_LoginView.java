package com.uniovi.sdi2223entrega2test.n.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PO_LoginView extends PO_NavView {

	static public void fillLoginForm(WebDriver driver, String emailp, String passwordp) {
		WebElement email = driver.findElement(By.name("email"));
		email.click();
		email.clear();
		email.sendKeys(emailp);
		WebElement password = driver.findElement(By.name("password"));
		password.click();
		password.clear();
		password.sendKeys(passwordp);
		//Pulsar el boton de Alta.
		By boton = By.className("btn");
		driver.findElement(boton).click();	
	}
	static public void login(WebDriver driver,String user, String password, String checkText) {
		PO_HomeView.clickOption(driver, "login", "class", "btn btn-primary");
		fillLoginForm(driver, user, password);
		PO_View.checkElementBy(driver, "text", checkText);
	}

	static public void loginAsAdmin(WebDriver driver) {
		login(driver, "admin@email.com", "admin", "Usuarios");
	}

	static public void logout(WebDriver driver) {
		PO_HomeView.clickOption(driver, "logout", "class", "btn btn-primary");
	}
}
