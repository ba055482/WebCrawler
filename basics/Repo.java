package basics;

/**
 * Created by Darth Bg on 08/09/2016.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


/**
 * This class is used to initialize the repository and handle every basic database activity.
 *
 *
 *      DATABASE: REPO
 *
 *      mysql> CREATE DATABASE repo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 *      Query OK, 1 row affected (0.04 sec)
 *
 *
 *
 *      mysql> use repo
 *      Database changed
 *      mysql> create table documents(
 *          -> doc_id int(11) not null auto_increment,
 *          -> doc_url varchar(500) not null ,
 *
 *          -> doc_text text not null,
 *          -> constraint pk_doc_id primary key (doc_id)
 *          -> );
 *      Query OK, 0 rows affected (0.53 sec)
 *
 *
 *
 *      mysql>  create table hits(
 *          ->  hit_id int(11) not null auto_increment,
 *          ->  hit_doc_id int(11) not null,
 *          ->  hit_text varchar (200) not null,
 *
 *          ->  constraint pk_hit_id primary key (hit_id),
 *          ->  constraint fk_hit_doc_id foreign key (hit_doc_id) references documents(doc_id)
 *          ->  );
 *      Query OK, 0 rows affected (0.47 sec)
 *
 *
 *
 *
 *      mysql> show databases;
 *      +--------------------+
 *      | Database           |
 *      +--------------------+
 *      | information_schema |
 *      | mysql              |
 *      | performance_schema |
 *      | repo               |
 *      | sys                |
 *      +--------------------+
 *      5 rows in set (0.07 sec)
 *
 *
 *
 *      mysql> show tables;
 *      +----------------+
 *      | Tables_in_repo |
 *      +----------------+
 *      | documents      |
 *      | hits           |
 *      +----------------+
 *      2 rows in set (0.00 sec)
 *
 *
 *
 *      mysql> describe DOCUMENTS;
 *      +----------+--------------+------+-----+---------+----------------+
 *      | Field    | Type         | Null | Key | Default | Extra          |
 *      +----------+--------------+------+-----+---------+----------------+
 *      | doc_id   | int(11)      | NO   | PRI | NULL    | auto_increment |
 *      | doc_url  | varchar(500) | NO   |     | NULL    |                |
 *      | doc_text | text         | NO   |     | NULL    |                |
 *      +----------+--------------+------+-----+---------+----------------+
 *      3 rows in set (0.04 sec)
 *
 *
 *
 *      mysql> describe HITS;
 *      +------------+--------------+------+-----+---------+----------------+
 *      | Field      | Type         | Null | Key | Default | Extra          |
 *      +------------+--------------+------+-----+---------+----------------+
 *      | hit_id     | int(11)      | NO   | PRI | NULL    | auto_increment |
 *      | hit_doc_id | int(11)      | NO   | MUL | NULL    |                |
 *      | hit_text   | varchar(200) | NO   |     | NULL    |                |
 *      +------------+--------------+------+-----+---------+----------------+
 *      3 rows in set (0.01 sec)
 *
 *
 * HINT: Windows CMD and UTF-8 charset
 *      To display greek text in Windows CMD:
 *      - change CMD font to Lucida Console
 *      - issue the command "chcp 65001" (which will change the code page to UTF-8. Also, you need to use Lucida console fonts)
 *
 */
public class Repo {

    public static final int ROW_INSERTED = 1;
    public static final int ROW_NOT_INSERTED = -1;

    public static final int UNSPECIFIED_ERROR = -10;


    private Connection connect = null;
    private Statement statement = null;
    private String sql = "";
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;
    private String repoDB = "repo";
    private String repoUser = "repoman";
    private String repoPassword = "repoman";



    public String getRepoDB() {
        return repoDB;
    }

    public void setRepoDB(String repoDB) {
        this.repoDB = repoDB;
    }



    public Repo(String rootUser, String rootPassword) throws SQLException, ClassNotFoundException {
        initialize(rootUser, rootPassword);
    }




