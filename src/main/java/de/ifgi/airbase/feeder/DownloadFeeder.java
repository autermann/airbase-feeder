/**
 * 
 */

package de.ifgi.airbase.feeder;

import static de.ifgi.airbase.feeder.util.Utils.get;
import static java.lang.Boolean.parseBoolean;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ifgi.airbase.feeder.util.Utils;

/**
 * @author Daniel Nüst (daniel.nuest@uni-muenster.de)
 * 
 */
public class DownloadFeeder extends Feeder {
    private static final Logger log = LoggerFactory.getLogger(DownloadFeeder.class);

    private static final String DOWNLOAD_COUNTRIES_PROPERTY = "eea.download.countries";
    private static final String DOWNLOAD_ALL_COUNTRIES_PROPERTY = "eea.download.countries.all";
    private static final String DOWNLOAD_OVERWRITE_PROPERTY = "eea.download.overwrite";
    private static final String AIRBASE_VERSION = Utils.get("eea.airbase.version");
    private static final String DOWNLOAD_URL = Utils.get("eea.airbase.download");
    private static final String FILE_NAME = "AirBase_%2$s_v%1$s.zip";
    private static final String COUNTRIES_DELIMITER = ";";
    private static final int BUFFER_SIZE = 1024 * 4; // 4k buffer
    private static final boolean DOWNLOAD_ALL_COUNTRIES = parseBoolean(get(DOWNLOAD_ALL_COUNTRIES_PROPERTY));
    private static final boolean OVERWRITE_DOWNLOADS = parseBoolean(get(DOWNLOAD_OVERWRITE_PROPERTY));
    private static final String[] COUNTRIES = {
            // Tried to do it with locales, but not really possible, and the list seems to be fixed
            // Locale[] locales = Locale.getAvailableLocales();
            // see http://www.java2s.com/Tutorial/Java/0220__I18N/Getalistofcountrynames.htm
            "AD", // Andorra
            "AL", // Albania
            "AT", // Austria
            "BA", // Bosnia and Herzegovina
            "BE", // Belgium
            "BG", // Bulgaria
            "CH", // Switzerland
            "CY", // Cyprus
            "CZ", // Czech Republic
            "DE", // Germany
            "DK", // Denmark
            "EE", // Estonia
            "ES", // Spain
            "FI", // Finland
            "FR", // France
            "GB", // United Kingdom
            "GR", // Greece
            "HR", // Croatia
            "HU", // Hungary
            "IE", // Ireland
            "IS", // Iceland
            "IT", // Italy
            "LI", // Liechtenstein
            "LT", // Lithuania
            "LU", // Luxembourg
            "LV", // Latvia
            "ME", // Montenegro
            "MK", // Macedonia, Former Yugoslav Rep
            "MT", // Malta
            "NL", // Netherlands
            "NO", // Norway
            "PL", // Poland
            "PT", // Portugal
            "RO", // Romania
            "RS", // Serbia
            "SE", // Sweden
            "SI", // Slovenia
            "SK", // Slovakia
            "TR" // Turkey
    };

    /**
     * @param args
     */
    public static void main(String[] args) {
        log.info("Starting DownloadFeeder, overwriting existing download: " + OVERWRITE_DOWNLOADS);

        // create temp dir
        String directory;
        try {
            directory = Utils.createTempDir();
        }
        catch (IOException e) {
            log.error("Could not create temporary directory!", e);
            return;
        }

        // download files
        final String[] countries = DOWNLOAD_ALL_COUNTRIES ? COUNTRIES
                                   : Utils.get(DOWNLOAD_COUNTRIES_PROPERTY).split(COUNTRIES_DELIMITER);

        downloadFiles(countries, directory);

        // start feeding
        Feeder eea2sos = new Feeder();
        eea2sos.process(directory);
    }

    /**
     * @param baseurl
     * @param countries
     * @param suffix
     * @param directory
     */
    private static void downloadFiles(String[] countries, String directory) {
        log.info("Downloading files of {} countries", countries.length);

        // create remote
        URL[] remote = new URL[countries.length];
        File[] local = new File[countries.length];
        for (int i = 0; i < countries.length; i++) {
            String country = countries[i].trim();
            try {
                remote[i] = new URL(String.format(DOWNLOAD_URL, AIRBASE_VERSION, country));
            } catch (MalformedURLException e) {
                log.error("Could not create URL", e);
                continue;
            }

            String filename = String.format(FILE_NAME, AIRBASE_VERSION, country);
            local[i] = new File(directory, filename);
        }


        // download files
        downloadFilesToPaths(remote, local);
    }

    /**
     * @param remote
     * @param local
     */
    private static void downloadFilesToPaths(URL[] urls, File[] targetPaths) {
        for (int i = 0; i < urls.length; i++) {
            URL url = urls[i];
            File file = targetPaths[i];
            log.debug("Downloading URL {} to {}", url, file);

            if (file.exists() && !OVERWRITE_DOWNLOADS) {
                log.warn("File already found, not downloading again: {}", file);
                continue;
            }

            BufferedOutputStream os = null;
            InputStream is = null;
            try {
                is = url.openStream();
                os = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);
                byte data[] = new byte[BUFFER_SIZE];
                int x;
                while ( (x = is.read(data, 0, BUFFER_SIZE)) >= 0) {
                    os.write(data, 0, x);
                }
            } catch (IOException e) {
                log.error("Could not open stream to " + url.toString(), e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        log.error("Could not close InputStream", ex);
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ex) {
                        log.error("Could not close OutputStream", ex);
                    }
                }
            }
        }
    }

}
