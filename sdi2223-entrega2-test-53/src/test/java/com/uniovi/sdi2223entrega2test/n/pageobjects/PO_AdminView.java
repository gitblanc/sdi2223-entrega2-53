package com.uniovi.sdi2223entrega2test.n.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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

	static public void tryToDeleteAdmin(WebDriver driver) {
		// Cambiamos el primer usuario borrable de la lista por admin
		((JavascriptExecutor)driver).executeScript("document.getElementsByName('check')[0].value = 'admin@email.comm';");
		WebElement userToDelete = driver.findElements(By.name("check")).get(0);
		userToDelete.click();

		By boton = By.xpath("/html/body/div/form/button");
		driver.findElement(boton).click();

		checkElementBy(driver, "text", "No es posible borrar el usuario administrador");
	}

	public static void goToLastUsersPage(WebDriver driver) {
		for (int i = 2; i < 4; i++) {
			checkElementBy(driver, "id", "pl-" + i).get(0).click();
		}
	}

}
