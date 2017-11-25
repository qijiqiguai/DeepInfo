package tech.qi.deepinfo.frame.core;


/**
 * @author wangqi
 */
public class ServiceException extends RuntimeException{
    private static final long serialVersionUID = 703489799884576939L;

    private int status;
    private String refInfo;

    public ServiceException(int statusCode, String message, String refInfo) {
        super(message);
        this.status = statusCode;
        this.refInfo = refInfo;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRefInfo() {
        return refInfo;
    }

    public void setRefInfo(String refInfo) {
        this.refInfo = refInfo;
    }
}
