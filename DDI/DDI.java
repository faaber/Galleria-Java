package DDI;

import APP.controlloAccesso.Permesso;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe che implementa e gestisce la comunicazione con il DB.
 * Implementa il design pattern Singleton.
 */
public class DDI {

    private static Properties ProprietàDB;
    private static List<Connection> RilasciaConnessioni;

    private static DDI instance = null;

    /**
     * Metodo che implementa il design pattern Singleton.
     * @return L'istanza della classe o null se non è stato possibile istaurare
     * una connessione con il DB.
     */
    public static DDI getInstance() {
        if (instance == null) {
            try {
                instance = new DDI();
            } catch (SQLException ex) {
                Logger.getLogger(DDI.class.getName()).log(Level.SEVERE, "Errore: Fallimento nell'istaurazione della connessione con il db", ex);
                instance = null;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DDI.class.getName()).log(Level.SEVERE, "Errore: Driver per l'accesso al db non trovato", ex);
                instance = null;
            }
        }
        return instance;
    }

    /**
     * Costruttore della classe. Può non fallire sotto determinate condizioni.
     * @throws ClassNotFoundException Quando vi sono stati errori con il caricamento del driver.
     * @throws SQLException Quando vi sono stati errori con l'istaurazione della connesione col DB.
     */
    private DDI() throws ClassNotFoundException, SQLException {
        RilasciaConnessioni = new ArrayList<Connection>();
        try {
            CaricaCredenziali();
        } catch (IOException ex) {
            Logger.getLogger(DDI.class.getName()).log(Level.SEVERE, null, ex);
        }
        try{
        CaricaDriver();
        RilasciaConnessione(getConnection()); // test di connessione
        }catch(ClassNotFoundException | SQLException e){
            Logger.getLogger(DDI.class.getName()).log(Level.SEVERE, "Le credenziali caricate non sono valide, carico credenziali di default\n", e);
            CaricaCredenzialiDefault();
            CaricaDriver();
            RilasciaConnessione(getConnection()); // test di connessione
        }
    }

    /**
     * Carica delle credenziali di default per l'accesso al DB.
     */
    private static void CaricaCredenzialiDefault() {
        ProprietàDB.setProperty("driver", "org.gjt.mm.mysql.Driver");
        ProprietàDB.setProperty("url", "jdbc:mysql://localhost/galleria");
        ProprietàDB.setProperty("username", "root");
        ProprietàDB.setProperty("password", "1234");
    }

    /**
     * Consente di ottenere una connessione al db per effettuare delle operazioni.
     * @return Un'istanza di connesione.
     * @throws SQLException In caso di errori nell'apertura della connessione.
     */
    private static synchronized Connection getConnection() throws SQLException {
        Connection connessione;

        if (!RilasciaConnessioni.isEmpty()) {

            connessione = RilasciaConnessioni.get(0);
            RilasciaConnessioni.remove(0);

            try {

                if (connessione.isClosed()) {
                    connessione = getConnection();
                }
            } catch (SQLException e) {
                connessione = getConnection();
            }
        } else {
            connessione = ApriConnessione();
        }

        return connessione;
    }

    /**
     * Consente di rilasciare una connessione dopo l'uso.
     * @param rilasciaconnessione La connessione d'interesse.
     */
    private static synchronized void RilasciaConnessione(Connection rilasciaconnessione) {

        RilasciaConnessioni.add(rilasciaconnessione);
    }

    /**
     * Apre una connessione con il DB
     * @return La connessione aperta.
     * @throws SQLException In caso di errori nella connessione.
     */
    private static Connection ApriConnessione() throws SQLException {
        Connection nuovaconnessione = null;

        nuovaconnessione = DriverManager.getConnection(ProprietàDB.getProperty("url"),
                ProprietàDB.getProperty("username"),
                ProprietàDB.getProperty("password"));

        nuovaconnessione.setAutoCommit(false);

        return nuovaconnessione;
    }

    /**
     * Prova a caricare il driver.
     * @throws ClassNotFoundException Quando ci sono stati errori nel caricare il driver.
     */
    private static void CaricaDriver() throws ClassNotFoundException {
        if(ProprietàDB.getProperty("driver")==null)
            throw new ClassNotFoundException();
        Class.forName(ProprietàDB.getProperty("driver"));
    }

    /**
     * Carica le credenziali per l'accesso al DB.
     * Prova prima a trovarle sul file, se non riesce chiama il metodo che carica quelle di default.
     * @throws IOException In caso di problemi con il caricamento.
     */
    private static void CaricaCredenziali() throws IOException {
        ProprietàDB = new Properties();
        try {
            InputStream fileProperties = DDI.class.getResourceAsStream("Credenziali_DB");
            if (fileProperties != null) {
                ProprietàDB.load(fileProperties);
            } else {
                Logger.getLogger(DDI.class.getName()).log(Level.WARNING, "Attenzione: file delle credenziali non presente, verranno usate le credenziali di default");
                CaricaCredenzialiDefault();
            }
        } catch (IOException e) {
            Logger.getLogger(DDI.class.getName()).log(Level.WARNING, "Attenzione: Non è stato possibile aprire il file delle credenziali, verranno usate le credenziali di default", e);
            CaricaCredenzialiDefault();
        }
    }

    /**
     * Effettua l'autenticazione dell'utente.
     *
     * @param username Username da verificare.
     * @param password Password immessa.
     * @return L'istanza di un <code>Permesso</code> oppure <code>null</code>.
     */
    public Permesso readUtente(String username, String password) {
        Permesso permesso = null;
        if (password == null) {
            password = "";
        }
        if (username == null) {
            username = "";
        }
        try {
            Connection con = getConnection();
            Statement stmt = con.createStatement();
            try {
                ResultSet rs = stmt.executeQuery("select Livello_accesso from utente where Username = '" + username + "' and Password = '" + password + "'");
                if (rs.next() == true) {
                    switch (rs.getString(1)) {
                        case "Operatore":
                            permesso = Permesso.OPERATORE;
                            break;
                        case "Controllore":
                            permesso = Permesso.CONTROLLORE;
                            break;
                    }
                    con.commit();
                }
                return permesso;
            } finally {

                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    RilasciaConnessione(con);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DDI.class.getName()).log(Level.SEVERE, "Si è verificato un problema con l'accesso al db", ex);
        }
        return null;
    }

    /**
     * Registra nel database l'attivazione della PAI.
     * @return <code>true/false</code> in base al fatto che la registrazione nel db sia andata
     * a buon fine o meno
     */
    public boolean writeAttivazionePAI() {
            /*
             * ATTENZIONE
             * Allo stato attuale è possibile generare record di attivazione
             * anche in caso ve ne sia uno non ancora chiuso (per esempio
             * se avete chiuso il programma dopo aver attivato la PAI)
             */
        try {
            Connection con = getConnection();
            Statement stmt = con.createStatement();
            try {
                stmt.executeUpdate("insert into attivazione_pai (username_responsabile) value (NULL)");
                con.commit();
            } finally {

                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    RilasciaConnessione(con);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DDI.class.getName()).log(Level.SEVERE, "Si è verificato un problema con l'accesso al db", ex);
            return false;
        }      
        return true;
    }
    
    /**
     * Registra nel database la disattivazione della PAI da parte di un utente.
     * @param username L'utente che ha effettuato la disattavazione.
     * @return <code>true/false</code> in base al fatto che la registrazione nel db sia andata
     * a buon fine o meno.
     */
    public boolean writeDisattivazionePAI(String username){
           /*
            * ATTENZIONE
            * Allo stato attuale non è presente alcuna verifica sull'utente che
            * ha chiamato tale procedura, è pertanto possibile dichiarare che un
            * username non registrato al db abbia chiuso un record d'attivazione.
            * Inoltre, a causa delle mancanze della procedura di apertura di un
            * nuovo record, la chiusura viene effettuata sull'ultimo record
            * aperto ma non chiuso in ordine temporale (tra i vari che
            * potrebbero essere presenti).
            */
       try {
            if (username == null)
                return false;
            Connection con = getConnection();
            Statement stmt = con.createStatement();
            try {
                stmt.executeQuery("SELECT id FROM galleria.attivazione_pai where data_disattivazione LIKE '0000-00-00 00:00:00' ORDER BY id DESC LIMIT 1;");
                ResultSet rs=stmt.getResultSet();
                if(rs.next()==false)
                    return false;
                String id=rs.getString(1);
                stmt.executeUpdate("UPDATE `galleria`.`attivazione_pai` SET `username_responsabile`='"+username+"' WHERE `id`="+id+";");
                con.commit();
            } finally {

                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    RilasciaConnessione(con);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DDI.class.getName()).log(Level.SEVERE, "Si è verificato un problema con l'accesso al db", ex);
            return false;
        }
       return true;
    }
}