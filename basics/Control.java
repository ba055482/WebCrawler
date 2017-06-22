package basics;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


/**
 * Created by BAGIOS on 16-06-16.
 *
 * Project requires the following applications to be installed or to simply exist:
 * - MySQL Database (5.7.13 recommended)
 * - Xpdf utilities
 * - tesseract-ocr 1.03
 * - convert.exe by ImageMagick
 *
 */
public class Control {

    public static String CURRENT_USER;
    public static final String USER_DIR = System.getProperty("user.dir");
    public static String CRAWL_PROPERTIES = USER_DIR + "\\crawl.properties";
    public static final String PATH_TO_TEMP_DIR = System.getProperty("java.io.tmpdir") + "mypool";
    public static String PATH_TO_PDFTOTEXT_EXE;
    public static final String PATH_TO_CONVERT_RES = "/ext/convert.exe";
    public static final String PATH_TO_CONVERT_EXE = PATH_TO_TEMP_DIR + "\\convert.exe";
    public static String PATH_TO_TESSERACT_EXE;
    public static String ROOT_USER;
    public static String ROOT_PASSWORD;
    public static int WORDS_PER_PHRASE;
    public static final int MINIMUM_VALID_WORDS = 25;
    public static final float MINIMUM_GREEK_WORDS_PERCENTAGE = (float) 0.5;
    public static int DOCUMENTS_ADDED_PER_TIME;
    public static int URLS_RETURNED_PER_SEARCH;
    public static final int SEARCH_GOOGLE= 0;
    public static final int SEARCH_YAHOO= 1;
    public static final int SEARCH_YAHOO_AGAIN= 2;
    public static final int SEARCH_BING = 3;
    public static String AZURE_KEY;
    public static String BING_SEARCH_API_KEY;
    public static int NUMBER_OF_RESULTS;
    public static String[] KEYWORDS;
    public static String INPUT_FILE;
    public static Snitch ruffian;
    public static int LOG_LEVEL = 0;
    public static Repo repo;
    public static Properties properties;

    private static Pattern patternURLForDocument;
    public static Matcher matcher;
    private static final String URL_DOCUMENT_PATTERN = "(https?:\\/\\/[A-Za-z0-9\\.#?_!@\\-\\(\\)&$~]+)([\\/A-Za-z0-9#?_!@\\-\\(\\)&$~]*)\\.((pdf)|(docx?))";
    static {
        patternURLForDocument = Pattern.compile(URL_DOCUMENT_PATTERN);
    }

    public static Random genEngine;
    public static Random genTimeout;

    public static int engine;
    public static int timeout;





    public static void initialize(){

        String msg = "";
        KEYWORDS = new String[]{""};
        INPUT_FILE = "";
        File convert;
        File tesseractOCR;
        File pdftotext;
        properties = new Properties();

        System.setProperty("file.encoding", "UTF-8");

        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (IOException e) {
            System.err.println("ERROR: Could not execute a simple 'CLS' command. Continuing...");
        } catch (InterruptedException e) {
            System.err.println("ERROR: Execution of command 'CLS' was interrupted. Wow...");
        }


        System.out.println(PATH_TO_TEMP_DIR);
        System.out.println(PATH_TO_CONVERT_EXE);
        System.out.println(USER_DIR);



        File tempDir = new File(PATH_TO_TEMP_DIR);
        if(!tempDir.exists()){
            tempDir.mkdir();
        }

        ruffian = new Snitch(PATH_TO_TEMP_DIR+"\\ruffian.log");


        // load crawl.properties file
        try {
            InputStream input = new FileInputStream(CRAWL_PROPERTIES);
            InputStreamReader isr = new InputStreamReader(input, "UTF-8");
            if(isr != null){
                properties.load(isr);
                CURRENT_USER = properties.getProperty("current_user");
                ROOT_USER = properties.getProperty("root_user");
                ROOT_PASSWORD = properties.getProperty("root_pass");
                WORDS_PER_PHRASE = new Integer(properties.getProperty("words_per_phrase")).intValue();
                DOCUMENTS_ADDED_PER_TIME = new Integer(properties.getProperty("docs_added_per_search")).intValue();
                URLS_RETURNED_PER_SEARCH = new Integer(properties.getProperty("urls_fetched_per_search")).intValue();
                BING_SEARCH_API_KEY = properties.getProperty("bing_api").replace(" ", "");
                NUMBER_OF_RESULTS = new Integer(properties.getProperty("number_of_results")).intValue();
                LOG_LEVEL = new Integer(properties.getProperty("log_level")).intValue();
                PATH_TO_TESSERACT_EXE = properties.getProperty("tesseract_home") + "\\tesseract.exe";
                PATH_TO_PDFTOTEXT_EXE = properties.getProperty("xpdf_home") + "\\bin32\\pdftotext.exe";
                if(properties.getProperty("keywords")!=null) {
                    KEYWORDS = properties.getProperty("keywords").split(" ");
                }
                else{
                    KEYWORDS = "   ".split(" ");
                }
                if(LOG_LEVEL < 0 || LOG_LEVEL > 2){
                    LOG_LEVEL = 1;
                }
                msg += "\n";
                msg += "-----------------------------------------------------\n";
                msg += "Current configuration:\n";
                msg += "\n";
                msg += " * Logged-in user: " + CURRENT_USER + "\n";
                msg += " * DB root user: " + ROOT_USER + "\n";
                msg += " * DB root password: ******** (read, ok) \n";
                msg += " * Words per phrase: " + WORDS_PER_PHRASE + "\n";
                msg += " * Documents added to Repo per search: " + DOCUMENTS_ADDED_PER_TIME + "\n";
                msg += " * URLs returned per search: " + URLS_RETURNED_PER_SEARCH + "\n";
                if(BING_SEARCH_API_KEY != null && BING_SEARCH_API_KEY.length() > 20) {
                    msg += " * Bing Web Search API Key: ******** (read, ok) \n";
                }
                else{
                    msg += " * Bing Web Search API Key: Not set (Bing search will not be used) \n";
                }
                msg += " * Number of results: " + NUMBER_OF_RESULTS + "\n";
                msg += " * Log level: " + LOG_LEVEL + "\n";
                msg += " * TesseractOCR exe: " + PATH_TO_TESSERACT_EXE + "\n";
                msg += " * pdftotext exe: " + PATH_TO_PDFTOTEXT_EXE + "\n";
                msg += " * keywords: ";
                for(String keyword : KEYWORDS) {
                    msg += keyword + " ";
                }
                msg += "\n";
                msg += "-----------------------------------------------------\n";
                System.out.println(msg);
                ruffian.log(Snitch.level.DEBUG, "Control.initialize\t: " + msg);
                input.close();
            }
            else{
                System.err.println("ERROR: Could not load properties file properly.");
                ruffian.log(Snitch.level.ERROR, "Control.initialize\t: Trying to load a null InputStream, properties file could not be found.");
                input.close();
                cleanUp(1);
            }
        } catch (FileNotFoundException e) {
            ruffian.log(Snitch.level.ERROR, "Control.initialize\t: File crawl.properties not found.");
            System.err.println("File crawl.properties not found!");
            cleanUp(2);
        } catch (IOException e) {
            ruffian.log(Snitch.level.ERROR, "Control.initialize\t: I/O Exception while trying to load crawl.properties file.");
            System.err.println("I/O Exception while trying to load crawl.properties file.");
            cleanUp(3);
        }

        tesseractOCR = new File(PATH_TO_TESSERACT_EXE);
        if(!tesseractOCR.exists()){
            System.err.println("TesseractOCR not installed properly. Execution aborted.");
            ruffian.log(Snitch.level.ERROR, "Control.initialize\t: Could not find executable " + PATH_TO_TESSERACT_EXE);
            cleanUp(8);
        }

        pdftotext = new File(PATH_TO_PDFTOTEXT_EXE);
        if(!pdftotext.exists()){
            System.err.println("XPDF suite not installed properly. Execution aborted.");
            ruffian.log(Snitch.level.ERROR, "Control.initialize\t: Could not find executable " + PATH_TO_PDFTOTEXT_EXE);
            cleanUp(9);
        }


        ruffian.log(Snitch.level.ERROR,"(TEST) This is an ERROR entry.");
        ruffian.log(Snitch.level.INFORMATION,"(TEST) This is an INFORMATION entry.");
        ruffian.log(Snitch.level.DEBUG,"(TEST) This is a DEBUG entry.");

        // extract convert.exe from jar
        InputStream input_convert = Control.class.getResourceAsStream(PATH_TO_CONVERT_RES);

        try {
            if(input_convert != null){
                convert = new File(PATH_TO_CONVERT_EXE);
                if(!convert.exists()) convert.createNewFile();
                Files.copy(input_convert, convert.toPath(), REPLACE_EXISTING);
                input_convert.close();
            }
            else{
                System.err.println("ERROR: Could not extract necessary binaries, due to null input stream");
                ruffian.log(Snitch.level.ERROR, "Control.initialize\t: Trying to load a null InputStream to extract pfdtotext.exe and convert.exe");
                input_convert.close();
                cleanUp(5);
            }
        } catch (IOException e) {
                ruffian.log(Snitch.level.ERROR, "Control.initialize\t: I/O Exception while copying pdftotext.exe or convert.exe to " + PATH_TO_TEMP_DIR);
                System.err.println("Error extracting necessary executables to temp directory.");
                cleanUp(4);
        }


        try {
            repo = new Repo(ROOT_USER, ROOT_PASSWORD);
        } catch (SQLException e) {
            ruffian.log(Snitch.level.ERROR, "Control.initialize\t: Database REPO could not be found");
            System.err.println("Database not created properly. Execution aborted.");
            cleanUp(7);
        } catch (ClassNotFoundException e) {
            ruffian.log(Snitch.level.ERROR, "Control.initialize\t: Unable to load class com.mysql.jdbc.Driver, make sure MySQL JDBC driver is installed");
            System.err.println("MySQL JDBC driver not installed properly. Execution aborted.");
            cleanUp(6);
        }

        genEngine = new Random();
        genTimeout = new Random();

    }


