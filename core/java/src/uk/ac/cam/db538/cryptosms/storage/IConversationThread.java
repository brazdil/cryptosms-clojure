package uk.ac.cam.db538.cryptosms.storage;

import uk.ac.cam.db538.cryptosms.IdentificationNumber;
import uk.ac.cam.db538.cryptosms.PhoneNumber;

public interface IConversationThread {
	public PhoneNumber getRecipient();
	public boolean isEncryptionActive(IdentificationNumber simId);
}
