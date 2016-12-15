package eccezioni;

/**
 * Modella l'impossibilità ad eseguire una data funzionalità del sistema su richiesta dell'utente.
 * Il motivo può essere generico o specifico al permesso dell'utente
 * e allo stato della PAI.
 */
public class FunzioneNonDisponibileException extends Exception {
    public boolean permessoInsufficiente;
    public boolean paiInCorso;
        
    private final static String ERRORE_STANDARD="\nNon è stato possibile eseguire la funzione";
    private final static String ERRORE_PERMESSO="-Attuale livello di permesso insufficiente";
    private final static String ERRORE_PAI="-PAI in corso";
    
    /**
     * Genera una nuova eccezione.
     * @param pPermessoInsufficiente Se il permesso dell'utente era insufficiente per accedere alla funzionalità.
     * @param pPaiInCorso Se la PAI era in corso e pertanto non è stato possibile accedere alla funzionalità.
     */
    public FunzioneNonDisponibileException(boolean pPermessoInsufficiente, boolean pPaiInCorso) {
        super();
        permessoInsufficiente=pPermessoInsufficiente;
        paiInCorso=pPaiInCorso;
    }

    @Override
    public String getMessage() {
        String message=ERRORE_STANDARD;
        if(permessoInsufficiente || paiInCorso)
            message+=":";
        if(permessoInsufficiente)
            message+="\n"+ERRORE_PERMESSO;
        if(paiInCorso)
            message+="\n"+ERRORE_PAI;
        return message;
    }
}