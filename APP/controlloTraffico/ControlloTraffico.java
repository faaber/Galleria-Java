package APP.controlloTraffico;

import APP.arduino.LogicTask;
import java.util.Date;

/**
 * Classe Control che gestisce lo stato di circolazione, comunicato all'utente
 * tramite i semafori. Utilizziamo il design pattern Singleton
 */
public final class ControlloTraffico {
    public static final int DURATA_MAX = 90, DURATA_MIN = 1;  
    
    private Circolazione circolazione = Circolazione.SENSO_UNICO_SX;
    private boolean smf1Verde = true;
    private boolean smf2Verde = true;
    private int durataVerdeDXRossoSX = 1;
    private int durataVerdeSXRossoDX = 1;
    private int durataRossoAggiuntiva = 1;
    
    private static ControlloTraffico instance = null;
    
    private int durataStatoAlternatoAttuale;
    private long latenzaOperazione = 50;
    
    private Date dataInvio;
    private Date dataInvioCheck;
    private Date dataInvioAux;
    private Date dataStatoAlternato;
    private Date dataStatoAlternatoCheck;
    private Date dataStatoAlternatoAux;
    private StatoAlternato statoAlternato;
    
    private ControlloTraffico() {
    }
    
    private enum StatoAlternato {
        VERDE_SX,
        INTERDIZIONE_VERDE_SX,
        VERDE_DX,
        INTERDIZIONE_VERDE_DX
    }
    
    /**
     * Metodo che implementa il design pattern Singleton
     * @return L'istanza della classe.
     */
    public static ControlloTraffico getInstance() {
        if (instance == null) {
            instance = new ControlloTraffico();
        }
        return instance;
    }
    
    /**
     * Esegue operazioni ad intervalli di tempo la cui lunghezza in millisecondi
     * e' indicata nella variabile <code>latenzaOperazione</code>. Verifica il
     * valore della variabile <code>circolazione</code> nella classe Arduino.
     * In base al suo valore, imposta il criterio di circolazione tra <code>Custom</code>,
     * <code>DoppioSenso</code>, <code>SensoUnicoAlter</code>, <code>SensoUnicoSx</code>,
     * <code>SensoUnicoDx</code>, <code>Interdetto</code>
     */
    public void eseguiOperazioniTraffico() {
        if (dataInvio == null) {
            dataInvio = new Date();
            dataInvioAux = new Date();
            dataInvio.setTime(dataInvioAux.getTime() - 10000);
        }
        
        dataInvioCheck = new Date();
        
        if (dataInvioCheck.getTime() > dataInvio.getTime() + latenzaOperazione) {
            dataInvio = new Date();
            
        
            if (null != circolazione) switch (circolazione) {
                case CUSTOM:
                    impostaCriterioCustom();
                    break;
                case DOPPIO_SENSO:
                    impostaCriterioDoppioSenso();
                    break;
                case SENSO_UNICO_ALTER:
                    impostaCriterioSensoUnicoAlter();
                    break;
                case SENSO_UNICO_SX:
                    impostaCriterioSensoUnicoSx();
                    break;
                case SENSO_UNICO_DX:
                    impostaCriterioSensoUnicoDx();
                    break;
                case INTERDETTA:
                    impostaCriterioInterdetto();
                    break;
                default:
                    break;
            }
        }
    }
    
    private void impostaCriterioCustom() {
        inviaValoriAiSemafori();
    }
    
    private void inviaValoriAiSemafori() {
        // Smf1
        if (isSmf1Verde()) {
            LogicTask.getInstance().writeOnSerialPort(1, 1);
        }
        else {
            LogicTask.getInstance().writeOnSerialPort(1, 0);
        }

        // Smf2
        if (isSmf2Verde()) {
            LogicTask.getInstance().writeOnSerialPort(1, 3);
        }
        else {
            LogicTask.getInstance().writeOnSerialPort(1, 2);
            }
    }
    
    private void impostaCriterioDoppioSenso() {
        setSmf1Verde(true);
        setSmf2Verde(true);
        
        inviaValoriAiSemafori();
    }
    
