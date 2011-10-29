package uk.ac.cam.db538.cryptosms.storage;

import uk.ac.cam.db538.cryptosms.PhoneNumber;

public interface IStorage {
	public void close();
	public IConversationThread getConversationThread(PhoneNumber recipient);
}
