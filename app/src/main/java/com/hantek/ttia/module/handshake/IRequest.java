package com.hantek.ttia.module.handshake;

/**
 * Created by wsh on 2015/8/13.
 */
public interface IRequest {
    boolean checkConnection();
    boolean request();
    boolean waitResp();
    Object getResponse();
    void execute(Object data);
    void rollBack();
}