    public static void cleanUp(){

        File d_temp = new File(PATH_TO_TEMP_DIR);
        File[] tempFiles = d_temp.listFiles();

        // empty %TEMP% directory
        for(File file : tempFiles){
            if(file.exists() && !file.getName().equalsIgnoreCase(ruffian.getFileName())){
                ruffian.log(Snitch.level.DEBUG, "Control.cleanUp\t: deleting file " + file.getName());
                file.delete();
            }
        }

        // close ruffian
        ruffian.log(Snitch.level.INFORMATION, "Control.cleanUp\t: This log is now closing");
        ruffian.closeLogFile();
    }


    public static void cleanUp(int errorLevel){
        cleanUp();
        System.exit(errorLevel);
    }



    /**
     * This method is used to check the input when entering plain is selected.
     * It is an alternative way of calling the isValidText method with its default values.
     * @param input
     * @return
     */
    public static boolean isValidText(String input) {
        return isValidText(input, 25, 4, 75);
    }


    /**
     * This method is used to check the input when entering plaintext is selected.
     * TO-DO: more checks are needed to verify that the input text provided is consisted of actual words.
     * @param input
     * @param minWordsCount
     * @param minCharactersPerWord
     * @param maxSmallWordsLimit
     * @return
     */
    public static boolean isValidText(String input, int minWordsCount, int minCharactersPerWord, int maxSmallWordsLimit) {

        boolean wordsCountOK = false;
        boolean charactersPerWordOK = false;
        boolean smallWordsLimitOK = false;

        String[] words = input.split("[\\s.,!;]+");
        ruffian.log(Snitch.level.DEBUG, "Control.isValidText\t: words parsed from input file = " + words.length);

        if(words.length >= minWordsCount) wordsCountOK = true;

        //TO-DO: configure this check using the minCharactersPerWord variable
        charactersPerWordOK = true;

        //TO-DO: configure this check using the maxSmallWordsLimit variable
        smallWordsLimitOK = true;


        if(wordsCountOK && charactersPerWordOK && smallWordsLimitOK) return true;
        else return false;
    }


    public static boolean parsedWithSuccess(String[] words){

        int numberOfGreekWords = 0;
        int numberOfWords = 0;
        float percentageOfValidGreekWords;

        for(String word : words){
            if(word.length() > 1)
                numberOfWords ++;
            if( word.matches("[Α-Ωα-ωάέήίόύώΆΈΉΊΌΎΏϊϋΪΫΐΰ]+") )
                numberOfGreekWords ++;
        }

        percentageOfValidGreekWords = (float) numberOfGreekWords/numberOfWords;

        if( numberOfGreekWords < MINIMUM_VALID_WORDS || percentageOfValidGreekWords < MINIMUM_GREEK_WORDS_PERCENTAGE ) {
            ruffian.log(Snitch.level.DEBUG, "Control.parsedWithSuccess\t: The document was found to be invalid");
            return false;
        }

        //debug information
        ruffian.log(Snitch.level.DEBUG,"greekWords : " + numberOfGreekWords );
        ruffian.log(Snitch.level.DEBUG,"totalWords : " + numberOfWords );
        ruffian.log(Snitch.level.DEBUG,"greekWords/totalWords : " + percentageOfValidGreekWords );

        return true;
    }


