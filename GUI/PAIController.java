/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import controlloAccesso.ControlloAccesso;
import controlloAccesso.Funzione;
import controlloPAI.ControlloPAI;
import static java.lang.Thread.sleep;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import main.Main;

/**
 * FXML Controller class
 *
 * @author Lorenzo
 */
public class PAIController implements Initializable {

    @FXML
    private VBox containerPAI;
    @FXML
    private Label valorePAI;
    @FXML
    private ToggleButton buttonDisattivaPAI;

    //Elementi di supporto non grafici
    private boolean paiInCorso;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        recuperaImpostazioniInUso();
        Main.GUIcontrollers.putInstance(PAIController.class, this);
    }    

    @FXML
    private void disattivaPAI(ActionEvent event) {
        paiInCorso=false;
        buttonDisattivaPAI.setSelected(true);
        ((MainController)(Main.GUIcontrollers.getInstance(MainController.class))).modifichePendenti=true;
    }
    
    /**************************************************************************/
    
    protected void adottaNuoveImpostazioni(){
        // Invio PAI al controller

        if(buttonDisattivaPAI.isSelected()){
            buttonDisattivaPAI.setSelected(false);

            ControlloAccesso.getInstance().richiediFunzione(Funzione.DISATTIVA_PAI, null);

            //Lazy trigger per essere certi di mantenere coerenza tra control e gui
            recuperaImpostazioniInUso();
        }
    }
    
    protected void recuperaImpostazioniInUso(){
        //buttonDisattivaPAI.selectedProperty().set(false);
        paiInCorso=ControlloPAI.getInstance().isPAIAttiva();
        
        if(paiInCorso){
            valorePAI.textProperty().setValue("ATTIVA");
            valorePAI.textFillProperty().setValue(Paint.valueOf("red"));
            buttonDisattivaPAI.disableProperty().set(false);
        }else{
            buttonDisattivaPAI.disableProperty().set(true);
            valorePAI.textProperty().setValue("NON ATTIVA");
            valorePAI.textFillProperty().setValue(Paint.valueOf("green"));
        }
    }
    
    protected void disabilitaVista(boolean val){
        containerPAI.disableProperty().set(val);
    }
    
    protected boolean isPaiInCorso(){
        return paiInCorso;
    }
    
    protected boolean isPaiInCorsoIncoherent(){
        if(ControlloPAI.getInstance().isPAIAttiva() && paiInCorso==false)
            return true;
        else
            return false;
    }  
}
