package controlloIlluminazione;

import controlloTraffico.IR;
import arduino.LogicTask;
import java.util.Date;

/**
 * Classe Control che gestisce l'illuminazione e i valori d'intensita' dei LED. Utilizziamo
 * il design pattern Singleton
 */
public final class ControlloIlluminazione {
    public static final int INTENSITA_MAX=255, INTENSITA_MIN=0, NUM_LED=14;
    
    private static ControlloIlluminazione instance = null;
    
    private boolean vetturaInTransito = false;
    private boolean posizionePrecedenteValida = false;
    private boolean criterioDinamicoAttivo = false;
    private int intensitaCriterioCostante = 255;
    private int numeroLEDStriscia = NUM_LED;
    private int numeroLEDFadeIn = 8;
    private int numeroLEDFadeOut = 7;
    private int posizioneVetturaIntero;
    private int posizioneVetturaInteroCheck;
    private int frazioniDiLED = 5;
    private int numeroLEDVirtuali = numeroLEDStriscia + numeroLEDFadeIn + numeroLEDFadeOut;
    private int posizioneFronte;
    private int intervalloAggiornamento = 20;
    private long intervalloMillisecondi;
    private double raggioInterno = 15.0;
    private double raggioEsterno = 20.3;
    private double raggioPercorsoInUso = raggioEsterno;
    private double distanzaGalleriaInizioCurva = 10.3;
    private double intervalloDistanzaLED = 3.33;
    private double puntoZero = intervalloDistanzaLED * 0.5 - intervalloDistanzaLED * numeroLEDFadeIn;
    private double posizioneIR1 = - distanzaGalleriaInizioCurva - 2 * Math.PI * raggioPercorsoInUso * (IR.getInstance().getPosizioneIR1() / 360);
    private double posizioneIR2 = - distanzaGalleriaInizioCurva - 2 * Math.PI * raggioPercorsoInUso * (IR.getInstance().getPosizioneIR2() / 360);
    private double distanzaIR1IR2 = posizioneIR2 - posizioneIR1;
    private double posizioneVettura;
    private double velocitaVettura;
    
    private int[] intensitaLED = new int[numeroLEDStriscia];
    // private int[] formaOnda = new int[] {0, 255, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 255, 0};
    private int[] formaOnda = new int[] {0, 128, 255, 255, 255, 255, 255, 255, 255, 128, 0, 0, 0, 0, 0};
    private Date IR1Data;
    private Date IR2Data;
    private Date dataCheck;
    private Date data1;
    
    private ControlloIlluminazione() {
    }
    
    /**
     * Metodo che implementa il desing pattern Singleton
     */
    public static ControlloIlluminazione getInstance() {
        if (instance == null) {
            instance = new ControlloIlluminazione();
        }
        return instance;
    }
    
    public boolean isVetturaInTransito() {
        return vetturaInTransito;
    }

    public void setVetturaInTransito(boolean vetturaInTransito) {
        this.vetturaInTransito = vetturaInTransito;
    }

    public Date getIR1Data() {
        return IR1Data;
    }

    public void setIR1Data(Date IR1Data) {
        this.IR1Data = IR1Data;
    }

    public Date getIR2Data() {
        return IR2Data;
    }

    public void setIR2Data(Date IR2Data) {
        this.IR2Data = IR2Data;
    }
    
    /**
     * Esegue periodicamente le operazioni relative all'illuminazione. Imposta
     * il criterio costante oppure calcola l'intensita' dei LED
     */
    public void eseguiOperazioniIlluminazione() {
        if (!criterioDinamicoAttivo) {
            if (data1 == null) {
                dataCheck = new Date();
                data1 = new Date();
                data1.setTime(dataCheck.getTime() - 1000);
            }
            
            dataCheck = new Date();
            
            if (dataCheck.getTime() > data1.getTime() + intervalloAggiornamento) {
                impostaCriterioCostante();
                
                data1 = new Date();
            }
        }
        else if (vetturaInTransito) {
            dataCheck = new Date();
            
            intervalloMillisecondi = dataCheck.getTime() - IR2Data.getTime();
            posizioneVettura = posizioneIR2 - puntoZero + velocitaVettura * (intervalloMillisecondi / (double) 1000);
            
            posizioneVetturaInteroCheck = (int) (posizioneVettura / (intervalloDistanzaLED / (double) frazioniDiLED));
            
            if (!posizionePrecedenteValida) {
                if (posizioneVetturaInteroCheck >= 0) {
                    posizioneVetturaIntero = posizioneVetturaInteroCheck;
                    posizionePrecedenteValida = true;
                    
                    calcolaIntensitaLED();

                    // Su Arduino imposto vetturaInTransito = true
                    LogicTask.getInstance().writeOnSerialPort(2, 0);
                    // Invio ad Arduino la posizione della macchina
                    LogicTask.getInstance().writeOnSerialPort(3, posizioneVetturaIntero);
                }
            }
            else if (posizioneVetturaIntero > (numeroLEDFadeIn + numeroLEDStriscia + numeroLEDFadeOut - 2) * frazioniDiLED + 1) {
                posizionePrecedenteValida = false;
                vetturaInTransito = false;
                
                // Su Arduino imposto vetturaInTransito = false
                LogicTask.getInstance().writeOnSerialPort(2, 1);
                
                IR.getInstance().setIR1Lock(false);
                IR.getInstance().setIR2Lock(false);
                IR.getInstance().setIR1Occluso(false);
                IR.getInstance().setIR2Occluso(false);
            }
            else {
                if (posizioneVetturaIntero != posizioneVetturaInteroCheck) {
                    posizioneVetturaIntero = posizioneVetturaInteroCheck;
                    
                    calcolaIntensitaLED();

                    // Invio ad Arduino la posizione della macchina
                    LogicTask.getInstance().writeOnSerialPort(3, posizioneVetturaIntero);
                }
            }
        }
    }
    
