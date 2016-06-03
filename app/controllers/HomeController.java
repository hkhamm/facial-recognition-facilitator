package controllers;

import com.google.common.io.Files;

import play.mvc.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This controller contains an action to handle HTTP requests to the application's home page.
 */
public class HomeController extends Controller {

    public Result index() {
        String text = "";
        try {
            text = Files.toString(new File("res/index.txt"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ok(text);
    }
}
