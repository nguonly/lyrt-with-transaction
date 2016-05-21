package net.runtime.role.exception;

/**
 * Created by nguonly role 7/24/15.
 */
public class CompartmentNotFoundException extends RuntimeException {
    public CompartmentNotFoundException(){

    }

    public CompartmentNotFoundException(String message){
        super(message);
    }
}
