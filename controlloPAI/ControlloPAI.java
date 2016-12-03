package controlloPAI;

import controlloIlluminazione.ControlloIlluminazione;
import controlloTraffico.Circolazione;
import controlloTraffico.ControlloTraffico;
import java.util.Date;

/**
 * Classe Control che gestisce le attività legate al rilevamento
 * della temperatura. Utilizziamo il design pattern Singleton
 */
public final class ControlloPAI {
    private static ControlloPAI instance = null;
    
    /**
     * Indica se la temperatura rilevata e' alta
     */
    private boolean temperaturaAlta = false;
    /**
     * Specifica se dev'essere attivo il lock sulla temperatura, ossia la
     * temperatura viene rilevata come alta solo se non ci sono input bassi
     * per un fissato numero di millisecondi
     */
    private boolean temperaturaLock = false;
    /**
     * Indica se la temperatura e' stata rilevata come alta per un fissato
     * numero di millisecondi. Corrisponde alla rilevazione dell'incendio
     */
    private boolean temperaturaAttivato = false;
    private int sogliaTemperatura = 24;
    
    /**
     * Specifica se la data memorizzata nella variabile <code>data</code>
     * e' valida
     */
    private boolean isDataValida = false;
    /**
     * Istante di ricezione del piu' recente input di temperatura alta
     */
    private Date data;
    /**
     * Ritardo in millisecondi prima dell'impostazione della temperatura alta
 quando Arduino.temperaturaLock è <code>true</code>
     */
    private long ritardoAttivazione = 500;
    /**
     * Variabile utilizzata per effettuare il controllo di routine dell'assenza
     * di un input di temperatura bassa
     */
    private Date dataCheck;
    
    private boolean PAIAttiva = false;
    
    private ControlloPAI() {
    }
    
    /**
     * Metodo che implementa il design pattern Singleton
     */
    public static ControlloPAI getInstance() {
        if (instance == null) {
            instance = new ControlloPAI();
        }
        return instance;
    }
    
    public void ricevutoInputTemperaturaAlta() {
        if (!isTemperaturaLock()) {
            setTemperaturaAlta();
        }
        if (!isDataValida) {
            data = new Date();
            isDataValida = true;
        }
    }
    
    public void ricevutoInputTemperaturaBassa() {
        if (!isTemperaturaLock()) {
            setTemperaturaBassa();
        }
        if (isDataValida) {
            isDataValida = false;
        }
    }
    
    public void setTemperaturaAlta() {
        setTemperaturaAlta(true);
    }
    
    public void setTemperaturaBassa() {
        setTemperaturaAlta(false);
    }
    
    public void eseguiOperazioniTemperatura() {
        if (isTemperaturaLock() && isDataValida &&
                !isTemperaturaAttivato()) {
            dataCheck = new Date();
            
            if (dataCheck.getTime() > data.getTime() + ritardoAttivazione) {
                setTemperaturaAlta();
                attivaProceduraAntincendio();
            }
        }
        else if (!isTemperaturaLock() && !isDataValida &&
                !isTemperaturaAttivato()) {
            setTemperaturaBassa();
        }
    }
    
        
    /**
     * Attiva la procedura antincendio. Imposta il criterio d'illuminazione
     * a costante, la circolazione a interdetto e registra l'attivazione
     * della PAI nel database
     */
    public void attivaPAI() {
        PAIAttiva = true;
        
        ControlloIlluminazione.getInstance().setCriterioDinamicoAttivo(false);
        ControlloTraffico.getInstance().setCircolazione(Circolazione.INTERDETTA);
        // Operazione da aggiungere: registra l'attivazione della PAI nel database        
    }
    
    /**
     * Disattiva la procedura antincendio. Ripristina il funzionamento ordinario
     * della galleria, registrando la disattivazione della PAI nel database
     */
    public void disattivaPAI() {
        PAIAttiva = false;
        
        // Opereazione da aggiungere: registra la disattivazione della PAI nel database
    }

    public boolean isPAIAttiva() {
        return PAIAttiva;
    }
    
    private void attivaProceduraAntincendio() {
        setTemperaturaAttivato(true);
    }
    
    
    public boolean isTemperaturaLock() {
        return temperaturaLock;
    }

    public void setTemperaturaLock(boolean temperaturaLock) {
        this.temperaturaLock = temperaturaLock;
    }

    public boolean isTemperaturaAttivato() {
        return temperaturaAttivato;
    }

    public void setTemperaturaAttivato(boolean temperaturaAttivato) {
        this.temperaturaAttivato = temperaturaAttivato;
    }

    public int getSogliaTemperatura() {
        return sogliaTemperatura;
    }

    public void setSogliaTemperatura(int sogliaTemperatura) {
        this.sogliaTemperatura = sogliaTemperatura;
    }
    
    public boolean isTemperaturaAlta() {
        return temperaturaAlta;
    }

    public void setTemperaturaAlta(boolean temperaturaAlta) {
        this.temperaturaAlta = temperaturaAlta;
    }
}