package tokyo.northside.tipoftheday.data;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * HTML tip data.
 */
public class HtmlTipData {

    private final URI documentUri;
    private @Nullable String text = null;

    public HtmlTipData(URI documentUri) {
        this.documentUri = documentUri;
    }

    public static HtmlTipData from(URI uri) {
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
     * Get a document as text.
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
