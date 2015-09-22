import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.lang.NullPointerException;

class Page {
    private String name;                    // Full page name
    private String path;                    // File path
    private String title;                   // Page title (file name)
    private boolean isNewPage = false;      // Flag for data loading

    // Page metadata variables
    private String wikiVersion;
    private Date creationDate;
    // Store unknkown headers for forward compatibility
    private ArrayList<String> extraHeaders; 

    // Zim format constants
    public final String DEFAULT_VERSION = "zim 0.4";
    public final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    
    /** Create a new Page based on a Notebook and a page name.
     * @param nb The notebook the page is in
     * @param pageName The full page name
     * @throws IOException
     */
    public Page(Notebook parent, String pageName) throws IOException {
        this.name = pageName;
        this.path = parent.getPageFilename(pageName);
        if (pageName.contains(":")) {
            int sepPos = pageName.lastIndexOf(":");
            this.title = nameParts.substring(sepPos + 1, pageName.length());
        } else {
            this.title = pageName;
        }
        this.loadData();
    }

    /** Create a new page at a file path.
     * @param path The file path of the page
     * @throws IOException
     */
    public Page(String path) throws IOException {
        this.path = path;
        String pageName;
        assert(path.endsWith(".txt"));
        int sep = path.lastIndexOf(File.separator);
        int dot = path.lastIndexOf(".");
        if (sep < 0) {
            this.name = path.substring(0, dot);
        } else {
            this.name = path.substring(sep + 1, dot);
        }
        this.title = this.name;
        this.loadData();
    }

    /** Returns true if the page exists.
     */
    public boolean exists() {
        File page = new File(this.path);
        return page.exists();
    }

    /** Returns the full name of the page.
     */
    public String getName() {
        return this.name;
    }

    /** Returns the title of the page.
     */
    public String getTitle() {
        return this.title;
    }

    /** Returns the wiki markup version as a string.
     */
    public String getWikiVersion() {
        return this.wikiVersion;
    }

    /** Sets the wiki version to be recorded in the page
     * @param versionString The string representing the version, as it will be
     * written to file.
     */
    public void setWikiVersion(String versionString) {
        this.wikiVersion = versionString;
    }

    /** Returns the page creation date
     */
    public Date getCreationDate() {
        return this.creationDate;
    }

    /** Sets the page creation date to be recorded on the page
     * @param creationDate The date to record
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /** Returns true if the page was created by this Page instance.
     */
    public boolean isNewPage() {
        return this.isNewPage;
    }

    /** Returns the contents of the page as a String (headers are excluded).
     * @throws IOException
     */
    public String read() throws IOException {
        File pageFile = new File(this.path);
        StringBuilder bodyText = new StringBuilder(2000);
        try {
            BufferedReader pageReader = new BufferedReader(new FileReader(path));
            // Skip the headers (i.e. to the first blank line)
            String nextLine = "default_value_for_loop";
            while (! nextLine.equals("")) {
                nextLine = pageReader.readLine();
            }
            // Read the rest of the body text and build it into a page
            while (pageReader.ready()) {
                bodyText.append(pageReader.readLine() + "\n");
            }
        } catch (FileNotFoundException e) {
            System.err.println("Tried to read empty file at " + this.path);
            return null;
        }
        return bodyText.toString();
    }

    /** Writes the contents of the page.
     * @param bodyText The new page contents to be written
     * @throws IOException
     */
    public void write(String bodyText) throws IOException {
        // Header strings; perhaps these should be constants?
        String contentTypeHeader = "Content-Type: text/x-zim-wiki\n";
        String wikiFormatHeader = "Wiki-Format: ";
        String creationDateHeader = "Creation-Date: ";
        SimpleDateFormat formatter = new SimpleDateFormat(this.DATE_FORMAT);
        // Append metadata values to header strings
        wikiFormatHeader = wikiFormatHeader + this.wikiVersion + "\n";
        creationDateHeader = creationDateHeader + formatter.format(this.creationDate) + "\n";
        // Write the headers to disk
        BufferedWriter pageFile = new BufferedWriter(new FileWriter(this.path));
        pageFile.write(contentTypeHeader, 0, contentTypeHeader.length());
        pageFile.write(wikiFormatHeader, 0, wikiFormatHeader.length());
        pageFile.write(creationDateHeader, 0, creationDateHeader.length());
        for (String extra : this.extraHeaders) {
            pageFile.write(extra + "\n");
        }
        pageFile.write('\n');
        // Write the body text to disk
        pageFile.write(bodyText, 0, bodyText.length());
        pageFile.close();
    }

    /** Loads the information from the headers.
     * @throws IOException
     */
    private void loadData() throws IOException {
        this.extraHeaders = new ArrayList<String>();
        String nextLine;
        try {
            BufferedReader pageFile = new BufferedReader(new FileReader(this.path));
            nextLine = pageFile.readLine();
            if (nextLine == null || ! nextLine.equals("Content-Type: text/x-zim-wiki")) {
                throw new IOException("Not a zim page");
            }
            while (pageFile.ready()) {
                nextLine = pageFile.readLine();
                // The first empty line separates the headers from the contents; break when we find it
                if (nextLine.equals("")) {
                    break;
                }
                // Split the line into header name and value
                String[] tokens = nextLine.split(": ");
                switch (tokens[0]) {
                    case "Wiki-Format":
                        this.wikiVersion = tokens[1];
                        break;
                    case "Creation-Date":
                        SimpleDateFormat formatter = new SimpleDateFormat(this.DATE_FORMAT);
                        this.creationDate = formatter.parse(tokens[1], new ParsePosition(0));
                        break;
                    default:
                        System.err.println("Warning: unrecognised header parsing " + this.path + ": " + tokens[0]);
                        this.extraHeaders.add(nextLine);
                        break;
                }
            }
            /* This block is here because without the version and creation date headers we get
             * NullPointerExceptions elsewhere. Alternative strategies:
             * Set Default Values - This has the disadvantage that it will add potentially
             *      redundant headers that might not be supported by other versions.
             * Fail gracefully - This requires all related code to be rewritten to anticipate
             *      possible nulls.
             */
            if (this.wikiVersion == null || this.creationDate == null) {
                throw new IOException("Zim page is missing mandatory headers");
            }
        } catch (FileNotFoundException e) {
            // This means the page is new, so let's set some defaults
            this.wikiVersion = this.DEFAULT_VERSION;
            this.creationDate = new Date();
            this.isNewPage = true;
        } catch (NullPointerException e) {
            System.err.println("Unable to parse date for page at " + this.path);
            System.err.println("This shouldn't happen unless there is no date value.");
        }
    }
}
