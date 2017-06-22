package basics;

import java.io.IOException;

/**
 * Created by BAGIOS on 05-07-16.
 */
public class Base {


    public static String[] words = null;
    public static String[] phrases = null;



    public static void mainL(String[] args){
        Control.initialize();

        String phrase = "αναπαριστούν διπλώματα πρωτεϊνών";
        String[] result = Control.getDataFromYahoo(phrase, "");

        for(String url : result){
            System.out.println(url);
        }

        System.out.println(result.length);

    }


    // Testing database
    public static void mainK(String[] args){

        Control.initialize();

        String[] urls = {
                "http://repfiles.kallipos.gr/html_books/4410/Ch10.html",
                "http://documents.tips/documents/-55cf992b550346d0339bf6a7.html"};

        String phrase = "Once upon a time on the WEB...";

        Control.addToRepo(urls, phrase);

        int[] x = Control.searchTheRepo("εκθετικής πολυπλοκότητας");

        System.out.println("Value returned: " + x.length);

    }





    public static void mainJ(String[] argv) {

        Control.initialize();

        String phrase = "θα μπορούσαμε να φανταστούμε";
        String[] keywords;

        keywords = new String[]{
                "τεχνητή",
                "νοημοσύνη"
        };

        //String[] result = Control.searchTheNet(phrase, keywords);
        String[] result = Control.searchTheNet(phrase, keywords);
        for( String url : result ){
            System.out.println(url);
        }

    }



    public static void mainIJ(String[] argv) {

        Control.initialize();

        String phrase = "θα μπορούσαμε να φανταστούμε";
        String keywords = "τεχνητή νοημοσύνη";

        //String[] result = Control.searchTheNet(phrase, keywords);
        String[] result = Control.getDataFromBingEx(phrase, keywords);
        for( String url : result ){
            System.out.println(url);
        }

    }





    public static void mainI(String[] args){

        String[] urls;

        Control.initialize();

        String[] phrases = {" Κοινωνία και Νέες Τεχνολογίες"};//, " Αλγόριθμοι και ΠολυΠλοκότητα", " Αλγόριθμοι και Πολυπλοκότητα", " Είναι εκθετικής πολυπλοκότητας"};

        for(String phrase : phrases){
            urls = Control.searchTheNet(phrase, new String[]{" "});
        }

    }



