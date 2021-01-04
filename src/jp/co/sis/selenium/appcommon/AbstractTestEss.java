package jp.co.sis.selenium.appcommon;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import jp.co.sis.selenium.sys.AbstractTestBase;
import jp.co.sis.selenium.sys.DriverType;


/**
 * ESSiテスト抽象クラス
 * @author SIS
 * @version 1.0.0
 *
 */
public abstract class AbstractTestEss extends AbstractTestBase {

    @Override
    protected void setUp() throws Exception {
        driverType = DriverType.chrome;
        super.setUp();
    }

    /**
     * ログイン
     *
     * @param userId ユーザID
     * @param password パスワード
     */
    protected void login(String userId, String password) {

        driver.get(baseUrl + basePath + baseSoshikiCd);
        driver.findElement(By.id("im_user")).clear();
        driver.findElement(By.id("im_user")).sendKeys(userId);
        driver.findElement(By.id("im_password")).clear();
        driver.findElement(By.id("im_password")).sendKeys(password);
        driver.findElement(By.cssSelector("input.imui-btn-login")).click();
//        wait.until(ExpectedConditions.titleContains("ポータル"));

    }

    /**
     * ログイン
     *
     * @param userId ユーザID ※パスワードがユーザIDと同一
     */
    protected void login(String userId) {
        this.login(userId, userId);
    }

    /**
     * ログアウト
     */
    protected void logout() {
        WebElement userUtilityElem = driver.findElement(By.xpath("//*[@id='imui-user-utility']/li/a/span[1]"));
        WebElement logoutElem = driver.findElement(By.xpath("//ul[@id='imui-user-utility']/li/ul/li[2]/a/span"));
        builder.moveToElement(userUtilityElem).click(logoutElem).build().perform();
    }

}