    /**
     * This function will attempt to extract text from a PDF file.
     * This function makes use of pdftotext.exe from Xpdf utilities.
     * When called, it will attempt to extract text from a PDF file.
     * Because of a variety on charset encoding, pdftotext.exe may be used twice in order to have a better chance on extracting valid text.
     * The first invocation of pdftotext.exe will be used with the "-enc UTF-8" argument in order to set the character encoding to UTF-8.
     * The second one, will be issued with no encoding-related arguments and the output will be considered to be of ISO-8859-7 charset.
     * After extraction, a validation will be initiated to check whether the text has been parsed with success or the PDF file was actually more complex.
     * If validation is passed, one way ore another, it will return the parsed text.
     * If validation is not passed, then the input PDF will be re-analyzed using a OCR-oriented procedure.
     *
     * System Requirements:
     *                      - pdftotext.exe  :  Utility included in Xpdf packages.
     *                                          Creates a new TXT file with the text extracted from the input PDF file.
     *                                          Xpdf: A PDF Viewer for X
     *                                          Link: http://www.foolabs.com/xpdf/home.html
     *
     * @param   fileName    Absolute path to target PDF file.
     * @return  String[]    String array of each word parsed from input PDF file.
     */
    public static String[] parsePDF(String fileName){

        String[] result = null;

        result = parsePDF(fileName, "pdfbox");

        // Once the input file has been processed, a validation is initiated.
        // If the validation succeeds, text has been successfully parsed from the PDF document.

        ruffian.log(Snitch.level.DEBUG,"Control.parsePDF\t:  Validating file   : "+fileName);

        if( parsedWithSuccess(result) ){
            ruffian.log(Snitch.level.INFORMATION,"Control.parsePDF\t: Validation : OK");
            return result;
        }
        else {
            result = parsePDF(fileName, "-enc UTF-8");
            ruffian.log(Snitch.level.INFORMATION,"Control.parsePDF\t:  Validating file   : "+fileName);
        }
        if( parsedWithSuccess(result) ){
            ruffian.log(Snitch.level.INFORMATION,"Control.parsePDF\t: Validation : OK");
            return result;
        }
        else{

            result = parsePDF(fileName, "");
            ruffian.log(Snitch.level.DEBUG,"Control.parsePDF\t: Re-Validating file: "+fileName);

            if(parsedWithSuccess(result)){
                ruffian.log(Snitch.level.INFORMATION,"Control.parsePDF\t: Validation : OK");
                return result;
            }
            // There is a high chance that the target PDF is unable to be processed, as it has been built from/to be consisted of images.
            // If the validation fails, it means that PDF text was not parsed because it is embedded to an image.
            // In this case, we will try to extract the text by applying a OCR (Optical Character Recognition) technique.
            // TO-DO: check pages. If > 50, skip and continue with next document. No reason to waist more than 30'...
            else {
                Date started = new Date();
                ruffian.log(Snitch.level.INFORMATION, "Control.parsePDF\t: Validation : NOT OK, initiating OCR at " + started.getTime());
                result = startOCR(fileName);
                Date completed = new Date();
                ruffian.log(Snitch.level.INFORMATION, "Control.parsePDF\t: After validation..... OCR completed at " + completed.getTime());
            }
        }
        return result;
    }



    public static String[] parsePDF(String fileName, String encodingArg){

        String txtName = PATH_TO_TEMP_DIR + "\\pdftotext.txt";
        String[] result = null;
        String cmd = "";
        PDFParser parser = null;
        PDDocument pdDoc = null;
        COSDocument cosDoc = null;
        PDFTextStripper pdfStripper;

        String parsedText;
        File file = new File(fileName);


        if(encodingArg.equalsIgnoreCase("pdfbox")){

            try {
                parser = new PDFParser(new FileInputStream(file));

                parser.parse();
                cosDoc = parser.getDocument();
                pdfStripper = new PDFTextStripper();
                pdDoc = new PDDocument(cosDoc);
                //pdfStripper.setStartPage(1);
                //pdfStripper.setEndPage(5);
                parsedText = pdfStripper.getText(pdDoc);
                ruffian.log(Snitch.level.DEBUG, "Control.parsePDF\t: Parsed text size = " + parsedText.length());
                pdDoc.close();
                return prepareText(parsedText);
            } catch (IOException e) {
                parsedText = "n/a";
                result = new String[]{parsedText};
                return result;
            }

        }
        else {


            try {
                cmd = "\"" + PATH_TO_PDFTOTEXT_EXE + "\" " + encodingArg + " \"" + fileName + "\" \"" + txtName + "\"";
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
                if (p.exitValue() == 3) {
                    ruffian.log(Snitch.level.INFORMATION, "Control.parsePDF\t: Unable to use Xpdf due to file permissions. Starting OCR...");
                    result = startOCR(fileName);
                } else {
                    ruffian.log(Snitch.level.DEBUG, "Control.parsePDF\t: Temporary file created: " + txtName);
                    result = parseTXT(txtName, encodingArg);
                }
            } catch (IOException e) {
                ruffian.log(Snitch.level.ERROR, "Control.parsePDF\t: I/O Exception while executing CMD> " + cmd);
            } catch (InterruptedException e) {
                ruffian.log(Snitch.level.ERROR, "Control.parsePDF\t: Batch job was interrupted CMD> " + cmd);
            }

            return result;
        }
    }


