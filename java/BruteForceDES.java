////////////////////////////////////////////////////////////////////////////////
//
//    $Id: SealedDES.java,v 1.1 2008/09/10 20:21:47 randal Exp $
//
//    Randal C. Burns
//    Department of Computer Science
//    Johns Hopkins University
//
//    $Source: /home/randal/repository/public_html/420/src/SealedDES.java,v $
//    $Date: 2008/09/10 20:21:47 $        
//    $Revision: 1.1 $
//
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
//
//  class: SealedDES
//
//  SealedDES encapsulates the DES encryption and decryption of Strings 
//  into SealedObjects.  It represesnts keys as integers (for simplicity).
//  
//  The main function gives and example of how to:
//    (1) generate a random 24 bit key by starting with a zero valued
//          8 bytes (64 bit key) and then encoding a string with that key
//    (2) perform a brute force search for that key and exame the 
//          resulting output for a known portion of plaintext (in this
//          case "Hopkins".
//
//  Your assignment will be to parallelize this process.
//
////////////////////////////////////////////////////////////////////////////////
import javax.crypto.*;

import java.security.*;

import javax.crypto.spec.*;

import java.util.Random;
import java.io.PrintStream;


class BruteForceDES implements Runnable
{
	// Cipher for the class
	Cipher des_cipher;

	// Key for the class
	SecretKeySpec the_key = null;

	// Byte arrays that hold key block
	byte[] deskeyIN = new byte[8];
	byte[] deskeyOUT = new byte[8];

	int threadId;
	long maxkey;
	SealedObject sldObj;

	// Constructor: initialize the cipher
	public BruteForceDES (int threadId, long maxkey, SealedObject sldObj) 
	{
		this.threadId =  threadId;
		this.maxkey = maxkey;
		this.sldObj = sldObj;

		try 
		{
			des_cipher = Cipher.getInstance("DES");
		} 
		catch ( Exception e )
		{
			System.out.println("Failed to create cipher.  Exception: " + e.toString() +
					" Message: " + e.getMessage()) ; 
		}
	}

	// Decrypt the SealedObject
	//
	//   arguments: SealedObject that holds on encrypted String
	//   returns: plaintext String or null if a decryption error
	//     This function will often return null when using an incorrect key.
	//
	public String decrypt ( SealedObject cipherObj )
	{
		try 
		{
			return (String)cipherObj.getObject(the_key);
		}
		catch ( Exception e )
		{
			//      System.out.println("Failed to decrypt message. " + ". Exception: " + e.toString()  + ". Message: " + e.getMessage()) ; 
		}
		return null;
	}

	// Encrypt the message
	//
	//  arguments: a String to be encrypted
	//  returns: a SealedObject containing the encrypted string
	//
	public SealedObject encrypt ( String plainstr )
	{
		try 
		{
			des_cipher.init ( Cipher.ENCRYPT_MODE, the_key );
			return new SealedObject( plainstr, des_cipher );
		}
		catch ( Exception e )
		{
			System.out.println("Failed to encrypt message. " + plainstr +
					". Exception: " + e.toString() + ". Message: " + e.getMessage()) ; 
		}
		return null;
	}

	//  Build a DES formatted key
	//
	//  Convert an array of 7 bytes into an array of 8 bytes.
	//
	private static void makeDESKey(byte[] in, byte[] out)  
	{
		out[0] = (byte) ((in[0] >> 1) & 0xff);
		out[1] = (byte) ((((in[0] & 0x01) << 6) | (((in[1] & 0xff)>>2) & 0xff)) & 0xff);
		out[2] = (byte) ((((in[1] & 0x03) << 5) | (((in[2] & 0xff)>>3) & 0xff)) & 0xff);
		out[3] = (byte) ((((in[2] & 0x07) << 4) | (((in[3] & 0xff)>>4) & 0xff)) & 0xff);
		out[4] = (byte) ((((in[3] & 0x0F) << 3) | (((in[4] & 0xff)>>5) & 0xff)) & 0xff);
		out[5] = (byte) ((((in[4] & 0x1F) << 2) | (((in[5] & 0xff)>>6) & 0xff)) & 0xff);
		out[6] = (byte) ((((in[5] & 0x3F) << 1) | (((in[6] & 0xff)>>7) & 0xff)) & 0xff);
		out[7] = (byte) (   in[6] & 0x7F);

		for (int i = 0; i < 8; i++) {
			out[i] = (byte) (out[i] << 1);
		}
	}

