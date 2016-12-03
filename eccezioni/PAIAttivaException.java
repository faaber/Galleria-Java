package eccezioni;

public class PAIAttivaException extends Exception {
    public PAIAttivaException() {
        super("La procedura antincendio è attiva.");
    }
    
    public PAIAttivaException(String pMessaggio) {
        super(pMessaggio);
    }
}