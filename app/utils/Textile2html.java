package utils;

import java.io.StringReader;

import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.rendering.converter.Converter;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;

public class Textile2html {

    public static String parse(String textile) throws ComponentLookupException, ConversionException, ParseException,
            TransformationException {
        // Initialize Rendering components and allow getting instances
        EmbeddableComponentManager componentManager = new EmbeddableComponentManager();
        componentManager.initialize(Textile2html.class.getClassLoader());

        Parser parser = componentManager.lookup(Parser.class, Syntax.XWIKI_2_0.toIdString());
        XDOM xdom = parser.parse(new StringReader(textile));

        // Execute the Macro Transformation to execute Macros.
        Transformation transformation = componentManager.lookup(Transformation.class, "macro");
        TransformationContext txContext = new TransformationContext(xdom, parser.getSyntax());
        transformation.transform(xdom, txContext);

        Converter converter = componentManager.lookup(Converter.class);

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer renderer = componentManager.lookup(BlockRenderer.class, Syntax.XHTML_1_0.toIdString());
        renderer.render(xdom, printer);

        // com.google.appengine.api.utils.SystemProperty.environment.value().value()

        // converter.convert(new StringReader(textile), Syntax.XWIKI_2_0, Syntax.XHTML_1_0, printer);

        // MarkupLanguage language = new TextileLanguageCustom();
        // MarkupParser markupParser = new MarkupParser();
        // markupParser.setMarkupLanguage(language);
        // String htmlContent = "Erreur lors de la conversion textile";
        // try {
        // htmlContent = markupParser.parseToHtml(textile);
        // } catch (Exception e) {
        // Logger.error("Erreur lors de la conversion textile to html textile = %s, erreur %s ",textile, e.getCause());
        // }
        return printer.toString();
    }

}