	// Set the key (convert from a long integer)
	public void setKey ( long theKey )
	{
		try 
		{
			// convert the integer to the 8 bytes required of keys
			deskeyIN[0] = (byte) (theKey        & 0xFF );
			deskeyIN[1] = (byte)((theKey >>  8) & 0xFF );
			deskeyIN[2] = (byte)((theKey >> 16) & 0xFF );
			deskeyIN[3] = (byte)((theKey >> 24) & 0xFF );
			deskeyIN[4] = (byte)((theKey >> 32) & 0xFF );
			deskeyIN[5] = (byte)((theKey >> 40) & 0xFF );
			deskeyIN[6] = (byte)((theKey >> 48) & 0xFF );

			// theKey should never be larger than 56-bits, so this should always be 0
			deskeyIN[7] = (byte)((theKey >> 56) & 0xFF );

			// turn the 56-bits into a proper 64-bit DES key
			makeDESKey(deskeyIN, deskeyOUT);

			// Create the specific key for DES
			the_key = new SecretKeySpec ( deskeyOUT, "DES" );
		}
		catch ( Exception e )
		{
			System.out.println("Failed to assign key" +  theKey +
					". Exception: " + e.toString() + ". Message: " + e.getMessage()) ;
		}
	}


	@Override
	public void run() {

		PrintStream p = new PrintStream(System.out);
		long runStart = System.currentTimeMillis();

		// Search for the right key
		for ( long i = this.maxkey * this.threadId; i < this.maxkey * (this.threadId + 1) + 1; i++ )
		{
			// Set the key and decipher the object
			this.setKey ( i );
			String decryptstr = this.decrypt ( this.sldObj );

			// Does the object contain the known plaintext
			if (( decryptstr != null ) && ( decryptstr.indexOf ( "Hopkins" ) != -1 ))
			{
				//  Remote printlns if running for time.
				p.printf("Thread " + this.threadId + " Found decrypt key %016x producing message: %s\n", i , decryptstr);

				//System.out.println (  "Found decrypt key " + i + " producing message: " + decryptstr );
			}

			// Update progress every once in awhile.
			//  Remote printlns if running for time.
			if ( i % 100000 == 0 )
			{ 
				long elapsed = System.currentTimeMillis() - runStart;
				System.out.println ( "Thread " + this.threadId + " Searched key number " + i + " at " + elapsed + " milliseconds.");
			}
		}

	}



	// Program demonstrating how to create a random key and then search for the key value.
	public static void main ( String[] args )
	{
		if ( args.length != 2 )
		{
			System.out.println ("BruteForceDES #threads key_size_in_bits");
			return;
		}

		// Get the argument
		long keybits = Long.parseLong ( args[1] );

		long maxkey = ~(0L);
		maxkey = maxkey >>> (64 - keybits);

		// Create a simple cipher
		BruteForceDES enccipher = new BruteForceDES (0, 0, null);//

		// Get a number between 0 and 2^64 - 1
		Random generator = new Random ();
		long key =  generator.nextLong();

		// Mask off the high bits so we get a short key
		key = key & maxkey;

		// Set up a key
		enccipher.setKey ( key ); 

		// Generate a sample string
		String plainstr = "Johns Hopkins afraid of the big bad wolf?";

		// Encrypt
		//		SealedObject sldObj = enccipher.encrypt ( plainstr );

		// Here ends the set-up.  Pretending like we know nothing except sldObj,
		// discover what key was used to encrypt the message.

		int numThreads = Integer.parseInt (args[0]);
		SealedObject[] sldObj = new SealedObject[numThreads];
		for (int i = 0; i < numThreads; i++) {
			sldObj[i] = enccipher.encrypt ( plainstr );
		}

		long runStart = System.currentTimeMillis();
		Thread[] threads = new Thread[numThreads];

		for (int i = 0; i < numThreads; i++) {
			threads[i] = new Thread(new BruteForceDES(i, maxkey / numThreads, sldObj[i]));
			threads[i].start();
		}

		for (int i = 0; i < numThreads; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				System.out.println("Thread interrupted.  Exception: " + e.toString() +
						" Message: " + e.getMessage()) ;
				return;
			}
		}

		long elapsed = System.currentTimeMillis() - runStart;
		long keys = maxkey + 1;
		System.out.println ( "Completed search of " + keys + " keys at " + elapsed + " milliseconds.");
		//		System.out.print(elapsed + " ");

	}



}

////////////////////////////////////////////////////////////////////////////////
//
//  Revsion History 
//    
//  $Log: SealedDES.java,v $
//  Revision 1.1  2008/09/10 20:21:47  randal
//  Initial checkin.
//
//  Revision 1.1  2008/09/05 20:31:11  randal
//  Checkin.
//
////////////////////////////////////////////////////////////////////////////////
