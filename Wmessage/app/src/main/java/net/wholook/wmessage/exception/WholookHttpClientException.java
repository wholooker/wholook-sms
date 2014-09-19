package net.wholook.wmessage.exception;

/**
 * Created by wholook on 14. 9. 7..
 */
public class WholookHttpClientException extends Exception {

    public WholookHttpClientException(){
        super();
    }

    public WholookHttpClientException( String message){
        super(message);
    }
}