    /**
     * This function will attempt to analyze the input PDF using a Optical Character Recognition solution where the default parsing procedure fails.
     * This implementation is really clumsy and rough.
     * It is considered time-consuming and may require a fair amount of free space.
     *
     * System Requirements:
     *              - convert.exe   :   Utility included in ImageMagick package.
     *                                  Used to create a .tiff image of the input PDF file in order to make the embedded text extractable.
     *                                  The resulting .tiff file will be a multi-page TIFF file.
     *                                  For a 15-page PDF, you can expect the resulting TIFF to be around 300MB.
     *                                  ImageMagick-7.0.2-10-portable-Q16-x86
     *                                  Link: http://www.imagemagick.org/script/index.php
     *
     *              - tesseract.exe :   Utility from Tesseract OCR project.
     *                                  Used to extract text from a .tiff image.
     *                                  Tesseract OCR
     *                                  Link: https://github.com/tesseract-ocr/tesseract/wiki
     *
     * @param   fileName    Absolute path to target PDF file.
     * @return  String[]    String array of each word parsed from input PDF file.
     */
    public static String[] startOCR(String fileName) {

        String tiffName = PATH_TO_TEMP_DIR+"\\convert.tiff";
        String txtName = PATH_TO_TEMP_DIR+"\\tesseract";
        String[] pageResult = null;
        String[] fullResult = null;
        int finalSize;
        ArrayList<String[]> pages;
        PDDocument doc = null;
        File file;
        int numberOfPages;
        String command = "";
        int step = 50;
        int position = 0;
        int residual = 0;
        int start = 0;
        int end = 0;
        String areaOfEffect = "";

        file = new File(fileName);

        try {
            doc = PDDocument.load(file);
        } catch (IOException e) {
            ruffian.log(Snitch.level.ERROR, "Control.startOCR\t: I/O Exception while attempting to load a pdf document.");
        }
        numberOfPages = doc.getNumberOfPages();
        ruffian.log(Snitch.level.DEBUG, "Control.startOCR\t: Total pages = " + numberOfPages );

        pages = new ArrayList<>();

        residual = numberOfPages%step;

        for(int i=0;position<numberOfPages;i++){

            if(i == 0){
                start = 0;
            }
            else{
                start = end;
            }

            if(position + step < numberOfPages - 1){
                end += step;
            }
            else{
                end += residual;
            }

            position = end;

            areaOfEffect = "[" + start + "-" + end + "]";


            ruffian.log(Snitch.level.DEBUG, "Control.startOCR\t: Converting pages " + areaOfEffect );


            try {
                command = "\""+PATH_TO_CONVERT_EXE+"\" -density 200 \""+ fileName + areaOfEffect + "\" -depth 8 -strip -background white -alpha off \""+tiffName+"\"";
                Process p = Runtime.getRuntime().exec(command);
                p.waitFor();
            } catch (IOException e) {
                ruffian.log(Snitch.level.ERROR, "Control.startOCR\t: I/O Exception while executing CMD> " + command);
            } catch (InterruptedException e) {
                ruffian.log(Snitch.level.ERROR, "Control.startOCR\t: Batch job was interrupted CMD>  " + command);
            }

            try {
                command = "\"" + PATH_TO_TESSERACT_EXE + "\" -l ell \"" + tiffName + "\" \"" + txtName + "\"";
                Process p = Runtime.getRuntime().exec(command);
                p.waitFor();
            } catch (IOException e) {
                ruffian.log(Snitch.level.ERROR, "Control.startOCR\t: I/O Exception while executing CMD> " + command);
            } catch (InterruptedException e) {
                ruffian.log(Snitch.level.ERROR, "Control.startOCR\t: Batch job was interrupted CMD> " + command);
            }

            /**
             * Example of tesseract.exe invocation:
             * tesseract.exe -l ell "C:\temp\alpha\my.tiff" "C:\temp\alpha\my"
             * Arguments:
             *              -l  ell                 :   specifies which language will be used for OCR (here is ell -> greek)
             *              "C:\temp\alpha\my.tiff" :   input file, my.tiff will be processed.
             *              "C:\temp\alpha\my"      :   output file, text will be save to a new file named my.txt
             *                                          .txt is appended automatically therefore txtName parameter must be updated afterwards
             */
            txtName += ".txt";

            ruffian.log(Snitch.level.DEBUG, "Control.startOCR\t: Parsing pages " + areaOfEffect );
            try {
                pageResult = parseTXT(txtName);
            } catch (FileNotFoundException e) {
                ruffian.log(Snitch.level.ERROR, "Control.startOCR\t: could not find file " + txtName);
            }

            //clean-up temporary information
            deleteTemporaryFile(tiffName);
            deleteTemporaryFile(txtName);

            pages.add(pageResult);
            txtName = txtName.replaceAll("\\.txt", "");
        }

        finalSize = 0;
        for(String[] page : pages){
            finalSize += page.length;
        }

        fullResult = new String[finalSize];

        int r = 0;
        for(String[] page : pages){
            for(String word : page){
                    fullResult[r++] = word;
            }
        }

        ruffian.log(Snitch.level.INFORMATION, "Control.startOCR\t: OCR completed, total words parsed : " + r );

        return fullResult;
    }



    public static String[] parseWORD(String filename) throws Exception {

        File file;
        String fileType;
        String text;
        String[] result = null;

        file = new File(filename);
        fileType = getFileType(file);

        if(fileType.equalsIgnoreCase("docx")) {

            XWPFDocument docx = new XWPFDocument(new FileInputStream(file));
            XWPFWordExtractor xwe = new XWPFWordExtractor(docx);
            text = xwe.getText();
        }
        else if(fileType.equalsIgnoreCase("doc")){

            HWPFDocument doc = new HWPFDocument(new FileInputStream(file));
            WordExtractor we = new WordExtractor(doc);
            text = we.getText();
        }
        else throw new Exception("Unknown file type, expected one of: doc, docx");

        result = prepareText(text);

        return result;

    }




    public static String[] parseTXT(String fileName) throws FileNotFoundException {

        BufferedReader br;
        InputStreamReader isr;
        FileInputStream fis;
        String fulltext = "";
        String[] result = null;
        String tmp = "";

        try {
            fis = new FileInputStream(fileName);
            isr = new InputStreamReader(fis, "UTF-8");
            br = new BufferedReader(isr);

            if (!br.ready()) {
                ruffian.log(Snitch.level.ERROR, "Control.parseTXT\t: Unable to parse file: " + fileName);
                result = new String[]{"dummy"};
                return result;
            }
            fulltext = br.readLine();
            while( (tmp = br.readLine()) != null){
                fulltext = fulltext + tmp + "\n";
            }
        } catch (IOException e){
            // Maybe write some code here?
        }
        result = prepareText(fulltext);

        return result;

    }