    public static void mainH(String[] args){

        Control.initialize();

        String[] urlsLVL00 = {
                "http://repfiles.kallipos.gr/html_books/4410/Ch10.html"
        };

        String[] urlsLVL01 = {
                "https://forum.ubuntu-gr.org/viewtopic.php?f=61&t=20487&start=10",
                "http://repfiles.kallipos.gr/html_books/4410/Ch10.html"
        };

        String[] urlsLVL02 = {
                "https://forum.ubuntu-gr.org/viewtopic.php?f=61&t=20487&start=10",
                "http://repfiles.kallipos.gr/html_books/4410/Ch10.html",
                "https://repository.kallipos.gr/bitstream/11419/4015/1/chapter10Final.pdf",
                "http://nestor.teipel.gr/xmlui/bitstream/handle/123456789/13400/STE_MHP_00155_Medium.pdf?sequence=1"
        };

        String[] urlsLVL20 = {
                "https://forum.ubuntu-gr.org/viewtopic.php?f=61&t=20487&start=10",
                "https://repository.kallipos.gr/bitstream/11419/4015/1/chapter10Final.pdf",
                "http://repfiles.kallipos.gr/html_books/4410/Ch10.html",
                "http://emarkou.users.uth.gr/greek/id/folding.pdf",
                "http://nestor.teipel.gr/xmlui/bitstream/handle/123456789/13400/STE_MHP_00155_Medium.pdf?sequence=1",
                "http://cgi.di.uoa.gr/~charnik/files/vtrees_summary.pdf",
                "http://dione.lib.unipi.gr/xmlui/bitstream/handle/unipi/4260/Voutsinas.pdf?sequence=2",
                "http://docplayer.gr/2347859-Symvoli-sti-theoria-programmatismoy-ergasion-kai-efarmoges-tis-stin-paragogi-kai-sto-perivallon.html",
                "http://docplayer.gr/11874998-Metaglottistes-ii-nkavv-uop-gr-katamerismos-katahoriton-nikolaos-kavvadias-nkavv-uop-gr-metaglottistes-ii.html",
                "http://ikee.lib.auth.gr/record/123887/files/thesis.pdf?version=1",
                "http://ikee.lib.auth.gr/record/124083/files/Development%20of%20a%20randomized%20algorithm%20for%20feature%20analysis%20and%20move%20evaluation%20in%20chess%20-%20Afroditi%20Xafi%20-%201310.pdf",
                "http://ikee.lib.auth.gr/record/109196/files/gri-2008-1877.pdf",
                "http://cnf.e-steki.gr/showthread.php?t=2846&page=50",
                "http://eureka.lib.teithe.gr:8080/bitstream/handle/10184/11000/Skodras_Thomas.pdf?sequence=1",
                "http://artemis-new.cslab.ece.ntua.gr:8080/jspui/bitstream/123456789/5946/1/PD2008-0024.pdf",
                "https://dspace.lib.uom.gr/bitstream/2159/13404/2/KourtelisMsc2008.pdf",
                "http://eureka.lib.teithe.gr:8080/bitstream/handle/10184/11000/Skodras_Thomas.pdf?sequence=1"
        };

        urlsLVL20 = Control.sortURLs(urlsLVL20);

        //Control.addToRepo(urlsLVL20, "είναι εκθετικής πολυπλοκότητας");



    }



    public static void mainG(String[] args){

        Control.initialize();

        try {
            words = Control.parseFile("C:\\\\Users\\\\"+Control.CURRENT_USER+"\\\\Downloads\\\\Διπλωματική Κυριάκος (final).pdf");
        } catch (Exception e) {
            e.printStackTrace();
        }

        phrases = Control.configureText(words);


        int count=0;
        for(String phrase : phrases) {
            System.out.println("Query #" + count++);
            //TO-DO: add sleep() with a random value to generate queries "randomly"
            Control.searchTheNet(phrase, new String[]{" "});
        }

    }



    public static void mainF(String[] args){

        Control.initialize();

        words = Control.parsePDF("C:\\Temp\\mypool\\tempfile.pdf");

        for(String word : words)
            System.out.println(word);

        System.out.println("...");

    }


    public static void mainE(String[] args){

        Control.initialize();

        String[] urls;
        String url1 = "https://dspace.lib.uom.gr/bitstream/2159/13404/2/KourtelisMsc2008.pdf";
        String url2 = "http://emarkou.users.uth.gr/greek/id/folding.pdf";
        String phrase = "είναι εκθετικής πολυπλοκότητας";

        urls = new String[] {url1, url2};
        Control.addToRepo(urls, phrase);

    }


    public static void mainD(String[] arg){

        Control.initialize();

        try {
            words = Control.parseFile("C:\\Users\\"+Control.CURRENT_USER+"\\Downloads\\Κοινωνία και Νέες Τεχνολογίες-it20949.pdf");
        } catch (Exception e) {
            e.printStackTrace();
        }

        phrases = Control.configureText(words);
        System.out.println("---------------SIZE-OF-PHRASES:-" + phrases.length + "---------------");

    }



