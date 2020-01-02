package io.github.sukgu;

import static java.lang.System.err;
import static java.lang.System.out;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.io.IOException;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// https://www.baeldung.com/junit-before-beforeclass-beforeeach-beforeall
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;

import io.github.sukgu.Shadow;
import io.github.bonigarcia.wdm.WebDriverManager;

public class ShadowTest {

	private final static String baseUrl = "https://www.virustotal.com";
	// private static final String urlLocator = "a[data-route='url']";
	private static final String urlLocator = "*[data-route='url']";
	private boolean debug = Boolean
			.parseBoolean(getPropertyEnv("DEBUG", "false"));;
	private static Map<String, String> env = System.getenv();
	private static boolean isCIBuild = checkEnvironment();

	private static WebDriver driver = null;
	private static Shadow shadow = null;
	private static String browser = getPropertyEnv("PROFILE", getPropertyEnv("webdriver.driver", "chrome"));
	// use -P profile to override
	private static final boolean headless = Boolean
			.parseBoolean(getPropertyEnv("HEADLESS", "false"));

	protected static String osName = getOSName();
	public static String getBrowser() {
		return browser;
	}

	public static void setBrowser(String browser) {
		ShadowTest.browser = browser;
	}

	@BeforeClass
	public static void injectShadowJS() {
		err.println("Launching " + browser);
		if (browser.equals("chrome")) {
			WebDriverManager.chromedriver().setup();
			driver = new ChromeDriver();
		}
		if (browser.equals("firefox")) {
			WebDriverManager.firefoxdriver().setup();
			driver = new FirefoxDriver();
		} // TODO: finish for other browsers
		driver.navigate().to(baseUrl);
		shadow = new Shadow(driver);
	}

	@Before
	public void init() {

	}

	@Test
	public void testApp() {

	}

	@Test
	public void testJSInjection() {
		WebElement element = shadow.findElement(urlLocator);
		err.println(element);
		// Assertions.assertEquals(new String(""), shadow.driver.getPageSource(),
		// "Message");
	}

	@Test
	public void testGetAllObject() {
		List<WebElement> elements = shadow.findElements(urlLocator);
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		err.println(String.format("Located %d elements:", elements.size()));
		elements.stream().forEach(err::println);
		elements.stream().map(o -> o.getTagName()).forEach(err::println);
		// default toString() is not be particularly useful
		elements.stream().forEach(err::println);
		elements.stream()
				.map(o -> String.format("innerHTML: %s", o.getAttribute("innerHTML")))
				.forEach(err::println);
		elements.stream()
				.map(o -> String.format("outerHTML: %s", o.getAttribute("outerHTML")))
				.forEach(err::println);
	}

	@Test
	public void testAPICalls1() {
		WebElement element = shadow.findElements(urlLocator).stream()
				.filter(o -> o.getTagName().matches("div")).collect(Collectors.toList())
				.get(0);

		WebElement element1 = shadow.getNextSiblingElement(element);
		assertThat(element1, notNullValue());
		// TODO: examine the collection of elements returned earlier
	}

	@Test
	public void testAPICalls2() {
		List<WebElement> elements = shadow.findElements(urlLocator);
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		err.println(String.format("Located %d elements:", elements.size()));
		WebElement element = elements.stream()
				.filter(o -> o.getTagName().matches("div")).collect(Collectors.toList())
				.get(0);
		elements = shadow.findElements(element, "img");
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
	}

	@Ignore
	// TODO:
	@Test
	public void testAPICalls3() {
		WebElement element = shadow.findElement(urlLocator);
		List<WebElement> elements = shadow.getSiblingElements(element);
		// javascript error: object.siblings is not a function
		// https://www.w3schools.com/jquery/traversing_siblings.asp
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
	}

	@Ignore
	// TODO:
	@Test
	public void testAPICalls4() {
		WebElement element = shadow.findElement(urlLocator);
		List<WebElement> elements = shadow.getChildElements(element);
		// javascript error: Illegal invocation
		// https://stackoverflow.com/questions/10743596/why-are-certain-function-calls-termed-illegal-invocations-in-javascript
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));

	}

	@Test
	public void testAPICalls5() {
		WebElement element = shadow.findElement(urlLocator);
		List<WebElement> elements = shadow.findElements(element, "#wrapperLink");
		assertThat(elements, notNullValue());
		assertThat(elements.size(), greaterThan(0));
		err.println(
				String.format("Located %d #wrapperLink elements:", elements.size()));
		elements.stream()
				.map(o -> String.format("outerHTML: %s", o.getAttribute("outerHTML")))
				.forEach(err::println);
	}

	@After
	public void tearDown() {
	}

	@AfterClass
	public static void tearDownAll() {
		driver.close();
	}

	// Utilities
	public static String getOSName() {
		if (osName == null) {
			osName = System.getProperty("os.name").toLowerCase();
			if (osName.startsWith("windows")) {
				osName = "windows";
			}
		}
		return osName;
	}

	// origin:
	// https://github.com/TsvetomirSlavov/wdci/blob/master/code/src/main/java/com/seleniumsimplified/webdriver/manager/EnvironmentPropertyReader.java
	public static String getPropertyEnv(String name, String defaultValue) {
		String value = System.getProperty(name);
		if (value == null || value.length() == 0) {
			value = System.getenv(name);
			if (value == null || value.length() == 0) {
				value = defaultValue;
			}
		}
		return value;
	}

	public static boolean checkEnvironment() {
		boolean result = false;
		if (env.containsKey("TRAVIS") && env.get("TRAVIS").equals("true")) {
			result = true;
		}
		return result;
	}

}
