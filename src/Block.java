//a blockchain is a linked list of blocks, with pointers foward and refrences to each prev nodes hash
//the hash of every block is calculated with of the previous block, data inside block, timestamp, and nonce used by miner
//a blockchain is immutable, one change, changes the entire chains hashes

import java.util.Date;
import java.util.ArrayList;

public class Block {
    public String hash;
    public String previousHash;
    private String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    private long timeStamp;
    private int nonce;

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        String newHash = Hash.applySHA256(previousHash + Integer.toString(nonce) + Long.toString(timeStamp) + merkleRoot);
        return newHash;
    }

    public boolean mineBlock(int difficulty) {
        merkleRoot = Hash.getMerkleRoot(transactions);
        String target = new String(new char[difficulty]).replace('\0', '0'); //creates a string with '0' * difficulty
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Mined " + hash);
        return true;
    }

    public boolean addTransaction(Transaction transaction) {
        if (transaction == null) return false;
        // this is commented out, cuz i took care of this process in zynChain.java with bacth transactions
        /* 
        if (!"0".equals(previousHash)) {
            if (transaction.processTransaction() != true) {
                System.out.println("Failed to process transaction.");
            }
        }
        */
        transactions.add(transaction);
        System.out.println("Transaction processing.");
        return true;
    }
}
