package GUI.tools;

import java.util.LinkedList;
import javafx.scene.control.RadioButton;

/**
 * Consente di generare un mapping tra una serie di radioButton ed una serie di enum.
 */
public class MyRadioButtonsWrapper {
    
    private class MyRadioButtonWrapper{
        public RadioButton button;
        public Enum en;

        public MyRadioButtonWrapper(RadioButton button, Enum en){
            this.button=button;
            this.en=en;
        }
    }
    
    private LinkedList<MyRadioButtonWrapper> myRbuttons;

    /**
     * Costruttore.
     */
    public MyRadioButtonsWrapper() {
        myRbuttons=new LinkedList<>();
    }
    
    /**
     * Aggiunge una coppia RadioButton-Enum alla serie.
     * Viene inoltre effettuato un controllo di integrità della serie per verificare che
     * uno dei due elementi non sia già presente, pregiudicandone l'inserimento.
     * @param button L'istanza del RadioButton
     * @param en L'Enum che si desidera associare
     * @return <code>true/false</code> in base al fatto che l'inserimento sia andato a buon fine o meno
     */
    public boolean add(RadioButton button, Enum en){
        if(checkIntegrity(button, en)){
            myRbuttons.add(new MyRadioButtonWrapper(button, en));
            return true;
        }
        else
            return false;
    }

    /**
     * Consente di ottenere un RadioButton dato il suo Enum.
     * @param en L'Enum per il quale si vuole trovare il RadioButton associato
     * @return L'istanza del RadioButton, null se non vi sono corrispondenze
     */
    public RadioButton getButton(Enum en){
        for(MyRadioButtonWrapper mrbw : myRbuttons)
            if(mrbw.en==en)
                return mrbw.button;
        return null;
    }
    
    /**
     * Consente di ottenere un Enum dato il suo RadioButton.
     * @param button Il RadioButton per il quale si vuole trovare L'Enum associato
     * @return L'istanza dell'Enum, null se non vi sono corrispondenze
     */
    public Enum getEnum(RadioButton button){
        for(MyRadioButtonWrapper mrbw : myRbuttons)
           if(mrbw.button==button)
               return mrbw.en;
        return null;
    }
    
    /**
     * Verifica se è già presente una coppia contenente il RadioButton o l'Enum.
     * @param rb
     * @param en
     * @return <code>True/false</code> in base al fatto che la premessa si sia verificata o meno.
     */
    private boolean checkIntegrity(RadioButton rb, Enum en){
        if(getButton(en)==null && getEnum(rb)==null)
            return true;
        else
            return false;
    }
}
