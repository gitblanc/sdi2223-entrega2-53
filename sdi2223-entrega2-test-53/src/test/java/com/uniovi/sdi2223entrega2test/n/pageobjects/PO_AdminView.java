package com.uniovi.sdi2223entrega2test.n.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PO_AdminView extends PO_NavView {

	static public List<WebElement> getUsersList(WebDriver driver) {
		return checkElementBy(driver, "@name", "check");
	}
	static public void deleteUsers(WebDriver driver, int... indexes) {
		List<WebElement> usersList = getUsersList(driver);

		for (int index : indexes) {
			WebElement userToDelete = usersList.get(index);
			userToDelete.click();
		}

		By boton = By.xpath("/html/body/div/form/button");
		driver.findElement(boton).click();
	}

}
