import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;

class Notebook {
    // Logical state variables
    private static String rootPath;     // Path to the notebook folder
    private boolean isNewNotebook = false;
    private boolean dataIsLoaded;       // True when notebook.zim has been parsed

    // Notebook metadata variables
    private String name;
    private String version;
    private String interwiki;
    private String homePage;
    private String iconPath;
    private String documentPath;
    private boolean shared;
    private String endOfLine;
    private boolean disableTrash;
    private String profile;
    private ArrayList<String> extraEntries;   // Storage for unrecognised information

    // Constants
    // public enum newlineStyle = { WINDOWS, UNIX, MAC };

    /** Creates a new Notebook object located at the path given.
     * @param root The path of the Zim notebook's root folder
     * @throws IOException
     */
    public Notebook(String root) throws IOException {
        this.rootPath = root;
        this.loadData();
    }

    /** Returns true if the directory exists and is a valid notebook.
     */
    public boolean exists() {
        File n = new File(this.rootPath);
        File zim = new File(this.rootPath + File.separator + "notebook.zim");
        return n.exists() && zim.exists();
    }

    /** Returns true if this notebook will be created by this instance.
     */
    public boolean isNewNotebook() {
        return this.isNewNotebook;
    }

    //{{{ GETTERS AND SETTERS
    /** Returns the name of the notebook.
     */
    public String getName() {
        return this.name;
    }

    /** Returns the version of the notebook.
     */
    public String getVersion() {
        return this.version;
    }

    /** Returns the interwiki keyword of the notebook.
     */
    public String getInterwiki() {
        return this.interwiki;
    }

    /** Returns the name of the notebook's home page.
     */
    public String getHomePage() {
        return this.homePage;
    }

    /** Returns the path of the notebook's icon.
     */
    public String getIconPath() {
        return this.iconPath;
    }

    /** Returns the path of the notebook's document root.
     */
    public String getDocumentPath() {
        return this.documentPath;
    }

    /** Returns true if the notebook is shared.
     */
    public boolean getShared() {
        return this.shared;
    }

    /** Returns the notebook's end of line style.
     *  Valid values are Notebook.DOS, Notebook.UNIX, and Notebook.MAC
     */
    public String getEndOfLine() {
        return this.endOfLine;
    }

    /** Returns true if the notebook's trash is disabled.
     */
    public boolean getTrashDisabled() {
        return this.disableTrash;
    }

    /** Returns the profile name of the notebook.
     */
    public String getProfile() {
        return this.profile;
    }
    //}}} GETTERS AND SETTERS

    /** Writes the entries in notebook.zim to file
     * @throws IOException
     */
    public void saveData() throws IOException {
        BufferedWriter metadataFile = new BufferedWriter(new Filewriter(this.rootPath + File.separator + "notebook.zim"));
        // Write the identifier
        String sectionHeader = "[Notebook]\n";
        metadataFile.write(sectionHeader, 0, sectionHeader.length());
        // Prepare the known entries
        String versionEntry = "version=";
        String nameEntry = "name=";
        String interwikiEntry = "interwiki=";
        String homeEntry = "home=";
        String iconEntry = "icon=";
        String documentEntry = "document_root=";
        String sharedEntry = "shared=";
        String eolEntry = "endofline=";
        String trashEntry = "disable_trash=";
        String profileEntry = "profile=";
        if (this.version==null) {
            versionEntry = versionEntry + "\n";
        } else {
            versionEntry = versionEntry + this.version + "\n";
        }
        if (this.name==null) {
            nameEntry = nameEntry + "\n";
        } else {
            nameEntry = nameEntry + this.name + "\n";
        }
        if (this.interwiki==null) {
            interwikiEntry = interwikiEntry + "\n";
        } else {
            interwikiEntry = interwikiEntry + this.interwiki + "\n";
        }
        if (this.homePage==null) {
            homeEntry = homeEntry + "\n";
        } else {
            homeEntry = homeEntry + this.homePage + "\n";
        }
        if (this.iconPath==null) {
            iconEntry = iconEntry + "\n";
        } else {
            iconEntry = iconEntry + this.iconPath + "\n";
        }
        if (this.documentPath==null) {
            documentEntry = documentEntry + "\n";
        } else {
            documentEntry = documentEntry + this.documentPath + "\n";
        }
        if (this.shared==null) {
            sharedEntry = sharedEntry + "\n";
        } else if (shared==true) {
            sharedEntry = sharedEntry + "True\n";
        } else {
            sharedEntry = sharedEntry + "False\n";
        }
        if (this.endOfLine==null) {
            eolEntry = eolEntry + "\n";
        } else {
            eolEntry = eolEntry + this.endOfLine + "\n";
        }
        if (this.disableTrash==null) {
            trashEntry = trashEntry + "\n";
        } else if (this.disableTrash==true) {
            trashEntry = trashEntry + "True\n";
        } else {
            trashEntry = trashEntry + "False\n";
        }
        if (this.profile==null) {
            profileEntry = profileEntry + "\n";
        } else {
            profileEntry = profileEntry + this.profile + "\n";
        }
        // Write the known entries
        metadataFile.write(versionEntry, 0, versionEntry.length());
        metadataFile.write(nameEntry, 0, nameEntry.length());
        metadataFile.write(interwikiEntry, 0, interwikiEntry.length());
        metadataFile.write(homeEntry, 0, homeEntry.length());
        metadataFile.write(iconEntry, 0, iconEntry.length());
        metadataFile.write(documentEntry, 0, documentEntry.length());
        metadataFile.write(sharedEntry, 0, sharedEntry.length());
        metadataFile.write(eolEntry, 0, eolEntry.length());
        metadataFile.write(trashEntry, 0, trashEntry.length());
        metadataFile.write(profileEntry, 0, profileEntry.length());
        // Write the unknown entries
        for (String e : this.extraEntries) {
            metadataFile.write(e + "\n", 0, e.length() + 1);
        }
        // Save the file
        metadataFile.close();
    }

