package DDI;

import controlloAccesso.ControlloAccesso;
import controlloAccesso.Permesso;
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

public class DDI {

    private static Properties ProprietàDB;
    private static List<Connection> RilasciaConnessioni;

    private static DDI instance = null;

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
     * Costruttore
     */
    private DDI() throws ClassNotFoundException, SQLException {
        RilasciaConnessioni = new ArrayList<Connection>();
        try {
            CaricaCredenziali();
        } catch (IOException ex) {
            Logger.getLogger(DDI.class.getName()).log(Level.SEVERE, null, ex);
        }
        CaricaDriver();
        RilasciaConnessione(getConnection()); // test di connessione
    }

    private static void CaricaCredenzialiDefault() {
        ProprietàDB.setProperty("driver", "org.gjt.mm.mysql.Driver");
        ProprietàDB.setProperty("url", "jdbc:mysql://localhost/tunnel_db");
        ProprietàDB.setProperty("username", "root");
        ProprietàDB.setProperty("password", "admin");
    }

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

    private static synchronized void RilasciaConnessione(Connection rilasciaconnessione) {

        RilasciaConnessioni.add(rilasciaconnessione);
    }

    private static Connection ApriConnessione() throws SQLException {
        Connection nuovaconnessione = null;

        nuovaconnessione = DriverManager.getConnection(ProprietàDB.getProperty("url"),
                ProprietàDB.getProperty("username"),
                ProprietàDB.getProperty("password"));

        nuovaconnessione.setAutoCommit(false);

        return nuovaconnessione;
    }

    private static void CaricaDriver() throws ClassNotFoundException {
        Class.forName(ProprietàDB.getProperty("driver"));
    }

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
     * Effettua l'autenticazione dell'utente
     *
     * @param username Username da verificare
     * @param password Password immessa
     * @return Livello autorizzazione oppure null
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
                /*QUERY FUNZIONANTE MANCA CONTROLLO CONTROLLO VERIDICITà CON I DATI INSERITI TEXTFIELD*/
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
     * Scrive nel database l'informazione dell'attivazione o disattivazione
     * della procedura antincendio
     *
     * @param isAttiva <code>true</code> se la procedura viene attivata,
     * <code>false</code> se la procedura viene disattivata
     */
    public void writePAI(boolean isAttiva) {
        try {
            /* Operazioni: scrive nel database lo stato di attivazione della procedura
            * antincendio e memorizza ora e data (con gli appositi operatori SQL)
            * ATTENZIONE: MANCA VERIFICA DI CONSISTENZA SULL'UTENTE CHE EFFETTUA TALE OPERAZIONE
             */
            String username = ControlloAccesso.getInstance().getUtenteLoggato();
            if ((username != null && isAttiva == true) || (username == null && isAttiva == false)) {
                return;
            }
            Connection con = getConnection();
            Statement stmt = con.createStatement();
            con = getConnection();
            try {
                /*QUERY FUNZIONANTE SU WORCKBENCH NON COMPLETATA VISTO CHE DEVE ESSERE CAMBIATA CON I TEXTFIELD DI JAVAFX */
                if (username == null) {
                    username = "";
                }
                stmt.executeUpdate("insert into attivazione_pai (username_responsabile) value ('" + username + "')");

                /*PreparedStatement stm = null;
                stm = con.prepareStatement("insert into attivazione_pai (username_responsabile) value (\"?\")");
                stm.setString(1, Username);
                
                }*/
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
        }
    }
}