    public static String[] parseTXT(String fileName, String encodingArg) throws FileNotFoundException {

        String fullText = "";
        File file = new File(fileName);
        String[] result = null;
        BufferedReader in = null;

        try {
            if(encodingArg.equalsIgnoreCase("-enc UTF-8"))
                in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            else
                in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-7"));
            String str;
            while ((str = in.readLine()) != null) {
                fullText += str;
            }
            in.close();

        } catch (UnsupportedEncodingException e) {
            ruffian.log(Snitch.level.ERROR, "Control.parseTXT\t: Unsupported encoding of text file");
        } catch (FileNotFoundException e) {
            ruffian.log(Snitch.level.ERROR, "Control.parseTXT\t: Text file not found");
        } catch (IOException e) {
            ruffian.log(Snitch.level.ERROR, "Control.parseTXT\t: I/O Exception while attempting to open text file");
        }
        result = prepareText(fullText);

        return result;
    }

    public static String[] parseFile(String filename, int startPoint, int endPoint) throws Exception {
        return parseFile(filename);
    }


    public static String[] parseFile(String filename) throws Exception {

        String extension;
        String[] tokens = null;

        extension= getFileType(new File(filename));
        switch(extension.toLowerCase()){
            case "pdf":
                tokens = parsePDF(filename);
                break;
            case "doc":
                tokens = parseWORD(filename);
                break;
            case "docx":
                tokens = parseWORD(filename);
                break;
            case "txt":
                tokens = parseTXT(filename);
                break;
        }

        return tokens;
    }



    public static String getFileType(File file){

        String filename = file.getName();
        String[] parts = filename.split("[.]");
        return parts[parts.length - 1];

    }


    public static String getFileType(String fileName){

        String[] parts = fileName.split("[.]");
        return parts[parts.length - 1];

    }



    public static String[] prepareText(String fulltext){

        String[] preparedText = null;
        StringTokenizer tokenizer;

        // Remove hyperlinks
        fulltext = fulltext.replaceAll("\\S+://\\S+", "");


        // Merge all words split by new line
        fulltext = fulltext.replaceAll("-\\n", "");


        // Separate words with - symbol
        fulltext = fulltext.replaceAll("(\\S)-(\\S)", "$1 $2");


        // Concatenate words split by new line
        fulltext = fulltext.replaceAll("(\\S)-\r\n(\\S+)", "$1$2");


        // Remove / \ symbols to create separate words
        fulltext = fulltext.replaceAll("[\\\\/]", " ");


        // Remove unwanted characters
        //fulltext = fulltext.replaceAll("[!@#$%^&*\\(\\)–_+=`~0-9\\[\\]\\{\\};:'\"?><»«“”√.,\uD835\uDC49\uDC50\uDC5D\uDC65]", "");
        fulltext = fulltext.replaceAll("[^A-Za-zΑ-Ωα-ωΆΈΉΊΌΎΏάέήίόύώΪΫϊϋΐ \n]+", "");

        // Replace whitespace with new line character to create tokens
        fulltext = fulltext.replaceAll("\\s","\n");

        // Known issue with some pdf files: greek letter "π" decoded as "pi"
        fulltext = fulltext.replaceAll("([Α-Ωα-ωΆΈΉΊΌΎΏάέήίόύώΪΫϊϋΐ]*)pi([Α-Ωα-ωΆΈΉΊΌΎΏάέήίόύώΪΫϊϋΐ]+)","$1π$2");


        tokenizer = new StringTokenizer(fulltext, "\n");

        preparedText = new String[tokenizer.countTokens()];
        int i=0;
        while(tokenizer.hasMoreTokens())
            preparedText[i++] = tokenizer.nextToken();

        return preparedText;

    }


    private static void deleteTemporaryFile(String fileName) {
        File file = new File(fileName);
        if(file.exists()) {
            file.delete();
            ruffian.log(Snitch.level.DEBUG, "Control.deleteTemporaryFile\t: deleted file "+fileName);
        }
    }



    public static String[] configureText(String[] tokens){

        int phraseSize = WORDS_PER_PHRASE;
        int tcount = 0; // number of tokens
        int pcount = 0; // number of phrases
        String[] phrases = new String[tokens.length / phraseSize];

        // initializing phrases[]
        for(int i=0;i<phrases.length;i++)
            phrases[i] = "";

        // do the math
        while(pcount < phrases.length){

            if( (pcount*phraseSize+phraseSize) < tokens.length){
                for(int i=0;i<phraseSize;i++){
                    phrases[pcount] += " ";
                    phrases[pcount] += tokens[tcount+i];
                }
                tcount += phraseSize;
            }
            else {
                int dif = tokens.length - tcount;
                for(int i=0;i<dif;i++) {
                    phrases[pcount] += " ";
                    phrases[pcount] += tokens[tcount++];
                }
            }
            pcount++;
        }
        return phrases;
    }






    public static String getURLForDocument(String rawURL){

        String url = "";
        matcher = patternURLForDocument.matcher(rawURL);
        if(matcher.find()){
            url = matcher.group(0).trim();
        }
        return url;

    }


    public static String[] getDataFromGoogle(String phrase, String keywords) {

        ArrayList<String> urls = new ArrayList<>();
        String[] result;
        String google = "http://www.google.com/search?q=";
        String search = "\"" + phrase + "\"" + keywords;
        String charset = "UTF-8";
        String userAgent = "ExampleBot 1.0 (+http://example.com/bot)"; // Change this to your company's name and bot homepage!

        try {
            Elements links = Jsoup.connect(google + URLEncoder.encode(search, charset) + "&num=" + URLS_RETURNED_PER_SEARCH ).userAgent(userAgent).get().select(".g>.r>a");

            for (Element link : links) {
                String url = link.absUrl("href"); // Google returns URLs in format "http://www.google.com/url?q=<url>&sa=U&ei=<someKey>".
                url = URLDecoder.decode(url.substring(url.indexOf('=') + 1, url.indexOf('&')), "UTF-8");

                if (!url.startsWith("http")) {
                    continue; // Ads/news/etc.
                }

                urls.add(url);
                //ruffian.log(Snitch.level.DEBUG, "Control.getDataFromGoogle\t: URL added: " + url);

            }
        } catch(IOException e) {
            ruffian.log(Snitch.level.ERROR, "Control.getDataFromGoogle\t: I/O Exception while connecting with JSOUP");
        }

        result = new String[urls.size()];
        int i=0;
        for(String url : urls)
            result[i++] = url;

        ruffian.log(Snitch.level.DEBUG, "Control.getDataFromGoogle\t: Returning a list of " + urls.size() + " URLs");

        return result;
    }


