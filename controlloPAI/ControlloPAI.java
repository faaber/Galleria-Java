package controlloPAI;

import controlloAccesso.ControlloAccesso;
import controlloIlluminazione.ControlloIlluminazione;
import controlloTraffico.Circolazione;
import controlloTraffico.ControlloTraffico;
import DDI.DDI;
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
     * Indica qual è l'ultimo stato della temperatura rilevato (valore booleano,
     * positivo se la temperatura e' alta). Se questo valore non viene cambiato
     * per un intervallo pari a <code>ritardoAttivazione</code>, la variabile
     * <code>temperaturaAlta</code> viene aggiornata.
     */
    private boolean temperaturaAltaCandidata = false;
    /**
     * Specifica se dev'essere attivo il lock sulla temperatura, in tal caso, la
     * temperatura viene rilevata come alta solo se non ci sono input bassi
     * per un fissato numero di millisecondi
     */
    private boolean temperaturaLock = false;
    /**
     * Indica se la procedura antincendio e' stata attivata.
     */
    private boolean proceduraAttivata = false;
    private int sogliaTemperatura = 24;
    /**
     * Istante di ricezione del piu' recente input di temperatura alta
     */
    private Date data;
    /**
     * Attributo ausiliario per la gestione delle date
     */
    private Date dataAux;
    /**
     * Ritardo in millisecondi prima dell'impostazione della temperatura alta
     * quando temperaturaLock è <code>true</code>
     */
    private long ritardoAttivazione = 20;
    /**
     * Variabile utilizzata per effettuare il controllo di routine dell'assenza
     * di un input di temperatura bassa
     */
    private Date dataCheck;
    
    private boolean PAIAttiva = false;
    
    private ControlloPAI() {
        dataAux = new Date();
        data = new Date();
        data.setTime(dataAux.getTime() - 10000);
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
        if (!temperaturaAltaCandidata) {
            temperaturaAltaCandidata = true;
            data = new Date();
        }
    }
    
    public void ricevutoInputTemperaturaBassa() {
        if (temperaturaAltaCandidata) {
            temperaturaAltaCandidata = false;
            data = new Date();
        }
    }
    
    public void eseguiOperazioniPAI() {
        dataCheck = new Date();

        if (dataCheck.getTime() > data.getTime() + ritardoAttivazione) {
            temperaturaAlta = temperaturaAltaCandidata;
        }
        
        if (temperaturaLock && temperaturaAlta && !PAIAttiva) {
            attivaPAI();
        }
    }
    
        
    /**
     * Attiva la procedura antincendio. Imposta il criterio d'illuminazione
     * a costante, la circolazione a interdetto e registra l'attivazione
     * della PAI nel database
     */
    public void attivaPAI() {
        if (!PAIAttiva) {
            PAIAttiva = true;

            ControlloIlluminazione.getInstance().setIntensitaCriterioCostante(ControlloIlluminazione.INTENSITA_MAX);
            ControlloIlluminazione.getInstance().setCriterioDinamicoAttivo(false);
            ControlloTraffico.getInstance().setCircolazione(Circolazione.INTERDETTA);
            
            DDI.getInstance().writeAttivazionePAI();
        }
    }
    
    /**
     * Disattiva la procedura antincendio. Ripristina il funzionamento ordinario
     * della galleria, registrando la disattivazione della PAI nel database
     */
    public void disattivaPAI() {
        if (PAIAttiva && !temperaturaAlta) {
            PAIAttiva = false;

            DDI.getInstance().writeDisattivazionePAI(ControlloAccesso.getInstance().getUtenteLoggato());
        }
    }

    public boolean isPAIAttiva() {
        return PAIAttiva;
    }
    
    public boolean isTemperaturaLock() {
        return temperaturaLock;
    }

    public void setTemperaturaLock(boolean temperaturaLock) {
        this.temperaturaLock = temperaturaLock;
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
}