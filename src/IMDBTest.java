import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


public class IMDBTest {
    static WebDriver driver = null;

    public static void main(String[] args) {
        //set program configuration.
        String tvShowsListPath = "config/tv_shows_file.properties";
        String loginInfoPath = "config/login_info.properties";
        Properties tvShowsListProp = new Properties();
        try {
            tvShowsListProp.load(new FileInputStream(tvShowsListPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Properties loginProp = new Properties();
        try {
            loginProp.load(new FileInputStream(loginInfoPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String email = loginProp.getProperty("email");
        String password = loginProp.getProperty("password");
        double minRating = Double.parseDouble(tvShowsListProp.getProperty("rating"));
        List<String> tvShowsList = Arrays.asList(tvShowsListProp.getProperty("tv").split(","));

        //set the driver and open browser
        System.setProperty("webdriver.chrome.driver", ".\\driver\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        driver.navigate().to("https://imdb.com");

        //log in
        driver.findElement(By.id("imdb-signin-link")).click();
        driver.findElement(By.cssSelector("#signin-options > div > div:nth-child(2) > a:nth-child(1)")).click();
        WebElement emailBox = driver.findElement(By.id("ap_email"));
        emailBox.sendKeys(email);
        WebElement passwordBox = driver.findElement(By.id("ap_password"));
        passwordBox.sendKeys(password);
        driver.findElement(By.id("signInSubmit")).click();

        List<String> addedToWatchlist = getShowsWatchlist(tvShowsList, minRating);
        boolean testSuccess = checkWatchList(addedToWatchlist);
        assert testSuccess;
        driver.close();
    }

    private static List<String> getShowsWatchlist(List<String> tvShowsList, double minRating) {
        List<String> addedToWatchlist = new ArrayList<>();
        //iterate the list and search for each tv show
        WebDriverWait wait = new WebDriverWait(driver,15);

        for (String show : tvShowsList) {
            WebElement searchbox = driver.findElement(By.id("navbar-query"));
            wait.until(ExpectedConditions.elementToBeClickable(By.id("navbar-query")));
            searchbox.click();
            searchbox.sendKeys(show);

            wait.until(ExpectedConditions.presenceOfElementLocated((By.cssSelector("#navbar-suggestionsearch > div:nth-child(1) > a"))));
            searchbox.sendKeys(Keys.ARROW_DOWN);
            searchbox.sendKeys(Keys.RETURN);
            WebElement rating = driver.findElement(By.className("ratingValue"));
            double actualRating = Double.parseDouble(rating.getText().split("/")[0]);
            //if rating pass required minimum-add to watchlist
            if (actualRating >= minRating) {
                //add to watchlist
                try {
                    WebElement addButton = driver.findElement(By.cssSelector("#title-overview-widget > div.plot_summary_wrapper > div.uc-add-wl-button.uc-add-wl--not-in-wl.uc-add-wl > button.ipc-button.uc-add-wl-button-icon--add.watchlist--title-main-desktop-standalone.ipc-button--base.ipc-button--single-padding.ipc-button--default-height"));
                    addButton.click();
                    addedToWatchlist.add(driver.findElement(By.cssSelector("#title-overview-widget > div.vital > div.title_block > div > div.titleBar > div.title_wrapper > h1")).getText().strip());
                } catch (org.openqa.selenium.NoSuchElementException e) {
                    continue;
                }
            }
        }
        return addedToWatchlist;
    }

    private static boolean checkWatchList(List<String> addedToWatchlist) {
        WebElement watchListButton = driver.findElement(By.id("navWatchlistMenu"));
        watchListButton.click();
        List<WebElement> titles = driver.findElements(By.className("lister-item-header"));
        List<String> showTitleList = new ArrayList<>();
        for (WebElement show : titles) {
            showTitleList.add(show.getText());
        }

        for (String show : addedToWatchlist) {
            if (!showTitleList.contains(show)) {
                System.out.println("Watchlist test failed");
                return false;
            }
        }
        if (addedToWatchlist.size()!=showTitleList.size()){
            System.out.println("Watchlist test failed");
            return false;
        }
        System.out.println("Watchlist test passed");
        return true;
    }
}



