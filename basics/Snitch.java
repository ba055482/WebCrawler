package basics;

import java.io.*;

/**
 * Created by Darth Bg on 25/06/2016.
 */
public class Snitch {

    private File logFile;
    //private BufferedWriter writer;
    private OutputStreamWriter writer;

    public String getFileName() {
        return logFile.getName();
    }

    public enum level  {
        DEBUG,
        INFORMATION,
        ERROR
    }



    public Snitch(String fileName){


        logFile = new File(fileName);

        if(logFile.exists())
            logFile.delete();


        try {
            logFile.createNewFile();
            openLogFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void setlogFile(File logFile){

        closeLogFile();
        this.logFile = logFile;
        openLogFile();

    }


    public void openLogFile(){

        try {
            //writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile.getAbsolutePath()),"UTF-8"));
            writer = new OutputStreamWriter(new FileOutputStream(logFile.getAbsolutePath()),"UTF-8");
        }
        catch (FileNotFoundException e) {
            System.err.println("ERROR: Unable to write to log file!");
        } catch (UnsupportedEncodingException e) {
            System.err.println("ERROR: Unsupported encoding of log file: UTF-8!");
        }

    }



    public void log(level logLevel, String record){
        if(logLevel.ordinal() >= Control.LOG_LEVEL) {
            String msg = "\n";
            //openLogFile();
            if (logLevel == level.DEBUG)
                msg += "    DEBUG    : " + record;
            else if (logLevel == level.INFORMATION)
                msg += " INFORMATION : " + record;
            else if (logLevel == level.ERROR)
                msg += "    ERROR    : " + record;

            try {
                writer.append(msg);
                writer.flush();
            } catch (IOException e) {
                System.err.println("ERROR: Unable to write to log file.");
            }
            //closeLogFile();
        }
    }


    public void closeLogFile(){
        try {
            writer.close();
        } catch (IOException e) {
            System.err.println("ERROR: Unable to close log writer.");
        }
    }


}
