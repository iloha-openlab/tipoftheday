package tokyo.northside.swing.data;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HtmlTipData {

    private final URI documentUri;
    private final String stylesheetPath;
    private final Document document;

    public static HtmlTipData from(URI uri) throws IOException {
        String base = new File(uri).getAbsolutePath();
        String text = Files.readString(Path.of(uri));
        Document document = Jsoup.parse(text, base);
        Element link = document.select("html head link").first();
        String css = null;
        if (link != null) {
            css = link.attr("href");
        }
        return new HtmlTipData(uri, css, document);
    }

    public HtmlTipData(URI documentUri, String stylesheetPath, Document document) {
        this.documentUri = documentUri;
        this.stylesheetPath = stylesheetPath;
        this.document = document;
    }

    public URI getDocumentUri() {
        return documentUri;
    }

    public String getStylesheetPath() {
        String path = documentUri.toString();
        if (path.equals("/")) {
            return stylesheetPath;
        }
        int lastIdx = path.lastIndexOf("/");
        if (lastIdx > 0) {
            return Paths.get(path.substring(0, lastIdx), stylesheetPath).toString();
        }
        return stylesheetPath;
    }

    public String getHtml() {
        return document.text();
    }

    public Document getDocument() {
        return document;
    }

    @Override
    public String toString() {
        return "HtmlTipDocument{" +
                "documentUri=" + documentUri +
                ", stylesheetUri=" + stylesheetPath +
                '}';
    }

}
