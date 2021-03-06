package com.github.wycm;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GeetestCrawlerV2 {
    private static String BASE_PATH = "";
    //开始遍历处距离左边的距离
    private static final int GEETEST_WIDTH_START_POSTION = 60;
    //小方块距离左边界距离
    private static int START_DISTANCE = 6;
    private static ChromeDriver driver = null;
    //文档截图后图片大小
    private static Point imageFullScreenSize = null;
    //html 大小
    private static Point htmlFullScreenSize = null;
    static {
        System.setProperty("webdriver.chrome.driver", "/Users/wangyang/Downloads/chromedriver");
        if (System.getProperty("os.name").toLowerCase().contains("windows")){
            System.setProperty("webdriver.chrome.driver", "D:\\dev\\selenium\\chromedriver_V2.30\\chromedriver_win32\\chromedriver.exe");
        }
        driver = new ChromeDriver();
    }
    public static void main(String[] args) {
        try {

            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
            driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);

            driver.get("https://account.geetest.com/register");
            WebElement element = driver.findElement(By.id("email"));
            element.sendKeys("1234567890@qq.com");
            driver.findElement(By.className("geetest_radar_tip")).click();
            Thread.sleep(2 * 1000);
            Actions actions = new Actions(driver);
            //图一
            actions.clickAndHold(element).perform();
            BufferedImage image = getImageEle(driver.findElement(By.className("geetest_canvas_slice")));
            ImageIO.write(image, "png",  new File(BASE_PATH + "slider.png"));

            //设置原图可见
            driver.executeScript("document.getElementsByClassName(\"geetest_canvas_fullbg\")[0].setAttribute('style', 'display: block')\n");
            //图二
            image = getImageEle(driver.findElement(By.className("geetest_canvas_slice")));
            ImageIO.write(image, "png",  new File(BASE_PATH + "original.png"));
            //隐藏原图
            driver.executeScript("document.getElementsByClassName(\"geetest_canvas_fullbg\")[0].setAttribute('style', 'display: none')\n");
            element = driver.findElement(By.className("geetest_slider_button"));
            actions.clickAndHold(element).perform();
            int moveDistance = calcMoveDistince();
            int d = 0;

            List<MoveEntity> list = getMoveEntity(moveDistance);
            for(MoveEntity moveEntity : list){
                actions.moveByOffset(moveEntity.getX(), moveEntity.getY()).perform();
                System.out.println("向右总共移动了:" + (d = d + moveEntity.getX()));
                Thread.sleep(moveEntity.getSleepTime());
            }
            actions.release(element).perform();
            Thread.sleep(1 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
    private static BufferedImage getImageEle(WebElement ele) {
        try {
            byte[] fullPage = driver.getScreenshotAs(OutputType.BYTES);
            BufferedImage fullImg = ImageIO.read(new ByteArrayInputStream(fullPage));
            System.out.println("fullImage: width:" + fullImg.getWidth() + ", y:" + fullImg.getHeight());
            if (imageFullScreenSize == null){
                imageFullScreenSize = new Point(fullImg.getWidth(), fullImg.getHeight());
            }
            WebElement element = driver.findElement(By.className("loading"));

            System.out.println("html: width:" + element.getSize().width + ", y:" + element.getSize().height);
            if(htmlFullScreenSize == null){
                htmlFullScreenSize = new Point(element.getSize().getWidth(), element.getSize().getHeight());
            }
            Point point = ele.getLocation();
            int eleWidth = (int)(ele.getSize().getWidth() / (float)element.getSize().width * (float)fullImg.getWidth());
            int eleHeight = (int) (ele.getSize().getHeight() / (float)element.getSize().height * (float)fullImg.getHeight());
            BufferedImage eleScreenshot = fullImg.getSubimage((int)(point.getX() / (float)element.getSize().width * (float)fullImg.getWidth()), (int)(point.getY() / (float)element.getSize().height * (float)fullImg.getHeight()), eleWidth, eleHeight);
            return eleScreenshot;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static List<MoveEntity> getMoveEntity(int distance){
        distance = distance - 2;
        List<MoveEntity> list = new ArrayList<>();
        for (int i = 0 ;i <= distance - 3; i = i + 3){
            MoveEntity moveEntity = new MoveEntity();
            moveEntity.setX(3);
            moveEntity.setY(1);
            moveEntity.setSleepTime(0);
            list.add(moveEntity);
        }
        if (distance % 3 > 0){
            MoveEntity moveEntity = new MoveEntity();
            moveEntity.setX(distance % 3);
            moveEntity.setY(1);
            moveEntity.setSleepTime(0);
            list.add(moveEntity);
        }
        return list;
    }

    static class MoveEntity{
        private int x;
        private int y;
        private int sleepTime;//毫秒

        public MoveEntity(){

        }

        public MoveEntity(int x, int y, int sleepTime) {
            this.x = x;
            this.y = y;
            this.sleepTime = sleepTime;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getSleepTime() {
            return sleepTime;
        }

        public void setSleepTime(int sleepTime) {
            this.sleepTime = sleepTime;
        }
    }
    private static int calcMoveDistince() {
        int startWidth = (int)(GEETEST_WIDTH_START_POSTION * (imageFullScreenSize.x + 0.0f)/ htmlFullScreenSize.x);
        START_DISTANCE = (int)(START_DISTANCE * (imageFullScreenSize.x + 0.0f)/ htmlFullScreenSize.x);
        try {
            BufferedImage geetest1 = ImageIO.read(new File(BASE_PATH + "original.png"));
            BufferedImage geetest2 = ImageIO.read(new File(BASE_PATH + "slider.png"));
            for (int i = startWidth; i < geetest1.getWidth(); i++){
                for(int j = 0; j < geetest1.getHeight(); j++){
                    int[] fullRgb = new int[3];
                    fullRgb[0] = (geetest1.getRGB(i, j)  & 0xff0000) >> 16;
                    fullRgb[1] = (geetest1.getRGB(i, j)  & 0xff00) >> 8;
                    fullRgb[2] = (geetest1.getRGB(i, j)  & 0xff);

                    int[] bgRgb = new int[3];
                    bgRgb[0] = (geetest2.getRGB(i, j)  & 0xff0000) >> 16;
                    bgRgb[1] = (geetest2.getRGB(i, j)  & 0xff00) >> 8;
                    bgRgb[2] = (geetest2.getRGB(i, j)  & 0xff);
                    if(difference(fullRgb, bgRgb) > 255){
                        int moveDistance = (int)((i - START_DISTANCE) / ((imageFullScreenSize.x + 0.0f)/ htmlFullScreenSize.x));
                        System.out.println("需要移动的距离:" + moveDistance);
                        return moveDistance;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("计算移动具体失败");
    }
    private static int difference(int[] a, int[] b){
        return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]) + Math.abs(a[2] - b[2]);
    }
}
