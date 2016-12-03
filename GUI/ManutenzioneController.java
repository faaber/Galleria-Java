package GUI;

import arduino.LogicTask;
import controlloTraffico.IR;
import controlloIlluminazione.ControlloIlluminazione;
import controlloPAI.ControlloPAI;
import controlloTraffico.Circolazione;
import controlloTraffico.ControlloTraffico;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import main.Main;

/**
 * FXML Controller class
 */
public class ManutenzioneController implements Initializable {

    @FXML
    private Button cnSmf1;
    @FXML
    private Button cnSmf2;
    @FXML
    private Button cnIRLock;
    @FXML
    private Button cnTmpLock;
    @FXML
    private Button cnCircolazione;
    @FXML
    private Button cnArduinoCollegato;
    @FXML
    private Rectangle statusSmf1;
    @FXML
    private Rectangle statusTmp;
    @FXML
    private Rectangle statusSmf2;
    @FXML
    private Button cnSimulaIR1;
    @FXML
    private Button cnSimulaIR2;
    @FXML
    private Rectangle statusIR1;
    @FXML
    private Rectangle statusIR2;
    @FXML
    private ProgressBar cnBarraProgressoStriscia;
    @FXML
    private TextField cnPosizioneIR1;
    @FXML
    private TextField cnPosizioneIR2;
    
    // Variabili non relative agli elementi dell'interfaccia grafica
    
