package eccezioni;

public class PermessoInsufficienteException extends Exception {
    public PermessoInsufficienteException() {
        super("L'attuale livello di permesso è insufficiente per tale operazione");
    }
    
    public PermessoInsufficienteException(String pMessaggio) {
        super(pMessaggio);
    }
}