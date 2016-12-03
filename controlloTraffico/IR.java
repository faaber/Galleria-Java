package controlloTraffico;

import controlloIlluminazione.ControlloIlluminazione;
import java.util.Date;

/**
 * Classe Control che gestisce gli input relativi all'attivazione dei sensori
 * IR. Utilizziamo il design pattern Singleton
 */
public final class IR {
    private boolean IR1Occluso = false;
    private boolean IR2Occluso = false;
    private boolean IR1Lock = false;
    private boolean IR2Lock = false;
    private boolean IRLock = true;
    private double posizioneIR2 = 22.5;
    private double posizioneIR1 = 67.5;
    private static IR instance = null;
    
    private IR() {
    }
    
    /**
     * Metodo che implementa il design pattern Singleton
     */
    public static IR getInstance() {
        if (instance == null) {
            instance = new IR();
        }
        return instance;
    }
    
    /**
     * Questo metodo viene richiamato se viene ricevuto un segnale di transizione
     * del sensore IR1 da non occluso ad occluso o viceversa.
     * @param occluso <code>true</code> se il nuovo stato del sensore e' 'occluso',
     * <code>false</code> altrimenti
     */
    public void ricevutoInputIR1Occluso(boolean occluso) {
        if (ControlloIlluminazione.getInstance().isCriterioDinamicoAttivo()) {
            if (!isIRLock()) {
                setIR1Occluso(occluso);
            }
            else {
                if (!ControlloIlluminazione.getInstance().isVetturaInTransito() && !isIR1Lock() && occluso) {
                    setIR1Lock(true);
                    setIR1Occluso(true);
                    ControlloIlluminazione.getInstance().setIR1Data(new Date());
                }
            }
        }
    }
    
    /**
     * Questo metodo viene richiamato se viene ricevuto un segnale di transizione
     * del sensore IR2 da non occluso ad occluso o viceversa.
     * @param occluso <code>true</code> se il nuovo stato del sensore e' "occluso",
     * <code>false</code> altrimenti
     */
    public void ricevutoInputIR2Occluso(boolean occluso) {
        if (ControlloIlluminazione.getInstance().isCriterioDinamicoAttivo()) {
            if (!isIRLock()) {
                setIR2Occluso(occluso);
            }
            else {
                if (isIR1Lock() && !isIR2Lock() && occluso) {
                    setIR2Lock(true);
                    setIR2Occluso(true);
                    ControlloIlluminazione.getInstance().setIR2Data(new Date());
                    ControlloIlluminazione.getInstance().calcolaVelocitaVettura();
                    ControlloIlluminazione.getInstance().setVetturaInTransito(true);
                }
            }
        }
    }
    
    public boolean isIR1Occluso() {
        return IR1Occluso;
    }

    public void setIR1Occluso(boolean IR1Occluso) {
        this.IR1Occluso = IR1Occluso;
    }

    public boolean isIR2Occluso() {
        return IR2Occluso;
    }

    public void setIR2Occluso(boolean IR2Occluso) {
        this.IR2Occluso = IR2Occluso;
    }

    public double getPosizioneIR2() {
        return posizioneIR2;
    }

    public void setPosizioneIR2(double posizioneIR2) {
        this.posizioneIR2 = posizioneIR2;
    }

    public double getPosizioneIR1() {
        return posizioneIR1;
    }

    public void setPosizioneIR1(double posizioneIR1) {
        this.posizioneIR1 = posizioneIR1;
    }
    
    public boolean isIRLock() {
        return IRLock;
    }

    public void setIRLock(boolean IRLock) {
        this.IRLock = IRLock;
    }
    
    public boolean isIR1Lock() {
        return IR1Lock;
    }

    public void setIR1Lock(boolean IR1Lock) {
        this.IR1Lock = IR1Lock;
    }

    public boolean isIR2Lock() {
        return IR2Lock;
    }

    public void setIR2Lock(boolean IR2Lock) {
        this.IR2Lock = IR2Lock;
    }

}
