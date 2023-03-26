package net.yeyito.roblox;

import net.yeyito.Main;
import net.yeyito.VirtualBrowser;
import net.yeyito.connections.DiscordBot;
import net.yeyito.util.StringFilter;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemManager {
    public static void buyItem(long id) {
        Main.discordBot.sendMessageOnRegisteredChannels("Buying item: " + id,0);
        try {
            Desktop desktop = Desktop.getDesktop();
            URI uri = new URI("https://www.roblox.com/catalog/" + id);
            desktop.browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Robot robot = new Robot();

            // Wait 2.5s
            robot.delay(2500);

            // Move the mouse to the first point (1000px, 400px)
            robot.mouseMove(1000, 400);

            // Press and release the left mouse button
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

            // Wait 1s
            robot.delay(1000);

            // Move the mouse to the second point (900px, 690px)
            robot.mouseMove(900, 690);

            // Press and release the left mouse button
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void sellItem() {

    }
}
