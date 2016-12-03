/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI.tools;

import java.util.LinkedList;
import javafx.scene.control.RadioButton;

/**
 *
 * @author Lorenzo
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

    public MyRadioButtonsWrapper() {
        myRbuttons=new LinkedList<>();
    }
    
    public boolean add(RadioButton button, Enum en){
        if(checkIntegrity(button, en)){
            myRbuttons.add(new MyRadioButtonWrapper(button, en));
            return true;
        }
        else
            return false;
    }

    public RadioButton getButton(Enum en){
        for(MyRadioButtonWrapper mrbw : myRbuttons)
            if(mrbw.en==en)
                return mrbw.button;
        return null;
    }
    
    public Enum getEnum(RadioButton button){
        for(MyRadioButtonWrapper mrbw : myRbuttons)
           if(mrbw.button==button)
               return mrbw.en;
        return null;
    }
    
    private boolean checkIntegrity(RadioButton rb, Enum en){
        if(getButton(en)==null && getEnum(rb)==null)
            return true;
        else
            return false;
    }
}
