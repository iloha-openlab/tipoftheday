package tokyo.northside.swing.data;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class HtmlTipData {

    private final URI documentUri;
    private String text = null;

    public HtmlTipData(URI documentUri) {
        this.documentUri = documentUri;
    }

    public static HtmlTipData from(URI uri) throws IOException {
        return new HtmlTipData(uri);
    }

    /**
     * Getter for DocumentURI.
     * @return URI of document.
     */
    public URI getDocumentUri() {
        return documentUri;
    }

    /**
     * Get document as text.
     * @return text.
     * @throws IOException when there is no file at a specified URI.
     */
    public String getText() throws IOException {
        if (text != null) {
            return text;
        }
        text = Files.readString(Path.of(documentUri));
        return text;
    }
}