    public void initialize(String user, String password) throws SQLException, ClassNotFoundException {

        createConnection(user, password, "information_schema");

        sql = "DROP DATABASE IF EXISTS " + repoDB ;
        statement.executeUpdate(sql);

        flushPrivileges();

        preparedStatement = connect.prepareStatement("DROP USER IF EXISTS ?");
        preparedStatement.setString(1, repoUser);
        preparedStatement.executeUpdate();


        //preparedStatement = connect.prepareStatement("CREATE DATABASE ? DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci");
        sql = "CREATE DATABASE " + repoDB + " DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
        statement.executeUpdate(sql);

        flushPrivileges();
        //revokePrivileges();
        //flushPrivileges();

        preparedStatement = connect.prepareStatement("CREATE USER ? IDENTIFIED BY ?");
        preparedStatement.setString(1, repoUser);
        preparedStatement.setString(2, repoPassword);
        preparedStatement.executeUpdate();

        try {
            preparedStatement = connect.prepareStatement("GRANT ALL ON " + repoDB + ".* TO ? IDENTIFIED BY ?");
            //preparedStatement.setString(1, repoDB);
            preparedStatement.setString(1, repoUser);
            preparedStatement.setString(2, repoPassword);
            preparedStatement.executeUpdate();
        } finally {
            close();
        }

        createConnection(repoUser, repoPassword, repoDB);

        sql =   "CREATE TABLE documents(" +
                "doc_id INT(11) NOT NULL AUTO_INCREMENT," +
                "doc_url VARCHAR(500) NOT NULL ," +
                "doc_text LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL," +
                "CONSTRAINT pk_doc_id PRIMARY KEY (doc_id)" +
                ")";
        statement.executeUpdate(sql);

        sql =   "CREATE TABLE hits(" +
                "hit_id INT(11) NOT NULL AUTO_INCREMENT," +
                "hit_doc_id INT(11) NOT NULL," +
                "hit_text VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL," +
                "CONSTRAINT pk_hit_id PRIMARY KEY (hit_id)," +
                "CONSTRAINT fk_hit_doc_id FOREIGN KEY (hit_doc_id) REFERENCES documents(doc_id)" +
                ")";
        statement.executeUpdate(sql);

        sql = "SET NAMES 'utf8mb4'" ;
        statement.executeUpdate(sql);

    }



    public void createConnection(String user, String password, String database) throws ClassNotFoundException, SQLException {

        String db_url = "jdbc:mysql://localhost/" + database + "?useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=utf-8";


        // This will load the Repo driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        connect = DriverManager.getConnection(db_url, user, password);
        //connect = DriverManager.getConnection("jdbc:mysql://localhost/?user=groot&password=iamgroot");

        // Statements allow to issue SQL queries to the database
        statement = connect.createStatement();
    }


    /**
     * This method is used to add a new document to repository.
     * The following steps are performed:
     * 1. The new document is inserted in the DOCUMENTS table
     * 2. The DocumentID (doc_id) of the new document, which is automatically generated from the database, is acquired for reference.
     * 3. HITS table is updated with the new finding (doc_id, hitText)
     *
     * @param docURL    The URL of the to-be-inserted document.
     * @param docText   The full text of the to-be-inserted document.
     * @param hitText   The search phrase that was found in the to-be-inserted document.
     */
    public void addSourceToRepo(String docURL, String docText, String hitText) throws SQLException {

        int docID = 0;

        preparedStatement = connect.prepareStatement("INSERT INTO documents (doc_url, doc_text) VALUES(?, ?)");
        preparedStatement.setString(1, docURL);
        preparedStatement.setString(2, docText);
        preparedStatement.executeUpdate();

        preparedStatement = connect.prepareStatement("SELECT doc_id FROM documents WHERE doc_url=?");
        preparedStatement.setString(1, docURL);
        resultSet = preparedStatement.executeQuery();
        while(resultSet.next()){
            docID = resultSet.getInt(1);
        }

        preparedStatement = connect.prepareStatement("INSERT INTO hits (hit_doc_id, hit_text) VALUES(?, ?)");
        preparedStatement.setInt(1, docID);
        preparedStatement.setString(2, hitText);
        preparedStatement.executeUpdate();

        commit();
    }


