/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import GUI.tools.MyRadioButtonsWrapper;
import controlloAccesso.Funzione;
import controlloIlluminazione.ControlloIlluminazione;
import controlloIlluminazione.Criterio;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import main.Main;

/**
 * Settore della GUI che si occupa della tab Illuminazione.
 */
public class IlluminazioneController extends SettoreController{

    @FXML
    private VBox containerIlluminazione;                                        //Trattato come Node
    @FXML
    private RadioButton rButtonCriterioDinamico;
    @FXML
    private ToggleGroup tgroupCriterio;
    @FXML
    private RadioButton rButtonCriterioCostMan;
    @FXML
    private Label valoreLivelloCM;
    @FXML
    private Slider sliderLivelloCM;
    @FXML
    private BarChart<?, ?> chartCurva;
    @FXML
    private CategoryAxis axisPixel;

    //Elementi di supporto non grafici
    private XYChart.Series serieCurva;
    private Criterio criterio;
    private MyRadioButtonsWrapper myRbuttons;
    
    public IlluminazioneController(){
        serieCurva=new XYChart.Series<>();
        myRbuttons=new MyRadioButtonsWrapper();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        paneSettore=containerIlluminazione;

        sliderLivelloCM.setMax(ControlloIlluminazione.INTENSITA_MAX);
        sliderLivelloCM.setMin(ControlloIlluminazione.INTENSITA_MIN);
        
        //valoreLivelloCM.textProperty().bind(sliderLivelloCM.valueProperty().asString());
        sliderLivelloCM.valueProperty().addListener((observable, oldValue, newValue) -> {           
            valoreLivelloCM.textProperty().setValue(String.valueOf((int)sliderLivelloCM.getValue()));
            ((MainController)(Main.GUIcontrollers.getInstance(MainController.class))).modifichePendenti=true;
        });
        
        associaPulsantiAdEnum();
        
        chartCurva.setAnimated(false);
        for(int i=0; i<ControlloIlluminazione.NUM_LED; i++)
            serieCurva.getData().add(new XYChart.Data(String.valueOf(i+1), 0));
        chartCurva.getData().addAll(serieCurva);
        axisPixel.autoRangingProperty().set(true);
        
        Main.GUIcontrollers.putInstance(IlluminazioneController.class, this);
        
    }

    /**
     * Registra un cambio al tipo di <code>Criterio</code> d'illuminazione richiesto dall'utente.
     * @param event L'evento che genera questo cambiamento.
     */
    @FXML
    private void cambiaCriterio(ActionEvent event) {
        criterio=(Criterio)myRbuttons.getEnum((RadioButton)event.getSource());
        ((MainController)(Main.GUIcontrollers.getInstance(MainController.class))).modifichePendenti=true;
        System.out.println("Richiedi criterio: "+criterio);
    }
    
    /**
     * Aggiorna il grafico della curva d'illuminazione richiedendo i dati recenti
     * al controller logico sottostante
     * @see ControlloIlluminazione#getIntensitaLED() 
     */
    protected void aggiornaCurva(){
        int aux[]=ControlloIlluminazione.getInstance().getIntensitaLED();
        for(int i=0; i<ControlloIlluminazione.NUM_LED; i++){
            ((XYChart.Data) serieCurva.getData().get(i)).YValueProperty().set(aux[i]);
            //System.out.print(aux[i]+" ");
        }
        //System.out.println();
    }

    @Override
    protected void associaPulsantiAdEnum() {
        myRbuttons.add(rButtonCriterioCostMan, Criterio.COSTANTE_MANUALE);
        myRbuttons.add(rButtonCriterioDinamico, Criterio.DINAMICO);
    }

    @Override
    protected void recuperaImpostazioniInUso(){
        sliderLivelloCM.valueProperty().set(ControlloIlluminazione.getInstance().getIntensitaCriterioCostante());
        criterio=ControlloIlluminazione.getInstance().obtainCriterio();
        myRbuttons.getButton(criterio).selectedProperty().set(true);
        // Aggiorna curva dovrebbe in qualche modo finire qui dentro?
    }
    @Override
    protected void adottaNuoveImpostazioni(){
        //ControlloIlluminazione.getInstance().provideCriterio(criterio);
        richiediFunzioneSafely(Funzione.SET_CRITERIO, criterio);
        //ControlloIlluminazione.getInstance().setIntensitaCriterioCostante((int)sliderLivelloCM.getValue());
        richiediFunzioneSafely(Funzione.SET_LIVELLO_CM, (int)sliderLivelloCM.getValue());
        
        //Lazy trigger per essere certi di mantenere coerenza tra control e gui
        recuperaImpostazioniInUso();
    }
}
