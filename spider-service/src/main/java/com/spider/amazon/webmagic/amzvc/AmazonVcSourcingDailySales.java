package com.spider.amazon.webmagic.amzvc;

import com.common.exception.ServiceException;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.DriverPathCons;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.entity.Cookie;
import com.spider.amazon.utils.JsonToListUtil;
import com.spider.amazon.utils.WebDriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * Amazon供应商中心每日销量数据抓取
 * Sales Diagnostic
 * Download csv From Amazon Vendor central
 * https://vendorcentral.amazon.com/analytics/dashboard/salesDiagnostic
 *
 * Distribute View : Manufacturing
 * Sales View: Shipped COGS
 * Reporting Range: Daily
 */
@Component
@Slf4j
public class AmazonVcSourcingDailySales implements PageProcessor {

    private static int DATE_OFFSET=-3;

    private final static String manufacturingViewXPath = "//*[@id=\"dashboard-filter-distributorView\"]/div/awsui-button-dropdown/div/div/ul/li[contains(@data-testid,'manufacturer')]";
    private final static String sourcingViewXPath = "//*[@id=\"dashboard-filter-distributorView\"]/div/awsui-button-dropdown/div/div/ul/li[contains(@data-testid,'manufacturer')]";

    private final static String salesViewShippedCOGSLevelXPath = "//*[@id='dashboard-filter-viewFilter']//awsui-button-dropdown//ul/li[contains(@data-testid, \"shippedCOGSLevel\")]";

    private final String detailCsvXPath = "//*[@id=\"downloadButton\"]/awsui-button-dropdown/div/div/ul/li/ul[contains(@aria-label, 'Detail View')]/li[contains(@data-testid, 'salesDiagnosticDetail_csv')]";

    private SpiderConfig spiderConfig;

    @Autowired
    public AmazonVcSourcingDailySales(SpiderConfig spiderConfig) {
        this.spiderConfig = spiderConfig;
    }

    private Site site = Site
            .me()
            .setRetryTimes(3)
            .setDomain("https://vendorcentral.amazon.com/analytics/dashboard/salesDiagnostic")
            .setSleepTime(3000)
            .setUserAgent(
                    "User-Agent:Mozilla/5.0(Macintosh;IntelMacOSX10_7_0)AppleWebKit/535.11(KHTML,likeGecko)Chrome/17.0.963.56Safari/535.11");

    /**
     * 设置网站信息
     *
     * @return
     */
    public Site getSite() {
//        Set<Cookie> cookies = cookiesUtils.keyValueCookies2CookiesSet(cookiesConfigName, ";", "=");
        List<Cookie> listCookies = JsonToListUtil.amazonSourceCookieList2CookieList(JsonToListUtil.getListByPath(spiderConfig.getAmzVcCookieFilepath()));

        for (Cookie cookie : listCookies) {
            site.addCookie(cookie.getName(), cookie.getValue());
        }
        return site;
    }

    /**
     * 页面抓取过程
     *
     * @param page page
     */
    public void process(Page page) {
        if (log.isInfoEnabled()) {
            log.info("0.step21=>进入抓取");
        }


        // 1.建立WebDriver
        System.setProperty("webdriver.chrome.driver", DriverPathCons.CHROME_DRIVER_PATH);

        WebDriver driver = WebDriverUtils.getWebDriver(spiderConfig.getDownloadPath());

        try {

            // 1.1设置页面超时等待时间,20S
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

            WebDriverWait wait = new WebDriverWait(driver, 20);

            //4.1 navigate to sales daily page
            navigateToPage(driver, wait);

            //4.1点击日期选择按钮, Reporting range
            WebElement reportingRangeButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"dashboard-filter-reportingRange\"]//awsui-button-dropdown//awsui-button/button"), 10);
            if (log.isInfoEnabled()) {
                log.info("1.step105=>reportingRangeButtonElement:" + reportingRangeButtonElement.toString());
            }
            WebDriverUtils.elementClick(reportingRangeButtonElement);

