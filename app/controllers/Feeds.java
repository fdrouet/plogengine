package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Post;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.transformation.TransformationException;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Router;
import utils.Textile2html;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * This controller is responsible of feed rendering
 * 
 * @author fdrouet
 */
public class Feeds extends Controller {

    public final static String FEEDS = "FEEDS";

    public static void index() throws FeedException {

        // TODO Replace Rome by Apache Abdera ? (http://abdera.apache.org/)

        SyndFeedImpl feed = Cache.get(FEEDS, SyndFeedImpl.class);

        if (feed == null) {
            feed = new SyndFeedImpl();
            // one of : rss_0.90, rss_0.91, rss_0.92, rss_0.93, rss_0.94, rss_1.0 rss_2.0 or atom_0.3
            // source : http://wiki.java.net/twiki/bin/view/Javawsxml/Rome05TutorialFeedWriter
            feed.setFeedType(Play.configuration.getProperty("plog.feed.format", "rss_2.0"));
            feed.setTitle(Play.configuration.getProperty("plog.site.title", "plog.site.title Not set"));

            feed.setLink(Play.configuration.getProperty("plog.feed.url", Request.current().getBase() + Request.current().url));
            feed.setDescription(Play.configuration.getProperty("plog.site.description", "plog.site.description Not set"));

            List<Post> posts = Post.allPublished().fetch();
            List<SyndEntry> entries = new ArrayList<SyndEntry>();

            for (Post post : posts) {
                SyndEntry entry = new SyndEntryImpl();
                entry.setTitle(post.title);
                Map<String, Object> map = new HashMap<String, Object>();
                // Compute and define the entry url
                map.put("id", post.id);
                entry.setLink(Request.current().getBase() + Router.reverse("Application.showById", map).url);
                // FIXME Define the published date instead of the last updated date
                entry.setPublishedDate(post.postedAt);
                // Define the last updated date
                entry.setUpdatedDate(post.postedAt);
                // define the author if configured
                String author = Play.configuration.getProperty("plog.site.author.name");
                if (author != null) {
                    entry.setAuthor(author);
                }
                entries.add(entry);

                SyndContent description = new SyndContentImpl();
                description.setType("text/html");
                try {
                    description.setValue(Textile2html.parse(post.chapeau) + " ...");
                } catch (ComponentLookupException e) {
                    Logger.warn(e, "Problem during rss rendering for post id = " + post.id);
                } catch (ConversionException e) {
                    Logger.warn(e, "Problem during rss rendering for post id = " + post.id);
                } catch (ParseException e) {
                    Logger.warn(e, "Problem during rss rendering for post id = " + post.id);
                } catch (TransformationException e) {
                    Logger.warn(e, "Problem during rss rendering for post id = " + post.id);
                }
                entry.setDescription(description);
                entry.setUri(post.url);
            }
            feed.setEntries(entries);
            Cache.set(FEEDS, feed, Play.configuration.getProperty("plog.feed.cache.expiration", "12h"));
        } else {
            // we use the cached feed version
        }

        SyndFeedOutput output = new SyndFeedOutput();
        renderXml(output.outputString(feed));
    }

    /**
     * This action is responsible to flush the feed cache and redirect to the rss feed content
     */
    public static void refresh() {
        Cache.safeDelete(FEEDS);
        try {
            index();
        } catch (FeedException e) {
            Logger.warn(e, "Problem during rss cache eviction");
        }
    }
}
