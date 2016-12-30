package GUI;

import GUI.tools.MyRadioButtonsWrapper;
import APP.controlloAccesso.ControlloAccesso;
import APP.controlloAccesso.Permesso;
import java.io.IOException;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.Main;

/**
 * Gestore principale della GUI che si occupa della vista principale e della finestra di login.
 */
public class MainController implements Initializable {
    //Costanti
    public static final int LOGIN_W=320, LOGIN_H=320, APP_W=800, APP_H=600, MANT_W=525, MANT_H=480;
    
    @FXML
    private StackPane mainPane;                                                 // Trattato come Pane
    @FXML
    private VBox boxApplicazione;
    @FXML
    private TabPane tabMenu;
    @FXML
    private Tab tabIlluminazione;
    @FXML
    private Tab tabPAI;
    @FXML
    private Tab tabTraffico;
    @FXML
    private Button buttonAccetta;
    @FXML
    private Button buttonAnnulla;
    @FXML
    private Label valorePermesso;
    @FXML
    private Label valoreUser;
    @FXML
    private Button buttonLogout;
    @FXML
    private VBox boxLogin;
    @FXML
    private TextField fieldUsername;
    @FXML
    private PasswordField fieldPassword;
    @FXML
    private Button buttonLogin;
    @FXML
    private Button buttonManutenzione;
    @FXML
    private Label labelAccessoNegato;
    
    //Elementi di supporto non grafici
    private Stage stageManutenzione;
    private Permesso permesso;
    private MyRadioButtonsWrapper myRbuttons;
    private UpdateThread aggiornaInterfaccia;
    protected boolean modifichePendenti;
    private boolean avviata;
    