    /** Loads the values from notebook.zim.
     */
    private void loadData() throws IOException {
        BufferedReader metadataFile = new BufferedReader(new FileReader(rootPath + File.separator + "notebook.zim"));
        this.extraEntries = new ArrayList<String>();
        String nextLine;
        try {
            // The first line must be '[Notebook]'
            nextLine = metadataFile.readLine();
            if (nextLine == null || ! nextLine.equals("[Notebook]")) {
                throw new IOException("Not a zim notebook: notebook.zim doesn't start with a Notebook section");
            }
            // The second line must be the version entry
            nextLine = metadatFile.readLine();
            if (nextLine == null || !nextLine.startsWith("version=")) {
                throw new IOException("Not a zim notebook: no version entry in notebook.zim");
            } else {
                int sepPos = nextLine.lastIndexOf("=");
                this.version = nextLine.substring(sepPos + 1, nextLine.length());
            }
            // Read the remaining entries
            while (metadataFile.ready()) {
                nextLine = metadataFile.readLine();
                
                // Check that the line is a key=value format line
                if (nextLine.contains("=")) {
                    String[] tokens = nextLine.split("=");
                    switch (tokens[0]) {
                        case "name":
                            if (tokens.length > 1) {
                                this.name = tokens[1];
                            } else {
                                throw new IOException("Error parsing notebook.zim: empty name");
                            }
                            break;
                        case "interwiki":
                            if (tokens.length > 1) {
                                this.interwiki = tokens[1];
                            }
                            break;
                        case "home":
                            if (tokens.length > 1) {
                                this.homePage = tokens[1];
                            }
                            break;
                        case "icon":
                            if (tokens.length > 1) {
                                this.iconPath = tokens[1];
                            }
                            break;
                        case "document_root":
                            if (tokens.length > 1) {
                                this.documentPath = tokens[1];
                            }
                            break;
                        case "shared":
                            if (tokens.length > 1) {
                                this.shared = (Boolean.valueOf(tokens[1])).booleanValue();
                            }
                            break;
                        case "endofline":
                            if (tokens.length > 1) {
                                this.endOfLine = tokens[1];
                            }
                            break;
                        case "disable_trash":
                            if (tokens.length > 1) {
                                this.disableTrash = (Boolean.valueOf(tokens[1])).booleanValue();
                            }
                            break;
                        case "profile":
                            if (tokens.length > 1) {
                                this.profile = tokens[1];
                            }
                            break;
                        default:
                            System.err.println("Warning: unknown entry parsing notebook.zim for " + this.rootPath);
                            this.extraEntries.add(nextLine);
                            break;
                    }
                } else {
                    System.err.println("Warning: Notebook.zim contains more than simple entries after [Notebook] section.");
                }
            }
            metadataFile.close();
            this.dataIsLoaded = true;
        } catch (FileNotFoundException e) {
            // This means that this is a new notebook. Let's set some defaults
            this.version = "0.4";
            this.name = "Notes";
            this.interwiki = null;
            this.homePage = "Home";
            this.iconPath = null;
            this.documentPath = null;
            this.shared = false;
            this.endOfLine = this.unix;
            this.disable_trash = false;
            this.profile = null;
            this.isNewNotebook = true;
        } 
    }

    public String getPageFilename(String pageName) {
        String pageFilename = this.rootPath;
        if (pageName.contains(":")) {
            String[] pageNameParts = pageName.split(":");
            int i = 0;
            while (i < pageNameParts.length) {
                pageFilename = pageFilename + File.separator + pageNameParts[i];
                i++;
            }
        } else {
            pageFilename = pageFilename + File.separator + pageName;
        }
        pageFilename = pageFilename + ".txt";
        return pageFilename;
    }

    private String prepateEntry(String base, String value) {
        if (value==null) {
            return base + "\n";
        } else {
            return base + value + "\n";
        }
    }
    
    private String prepareEntry(String base, boolean value) {
        if (value==null) {
            return base + "\n";
        } else if (value==true) {
            return base + "True\n";
        } else {
            return base + "False\n";
        }
    }
}
