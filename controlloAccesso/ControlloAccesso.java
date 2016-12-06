package controlloAccesso;

import DDI.DDI;
import controlloIlluminazione.ControlloIlluminazione;
import controlloIlluminazione.Criterio;
import controlloTraffico.ControlloTraffico;
import controlloPAI.ControlloPAI;
import controlloTraffico.Circolazione;

public class ControlloAccesso {
    
    private static ControlloAccesso instance = null;
    
    public static ControlloAccesso getInstance() {
        if (instance == null) {
            instance = new ControlloAccesso();
        }
        return instance;
    }
    
    /**
     * Ha valore NULL se non ci sono utenti loggati
     */
    private String utenteLoggato;
    /**
     * Contiene l'informazione del livello di permesso dell'utente loggato
     */
    private Permesso permesso = null;
    
    private ControlloAccesso() {
        utenteLoggato = null;
        permesso = null;
        // Esiste per evitare l'istanziazione
    }
    
    /**
     * Effettua un tentativo di login
     * @param username Utente da loggare
     * @param password Password inserita
     * @return <code>true</code> se l'autenticazione ha avuto successo,
     *         <code>false</code> altrimenti
     */
    public Permesso effettuaLogin(String username, String password) {
        /* Richiama il metodo 'readUtente' nella classe DDI nel package DDI.
         * Se il login ha successo memorizza l'utente loggato nella variabile
         * "utenteLoggato"
         */
        
        // Richiamare il metodo "readUtente" del sottosistema DDI
        
        permesso=DDI.getInstance().readUtente(username, password);
        return permesso;
//        // Inizio stub
//        utenteLoggato = username;
//        if(username.equals("Lorenzo"))
//            permesso=Permesso.OPERATORE;
//        else
//            permesso=Permesso.CONTROLLORE;
//        
//        return permesso; // Da modificare
//        // Fine stub
    }
    
    public void effettuaLogout() {
        permesso=null;
        utenteLoggato=null;
    }
    
    public Permesso getPermesso() {
        return ControlloAccesso.getInstance().permesso;
    }
    
    public String getUtenteLoggato(){
        return utenteLoggato;
    }
       
    public void richiediFunzione(Funzione pFunzione, Object parametro){
        if(permesso==null || !permesso.supporta(pFunzione))
            return;
        
        switch(pFunzione){
            case SET_LIVELLO_CM:
                ControlloIlluminazione.getInstance().setIntensitaCriterioCostante((int) parametro);
                break;
            case SET_CRITERIO:
                ControlloIlluminazione.getInstance().provideCriterio((Criterio) parametro);
                break;
            case DISATTIVA_PAI:
                ControlloPAI.getInstance().disattivaPAI();
                break;
            case SET_CIRCOLAZIONE:
                ControlloTraffico.getInstance().setCircolazione((Circolazione) parametro);
                break;
            case SET_DURATA_R_AGG:
                ControlloTraffico.getInstance().setDurataRossoAggiuntiva((int) parametro);
                break;
            case SET_DURATA_V_DX:
                ControlloTraffico.getInstance().setDurataVerdeDXRossoSX((int) parametro);
                break;
            case SET_DURATA_V_SX:
                ControlloTraffico.getInstance().setDurataVerdeSXRossoDX((int) parametro);
                break;
        }
    }
}
