import java.util.concurrent.ThreadLocalRandom;

public class CoinFlip implements Runnable {

	private int threadId;
	private long numIters;
	private long heads;

	public CoinFlip(int threadId, long numIters) {
		this.threadId = threadId;
		this.numIters = numIters;
		this.heads = 0;
	}

	@Override
	public void run() {
		 for (long i = 0; i < this.numIters; i++) {
		 	this.heads += ThreadLocalRandom.current().nextInt(0, 2);
		 }
	}

	public static void main(String[] args) {
		if (args.length != 2)
			System.out.println("Usage: CoinFlip #threads #iterations");

		int numThreads = Integer.parseInt (args[0]);
		long numIters = Long.parseLong(args[1]);
		Thread[] threads = new Thread[numThreads];
		CoinFlip[] cp = new CoinFlip[numThreads];
		double startup = 0.0;

		long currentTime = System.currentTimeMillis();

		
		for (int i = 0; i < numThreads; i++) {
			cp[i] = new CoinFlip(i, numIters / ((long)(numThreads)));
			
			long time = System.nanoTime();
			
			threads[i] = new Thread(cp[i]);
			threads[i].start();
			
			startup += (double)(System.nanoTime() - time);
			
		}
		startup = startup / 1000000.0;

		long totalHeads = 0;
		for (int i = 0; i < numThreads; i++) {
			try {
				threads[i].join();
				totalHeads += cp[i].heads;
			} catch (InterruptedException e) {
				System.out.println("Thread interrupted.  Exception: " + e.toString() +
						" Message: " + e.getMessage()) ;
				return;
			}
		}

		long duration = System.currentTimeMillis() - currentTime;

		 System.out.println("#threads: " + numThreads);
		 System.out.println(totalHeads + " heads in " + numIters + " coin tosses.");
		 System.out.println("Elapsed time: " + duration + " ms");
		// System.out.print(duration + " ");
//		System.out.print(startup + " ");
	}

}
