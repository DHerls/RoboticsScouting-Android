package org.fullmetalfalcons.androidscouting.equations;

/**
 * Exception thrown when there is an error is parsing Equations from the config file
 *
 * Created by Dan on 1/24/2016.
 */
public class EquationParseException extends Exception {

    @SuppressWarnings("SameParameterValue")
    public EquationParseException(String message){
        super(message);
    }

    @SuppressWarnings("unused")
    public EquationParseException(Throwable cause){
        super(cause);
    }

    @SuppressWarnings("unused")
    public EquationParseException(String message, Throwable cause){
        super(message,cause);
    }
}
