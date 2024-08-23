//transaction hold lots of information
//in utxo based blockchain
//transactions hold public keys (sender and recipient), amount to transfer, input UTXOs, output UTXOs, and digital signature

import java.security.*;
import java.util.ArrayList;

public class Transaction {
    public String transactionID;        //final computed hash of the transaction
    public PublicKey sender;
    public PublicKey recipient;      
    public float value;
    public byte[] signature;

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int sequence;

    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    public boolean processTransaction() {
        if (verifySignature() == false) {
            System.out.println("Signature Failure");
            return false;
        }

        //gathers transaction inputs (making sure they are unspent)
        for (TransactionInput i : inputs) {
            i.UTXO = ZynChain.UTXOs.get(i.transactionOutputID);
        }

        //checks if the transaction is valid, making sure user has enough coins
        if (getInputsValue() < ZynChain.minimumTransaction) {
            System.out.println("Transaction to small " + getInputsValue());
        }

        //generate transaction outputs
        float leftOver = getInputsValue() - value;
        transactionID = calulateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionID));
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionID));

        //add outputes to UTXOs list in the chain
        for (TransactionOutput i : outputs) {
            ZynChain.UTXOs.put(i.ID, i);
        }

        //remove transaction inputs from UTXO lists as spent
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue; 
            ZynChain.UTXOs.remove(i.UTXO.ID);
        }

        return true;
    }

    //sums a persons total amount of UTXO
    public float getInputsValue() {
        float total = 0;
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue;
            total += i.UTXO.value;
        }
        return total;
    }

    //returns a person total amount of outputs
    public float getOutputsValue() {
        float total = 0;
        for (TransactionOutput i : outputs) {
            total += i.value;
        }
        return total;
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = Hash.getStringFromKey(sender) + Hash.getStringFromKey(recipient) + Float.toString(value)	;
        signature = Hash.applyECDSASig(privateKey,data);		
    }

    public boolean verifySignature() {
        String data = Hash.getStringFromKey(sender) + Hash.getStringFromKey(recipient) + Float.toString(value)	;
        return Hash.verifyECDSASig(sender, data, signature);
    }

	private String calulateHash() {
		sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
		return Hash.applySHA256(
				Hash.getStringFromKey(sender) +
				Hash.getStringFromKey(recipient) +
				Float.toString(value) + sequence
				);
	}

    public void print() {
        System.out.println(this.sender);
        System.out.println(this.recipient);
        System.out.println(this.value);
    }

}
