package APP.controlloAccesso;

import DDI.DDI;
import APP.controlloIlluminazione.ControlloIlluminazione;
import APP.controlloIlluminazione.Criterio;
import APP.controlloTraffico.ControlloTraffico;
import APP.controlloPAI.ControlloPAI;
import APP.controlloTraffico.Circolazione;
import APP.eccezioni.FunzioneNonDisponibileException;

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
     * @return L'istanza di un <code>Permesso</code> se l'autenticazione ha avuto successo,
     *         <code>null</code> altrimenti
     */
    public Permesso effettuaLogin(String username, String password) {
        
        permesso=DDI.getInstance().readUtente(username, password);
        if(permesso!=null)
            utenteLoggato=username;
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
    
    /**
     * Consente di richiedere le funzioni al sistema da parte di uno specifico utente.
     * Le informazioni sull'utente vengono reperite automaticamente.
     * @param pFunzione La funzione richiesta dall'utente.
     * @param parametro I parametri della funzione.
     * @throws FunzioneNonDisponibileException Quando la richiesta viene negata a causa di un motivo generico,
     * per mancanza di un permesso sufficiente o per lo stato della PAI.
     */
    public void richiediFunzione(Funzione pFunzione, Object parametro) throws FunzioneNonDisponibileException{
        boolean permessoInsufficiente=false, paiInCorso=false;
        if(permesso==null || !permesso.supporta(pFunzione))
            permessoInsufficiente=true;
        if(ControlloPAI.getInstance().isPAIAttiva()==true){
            if(permesso==Permesso.OPERATORE){
                if(pFunzione==Funzione.DISATTIVA_PAI){
                    ControlloPAI.getInstance().disattivaPAI();
                    return;
                }
                else
                    paiInCorso=true;
            }
            else
                paiInCorso=true;        
        }
        if(permessoInsufficiente || paiInCorso)
            throw new FunzioneNonDisponibileException(permessoInsufficiente, paiInCorso);
                
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
