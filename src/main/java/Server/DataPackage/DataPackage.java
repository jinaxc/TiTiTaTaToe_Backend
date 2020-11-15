package Server.DataPackage;

/**
 * @author jinaxCai
 */
public class DataPackage {
    private int responseCode;
    private int statusCode;
    private String data;

    public DataPackage(int responseCode, int statusCode, String data) {
        this.responseCode = responseCode;
        this.statusCode = statusCode;
        this.data = data;
    }

    public DataPackage(int responseCode, int statusCode) {
        this.responseCode = responseCode;
        this.statusCode = statusCode;
    }

    public DataPackage() {
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return responseCode + "" + Packages.SPLITTER + statusCode + Packages.SPLITTER + data;
    }
}
