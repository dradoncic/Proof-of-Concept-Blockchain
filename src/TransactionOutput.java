import java.security.PublicKey;


public class TransactionOutput {
    public String ID;
    public PublicKey recipient;
    public float value;
    public String parentTransactionID;

    public TransactionOutput(PublicKey recipient, float value, String parentTransactionID) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionID = parentTransactionID;
        this.ID = Hash.applySHA256(Hash.getStringFromKey(recipient)+Float.toString(value)+parentTransactionID);
    }

    public boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient);
    }
}