    /**
     * From copy&paste probably, cannot recall...
     *
     * @param resultSet
     * @throws SQLException
     */
    private void writeMetaData(ResultSet resultSet) throws SQLException {
        //   Now get some metadata from the database
        // Result set get the result of the SQL query

        System.out.println("The columns in the table are: ");

        System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
        for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
            System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
        }
    }


    /**
     * This method is used to find THE FIRST document from our repository which contains the search phrase.
     *
     * @param   phrase  The phrase which is being searched in our repository.
     * @return  int     The ID of the document containing the search phrase.
     *                  -1 in case the search phrase is not found on any document of the repository.
     */
    public int findHit(String phrase) throws SQLException {

        int hitID = -1;

        preparedStatement = connect.prepareStatement("SELECT doc_id FROM documents WHERE doc_text LIKE ? ORDER BY doc_id");
        preparedStatement.setString(1, "%" + phrase + "%");
        resultSet = preparedStatement.executeQuery();
        if(resultSet.next()) {
            hitID = resultSet.getInt(1);
        }

        commit();

        return hitID;
    }



    /**
     * This method will be used to find EVERY document from our repository which contains the search phrase.
     * Steps:
     * 1. Get the number of documents containing the search phrase.
     * 2. Create an array and add to it the DocumentID of each document that contains the search phrase.
     *
     * @param   phrase  The phrase which is being searched in our repository.
     * @return  int[]   An array of integers containing the IDs of every document containing the search phrase.
     */
    public int[] findAllHits(String phrase) throws SQLException {

        int hits[] = new int[0];
        int hitsCount = -1;
        int hitID = -1;

        preparedStatement = connect.prepareStatement("SELECT doc_id FROM documents WHERE doc_text LIKE ?");
        preparedStatement.setString(1, "%" + phrase + "%");
        resultSet = preparedStatement.executeQuery();
        resultSet.last();
        hitsCount = resultSet.getRow();
        hits = new int[hitsCount];
        int i=0;
        resultSet.beforeFirst();
        if( hitsCount > 0) {
            while (resultSet.next()) {
                hits[i++] = resultSet.getInt(1);
            }
        }

        commit();

        return hits;
    }

    // MUST CHECK CODE! --REALLY INCOMPLETE
    /**
     * This method is used to update HITS table with one or more hits of a search phrase on a document.
     *
     * @param docID
     * @param hitText
     * @return
     */
    public int updateHits(int docID, String hitText) throws SQLException {


        preparedStatement = connect.prepareStatement("INSERT INTO hits (hit_doc_id, hit_text) VALUES(?, ?)");
        preparedStatement.setInt(1, docID);
        preparedStatement.setString(2, hitText);
        preparedStatement.executeUpdate();

        commit();
        return ROW_INSERTED;

        //return UNSPECIFIED_ERROR;
    }


    public int updateHits(int[] docs, String hitText) throws SQLException {

        int newHits = 0;

        for( int i=0; i<docs.length; i++) {
            preparedStatement = connect.prepareStatement("INSERT INTO hits (hit_doc_id, hit_text) VALUES(?, ?)");
            preparedStatement.setInt(1, docs[i]);
            preparedStatement.setString(2, hitText);
            preparedStatement.executeUpdate();
            commit();
            newHits++;

        }
        if(newHits > 0)
            return ROW_INSERTED;
        else
            return UNSPECIFIED_ERROR;

    }



    private void flushPrivileges() throws SQLException {
        sql = "FLUSH PRIVILEGES";
        statement.executeUpdate(sql);
    }

    private void revokePrivileges() throws SQLException {
        sql = "REVOKE ALL PRIVILEGES, GRANT OPTION FROM " + repoUser;
        statement.executeUpdate(sql);
    }


    private void commit(){
        try {
            sql = "commit;";
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // You need to close the resultSet
    private void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ResultRecord> getResultRecords(int numberOfResults) throws SQLException {

        int size = 0;
        ArrayList<ResultRecord> resultRecords;

        preparedStatement = connect.prepareStatement("" +
                "SELECT d.doc_id DOCUMENTID, count(h.hit_doc_id) HITS, d.doc_url URL " +
                "FROM documents d, hits h " +
                "WHERE d.doc_id = h.hit_doc_id " +
                "GROUP BY h.hit_doc_id " +
                "ORDER BY count(h.hit_doc_id) " +
                "DESC" +
                "");
        resultSet = preparedStatement.executeQuery();
        resultSet.last();
        size = resultSet.getRow();
        resultSet.first();
        if(size>Control.NUMBER_OF_RESULTS){
            size = numberOfResults;
        }
        resultRecords = new ArrayList<>();
        while(resultSet.next() && resultSet.getRow() < size) {
            resultRecords.add(
                    new ResultRecord(
                            resultSet.getInt(1),
                            resultSet.getInt(2),
                            resultSet.getString(3)
                    )
            );
        }

        return resultRecords;
    }
}

/**
 *
 *
 * DATABASE BACKUP:
 * mysqldump repo > C:\temp\repo.noplag.sql -u repoman -p
 *
 * DATABASE RESTORE:
 * mysql repo < c:\temp\repo.nogplag.sql -u repoman -p
 *
 *
 *
 * EXAMPLE FOR GATHERING RESULTS:
 *
 * mysql> SELECT d.doc_id DOCUMENTID, count(h.hit_doc_id) HITS FROM documents d, hits h WHERE d.doc_id = h.hit_doc_id GROUP BY h.hit_doc_id ORDER BY  count(h.hit_doc_id) ASC;
 *
 * +------------+------+
 * | DOCUMENTID | HITS |
 * +------------+------+
 * |         45 |   12 |
 * |         41 |    4 |
 * |         40 |    3 |
 * |          8 |    2 |
 * |         75 |    2 |
 * |          3 |    1 |
 * |         67 |    1 |
 * |         16 |    1 |
 * |         80 |    1 |
 * |         29 |    1 |
 * |         42 |    1 |
 * |         55 |    1 |
 * |          4 |    1 |
 * |         68 |    1 |
 * |         17 |    1 |
 * |         81 |    1 |
 * |         30 |    1 |
 * |         43 |    1 |
 * |         56 |    1 |
 * |          5 |    1 |
 * |         69 |    1 |
 * |         18 |    1 |
 * |         31 |    1 |
 * |         44 |    1 |
 * |         57 |    1 |
 * |          6 |    1 |
 * |         70 |    1 |
 * |         19 |    1 |
 * |         32 |    1 |
 * |         58 |    1 |
 * |          7 |    1 |
 * |         71 |    1 |
 * |         20 |    1 |
 * |         33 |    1 |
 * |         46 |    1 |
 * |         59 |    1 |
 * |         72 |    1 |
 * |         21 |    1 |
 * |         34 |    1 |
 * |         47 |    1 |
 * |         60 |    1 |
 * |          9 |    1 |
 * |         73 |    1 |
 * |         22 |    1 |
 * |         35 |    1 |
 * |         48 |    1 |
 * |         61 |    1 |
 * |         10 |    1 |
 * |         74 |    1 |
 * |         23 |    1 |
 * |         36 |    1 |
 * |         49 |    1 |
 * |         62 |    1 |
 * |         11 |    1 |
 * |         24 |    1 |
 * |         37 |    1 |
 * |         50 |    1 |
 * |         63 |    1 |
 * |         12 |    1 |
 * |         76 |    1 |
 * |         25 |    1 |
 * |         38 |    1 |
 * |         51 |    1 |
 * |         64 |    1 |
 * |         13 |    1 |
 * |         77 |    1 |
 * |         26 |    1 |
 * |         39 |    1 |
 * |         52 |    1 |
 * |          1 |    1 |
 * |         65 |    1 |
 * |         14 |    1 |
 * |         78 |    1 |
 * |         27 |    1 |
 * |         53 |    1 |
 * |          2 |    1 |
 * |         66 |    1 |
 * |         15 |    1 |
 * |         79 |    1 |
 * |         28 |    1 |
 * |         54 |    1 |
 * +------------+------+
 * 81 rows in set (0.00 sec)
 */