    public static String[] getDataFromBing(String phrase, String keywords) {

        String[] result;
        final String accountKey = AZURE_KEY;
        final String bingUrlPattern = "https://api.datamarket.azure.com/Bing/Search/Web?Query=%%27%s%%27&$format=JSON&$top=" + URLS_RETURNED_PER_SEARCH;


        try {
            final String query = URLEncoder.encode("\"" + phrase + "\"" + keywords, Charset.defaultCharset().name());
            final String bingUrl = String.format(bingUrlPattern, query);

            final String accountKeyEnc = Base64.getEncoder().encodeToString((accountKey + ":" + accountKey).getBytes());

            final URL url = new URL(bingUrl);
            final URLConnection connection = url.openConnection();
            connection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);

            try (final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                final StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                final JSONObject json = new JSONObject(response.toString());
                final JSONObject d = json.getJSONObject("d");
                final JSONArray results = d.getJSONArray("results");
                final int resultsLength = results.length();
                result = new String[resultsLength];
                for (int i = 0; i < resultsLength; i++) {
                    final JSONObject aResult = results.getJSONObject(i);
                    ruffian.log(Snitch.level.DEBUG, "Control.getDataFromBing\t: URL added: " + aResult.get("Url"));
                    result[i] = aResult.get("Url").toString();
                    //System.out.println(aResult.get("Url"));
                }
                return result;
            }
        } catch (Exception e) {
            ruffian.log(Snitch.level.ERROR, "Control.getDataFromBing\t: Unable to query Bing url");
        }

