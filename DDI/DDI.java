package DDI;

import controlloAccesso.Permesso;

public class DDI {
    
    /**
     * Costruttore
     */
    public DDI() {
        
    }
    
    /**
     * Effettua l'autenticazione dell'utente
     * @param username Username da verificare
     * @param password Password immessa
     * @return Livello autorizzazione oppure null
     */
    public Permesso readUtente(String username, String password) {
        // Operazioni: interfacciarsi con JDBC
        
        return null; // Da modificare
    }
    
    /**
     * Scrive nel database l'informazione dell'attivazione o disattivazione
     * della procedura antincendio
     * @param isAttiva <code>true</code> se la procedura viene attivata,
     *                 <code>false</code> se la procedura viene disattivata
     */    
    public void writePAI(boolean isAttiva) {
        /* Operazioni: scrive nel database lo stato di attivazione della procedura
         * antincendio e memorizza ora e data (con gli appositi operatori SQL)
         */ 
    }
}
