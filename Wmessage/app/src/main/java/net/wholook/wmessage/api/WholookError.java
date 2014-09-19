package net.wholook.wmessage.api;

/**
 * Created by wholook on 14. 9. 17..
 */
public class WholookError {


    public static final int HANDSHAKE_EXCEPTION = 101;
    public static final int HANDSHAKE_RESPONSE_FAIL = 102;
    public static final int HANDSHAKE_UNKNOWN_RESULT = 103;


    public static final int LOGIN_EXCEPTION = 111;
    public static final int LOGIN_RESPONSE_FAIL = 112;
    //public static final int LOGIN_UNKNOWN_RESULT = 113;
    public static final int LOGIN_RESULT_JSONPARSING = 114;

    //public static final int SERVICE_UNKNOWN_USER = 121;
    public static final int SERVICE_GETMESSAGELIST_EXCEPTION = 122;
    public static final int SERVICE_GETMESSAGELIST_RESPONSE_FAIL = 123;
    public static final int SERVICE_GETMESSAGELIST_UNKNOWN_RESULT = 124;
    public static final int SERVICE_SMS_JSONPARSING = 125;

    public static final int SERVICE_MESSAGERETURN_EXCEPTION = 131;
    public static final int SERVICE_MESSAGERETURN_JSONEXCEPTION = 132;
    public static final int SERVICE_MESSAGERETURN_RESPONSE_FAIL = 133;
    //public static final int SERVICE_MESSAGERETURN_UNKNOWN_RESULT = 134;

}