    private void impostaCriterioSensoUnicoAlter() {
        if (dataStatoAlternato == null) {
            dataStatoAlternato = new Date();
            dataStatoAlternatoAux = new Date();
            dataStatoAlternato.setTime(dataStatoAlternatoAux.getTime() - 10000);
            
            statoAlternato = StatoAlternato.VERDE_SX;
            durataStatoAlternatoAttuale = getDurataVerdeSXRossoDX();
            
            setSmf1Verde(true);
            setSmf2Verde(false);
        }
        
        dataStatoAlternatoCheck = new Date();
        
        if (dataStatoAlternatoCheck.getTime() > dataStatoAlternato.getTime() + durataStatoAlternatoAttuale * 1000) {
            dataStatoAlternato = new Date();
            
            if (statoAlternato == StatoAlternato.VERDE_SX) {
                statoAlternato = StatoAlternato.INTERDIZIONE_VERDE_SX;
                durataStatoAlternatoAttuale = getDurataRossoAggiuntiva();
                setSmf1Verde(false);
                setSmf2Verde(false);
            }
            else if(statoAlternato == StatoAlternato.INTERDIZIONE_VERDE_SX) {
                statoAlternato = StatoAlternato.VERDE_DX;
                durataStatoAlternatoAttuale = getDurataVerdeDXRossoSX();
                setSmf1Verde(false);
                setSmf2Verde(true);
            }
            else if(statoAlternato == StatoAlternato.VERDE_DX) {
                statoAlternato = StatoAlternato.INTERDIZIONE_VERDE_DX;
                durataStatoAlternatoAttuale = getDurataRossoAggiuntiva();
                setSmf1Verde(false);
                setSmf2Verde(false);
            }
            else if(statoAlternato == StatoAlternato.INTERDIZIONE_VERDE_DX) {
                statoAlternato = StatoAlternato.VERDE_SX;
                durataStatoAlternatoAttuale = getDurataVerdeSXRossoDX();
                setSmf1Verde(true);
                setSmf2Verde(false);
            }
            
            inviaValoriAiSemafori();
        }
    }
    
    private void impostaCriterioSensoUnicoSx() {
        setSmf1Verde(true);
        setSmf2Verde(false);
        
        inviaValoriAiSemafori();
    }
    
    private void impostaCriterioSensoUnicoDx() {
        setSmf1Verde(false);
        setSmf2Verde(true);
        
        inviaValoriAiSemafori();
    }
    
    private void impostaCriterioInterdetto() {
        setSmf1Verde(false);
        setSmf2Verde(false);
        
        inviaValoriAiSemafori();
    }
    
    public Circolazione getCircolazione() {
        return circolazione;
    }

    public void setCircolazione(Circolazione circolazione) {
        this.circolazione = circolazione;
    }
    
        public int getDurataVerdeDXRossoSX() {
        return durataVerdeDXRossoSX;
    }

    public void setDurataVerdeDXRossoSX(int durataVerdeDXRossoSX) {
        int valore;
        
        if (durataVerdeDXRossoSX < DURATA_MIN) {
            valore = DURATA_MIN;
        }
        else if (durataVerdeDXRossoSX > DURATA_MAX) {
            valore = DURATA_MAX;
        }
        else {
            valore = durataVerdeDXRossoSX;
        }
        this.durataVerdeDXRossoSX = valore;
    }

    public int getDurataVerdeSXRossoDX() {
        return durataVerdeSXRossoDX;
    }

    public void setDurataVerdeSXRossoDX(int durataVerdeSXRossoDX) {
        int valore;
        
        if (durataVerdeSXRossoDX < DURATA_MIN) {
            valore = DURATA_MIN;
        }
        else if (durataVerdeSXRossoDX > DURATA_MAX) {
            valore = DURATA_MAX;
        }
        else {
            valore = durataVerdeSXRossoDX;
        }
        this.durataVerdeSXRossoDX = valore;
    }

    public int getDurataRossoAggiuntiva() {
        return durataRossoAggiuntiva;
    }

    public void setDurataRossoAggiuntiva(int durataRossoAggiuntiva) {
        int valore;
        
        if (durataRossoAggiuntiva < DURATA_MIN) {
            valore = DURATA_MIN;
        }
        else if (durataRossoAggiuntiva > DURATA_MAX) {
            valore = DURATA_MAX;
        }
        else {
            valore = durataRossoAggiuntiva;
        }
        this.durataRossoAggiuntiva = valore;
    }
    
    public boolean isSmf1Verde() {
        return smf1Verde;
    }

    public void setSmf1Verde(boolean smf1Verde) {
        this.smf1Verde = smf1Verde;
    }

    public boolean isSmf2Verde() {
        return smf2Verde;
    }

    public void setSmf2Verde(boolean smf2Verde) {
        this.smf2Verde = smf2Verde;
    }
}