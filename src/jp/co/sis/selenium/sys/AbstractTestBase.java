package jp.co.sis.selenium.sys;

import static org.junit.Assert.fail;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

/**
 * テスト抽象クラス<BR>
 *
 * @author SIS
 * @version 1.0.0
 */
public abstract class AbstractTestBase {

    protected WebDriver driver;
    protected String baseUrl = "http://localhost:8080";
    protected String basePath = "sistams";
    protected String baseSoshikiCd = "";
    protected boolean acceptNextAlert = true;
    protected StringBuffer verificationErrors = new StringBuffer();
    protected DriverType driverType = DriverType.chrome;
    protected WebDriverWait wait;
    protected Actions builder;
    /**
     * スクリーンショット保存先
     */
    protected String imageFileDir = ".\\screenshot\\";
    /**
     * ファイル出力先
     */
    protected String outputFilePath;
    /**
     * 画面遷移時のウインドウID
     */
    // 現在のウインドウID
    protected String currentWindowId;
    // 遷移先のウインドウID
    protected String newWindowId;

    protected Logger logger;
    protected ch.qos.logback.classic.Logger log;

    /**
     * 処理待ち時間
     */
    protected int waitSeconds = 10;

    protected Workbook workbook;

    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * 初期処理<BR>
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {

        // XXX 実行環境に合わせてパスを見直してください。
        switch (driverType) {
        case chrome:
            ChromeOptions gcOptions = new ChromeOptions();
            gcOptions.setBinary("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
            System.setProperty("webdriver.chrome.driver", ".\\driver\\chromedriver\\chromedriver.exe");

            gcOptions.addArguments("--disable-gpu");
            driver = new ChromeDriver(gcOptions);
            break;
        case firefox:
            FirefoxOptions ffOptions = new FirefoxOptions();
            ffOptions.setBinary("C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe");

            FirefoxProfile ffProfile = new FirefoxProfile();
            ffOptions.setProfile(ffProfile);

            DesiredCapabilities capabilities = DesiredCapabilities.firefox();
            capabilities.setCapability("marionette", true);
            ffOptions.addCapabilities(DesiredCapabilities.firefox());

            System.setProperty("webdriver.gecko.driver", ".\\driver\\geckodriver-v0.16.1-win64\\geckodriver.exe");
            driver = new FirefoxDriver(ffOptions);
            break;
        case IE_x64:
            // TODO IE 64bit 未検証（インターネットオプションやレジストリの変更が必要そう・・・）
            // System.setProperty("webdriver.ie.driver", ".\\driver\\IEDriverServer_x64_3.4.0\\IEDriverServer.exe");
            System.setProperty("webdriver.ie.driver", ".\\driver\\IEDriverServer_x64_3.11.1\\IEDriverServer.exe");
            driver = new InternetExplorerDriver();
            break;
        case IE_x86:
            // TODO
        case Edge:
            System.setProperty("webdriver.edge.driver", ".\\driver\\edgedriver\\MicrosoftWebDriver.exe");
            DesiredCapabilities cap = DesiredCapabilities.edge();
            cap.setCapability("marionette", true);
            driver = new EdgeDriver(cap);
            break;
        default:
            // TODO IE 32bit 未検証（インターネットオプションやレジストリの変更が必要そう・・・）
            System.setProperty("webdriver.ie.driver", ".\\driver\\IEDriverServer_Win32_3.4.0\\IEDriverServer.exe");
            driver = new InternetExplorerDriver();
            break;
        }

        // 要素を見つけるまでの時間を指定
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

        // 最大待機時間（秒）を設定
        wait = new WebDriverWait(driver, waitSeconds);

        // マウスアクション用アクションビルダー
        builder = new Actions(driver);

        // 画面最大化
        driver.manage().window().maximize();

    }

    /**
     * メイン処理<BR>
     *
     * @throws Exception
     */
    protected abstract void testMain() throws Exception;

    /**
     * 後処理<BR>
     *
     * @throws Exception
     */
    protected void tearDown() throws Exception {
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    protected boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    protected String closeAlertAndGetItsText() {
        try {
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            if (acceptNextAlert) {
                alert.accept();
            } else {
                alert.dismiss();
            }
            return alertText;
        } finally {
            acceptNextAlert = true;
        }
    }

    /**
     * スクリーンショット取得
     *
     * @param fname ファイル名
     * @throws Exception
     * @return スクリーンショットのファイル
     */
    protected File takeScreenShot(String fname) throws Exception {
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File(imageFileDir, fname));
        return scrFile;
    }

    /**
     * スクリーンショット取得
     * スクロールがある場合、自動スクロールで全画面を個別取得
     *
     * @param fname ファイル名
     * @throws Exception
     */
    protected void takeScreenShotAll(String fname) throws Exception {
        JavascriptExecutor jexec = (JavascriptExecutor) driver;
        int innerH = Integer.parseInt(String.valueOf(jexec.executeScript("return window.innerHeight")));
        // int innerW =Integer.parseInt(String.valueOf(jexec.executeScript("return window.innerWidth")));
        int scrollH = Integer
                        .parseInt(String.valueOf(jexec.executeScript("return document.documentElement.scrollHeight")));
        int i = 0;

        File file = takeScreenShot(fname + "_" + (i + 1) + "_.png");

        // 縦スクロール
        if (scrollH > innerH) {
            // ヘッダーがある画面用に補正
            innerH = innerH - 200;
            while (scrollH > innerH) {
                scrollH = scrollH - innerH;
                i++;
                jexec.executeScript("window.scrollTo(0, " + innerH * i + ")");
                takeScreenShot(fname + "_" + (i + 1) + "_.png");
            }
        }

        // 画像連結
        try {
            // 横サイズ
            int width = ImageIO.read(file).getWidth();
            // 縦サイズ
            // 画像数から算出
            int height = ImageIO.read(file).getHeight() * (i + 1);

            BufferedImage newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = newImg.getGraphics();
            int drawHeight = 0;
            for (int cnt = 0; cnt <= i; cnt++) {
                BufferedImage image = ImageIO
                                .read(new FileInputStream(imageFileDir + fname + "_" + (cnt + 1) + "_.png"));
                graphics.drawImage(image, 0, drawHeight, null);
                drawHeight += image.getHeight();
            }
            ImageIO.write(newImg, "png", new File(imageFileDir + "result\\" + fname + ".png"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * テキスト入力処理
     *
     * @param name 名称
     * @param value 入力文字列（nullの場合は空文字をセット）
     */
    protected void inputTextByName(String name, String value) {
        driver.findElement(By.name(name)).clear();
        if (value == null) {
            // nullの場合は、空文字をセット
            driver.findElement(By.name(name)).sendKeys("");
        } else {
            driver.findElement(By.name(name)).sendKeys(value);
        }
    }

    /**
     * テキスト入力処理
     *
     * @param name 名称
     * @param cell 入力文字列（nullの場合は空文字をセット）
     */
    protected void inputTextByName(String name, Cell cell) {
        driver.findElement(By.name(name)).clear();
        if (cell == null) {
            // nullの場合は、空文字をセット
            driver.findElement(By.name(name)).sendKeys("");
        } else {
            driver.findElement(By.name(name)).sendKeys(cell.getStringCellValue());
        }
    }

    /**
     * テキスト入力処理
     *
     * @param id ID
     * @param value 入力文字列（nullの場合は空文字をセット）
     */
    protected void inputTextById(String id, String value) {
        driver.findElement(By.id(id)).clear();
        if (value == null) {
            // nullの場合は、空文字をセット
            driver.findElement(By.id(id)).sendKeys("");
        } else {
            driver.findElement(By.id(id)).sendKeys(value);
        }
    }

    /**
     * クリック処理
     *
     * @param name 名称
     */
    protected void clickByName(String name) {
        driver.findElement(By.name(name)).click();
    }

    /**
     * クリック処理
     *
     * @param xpath XPath
     */
    protected void clickAByXpath(String xpath) {
        driver.findElement(By.xpath(xpath)).click();
    }

    /**
     * クリック処理
     *
     * @param id ID
     */
    protected void clickAById(String id) {
        driver.findElement(By.id(id)).click();
    }

    /**
     * クリック処理
     *
     * @param id ID
     */
    protected void clickById(String id) {
        driver.findElement(By.id(id)).sendKeys(Keys.ENTER);
    }

    /**
     * クリック処理
     *
     * @param xpath XPath
     */
    protected void clickByXpath(String xpath) {
        driver.findElement(By.xpath(xpath)).sendKeys(Keys.ENTER);
    }

    /**
     * チェックボックスON処理
     *
     * @param id ID（nullまたは空文字の場合は何もしない）
     */
    protected void checkBoxById(String id) {
        if (id != null && !("").equals(id)) {
            JavascriptExecutor jexec = (JavascriptExecutor) driver;
            String js = "document.getElementById('" + id + "').click();";
            jexec.executeScript(js);
        }
    }

    /**
     * リストボックス選択処理
     *
     * @param id ID
     * @param value 選択対象の表示名称（nullの場合は何もしない）
     */
    protected void setListboxById(String id, String value) {
        if (value != null) {
            // null以外の場合にリストボックス選択
            Select listbox = new Select(driver.findElement(By.id(id)));
            listbox.selectByVisibleText(value);
        }
    }

    /**
     * リストボックス選択処理
     *
     * @param id ID
     * @param cell 選択対象の表示名称（nullの場合は何もしない）
     */
    protected void setListboxById(String id, Cell cell) {
        if (cell != null) {
            // null以外の場合にリストボックス選択
            Select listbox = new Select(driver.findElement(By.id(id)));
            listbox.selectByVisibleText(cell.getStringCellValue());
        }
    }

    /**
     * リストボックス選択処理
     *
     * @param name 名称
     * @param value 選択対象の表示名称（nullの場合は何もしない）
     */
    protected void setListboxByName(String name, String value) {
        if (value != null) {
            // null以外の場合にリストボックス選択
            Select listbox = new Select(driver.findElement(By.name(name)));
            listbox.selectByVisibleText(value);
        }
    }

    /**
     * リストボックス選択処理
     *
     * @param name 名称
     * @param cell 選択対象の表示名称（nullの場合は何もしない）
     */
    protected void setListboxByName(String name, Cell cell) {
        if (cell != null) {
            // null以外の場合にリストボックス選択
            Select listbox = new Select(driver.findElement(By.name(name)));
            listbox.selectByVisibleText(cell.getStringCellValue());
        }
    }

    /**
     * リンククリック処理
     *
     * @param linkText クリック対象の文字列（nullの場合は何もしない）
     */
    protected void clickAnchor(String linkText) {
        if (linkText != null) {
            driver.findElement(By.linkText(linkText)).click();
        }
    }

    /**
     * リンククリック処理
     *
     * @param id クリック対象の文字列（nullの場合は何もしない）
     */
    protected void clickAnchorById(String id) {
        if (id != null) {
            driver.findElement(By.id(id)).click();
        }
    }

    /**
     * リンククリック処理
     *
     * @param cell クリック対象の文字列（nullの場合は何もしない）
     */
    protected void clickAnchor(Cell cell) {
        if (cell != null) {
            driver.findElement(By.linkText(cell.getStringCellValue())).click();
        }
    }

    /**
     * CSSセレクタクリック処理
     *
     * @param cssSelector クリック対象の文字列
     */
    protected void clickCssSelector(String cssSelector) {
        driver.findElement(By.cssSelector(cssSelector)).click();
    }

    /**
     * マスターファイルアップロード処理
     *
     * @param id ID
     * @param filePath アップロードファイルのパス
     */
    protected void setCustomMasterUpload(String id, String filePath) throws Exception {
        try {
            // フォーマット
            setListboxByName("format", id);
            // ファイル
            driver.findElement(By.name("file")).sendKeys(filePath);
            // アップロードボタン押下
            clickByXpath("//input[@value='アップロード']");

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /**
     * ファイルアップロード処理
     *
     * @param filePath アップロードファイルのパス
     */
    protected void setRecordMonthlyCsvUpload(String filePath) {
        // ファイル
        driver.findElement(By.name("file")).sendKeys(filePath);
        // アップロードボタン押下
        clickByXpath("//input[@value='アップロード']");
        // 登録ボタン押下
        clickByXpath("//input[@value='登録']");
    }

    /**
     * 画面拡大率の設定（デフォルト100%）
     *
     * @param rate 画面拡大率
     */
    protected void screenRate(String rate) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("document.body.style.zoom = '" + rate + "'");
    }

}
