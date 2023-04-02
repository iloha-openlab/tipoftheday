package tokyo.northside.swing.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class HtmlTipData {

    private final URI documentUri;
    private final String base;
    private final String text;

    public static HtmlTipData from(URI uri) throws IOException {
        String base = new File(uri).getAbsolutePath();
        String text = Files.readString(Path.of(uri));
        return new HtmlTipData(uri, base, text);
    }

    public HtmlTipData(URI documentUri, String base, String text) {
        this.documentUri = documentUri;
        this.base = base;
        this.text = text;
    }

    public URI getDocumentUri() {
        return documentUri;
    }

    public String getBase() {
        return base;
    }

    public String getText() {
        return text;
    }
}
