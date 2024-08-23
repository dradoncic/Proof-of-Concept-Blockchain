//this is where implement UTXO system
//bitcoin is not like ethereum in which coins are stored in user accounts 
//instead you wallet references individual or parts of coins
//so your wallet balance is the sume of all the UTXO addressed to you 

//this class references utxo, allowing miners to check your ownership
public class TransactionInput {
    public String transactionOutputID;
    public TransactionOutput UTXO;

    public TransactionInput(String transactionOutputID) {
        this.transactionOutputID = transactionOutputID;
    }

}
