package controllers;

import play.api.libs.json.Json;
import play.data.Form;
import play.mvc.*;
import views.html.*;



/**
 * The Controller serves as the main application controller for the server
 * It handles such things as serving up HTML pages and other misc actions.
 */
public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }


}
