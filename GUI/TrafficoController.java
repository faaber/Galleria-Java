/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import GUI.tools.MyRadioButtonsWrapper;
import controlloAccesso.Funzione;
import controlloTraffico.Circolazione;
import controlloTraffico.ControlloTraffico;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import main.Main;


public class TrafficoController extends SettoreController{
    //Elementi di supporto non grafici
    private Circolazione circolazione;
    private MyRadioButtonsWrapper myRbuttons;
    
    @FXML
    private HBox containerTraffico;                                             // Trattato come Node
    @FXML
    private RadioButton rButtonCircolazioneDS;
    @FXML
    private ToggleGroup tgroupCircolazione;
    @FXML
    private RadioButton rButtonCircSUSX;
    @FXML
    private RadioButton rButtonCircSUDX;
    @FXML
    private RadioButton rButtonCircSUA;
    @FXML
    private RadioButton rButtonCircInterd;
    @FXML
    private Label valoreDurataVerdeDX;
    @FXML
    private Slider sliderDurataVerdeDX;
    @FXML
    private Label valoreDurataVerdeSX;
    @FXML
    private Slider sliderDurataVerdeSX;
    @FXML
    private Label valoreDurataRossoAggiuntiva;
    @FXML
    private Slider sliderDurataRossoAggiuntiva;
    
    public TrafficoController(){
        myRbuttons=new MyRadioButtonsWrapper();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paneSettore=containerTraffico;
        // Vincoli sugli input dell'utente
        sliderDurataVerdeDX.setMax(controlloTraffico.ControlloTraffico.DURATA_MAX);
        sliderDurataVerdeDX.setMin(controlloTraffico.ControlloTraffico.DURATA_MIN);
        sliderDurataVerdeSX.setMax(controlloTraffico.ControlloTraffico.DURATA_MAX);
        sliderDurataVerdeSX.setMin(controlloTraffico.ControlloTraffico.DURATA_MIN);
        sliderDurataRossoAggiuntiva.setMax(controlloTraffico.ControlloTraffico.DURATA_MAX);
        sliderDurataRossoAggiuntiva.setMin(controlloTraffico.ControlloTraffico.DURATA_MIN);
       
        // Aggiunta di un changeListener agli slider in grado di aggiornare le relative labels
        valoreDurataVerdeSX.textProperty().setValue(String.valueOf((int)sliderDurataVerdeSX.getValue()));
        sliderDurataVerdeSX.valueProperty().addListener((observable, oldValue, newValue) -> {
            valoreDurataVerdeSX.textProperty().setValue(String.valueOf((int)sliderDurataVerdeSX.getValue()));
            ((MainController)(Main.GUIcontrollers.getInstance(MainController.class))).modifichePendenti=true;
        });
        valoreDurataVerdeDX.textProperty().setValue(String.valueOf((int)sliderDurataVerdeDX.getValue()));
        sliderDurataVerdeDX.valueProperty().addListener((observable, oldValue, newValue) -> {
            valoreDurataVerdeDX.textProperty().setValue(String.valueOf((int)sliderDurataVerdeDX.getValue()));
            ((MainController)(Main.GUIcontrollers.getInstance(MainController.class))).modifichePendenti=true;
        });
        valoreDurataRossoAggiuntiva.textProperty().setValue(String.valueOf((int)sliderDurataRossoAggiuntiva.getValue()));
        sliderDurataRossoAggiuntiva.valueProperty().addListener((observable, oldValue, newValue) -> {
            valoreDurataRossoAggiuntiva.textProperty().setValue(String.valueOf((int)sliderDurataRossoAggiuntiva.getValue()));
            ((MainController)(Main.GUIcontrollers.getInstance(MainController.class))).modifichePendenti=true;
        });
        
        associaPulsantiAdEnum();     
        
        Main.GUIcontrollers.putInstance(TrafficoController.class, this);      
    }
    
    @Override
    void associaPulsantiAdEnum(){
        myRbuttons.add(rButtonCircInterd, Circolazione.INTERDETTA);
        myRbuttons.add(rButtonCircSUA, Circolazione.SENSO_UNICO_ALTER);
        myRbuttons.add(rButtonCircSUDX, Circolazione.SENSO_UNICO_DX);
        myRbuttons.add(rButtonCircSUSX, Circolazione.SENSO_UNICO_SX);
        myRbuttons.add(rButtonCircolazioneDS, Circolazione.DOPPIO_SENSO);
    }
    
    @Override
    protected void recuperaImpostazioniInUso(){
        sliderDurataRossoAggiuntiva.valueProperty().set(ControlloTraffico.getInstance().getDurataRossoAggiuntiva());
        sliderDurataVerdeDX.valueProperty().set(ControlloTraffico.getInstance().getDurataVerdeDXRossoSX());
        sliderDurataVerdeSX.valueProperty().set(ControlloTraffico.getInstance().getDurataVerdeSXRossoDX());
        circolazione=ControlloTraffico.getInstance().getCircolazione();
        if(circolazione!=Circolazione.CUSTOM)
            myRbuttons.getButton(circolazione).selectedProperty().set(true);
        else
            circolazioneCustomImpostata();
    }
    @Override
    protected void adottaNuoveImpostazioni(){
        
        //ControlloTraffico.getInstance().setCircolazione(circolazione);
        richiediFunzioneSafely(Funzione.SET_CIRCOLAZIONE, circolazione);
        //ControlloTraffico.getInstance().setDurataRossoAggiuntiva((int) sliderDurataRossoAggiuntiva.getValue());
        richiediFunzioneSafely(Funzione.SET_DURATA_R_AGG, (int) sliderDurataRossoAggiuntiva.getValue());
        //ControlloTraffico.getInstance().setDurataVerdeDXRossoSX((int) sliderDurataVerdeDX.getValue());
        richiediFunzioneSafely(Funzione.SET_DURATA_V_DX, (int) sliderDurataVerdeDX.getValue());
        //ControlloTraffico.getInstance().setDurataVerdeSXRossoDX((int) sliderDurataVerdeSX.getValue());
        richiediFunzioneSafely(Funzione.SET_DURATA_V_SX, (int) sliderDurataVerdeSX.getValue());
        
        //Lazy trigger per essere certi di mantenere coerenza tra control e gui
        recuperaImpostazioniInUso();
    }

    /**
     * Registra un cambio al tipo di <code>Circolazione</code> richiesta dall'utente.
     * @param event L'evento che genera questo cambiamento.
     */
    @FXML
    private void cambiaCircolazione(ActionEvent event) {
        circolazione=(Circolazione)myRbuttons.getEnum((RadioButton)event.getSource());
        ((MainController)(Main.GUIcontrollers.getInstance(MainController.class))).modifichePendenti=true;
        System.out.println("Richiedi circolazione: "+circolazione);
    }
    
    /**
     * Notifica l'attivazione della <code>Circolazione.CUSTOM</code> da parte di un'altra classe.
     * Tale metodo è necessario, poiché <code>TrafficoController</code> non consente la gestione di
     * questa modalità di <code>Circolazione</code> essendo un dettaglio legato alla manutenzione tecnica del sistema.
     * @see ManutenzioneController
     */
    protected void circolazioneCustomImpostata(){
        if(circolazione!=Circolazione.CUSTOM){
            myRbuttons.getButton(circolazione).setSelected(false);
            circolazione=Circolazione.CUSTOM;
        }
    }
}
