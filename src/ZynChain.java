import java.util.ArrayList;
import java.security.*;
import java.util.HashMap;

public class ZynChain {
    public static ArrayList<Block> blockChain = new ArrayList<Block>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
	//implementing a sort of bacth processing mechanism
	public static ArrayList<Transaction> transactionPool = new ArrayList<Transaction>();
	

    public static int difficulty = 6;
    public static float minimumTransaction = 0.1f;
    public static Wallet walletA;
    public static Wallet walletB;
	public static Wallet miner;
    public static Transaction genesisTransaction;


	//lowkey a version of rollups as well, kinda sorta maybe??
	public static void batchTransaction(Transaction transaction) {
		if (transaction == null) {
			System.out.println("Transaction invalid.");
			return;
		}

		if (transaction.processTransaction() != true) {
			System.out.println("Failed to process transaction.");
		}

		transactionPool.add(transaction);


		// there is a problem here with the inout value being null, validityTest() is acting up, since we are pulling the fund out of thin air
		if (transactionPool.size() == 5) { 			//i mean we dont have miner packing blocks with transaction, so I implemented it kinda
			//add another transaction here, giving the miner a fee of coins
			//now add another transaction giving the miner 5 coins 
			processBatch();
		}
	}
	
	private static void processBatch() {
		Block newBlock = new Block(blockChain.get(blockChain.size() - 1).hash);

		for (Transaction transaction : transactionPool) {
			newBlock.addTransaction(transaction);
		}

		addBlock(newBlock);
		transactionPool.clear();
	}


	public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockChain.add(newBlock);
    }

    //checks for forks, making sure everyone is acting fairly
	public static Boolean validityTest() {
		Block currentBlock; 
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
		tempUTXOs.put(genesisTransaction.outputs.get(0).ID, genesisTransaction.outputs.get(0));
		
		//loop through blockchain to check hashes:
		for(int i=1; i < blockChain.size(); i++) {
			
			currentBlock = blockChain.get(i);
			previousBlock = blockChain.get(i-1);
			//compare registered hash and calculated hash:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("#Current Hashes not equal");
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined");
				return false;
			}
			
			//loop thru blockchains transactions:
			TransactionOutput tempOutput;
			for(int t=0; t <currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false; 
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false; 
				}
				
				for(TransactionInput input: currentTransaction.inputs) {	
					tempOutput = tempUTXOs.get(input.transactionOutputID);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputID);
				}
				
				for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.ID, output);
				}
				
				if( currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
				}
				if( currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
				
			}
			
		}
		System.out.println("Blockchain is valid");
		return true;
	}

	//maybe implement a command line interface?
	//maybe set a new java file, with the method
	//create wallet (name) ...makes a wallet
	//A->B(20) ...transaction
	//A balance? ... wallet balance
	//exit ...end code
	//could be pretty dope ngl

	public static void main(String[] args) {	
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider
		
		//Create wallets:
		walletA = new Wallet();
		walletB = new Wallet();		
		miner = new Wallet();

		Wallet coinbase = new Wallet();
		
		//create genesis transaction, which sends 100 NoobCoin to walletA: 
		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 10000f, null);
		genesisTransaction.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction	
		genesisTransaction.transactionID = "0"; //manually set the transaction id
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionID)); //manually add the Transactions Output
		UTXOs.put(genesisTransaction.outputs.get(0).ID, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.
		
		System.out.println("Creating and Mining Genesis block... ");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);

		
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		System.out.println("Miner's balance is: " + miner.getBalance());

		batchTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		batchTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		batchTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		batchTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		batchTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		System.out.println("Miner's balance is: " + miner.getBalance());
		validityTest();

	}
}
