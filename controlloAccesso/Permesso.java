package controlloAccesso;

/**
 * Quest'enumerazione codifica il livello di permesso degli utenti registrati.
 * <code>controllore</code> puo' solo visualizzare informazioni, non può effettuare
 * modifiche
 * <code>operatore</code> puo' visualizzare e modificare
 * <
 * @author Alessandro
 */
public enum Permesso {
    CONTROLLORE(),
    OPERATORE(
        Funzione.SET_DURATA_R_AGG,
        Funzione.SET_DURATA_V_SX,
        Funzione.SET_DURATA_V_DX,
        Funzione.SET_CIRCOLAZIONE,
        Funzione.DISATTIVA_PAI, 
        Funzione.SET_LIVELLO_CM,
        Funzione.SET_CRITERIO);
    
    public boolean supporta(Funzione funzione){
        for (Funzione funzioni1 : funzioni) {
            if (funzione == funzioni1) {
                return true;
            }
        }
        return false;
    }
    
    public Funzione[] getFunzioni(){
        return funzioni.clone();
    }
    
    private final Funzione[] funzioni;
    
    private Permesso(Funzione... pFunzioni){
        funzioni=pFunzioni;
    }
}