        return new String[0];
    }


    /**
     * Bing Search API is now useless due to Azure DataMarket/Data Services reaching end-of-life.
     * After migrating to Microsoft Cognitive Services, Bing Web Search API is now used instead.
     * This method does exactly the same thing with its predecessor, using MS Web Search API.
     *
     * System Requirements:
     *              - BING_SEARCH_API_KEY : User needs to declare Bing Web Search API to BING_SEARCH_API_KEY variable.
     *
     * @param   phrase    (String) Phrase to search word-by-word for exact matches.
     * @param   keywords    (String) One or more keywords to be included in the request.
     * @return  String[]     Array of each word parsed from input PDF file.
     */
    public static String[] getDataFromBingEx(String phrase, String keywords) {

        String[] result;
        URI uri = null;
        final String accountKey = BING_SEARCH_API_KEY;


        try {
            URIBuilder builder = new URIBuilder("https://api.cognitive.microsoft.com/bing/v5.0/search");
            builder.setParameter("q", "\""+phrase+"\" " + keywords);
            ruffian.log(Snitch.level.DEBUG, "Control.getDataFromBingEx\t: Phrase = \""+phrase+"\"");
            ruffian.log(Snitch.level.DEBUG, "Control.getDataFromBingEx\t: Keywords = \""+keywords+"\"");
            builder.setParameter("count", ""+URLS_RETURNED_PER_SEARCH+"");
            uri = builder.build();
        } catch (URISyntaxException e) {
            ruffian.log(Snitch.level.ERROR, "Control.getDataFromBingEx\t: Invalid syntax of URI " + e.toString());
        }

        try {
            final URL url = uri.toURL();
            final URLConnection connection = url.openConnection();
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", accountKey);
            connection.setRequestProperty("method", "get");

            try (final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                final StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                final JSONObject json = new JSONObject(response.toString());
                //System.out.println(response.toString());
                final JSONObject webPages = json.getJSONObject("webPages");
                final JSONArray value = webPages.getJSONArray("value");

                result = new String[value.length()];

                for(int i=0;i<value.length();i++){
                    result[i] = value.getJSONObject(i).getString("url");
                }

                ruffian.log(Snitch.level.DEBUG, "Control.getDataFromBingEx\t: Returning " + value.length() + " URL(s).");

                return result;
            }
        } catch (Exception e) {
            ruffian.log(Snitch.level.ERROR, "Control.getDataFromBingEx\t: Unable to read JSON data from Bing. " + e.toString());
        }

        return new String[0];
    }



    public static String[] getDataFromYahoo(String phrase, String keywords) {

        ArrayList<String> urls = new ArrayList<>();
        String[] result;
        String url = "";
        int length = 0;
        phrase = phrase.replaceAll(" ","+");
        keywords = keywords.replaceAll(" ","+");

        try {
            // The following URL is from a simple search that actually works.
            //url = "https://search.yahoo.com/search?p=βάση+δεδομένων&fr2=sb-top";

            // The following URL uses the advanced search that I want!
            //url = "https://search.yahoo.com/search?n=10&ei=UTF-8&va_vt=any&vo_vt=any&ve_vt=any&vp_vt=any&vst=0&vf=all&vm=i&fl=0&fr=sfp&p=πολυπλοκότητα+\"\"+διακριτού+πεδίου+ορισμού+χωρισμένου\"\"&vs=";
            url = "https://uk.search.yahoo.com/search?n=" + URLS_RETURNED_PER_SEARCH +
                    "&ei=UTF-8" +
                    "&va_vt=any" +
                    "&vo_vt=any" +
                    "&ve_vt=any" +
                    "&vp_vt=any" +
                    "&vst=0" +
                    "&vf=all" +
                    "&vm=i" +
                    "&fl=0" +
                    "&fr=sfp" +
                    "&p=" + keywords + "+\"\"" + phrase + "\"\"" +
                    "&vs=";
            Document doc2 = Jsoup.connect(url).timeout(5000).get();
            Elements elements = doc2.getElementsByClass("title").select("a[href]");
            for(Element e : elements){
                urls.add(e.absUrl("abs:href"));
            }
        } catch(SSLHandshakeException e){
            ruffian.log(Snitch.level.ERROR, "Control.getDataFromYahoo\t: Caught SSLHandshakeException while parsing: " + url);
        } catch (IOException e) {
            ruffian.log(Snitch.level.ERROR, "Control.getDataFromYahoo\t: I/O Exception caught while querying Yahoo.");
        }

        if(urls.size() > URLS_RETURNED_PER_SEARCH){
            length = URLS_RETURNED_PER_SEARCH;
        }
        else {
            length = urls.size();
        }

        result = new String[length];
        for( int i=0 ; i<length ; i++ ){
            result[i] = urls.get(i);
        }

        ruffian.log(Snitch.level.DEBUG, "Control.getDataFromYahoo\t: Returning a list of " + length + " URL(s)");

        return result;
    }



    public static int[] searchTheRepo(String phrase) {

        try {

            int[] docs = repo.findAllHits(phrase);

            if(docs.length == 0){
                ruffian.log(Snitch.level.DEBUG, "Control.searchTheRepo\t: No results found for '" + phrase + "'");
            }
            else {
                ruffian.log(Snitch.level.INFORMATION, "Control.searchTheRepo\t: New Hit(s)! Fetching " + docs.length + " document(s)");
            }

            return docs;

        } catch (SQLException e) {
            ruffian.log(Snitch.level.ERROR, "Control.searchTheRepo\t: SQLExceptionon findAllHits(" +
                    phrase +
                    ") " +
                    e.toString()
            );
        }
        return null;
    }


    public static boolean updateRepo(int pageid, String phrase) {

        int updateCode = -1;

        ruffian.log(Snitch.level.DEBUG, "Control.updateRepo\t: Updating repository with new hit (" +
                pageid +
                " , '" +
                phrase +
                "')..."
        );
        try {
            updateCode = repo.updateHits(pageid, phrase);
        } catch (SQLException e) {
            ruffian.log(Snitch.level.ERROR, "Control.updateRepo\t: SQLException thrown on updateHits("+
                    pageid +
                    ", " +
                    phrase +
                    ") " +
                    e.toString()
            );
        }

        if(updateCode != Repo.ROW_INSERTED){
            ruffian.log(Snitch.level.ERROR, "Control.updateRepo\t: Unknown error updating repository with new hit (" + pageid + " , '" + phrase + "')...");
            return false;
        }
        else {
            ruffian.log(Snitch.level.INFORMATION, "Control.updateRepo\t: Repository updated with new hit (" +
                    pageid +
                    " , '" +
                    phrase +
                    "')..."
            );
            return true;
        }

    }

    public static boolean updateRepo(int[] pages, String phrase) {

        int updateCode = -1;

        ruffian.log(Snitch.level.DEBUG, "Control.updateRepo\t: Updating " + pages.length + " repository entries for phrase '" + phrase + "')...");
        try {
            updateCode = repo.updateHits(pages, phrase);
        } catch (SQLException e) {
            ruffian.log(Snitch.level.ERROR, "Control.updateRepo\t: SQLException thrown on updateHits("+
                    pages +
                    ", " +
                    phrase +
                    ") " +
                    e.toString()
            );
        }

        if(updateCode != Repo.ROW_INSERTED){
            ruffian.log(Snitch.level.ERROR, "Control.updateRepo\t: Unknown error updating " +
                    pages.length +
                    " repository entries for phrase '" +
                    phrase +
                    "')..."
            );
            return false;
        }
        else {
            ruffian.log(Snitch.level.INFORMATION, "Control.updateRepo\t: Repository updated");
            return true;
        }

    }





    public static boolean addToRepo(String[] urls, String phrase) {

        File tempfile;
        File errorFile = new File(PATH_TO_TEMP_DIR + "\\tempfile.err");
        String fileType;
        String[] words;
        String fullText = "";
        URL url;
        Document document;
        Elements p;
        Elements li;


        tempfile = new File(PATH_TO_TEMP_DIR + "\\tempfile.pdf");

        int upos = 0; // position in urls array
        int inum = 0; // number of pages added to repo
        //for(int i=0;i<DOCUMENTS_ADDED_PER_TIME;i++){
        while( inum < DOCUMENTS_ADDED_PER_TIME ){

            if( upos >= urls.length )
                break;

            if(tempfile.exists()){
                tempfile.delete();
            }

            // dealing with pdf files with a different approach
            if( urls[upos].matches("(\\S+.pdf\\n)|(\\S+.pdf$)") ) {

                fileType = getFileType(urls[upos]);
                tempfile = new File(PATH_TO_TEMP_DIR + "\\tempfile." + fileType);
                try {
                    tempfile.createNewFile();
                    url = new URL(urls[upos]);
                    FileUtils.copyURLToFile(url, tempfile);
                } catch (IOException e) {
                    ruffian.log(Snitch.level.ERROR, "Control.addToRepo\t: Failed to create the tempfile for URL: " + urls[upos]);
                }

                ruffian.log(Snitch.level.INFORMATION, "Control.addToRepo\t: Created tempfile");

                try {

                    fullText = "";
                    words = parseFile(tempfile.getAbsolutePath());
                    for (String word : words) {
                        fullText += " ";
                        fullText += word;
                    }
                    ruffian.log(Snitch.level.DEBUG, "Control.addToRepo\t: Adding to repository for phrase: '" + phrase + "' fulltext length: " + fullText.length());
                    repo.addSourceToRepo(urls[upos], fullText, phrase);

                }
                catch (Exception e) {
                    ruffian.log(Snitch.level.ERROR, "Control.addToRepo\t: Unable to parse temporary file. Creating error file...");
                    if (errorFile.exists()) {
                        errorFile.delete();
                    }
                    tempfile.renameTo(errorFile);
                    upos++;
                    continue;
                }
                inum++;
                upos++;
                ruffian.log(Snitch.level.INFORMATION, "Control.addToRepo\t: New document inserted to repository");
            }
            else{
                try {
                    fullText = "";
                    document = Jsoup.connect(urls[upos]).timeout(5000).get();
                    p = document.select("p");
                    li = document.select("li");

                    for(Element element : p)
                        fullText += element.text() + " ";

                    for(Element element : li){
                        if(element.getAllElements().size() > 4)
                            fullText += element.text() + " ";
                    }
                    ruffian.log(Snitch.level.INFORMATION, "Control.addToRepo\t: Completed parsing html");
                    ruffian.log(Snitch.level.DEBUG, "Control.addToRepo\t: Adding to repository for phrase: '" + phrase + "' fulltext length: " + fullText.length());
                    repo.addSourceToRepo(urls[upos], fullText, phrase);
                    inum++;
                    upos++;
                    ruffian.log(Snitch.level.INFORMATION, "Control.addToRepo\t: New document inserted in repository");

                } catch (Exception e) {
                    ruffian.log(Snitch.level.ERROR, "Control.addToRepo\t: Unable to parse " + urls[upos] + " - Re-trying for next page...");
                    upos++;
                }
            }
        }

        if( inum > 0 )
            return true;
        else return false;
    }





    public static String[] searchTheNet(String phrase, String[] keywords) {

        String engineName = "";
        String[] docs = null;

        // do not use Bing search if Bing Web Search API key is not specified
        if(BING_SEARCH_API_KEY == null || BING_SEARCH_API_KEY.length() < 20){
            engine = genEngine.nextInt(3);
        }
        else{
            engine = genEngine.nextInt(4);
        }
        // a fair amount of waiting time between searches for 0-0.7 seconds
        timeout = genTimeout.nextInt(701);
        // I guess a fair amount of waiting time between searches would be 0-0.7 seconds.

        String keywordsAssembled = " ";
        for(String keyword : keywords)
            keywordsAssembled += keyword + " ";


        /*
         * SEARCH_GOOGLE= 0;
         * SEARCH_YAHOO= 1;
         * SEARCH_YAHOO_AGAIN= 2;
         * SEARCH_BING = 3;
         */

        if(engine == SEARCH_GOOGLE) {
            engineName="Google";
            ruffian.log(Snitch.level.DEBUG, "Control.searchTheNet\t: Querying " + engineName + " for '" + phrase + "'");
            docs = getDataFromGoogle(phrase, keywordsAssembled);
        }
        else if(engine == SEARCH_YAHOO || engine == SEARCH_YAHOO_AGAIN) {
            engineName="Yahoo";
            ruffian.log(Snitch.level.DEBUG, "Control.searchTheNet\t: Querying " + engineName + " for '" + phrase + "'");
            docs = getDataFromYahoo(phrase, keywordsAssembled);
        }
        else if(engine == SEARCH_BING) {
            engineName="Bing";
            ruffian.log(Snitch.level.DEBUG, "Control.searchTheNet\t: Querying " + engineName + " for '" + phrase + "'");
            docs = getDataFromBingEx(phrase, keywordsAssembled);
        }
        else{
            ruffian.log(Snitch.level.ERROR, "Control.searchTheNet\t: Unable to specify search engine, value returned: " + engine);
            return new String[0];
        }

        return docs;
    }


    public static String[] sortURLs(String[] urls) {

        String[] temp = new String[urls.length];
        int ti= 0; // stands for: temp index

        for(int t=0;t<temp.length;t++)
            temp[t] = "";

        for(String url : urls){
            if(!url.matches("\\S+.pdf$"))
                temp[ti++] = url;
        }

        for(String url : urls){
            if(url.matches("(\\S+.pdf$)|(\\S+.pdf\\n)"))
                temp[ti++] = url;
        }

        return temp;
    }

    public static void printResults() {

        ArrayList<ResultRecord> resultRecords = null;
        String msg = "\n" +
                " \t---------------\n" +
                " \t Crawl results \n" +
                " \t---------------\n" +
                " \n" +
                "";

        ruffian.log(Snitch.level.DEBUG, "Control.printResults\t: -->[]");

        try {
            resultRecords = repo.getResultRecords(NUMBER_OF_RESULTS);
        } catch (SQLException e) {
            ruffian.log(Snitch.level.ERROR, "Control.printResults	: SQLException thrown while trying to get result records " +
                e.toString()
            );
        }

        if(resultRecords == null){
            System.err.println(" ERROR: unable to get operation results. Exiting...");
            cleanUp(10);
        }

        msg += " Below you can find the top " + resultRecords.size() + " results from my search:\n\n";

        //msg += "DOC#\tHITS\tURL\n";
        msg += " DOC#\t\t|\tHITS\n";
        for(ResultRecord res : resultRecords){
            msg += "  ";
            msg += res.getDocID();
            msg += "\t\t|\t ";
            msg += res.getHitCount();
            msg += "\n";
        }

        msg += "\n To get more information about document URLs, phrases and hits, you can search my MySQL database.\n";
        msg += "\n E.g. mysql -u repoman -p\n";
        msg += "\n Note: password for user repoman is repoman.\n";

        System.out.println(msg);

        ruffian.log(Snitch.level.DEBUG, "Control.printResults\t: []-->");

    }

    public static void printBanner() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (IOException e) {
            System.err.println(" ERROR: Could not execute a simple 'CLS' command. Continuing...");
        } catch (InterruptedException e) {
            System.err.println(" ERROR: Execution of command 'CLS' was interrupted. Wow...");
        }
        String msg = "\n" +
                "\t---------------\n" +
                "\t W e l c o m e \n" +
                "\t---------------\n" +
                "\n" +
                " You are about to unleash yet another web crawler.\n"+
                " Please specify the file for processing.\n"+
                " Enter your input by specifying the file's absolute path.\n"+
                " To include keywords in your search, use crawl.properties file.\n"+
                " Keywords must be separated with space.\n"+
                " (Waiting for your input)\n";
        System.out.println(msg);
        System.out.print(" > ");
    }


    public static void printHelp() {

        String msg = "\n" +
                " Command syntax:\n"+
                " \t\t\"C:\\Temp\\mhxanikh-orash.pdf\"\n"+
                " \n" +
                "  Do not add more than 3 keywords on crawl.properties file.\n"+
                "  (Waiting for your input)\n";
        System.out.println(msg);
        System.out.print(" > ");

    }

    public static void requestInput() {

        String input;
        String[] argv = new String[0];
        InputStreamReader isr;
        BufferedReader br;

        try {
            isr = new InputStreamReader(System.in, "UTF-8");
            br = new BufferedReader(isr);

            //Scanner scanner = new Scanner(System.in);

            boolean inputIsValid = false;

            while (!inputIsValid){input = br.readLine();
                argv = input.split("\"");
                inputIsValid = validateInput(argv);
                if(!inputIsValid){
                    System.out.println(" Input is not correct. Please try again or type 'help'\n");
                    System.out.print(" > ");
                }
            }
        } catch (UnsupportedEncodingException e) {
            System.err.println(" UnsupportedEncodingException caught!");
            System.out.println(" Input is not correct. Please try again or type 'help'\n");
            System.out.print(" > ");

        } catch (IOException e) {
            System.err.println(" IOException caught!");
            System.out.println(" Input is not correct. Please try again or type 'help'\n");
            System.out.print(" > ");
        }


    }



    public static boolean validateInput(String[] input){

        String fileName;
        File file;

        if(input.length < 1){
            return false;
        }
        else if(input[0].equalsIgnoreCase("exit") ||
                input[0].equalsIgnoreCase("quit") ||
                input[0].equalsIgnoreCase("q") ||
                input[0].equalsIgnoreCase("bye")){
            cleanUp(0);
        } else if(input.length < 2){
            if(input[0].equalsIgnoreCase("help")){
                printHelp();
                requestInput();
            }
            return false;
        }
        else{
            fileName = input[1];
            file = new File(fileName);
            if(!file.exists() || file.isDirectory()){
                return false;
            }
            else{
                ruffian.log(Snitch.level.DEBUG, "Control.validateInput\t: File exists ("+ fileName +")");
                INPUT_FILE = fileName;
                return true;
            }
        }
        return false;
    }
}