            //4.2点击选择daily
            WebElement dailySelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"dashboard-filter-reportingRange\"]/div/awsui-button-dropdown//ul/li[contains(@data-testid, 'DAILY')]"), 10);
            if (log.isInfoEnabled()) {
                log.info("2.step112=>dailySelectElement:" + dailySelectElement.toString());
            }
            dailySelectElement.click();

            // 4.21 Choose DistributeView View
            WebElement distributeViewViewButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-distributorView']//awsui-button-dropdown//button"), 10);
            if (log.isInfoEnabled()) {
                log.info("1.step105=>distributeViewViewButtonElement:" + distributeViewViewButtonElement.toString());
            }
            distributeViewViewButtonElement.click();

            // 4.22点击选择View
            if (log.isInfoEnabled()) {
                log.info("1.1.step137=>点击选择View");
            }
            WebElement distributeViewSelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath(sourcingViewXPath), 10);
            if (log.isInfoEnabled()) {
                log.info("2.step112=>distributeViewSelectElement:" + distributeViewSelectElement.toString());
            }
            distributeViewSelectElement.click();

            // 4.21 Choose SalesView
            WebElement salesViewButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-viewFilter']//awsui-button-dropdown//button[1]"), 10);
            if (log.isInfoEnabled()) {
                log.info("1.step105=>salesViewButtonElement:" + salesViewButtonElement.toString());
            }
            salesViewButtonElement.click();

            // 4.22 Select Shipped COGS
            if (log.isInfoEnabled()) {
                log.info("1.1.step137=>点击选择SalesView");
            }
            WebElement salesViewSelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath(salesViewShippedCOGSLevelXPath), 10);
            if (log.isInfoEnabled()) {
                log.info("2.step112=>salesViewSelectElement:" + salesViewSelectElement.toString());
            }
            salesViewSelectElement.click();

            //4.3点击应用按钮
            WebElement applyElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-applyCancel']/div/awsui-button[2]/button"), 10);
            applyElement.click();

//            // 5.进行操作点击下载Excel,抓取标题
//            WebElement titleElement = driver.findElement(By.xpath("//title"));
//            String title = titleElement.getAttribute("text");

            // 6.抓取点击下载元素进行点击
            // 判断是否出现了Download按钮,未在规定时间内出现重新刷新页面
            WebElement downloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='downloadButton']//button[1]"), 10);
            downloadButtonElement.click();

            sleep(5000);

            // 7.抓取CSV元素生成并进行点击
            WebElement detailCsvDownloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath(detailCsvXPath), 10);
            WebDriverUtils.elementClick(detailCsvDownloadButtonElement);

            // 8.获取点击之后的弹出框点击确定
            if (log.isInfoEnabled()) {
                log.info("1.step132=>wait for alert is present");
            }

            wait.until(ExpectedConditions.alertIsPresent());
            if (log.isInfoEnabled()) {
                log.info("1.1.step137=>scrapy the alert");
            }
            Alert downloadAlertElement = driver.switchTo().alert();//获取弹出框
            log.info("alert text:" + downloadAlertElement.getText());//获取框中文本内容
            log.info("alert toString():" + downloadAlertElement.toString());
            downloadAlertElement.accept();

            try {
                sleep(30000);
            } catch (InterruptedException e) {
                throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(),RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        } finally {
            driver.quit();
        }

        if (log.isInfoEnabled()) {
            log.info("1.step84=>抓取结束");
        }

    }

    /**
     * Navigate driver to Daily Sales page
     * @param driver
     * @param wait
     * @throws InterruptedException
     */
    private void navigateToPage(WebDriver driver, WebDriverWait wait) throws InterruptedException {

        // 1.1设置页面超时等待时间,20S
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        // 2.初始打开页面
        driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
        driver.get("https://www.google.com");


        // 3.add Cookies 在工具类中解析json
        driver.manage().deleteAllCookies();
        List<Cookie> listCookies = JsonToListUtil.amazonSourceCookieList2CookieList(JsonToListUtil.getListByPath(spiderConfig.getAmzVcCookieFilepath()));

        WebDriverUtils.addCookies(driver, listCookies);

        // 4.重定向跳转
        driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
        driver.get("https://vendorcentral.amazon.com/analytics/dashboard");

        sleep(10000);

        //4.0 click salesDiagnostic
        WebElement inventoryHealthButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//span[1]/a[contains(@data-reactid,'salesDiagnostic')]"), 10);
        if (log.isInfoEnabled() && inventoryHealthButtonElement != null) {
            log.info("1.step105=>reportingRangeButtonElement:" + inventoryHealthButtonElement.toString());
        }
        WebDriverUtils.elementClick(inventoryHealthButtonElement);
        sleep(10000);

    }

    public static void main(String[] args) {
        System.out.println("0.step67=>抓取程序开启。");

        Spider.create(new AmazonVcSourcingDailySales(null))
                .addUrl("https://www.google.com")
                .run();

        System.out.println("end.step93=>抓取程序结束。");

    }


}