    public static void mainC(String[] argv){

        Control.initialize();


        try {
            words = Control.parseFile("C:\\Users\\Darth Bg\\Downloads\\vtrees_summary.pdf.pdf");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if( words != null ) {
            for (String word : words) {
                System.out.println(word);
            }
        }


    }



    public static void mainB(String[] argv){

        Control.initialize();

        Process p;
        try {
            p = Runtime.getRuntime().exec("\"d:\\MySQL\\MySQL Server 5.7\\bin\\mysqldump\" repo < %TEMP%\\mypool\\repo.sql -u repoman --password=repoman");
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Control.printResults();

    }


    public static void main(String[] argv) {

        //ruffian = new Snitch("C:\\temp\\ruffian.log");
        Control.initialize();
        Control.printBanner();


        /**
         * STEP 1
         * Parse file and configure its text to create a list of phrases to search in step 2
         * Actions executed here:
         * - parsing of a given file and creation of a list of words
         * - further configuration to build a list of phrases to begin our search attempts
         **/
        Control.requestInput();
        //if(true) return ;

        try {
            words = Control.parseFile(Control.INPUT_FILE);

        } catch (Exception e) {
            Control.ruffian.log(Snitch.level.ERROR, "Base.main\t: " + e.toString());
        }

        phrases = Control.configureText(words);

        System.out.println(" \n");
        System.out.println(" Number of searches: " + phrases.length);
        System.out.println(" Execution may take a long time to complete.");
        System.out.println(" \n");
        System.out.println(" Crawling in the web....");
        System.out.println(" \n");


        /**
         * STEP 2
         * search using CMA
         */

        int[] pages = new int[0];
        boolean updated = false;
        String[] urls = null;
        int progressBarElements = 25;
        int step = 0;
        int totalSteps = phrases.length + 1;
        int checkPoint = totalSteps/progressBarElements;
        int toPrint = 0;
        int ratio = 0;

        System.out.append("\n");
        System.out.append(" |");
        for(int i=0; i<progressBarElements; i++){
            System.out.append(" ");
        }
        System.out.append("| 0%");


        // TO-DO: implement steps, measure time elapsed and update user with progress
        for (String phrase : phrases) {

            step++;
            if(step % checkPoint == 0){
                System.out.append("\r |");
                toPrint = step/checkPoint;
                for(int i=0 ; i<=toPrint ; i++){
                    System.out.append("~");
                }
                for(int j=toPrint; j<progressBarElements; j++){
                    System.out.append(" ");
                }
                System.out.append("| ");
                ratio =new Integer(step*100/phrases.length).intValue();
                System.out.append(""+ratio);
                System.out.append("%");
            }



            pages = Control.searchTheRepo(phrase);
            /**
             * if pageid > -1 : we have a hit!
             * pageid will hold the page id from the page that has the phrase we searched for in the database repository
             * repository will be updated with the new hit
             */
            if (pages.length > 0) {
                updated = Control.updateRepo(pages, phrase);
                if (updated) {
                    Control.ruffian.log(Snitch.level.DEBUG, "Base.main\t: Updated " + pages.length + " repository entries for phrase '" + phrase + "'");
                } else {
                    Control.ruffian.log(Snitch.level.ERROR, "Base.main\t: Database error occured trying to update " + pages.length + " repository entries for phrase '" + phrase + "'");
                }
                pages = new int[0];
            }
            /**
             * if pageid == -1 : no hit result from the current repository
             * we will search the net and update our database repository with our findings for future references
             * links to PDF files will be moved to the end of urls array to avoid possible OCR usage which is really time consuming
             */
            else {
                //urls = Control.searchTheNet(phrase, keywords);
                urls = Control.searchTheNet(phrase, Control.KEYWORDS);
                if (urls.length > 0) {
                    urls = Control.sortURLs(urls);
                    updated = Control.addToRepo(urls, phrase);
                    if (!updated) {
                        for (String url : urls)
                            Control.ruffian.log(Snitch.level.ERROR, "Base.main\t: Database or parsing error occurred trying to add to the repository records for phrase: '" + phrase + "', URL=" + url);
                    }
                } else {
                    Control.ruffian.log(Snitch.level.INFORMATION, "Base.main\t: No online search results for: '" + phrase + "'");
                }
            }
        }

        System.out.println(" \n");
        System.out.println(" \n");
        System.out.println(" Web scraped. Back to my cave now.");

        Control.printResults();
        Control.cleanUp(0);

    }

}
