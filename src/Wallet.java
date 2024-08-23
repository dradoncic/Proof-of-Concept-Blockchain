//wallets just store public and private addresses
//this is a UTXO based blockchain, so the funds on chain are locked by whoever "owns" the coin key
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

public class Wallet {
    public PrivateKey privateKey;       //signs outgoing transactions
    public PublicKey publicKey;        //verifies incoming transactions
    public HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

    public Wallet() {
        generateKeyPair();
    }

    //usin g elliptic curve cryptography to generate pairs of keys 
    public void generateKeyPair() {
        try {
            KeyPairGenerator generate = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random  = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            //initializing the key generator
            generate.initialize(ecSpec, random);       //256 bytes provides security 
            KeyPair keyPair = generate.generateKeyPair();
            
            //set each key
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public float getBalance() {
        float total = 0;
        
        for (Map.Entry<String, TransactionOutput> item : ZynChain.UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            if (UTXO.isMine(publicKey)) {
                UTXOs.put(UTXO.ID, UTXO);
                total += UTXO.value;
            }
        }
        return total;
    }

    public Transaction sendFunds(PublicKey _recipient, float value) {
        if (getBalance() < value) {
            System.out.println("Not enough funds.");
            return null;
        }

        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.ID));
            if (total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        for (TransactionInput input : inputs) {
            UTXOs.remove(input.transactionOutputID);
        }
        
        return newTransaction;
    }

}
