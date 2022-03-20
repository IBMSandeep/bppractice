import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.RestAssured;
import jdk.jfr.Description;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.time.Duration;

import static io.restassured.RestAssured.given;

public class TestRun {
    boolean result;
    SoftAssert softAssert = new SoftAssert();
    @Description("Query any existing used car listing and confirm the following details are shown with values")
    @Test(description ="Run the UI test")
    public void TradeMeWebSiteUsedCars() throws InterruptedException {
        WebDriverManager.edgedriver().setup();
        WebDriver driver = new EdgeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
        driver.get("https://www.trademe.co.nz");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.id("search"))));
        driver.findElement(By.id("search")).sendKeys("Used Car");
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[contains(text(),'Cars for sale')]")));
        result = driver.findElement(By.xpath("//h1[contains(text(),'Cars for sale')]")).isDisplayed();
        softAssert.assertTrue(result, "Failed to display Used cars listing");
      //  driver.findElement(By.xpath("//div[@class='tm-motors-search-card__wrapper ng-star-inserted']//a")).click();
        driver.findElement(By.xpath("//span[@class='tm-motors-search-card__price']")).click();
        Thread.sleep(1000);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[contains(text(),'Back to search results')]")));
        result = driver.findElement(By.xpath("//span[contains(text(),'Back to search results')]")).isDisplayed();
        softAssert.assertTrue(result, "Failed to display details page of the cars");
        result = driver.findElement(By.xpath("//tg-icon[@name='vehicle-odometer']/../..")).getText()!=null &&
                 driver.findElement(By.xpath("//span[contains(text(),'Number plate')]/..")).getText()!=null &&
                 driver.findElement(By.xpath("//tg-icon[@name='vehicle-car']/../..")).getText()!=null &&
                 driver.findElement(By.xpath("//tg-icon[@name='vehicle-seat']/../..")).getText()!=null;
        softAssert.assertTrue(result, "Failed to display display following value");
        driver.close();
    }

    @Description("Retrieve list of charities and confirm that St John is included in the list")
    @Test(description ="Run the API test")
    public void ValidateAPerticularCharityInListOfCharities() {
        RestAssured.baseURI = "https://api.trademe.co.nz";
        boolean result=false;
        String response = given().header("Content-Type", "application/json")
                .when().get("/v1/Charities.json")
                .then().assertThat().statusCode(200).extract().asString();
        JSONArray jsonarray = new JSONArray(response);
        result = validateTheExpected(jsonarray);
        softAssert.assertTrue(result, "St John is not in the list");
    }

    public static boolean validateTheExpected (JSONArray jsonarray) throws JSONException {
        boolean getResult=false;
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject obj = jsonarray.getJSONObject(i);
            String name = obj.getString("Description");
            if (name.equals("St John")) {
                getResult = true;
                break;
            }
        }
        return getResult;
    }

    @AfterTest
    public void afterTest() {
        softAssert.assertAll();
    }
}

