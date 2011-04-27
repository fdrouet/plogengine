/**
 * 
 */
package controllers;

import static play.modules.pdf.PDF.renderPDF;
import models.Post;
import play.mvc.Controller;

/**
 * @author fdrouet
 * 
 */
public class Pdf extends Controller {
    public static void index(long id) {
        Post post = Post.findById(id);
        renderPDF(post);
    }

}
