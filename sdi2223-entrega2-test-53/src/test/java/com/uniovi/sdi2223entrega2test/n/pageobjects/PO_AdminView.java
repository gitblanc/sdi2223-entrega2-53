package com.uniovi.sdi2223entrega2test.n.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class PO_AdminView extends PO_NavView {

	static public List<WebElement> getUsersList(WebDriver driver) {
		return PO_View.checkElementBy(driver, "@name", "check");
	}

	static public void deleteUsers(WebDriver driver, int... indexes) {
		List<WebElement> usersList = getUsersList(driver);

		for (int index : indexes) {
			WebElement userToDelete = usersList.get(index);
			WebElement checkbox = userToDelete.findElement(By.name("ids"));
			checkbox.click();
		}

		By boton = By.xpath("/html/body/div/form/input");
		driver.findElement(boton).click();
	}

}