    public MainController(){
        myRbuttons=new MyRadioButtonsWrapper();
        permesso=null;
        aggiornaInterfaccia=null;
        modifichePendenti=false;
        avviata=false;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Vincoli dell'utente
        
        associaPulsantiAdEnum();

        // Collegamento dei controller GUI
        Main.GUIcontrollers.putInstance(MainController.class, this);
        
        // UI
        disabilitaPulsantiModifiche(true);
        labelAccessoNegato.setVisible(false);
        
        // Creazione Stage per ManutenzioneView
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/ManutenzioneView.fxml"));
            Parent root = loader.load(getClass().getResource("/GUI/ManutenzioneView.fxml"));
            stageManutenzione = new Stage();
            stageManutenzione.setScene(new Scene(root));
            stageManutenzione.setResizable(false);
            stageManutenzione.getScene().getWindow().setWidth(MANT_W);
            stageManutenzione.getScene().getWindow().setHeight(MANT_H);
            stageManutenzione.setTitle("Manutenzione");
            stageManutenzione.getIcons().add(new Image("/GUI/resources/icon+wrench.png"));

        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Finalizza le impostazioni e mostra a video l'interfaccia grafica.
     * Da lanciare solo una volta assegnato la <code>Scene</code> allo <code>Stage</code>.
     * <p>Il metodo effettua le sue operazioni solo alla prima chiamata. Ulteriori chiamate
     * verranno ignorate.
     */
    public void avviaGUI(){
        if(avviata)
            return;        
        showLoginBox();
        ((Stage)mainPane.getScene().getWindow()).setOnCloseRequest(ev -> {
            System.exit(0);
        });
        ((Stage)mainPane.getScene().getWindow()).setWidth(MainController.LOGIN_W);
        ((Stage)mainPane.getScene().getWindow()).setHeight(MainController.LOGIN_H);
        ((Stage)mainPane.getScene().getWindow()).setTitle("Galleria");
        ((Stage)mainPane.getScene().getWindow()).getIcons().add(new Image("/GUI/resources/icon.png"));
        ((Stage)mainPane.getScene().getWindow()).show();
        avviata=true;
    }

    
    /**
     * Conferma le impostazioni richieste dall'utente nei vari settori della GUI spingendole nella logica applicativa.
     * @param event L'evento che genera questa richiesta.
     */
    @FXML
    private void accettaModifiche(ActionEvent event) {
        ((PAIController)Main.GUIcontrollers.getInstance(PAIController.class)).adottaNuoveImpostazioni();
        
        /*
        Questo controllo evita che si cerchi di aggiornare inutilmente gli altri controller logici quando 
        non è stato possibile disattivare un'eventuale PAI. La sua rimozione NON implica malfunzionamenti
        ma lasciandolo si evita di sporcare inutilmente la console. Può essere rimosso all'occorrenza
        per mostrare l'utilità di ControlloAccesso.richiediFunzione.
        */
        if(!((PAIController)Main.GUIcontrollers.getInstance(PAIController.class)).isPaiInCorso()){
            ((TrafficoController)Main.GUIcontrollers.getInstance(TrafficoController.class)).adottaNuoveImpostazioni();
            ((IlluminazioneController)Main.GUIcontrollers.getInstance(IlluminazioneController.class)).adottaNuoveImpostazioni();
        }
        contestualizzaInterfaccia();
        disabilitaPulsantiModifiche(true);
    }

    /**
     * Annulla le impostazioni richieste dall'utente nei vari settori della GUI reperendole dalla logica applicativa.
     * @param event L'evento che genera questa richiesta.
     */
    @FXML
    private void annullaModifiche(ActionEvent event) {
        recuperaValoriInUso();    
        disabilitaPulsantiModifiche(true);
    }

    /**
     * Disconnette l'utente corrente dal sistema, ripresentando la schermata di login.
     * @param event L'evento che genera questa richiesta.
     */
    @FXML
    private void effettuaLogout(ActionEvent event) {        
        ControlloAccesso.getInstance().effettuaLogout();
        
        recuperaValoriInUso();
        disabilitaPulsantiModifiche(true);
        showLoginBox();
    }

    /**
     * Richiede al sistema di autenticare l'utente, presentando in caso di successo l'interfaccia principale.
     * @param event L'evento che genera questa richiesta.
     */
    @FXML
    private void effettuaLogin(ActionEvent event) {
        if(ControlloAccesso.getInstance().effettuaLogin(fieldUsername.textProperty().get(), fieldPassword.textProperty().get()) == null){
            labelAccessoNegato.visibleProperty().set(true);
            return;
        }
        labelAccessoNegato.visibleProperty().set(false);

        permesso=ControlloAccesso.getInstance().getPermesso();
        
        System.out.println("Connesso come "+permesso+"::"+fieldUsername.getText());
        
        recuperaValoriInUso();
        disabilitaPulsantiModifiche(true);

        showAppBox();           
    }
    
    /**************************************************************************/
    
    /**
     * Mostra la finestra di login, nascondendo tutte le altre.
     */
    private void showLoginBox(){
        if(aggiornaInterfaccia!=null)
            aggiornaInterfaccia.terminate();
        stageManutenzione.hide();

        boxLogin.toFront();
        boxApplicazione.toBack();
        ((Stage)mainPane.getScene().getWindow()).setWidth(LOGIN_W);
        ((Stage)mainPane.getScene().getWindow()).setHeight(LOGIN_H);
        ((Stage)mainPane.getScene().getWindow()).setResizable(false);
    }
    
    /**
     * Mostra la vista principale dell'applicazione, nascondendo tutte le altre.
     */
    private void showAppBox(){
        boxLogin.toBack();
        boxApplicazione.toFront();
        ((Stage)mainPane.getScene().getWindow()).setWidth(APP_W);
        ((Stage)mainPane.getScene().getWindow()).setHeight(APP_H);
        ((Stage)mainPane.getScene().getWindow()).setResizable(true);
        aggiornaInterfaccia=new UpdateThread(60D, false);
        aggiornaInterfaccia.start();  
    }
    
    /**
     * Apre la finestra contenente la vista di manutenzione, nel caso in cui non lo sia già.
     */
    @FXML
    private void showMaintenanceWindow(){
        stageManutenzione.show();
    }
    
    private void associaPulsantiAdEnum(){
        /* Nel caso in cui servano al MainController
        attualmente la MainView non gestisce alcun RadioButton
        */
    }
    
    /**
     * Abilita o disabilita i pulsanti che consentono di accettare/annullare le modifiche effettuate al sistema.
     * @param b <code>True</code> se i pulsanti vanno disabilitati, <code>false</code> altrimenti.
     * @see MainController#accettaModifiche(javafx.event.ActionEvent)
     * @see MainController#annullaModifiche(javafx.event.ActionEvent)
     */
    protected void disabilitaPulsantiModifiche(boolean b){
        if(b==true)
            modifichePendenti=false;
        buttonAccetta.setDisable(b);
        buttonAnnulla.setDisable(b);
    }
    
    protected void recuperaValoriInUso(){
        ((TrafficoController)Main.GUIcontrollers.getInstance(TrafficoController.class)).recuperaImpostazioniInUso();
        ((IlluminazioneController)Main.GUIcontrollers.getInstance(IlluminazioneController.class)).recuperaImpostazioniInUso();
        ((PAIController)Main.GUIcontrollers.getInstance(PAIController.class)).recuperaImpostazioniInUso();
        
        // TODO: Recupera da controlloAccesso
        contestualizzaInterfaccia();
    }    
    
    /**
     * Rende disponibili a video solo le interazioni consentite dal <code>Permesso</code> dell'user autenticato e dallo stato della PAI.
     */
    private void contestualizzaInterfaccia(){
        buttonManutenzione.setDisable(false);
        valoreUser.textProperty().set(fieldUsername.getText());
        boolean paiInCorso=((PAIController)Main.GUIcontrollers.getInstance(PAIController.class)).isPaiInCorso();
        switch(permesso){
            case OPERATORE:
                valorePermesso.textProperty().set("Operatore");
                ((PAIController)Main.GUIcontrollers.getInstance(PAIController.class)).disabilitaVista(false);
                ((IlluminazioneController)Main.GUIcontrollers.getInstance(IlluminazioneController.class)).disabilitaVista(paiInCorso);
                ((TrafficoController)Main.GUIcontrollers.getInstance(TrafficoController.class)).disabilitaVista(paiInCorso);
                disabilitaPulsantiModifiche(paiInCorso);
                ((ManutenzioneController)Main.GUIcontrollers.getInstance(ManutenzioneController.class)).disabilitaVista(false);
                break;
            case CONTROLLORE:
                valorePermesso.textProperty().set("Controllore");
                ((IlluminazioneController)Main.GUIcontrollers.getInstance(IlluminazioneController.class)).disabilitaVista(true);
                ((PAIController)Main.GUIcontrollers.getInstance(PAIController.class)).disabilitaVista(true);
                ((TrafficoController)Main.GUIcontrollers.getInstance(TrafficoController.class)).disabilitaVista(true);
                disabilitaPulsantiModifiche(paiInCorso);
                ((ManutenzioneController)Main.GUIcontrollers.getInstance(ManutenzioneController.class)).disabilitaVista(true);

                break;
        }       
    }
    
    /**************************************************************************/
    //Strumenti ausiliari
    
    private class UpdateThread extends Thread{
        
        private boolean stopRequested;
        private double freq;
        private boolean high_perf;
        
        public UpdateThread(double freq, boolean pHigh_perf) {
            super();
            this.stopRequested=false;
            this.freq=freq;
            this.high_perf=pHigh_perf;
        }
            
        
        @Override
        public void run() {
            Runnable operation=()->{recuperaValoriInUso();};
            if(high_perf){
                long lastTime=System.nanoTime(), now;
                double nsOp1=1000000000/freq;                                     //16666666.6667 ns sono 16ms, l'intervallo di tempo che deve esserci tra un update e l'altro per per averne 60 al secondo
                double nsOp2=1000000000/(freq/3);
                double deltaOp1=0, deltaOp2=0;

                while(!stopRequested){
                    now=System.nanoTime();
                    deltaOp1+=(now-lastTime)/nsOp1;                                           //delta=tempo trascorso rispetto ai 16 ms (60 game update al secondo)
                    deltaOp2+=(now-lastTime)/nsOp2;
                    lastTime=now;
                    if(deltaOp1>=1){
                        // Aggiornamento grafico e Pulsanti modifica
                        ((IlluminazioneController)Main.GUIcontrollers.getInstance(IlluminazioneController.class)).aggiornaCurva();
                        if(modifichePendenti)
                            disabilitaPulsantiModifiche(false);
                        deltaOp1--;
                    }
                    if(deltaOp2>=1){
                        if(((PAIController)Main.GUIcontrollers.getInstance(PAIController.class)).isPaiInCorsoIncoherent())
                                Platform.runLater(operation);
                        deltaOp2--;
                    }
                }
            }else{
                while(!stopRequested){
                    long time=(long) (1000/freq);
                    try {
                        ((IlluminazioneController)Main.GUIcontrollers.getInstance(IlluminazioneController.class)).aggiornaCurva();
                        if(modifichePendenti)
                            disabilitaPulsantiModifiche(false);
                        if(((PAIController)Main.GUIcontrollers.getInstance(PAIController.class)).isPaiInCorsoIncoherent())
                            Platform.runLater(operation);
                        Thread.sleep(time);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }        
        public void terminate(){
            stopRequested=true;
            this.interrupt();
        }
    }
}