    private boolean isTaskCreato = false;
    private double posizioneVetturaBarraProgresso = 0.0;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!isTaskCreato) {
            Task<Integer> aggiornaInterfacciaTask = new Task<Integer>() {
                @Override
                protected Integer call() {
                    while (!isCancelled()) {
                        try {
                            // Addormenta il thread per 0,2 millisecondi
                            Thread.sleep(20);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ManutenzioneController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        aggiornaElementiInterfaccia();
                    }

                    return 0;
                }
            };

            Thread th = new Thread(aggiornaInterfacciaTask);
            th.setDaemon(true);
            th.start();
            
            isTaskCreato = true;
            
            cnPosizioneIR1.setText(String.valueOf(IR.getInstance().getPosizioneIR2()));
            cnPosizioneIR2.setText(String.valueOf(IR.getInstance().getPosizioneIR1()));
            
            
            if (ControlloIlluminazione.getInstance().isCriterioDinamicoAttivo()) {
                ;
            }
            
            // Tmp lock
            if (ControlloPAI.getInstance().isTemperaturaLock()) {
                cnTmpLock.setText("tmp unlock");
            }
            else {
                cnTmpLock.setText("tmp lock");
            }
            
            // IR lock
            if (IR.getInstance().isIRLock()) {
                cnIRLock.setText("IR unlock");
            }
            else {
                cnIRLock.setText("IR lock");
            }
            
            // Arduino
            if (LogicTask.getInstance().isArduinoCollegato()) {
                cnArduinoCollegato.setText("Imp. Arduino coll.");
                cnArduinoCollegato.setDisable(true);
            }
            else {
                cnArduinoCollegato.setText("Imp. Arduino coll.");
            }
            
            // ControlloIlluminazione - inizio
            ControlloIlluminazione.getInstance().spegniLEDArduino();

            
            // ControlloIlluminazione - fine
        }
        
        // Collegamento dei controller GUI
        Main.GUIcontrollers.putInstance(ManutenzioneController.class, this);
    }


    @FXML
    private void acAzioneSmf1(ActionEvent event) {
        if (ControlloTraffico.getInstance().getCircolazione() == Circolazione.CUSTOM) {
            if (ControlloTraffico.getInstance().isSmf1Verde()) {
                ControlloTraffico.getInstance().setSmf1Verde(false);
            }
            else {
                ControlloTraffico.getInstance().setSmf1Verde(true);
            }
        }
    }

    @FXML
    private void acAzioneSmf2(ActionEvent event) {
        if (ControlloTraffico.getInstance().getCircolazione() == Circolazione.CUSTOM) {
            if (ControlloTraffico.getInstance().isSmf2Verde()) {
                ControlloTraffico.getInstance().setSmf2Verde(false);
            }
            else {
                ControlloTraffico.getInstance().setSmf2Verde(true);
            }
        }
    }
    
    @FXML
    private void acIRLock(ActionEvent event) {
        if (IR.getInstance().isIRLock()) {
            IR.getInstance().setIRLock(false);
            cnIRLock.setText("IR lock");
            System.out.println("IR unlocked.");
        }
        else {
            IR.getInstance().setIRLock(true);
            cnIRLock.setText("IR unlock");
            System.out.println("IR locked.");
        }
    }
    
    @FXML
    private void acTmpLock(ActionEvent event) {
        if (ControlloPAI.getInstance().isTemperaturaLock()) {
            ControlloPAI.getInstance().setTemperaturaLock(false);
            ControlloPAI.getInstance().setTemperaturaAttivato(false);
            cnTmpLock.setText("tmp lock");
            System.out.println("Tmp unlocked.");
        }
        else {
            ControlloPAI.getInstance().setTemperaturaLock(true);
            cnTmpLock.setText("tmp unlock");
            System.out.println("Tmp locked.");
        }
    }
    
    @FXML
    private void acCircolazione(ActionEvent event) {
        ControlloTraffico.getInstance().setCircolazione(Circolazione.CUSTOM); 
        // Lorenzo
        ((TrafficoController)(Main.GUIcontrollers.getInstance(TrafficoController.class))).circolazioneCustomImpostata();   
    }
    
    @FXML
    private void acArduinoCollegato(ActionEvent event) {
        if (!LogicTask.getInstance().isArduinoCollegato()) {
            LogicTask.getInstance().setArduinoCollegato(true);
            cnArduinoCollegato.setDisable(true);
            System.out.println("Arduino collegato.");
        }
    }

    @FXML
    private void acSimulaIR1(ActionEvent event) {
        IR.getInstance().ricevutoInputIR1Occluso(true);
        IR.getInstance().ricevutoInputIR1Occluso(false);
    }

    @FXML
    private void acSimulaIR2(ActionEvent event) {
        IR.getInstance().ricevutoInputIR2Occluso(true);
        IR.getInstance().ricevutoInputIR2Occluso(false);
    }

    @FXML
    private void acAggiornaPosizioneIR1(ActionEvent event) {
        double d = Double.parseDouble(cnPosizioneIR1.getText());
        if (d < 0.0) {
            d = 0.0;
        }
        
        IR.getInstance().setPosizioneIR2(d);
        ControlloIlluminazione.getInstance().ricalcolaPosizioniIR();
        cnPosizioneIR1.setText(String.valueOf(IR.getInstance().getPosizioneIR2()));
    }

    @FXML
    private void acAggiornaPosizioneIR2(ActionEvent event) {
        double d = Double.parseDouble(cnPosizioneIR2.getText());
        if (d < 0.0) {
            d = 0.0;
        }
        
        IR.getInstance().setPosizioneIR1(d);
        ControlloIlluminazione.getInstance().ricalcolaPosizioniIR();
        cnPosizioneIR2.setText(String.valueOf(IR.getInstance().getPosizioneIR1()));
    }
    
    private void aggiornaElementiInterfaccia() {
        // statusTmp
        if (!ControlloPAI.getInstance().isTemperaturaAlta()) {
            statusTmp.setFill(Color.GREEN);
        }
        else {
            statusTmp.setFill(Color.RED);
        }
        
        // statusIR1
        if (!IR.getInstance().isIR1Occluso()) {
            statusIR1.setFill(Color.GREEN);
        }
        else {
            statusIR1.setFill(Color.RED);
        }
        
        // statusIR2
        if (!IR.getInstance().isIR2Occluso()) {
            statusIR2.setFill(Color.GREEN);
        }
        else {
            statusIR2.setFill(Color.RED);
        }
        
        // Semafori
        if (ControlloTraffico.getInstance().isSmf1Verde()) {
            statusSmf1.setFill(Color.GREEN);
        }
        else {
            statusSmf1.setFill(Color.RED);
        }
        if (ControlloTraffico.getInstance().isSmf2Verde()) {
            statusSmf2.setFill(Color.GREEN);
        }
        else {
            statusSmf2.setFill(Color.RED);
        }
        
        
        // ControlloIlluminazione - inizio
        if (ControlloIlluminazione.getInstance().isVetturaInTransito() ||
                !ControlloIlluminazione.getInstance().isCriterioDinamicoAttivo()) {
            posizioneVetturaBarraProgresso = (double) ControlloIlluminazione.getInstance().getPosizioneVetturaIntero() /
                    (((double) ControlloIlluminazione.getInstance().getNumeroLEDVirtuali() - 2) *
                    ControlloIlluminazione.getInstance().getFrazioniDiLED());
            
            if (posizioneVetturaBarraProgresso < 0.0) {
                posizioneVetturaBarraProgresso = 0.0;
            }
            else if (posizioneVetturaBarraProgresso > 1.0) {
            
            }
            
            cnBarraProgressoStriscia.setProgress(posizioneVetturaBarraProgresso);
        }
    }
}