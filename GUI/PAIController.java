/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import APP.controlloAccesso.Funzione;
import APP.controlloPAI.ControlloPAI;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import main.Main;

/**
 * Settore della GUI che si occupa della tab PAI.
 */
public class PAIController extends SettoreController{

    @FXML
    private VBox containerPAI;
    @FXML
    private Label valorePAI;
    @FXML
    private ToggleButton buttonDisattivaPAI;

    //Elementi di supporto non grafici
    private boolean paiInCorso;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        recuperaImpostazioniInUso();
        Main.GUIcontrollers.putInstance(PAIController.class, this);
        paneSettore=containerPAI;
    }    

    /**
     * Registra un cambio allo stato della PAI come richiesto dall'utente.
     * @param event L'evento che genera questo cambiamento.
     */
    @FXML
    private void disattivaPAI(ActionEvent event) {
        if(buttonDisattivaPAI.isSelected()){
            paiInCorso=false;
            ((MainController)(Main.GUIcontrollers.getInstance(MainController.class))).modifichePendenti=true;
        }
        else{
            paiInCorso=true;
            ((MainController)(Main.GUIcontrollers.getInstance(MainController.class))).disabilitaPulsantiModifiche(true);
        }
    }
    
    /**************************************************************************/
    
    @Override
    protected void adottaNuoveImpostazioni(){
        if(buttonDisattivaPAI.isSelected()){
            buttonDisattivaPAI.setSelected(false);

            //ControlloPAI.getInstance().disattivaPAI();
            richiediFunzioneSafely(Funzione.DISATTIVA_PAI, null);
            //Lazy trigger per essere certi di mantenere coerenza tra control e gui
            recuperaImpostazioniInUso();
        }
    }
    
    @Override
    protected void recuperaImpostazioniInUso(){
        buttonDisattivaPAI.selectedProperty().set(false);
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
    
    /**
     * Restituisce lo stato della PAI come vista dall'utente.
     * @return <code>True/false</code> se è attiva/disattiva.
     */
    protected boolean isPaiInCorso(){
        return paiInCorso;
    }
    
    /**
     * Rivela un'eventuale incoerenza tra lo stato della PAI come vista (dunque eventualmente richiesta)
     * dall'utente e il suo stato effettivo contenuto nella logica applicativa. Questo risultato è necessario
     * ai fini della corretta gestione della GUI da parte del controllo grafico principale.
     * @return <code>True/false</code> se c'è/non c'è incoerenza.
     */
    protected boolean isPaiInCorsoIncoherent(){
        if(ControlloPAI.getInstance().isPAIAttiva() && paiInCorso==false && !buttonDisattivaPAI.isSelected())
            return true;
        else
            return false;
    }  

    @Override
    void associaPulsantiAdEnum() {
        return;
    }

}
