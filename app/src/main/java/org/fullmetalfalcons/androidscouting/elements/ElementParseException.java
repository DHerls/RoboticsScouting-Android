package org.fullmetalfalcons.androidscouting.elements;

/**
 * Exception thrown when there is an error in parsing Elements from the config file
 *
 * Created by Dan on 1/11/2016.
 */
@SuppressWarnings({"serial"})
public class ElementParseException extends Exception {


    public ElementParseException(String message){
        super(message);
    }

    @SuppressWarnings("unused")
    public ElementParseException(Throwable cause){
        super(cause);
    }

    @SuppressWarnings("unused")
    public ElementParseException(String message, Throwable cause){
        super(message,cause);
    }

}