    /**
     * Calcola la velocita' della vettura sulla base della distanza tra i sensori IR
     * e gli istanti di rilevazione dei medesimi sensori
     */
    public void calcolaVelocitaVettura() {
        velocitaVettura = distanzaIR1IR2 / ( (double) (IR2Data.getTime() - IR1Data.getTime()) / (double) 1000);
    }

    public int getPosizioneVetturaIntero() {
        return posizioneVetturaIntero;
    }

    public void setPosizioneVetturaIntero(int posizioneVetturaIntero) {
        this.posizioneVetturaIntero = posizioneVetturaIntero;
    }

    public int getNumeroLEDVirtuali() {
        return numeroLEDVirtuali;
    }

    public void setNumeroLEDVirtuali(int numeroLEDVirtuali) {
        this.numeroLEDVirtuali = numeroLEDVirtuali;
    }

    public int getFrazioniDiLED() {
        return frazioniDiLED;
    }

    public void setFrazioniDiLED(int frazioniDiLED) {
        this.frazioniDiLED = frazioniDiLED;
    }
    
    /**
     * Calcola l'intensita' dei LED sulla base della posizione della vettura
     */
    private void calcolaIntensitaLED() {
        int a, b, x, y, j;
        int posizioneLED;
        
        impostaLEDSpenti();
        
        posizioneFronte = posizioneVetturaIntero + numeroLEDFadeIn * frazioniDiLED;
        for (int i = 0; i < (numeroLEDFadeIn + numeroLEDFadeOut - 1) * frazioniDiLED; i++) {
            j = posizioneFronte - i - numeroLEDFadeIn * frazioniDiLED;
            if (j >= 0 && j % frazioniDiLED == 0) {
                posizioneLED = j / frazioniDiLED;
                if (posizioneLED < numeroLEDStriscia) {
                    a = i / frazioniDiLED;
                    b = a + 1;
                    x = i % frazioniDiLED;
                    y = (int) ((formaOnda[b] - formaOnda[a]) / (double) frazioniDiLED * x) + formaOnda[a];
                    intensitaLED[posizioneLED] = y;
                }
            }
        }
        
        // stampaIntensitaLED();
    }
    
    private void impostaLEDSpenti() {
        for (int i = 0; i < numeroLEDStriscia; i++) {
            intensitaLED[i] = 0;
        }
    }
    
    private void stampaIntensitaLED() {
        System.out.println("-");
        for (int i = 0; i < numeroLEDStriscia; i++) {
            System.out.println(intensitaLED[i]);
        }
    }
    
    /**
     * Attiva il criterio costante e aggiorna i valori di intensita' dei LED
     */
    public void impostaCriterioCostante() {
        int valoreIntensita = intensitaCriterioCostante;
        
        vetturaInTransito = false;
        IR.getInstance().setIR1Lock(false);
        IR.getInstance().setIR1Occluso(false);
        IR.getInstance().setIR2Lock(false);
        IR.getInstance().setIR2Occluso(false);

        // Invio ad arduino il valore del criterio costante
        LogicTask.getInstance().writeOnSerialPort(4, valoreIntensita);
        
        // Imposto il valore dei LED
        for (int i = 0; i < numeroLEDStriscia; i++) {
            intensitaLED[i] = valoreIntensita;
        }
    }
    
    /**
     * Invia ad Arduino il segnale di impostare a zero l'intensita' di tutti i LED
     */
    public void spegniLEDArduino() {
        LogicTask.getInstance().writeOnSerialPort(4, 0);
    }

    public void ricalcolaPosizioniIR() {
        posizioneIR1 = - distanzaGalleriaInizioCurva - 2 * Math.PI * raggioPercorsoInUso * (IR.getInstance().getPosizioneIR1() / 360);
        posizioneIR2 = - distanzaGalleriaInizioCurva - 2 * Math.PI * raggioPercorsoInUso * (IR.getInstance().getPosizioneIR2() / 360);
        distanzaIR1IR2 = posizioneIR2 - posizioneIR1;
    }
    
    public int[] getIntensitaLED() {
        return intensitaLED;
    }

    public void setIntensitaLED(int[] intensitaLED) {
        this.intensitaLED = intensitaLED;
    }

    public int getNumeroLEDStriscia() {
        return numeroLEDStriscia;
    }

    public void setNumeroLEDStriscia(int numeroLEDStriscia) {
        this.numeroLEDStriscia = numeroLEDStriscia;
    }

    public int getIntensitaCriterioCostante() {
        return intensitaCriterioCostante;
    }

    public void setIntensitaCriterioCostante(int intensitaCriterioCostante) {
        this.intensitaCriterioCostante = intensitaCriterioCostante;
    }

    public boolean isCriterioDinamicoAttivo() {
        return criterioDinamicoAttivo;
    }

    public void setCriterioDinamicoAttivo(boolean criterioDinamicoAttivo) {
        this.criterioDinamicoAttivo = criterioDinamicoAttivo;
    }
    
    // Questi due metodi sono utilizzati per effettuare matching tra l'enum Criterio
    // e la sua implementazione in questa classe che avviene attualmente attraverso un booleano
    public Criterio obtainCriterio(){
        if(criterioDinamicoAttivo)
            return Criterio.DINAMICO;
        else
            return Criterio.COSTANTE_MANUALE;
    }
    public void provideCriterio(Criterio criterio){
        if(criterio==Criterio.COSTANTE_MANUALE)
            setCriterioDinamicoAttivo(false);
        else if(criterio==Criterio.DINAMICO)
            setCriterioDinamicoAttivo(true);
    }
    
}